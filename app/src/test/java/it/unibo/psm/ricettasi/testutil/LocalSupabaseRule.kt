package it.unibo.psm.ricettasi.testutil

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.minimalConfig
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.AssumptionViolatedException
import org.junit.rules.ExternalResource

/**
 * Starts a local Supabase stack (Docker, via the Supabase CLI) before the test class and
 * stops it after. If Docker or the CLI are not installed, the whole class is skipped instead
 * of failed.
 */
class LocalSupabaseRule : ExternalResource() {

    lateinit var apiUrl: String
    lateinit var anonKey: String

    // Gradle can run unit tests with the working directory set to the project root or to
    // the app module, depending on how the build is invoked, but the supabase CLI commands
    // below only work from the folder that has supabase/config.toml. Walking up from the
    // working directory until that file shows up finds the right folder either way.
    private val projectRoot: File = run {
        var dir = File(System.getProperty("user.dir") ?: ".").absoluteFile
        while (!File(dir, "supabase/config.toml").exists()) { // climbing file tree until we find supabase/config.toml
            dir = dir.parentFile ?: error("Could not find supabase/config.toml above $dir")
        }
        dir // implicit return
    }

    override fun before() {
        requireToolAvailable("docker", "info")
        requireToolAvailable("npx", "--version")
        if (run("npx", "supabase", "status").exitCode != 0) {
            run("npx", "supabase", "start", timeoutSeconds = 180).requireSuccess("supabase start")
        }
        run("npx", "supabase", "db", "reset", "--local", timeoutSeconds = 120).requireSuccess("supabase db reset --local")

        val status = run("npx", "supabase", "status", "-o", "json").requireSuccess("supabase status -o json")
        val parsed = Json.parseToJsonElement(status.stdout).jsonObject
        apiUrl = parsed["API_URL"]!!.jsonPrimitive.content
        anonKey = parsed["ANON_KEY"]!!.jsonPrimitive.content
    }

    override fun after() {
        runCatching { run("npx", "supabase", "stop", timeoutSeconds = 30) }
    }

    fun newClient(): SupabaseClient =
        createSupabaseClient(supabaseUrl = apiUrl, supabaseKey = anonKey) {
            install(Postgrest)
            // The test client is throwaway and JVM-only: there is no Android storage to persist
            // a session into, and none of these tests need the session to survive past the run.
            install(Auth) { minimalConfig() }
        }

    /**
     * Logs the given client in as a brand new throwaway user, so RPCs granted only to the
     * "authenticated" role can be called. Safe to do on every run: the stack is local and
     * reset before the class anyway, so there is nothing to keep clean across runs.
     */
    suspend fun authenticateAsNewUser(client: SupabaseClient) {
        client.auth.signUpWith(Email) {
            email = "contract-test-${UUID.randomUUID()}@local.test"
            password = "Test1234!"
        }
    }

    private fun requireToolAvailable(vararg command: String) {
        val result = runCatching { run(*command, timeoutSeconds = 10) }.getOrNull()
        if (result == null || result.exitCode != 0) {
            throw AssumptionViolatedException(
                "'${command.joinToString(" ")}' not available: skipping local Supabase contract tests",
            )
        }
    }

    private class CommandResult(val exitCode: Int, val stdout: String, val stderr: String) {
        fun requireSuccess(label: String): CommandResult {
            check(exitCode == 0) { "$label failed (exit $exitCode):\n$stderr" }
            return this
        }
    }

    private fun run(vararg command: String, timeoutSeconds: Long = 30): CommandResult {
        val process = ProcessBuilder(*command).directory(projectRoot).start()
        val stdout = StringBuilder()
        val stderr = StringBuilder()
        val outThread = Thread { process.inputStream.bufferedReader().forEachLine { stdout.appendLine(it) } }
        val errThread = Thread { process.errorStream.bufferedReader().forEachLine { stderr.appendLine(it) } }
        outThread.start()
        errThread.start()
        val finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
        if (!finished) process.destroyForcibly()
        outThread.join(5_000)
        errThread.join(5_000)
        return CommandResult(if (finished) process.exitValue() else -1, stdout.toString(), stderr.toString())
    }
}
