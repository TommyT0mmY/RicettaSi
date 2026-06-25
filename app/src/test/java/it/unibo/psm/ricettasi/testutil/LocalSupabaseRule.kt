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
 *
 * before() does, in order: check if the tools are there, start the stack only if it is not
 * already running, then always reset the schema and seed data so every test class starts
 * from the same known state no matter what a previous run left behind.
 * Every external command (start, reset, status, stop) goes through the private run() helper at the bottom,
 * which wraps ProcessBuilder and hands back a CommandResult instead of making every call site
 * deal with streams and exit codes directly.
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

        // only start the stack if it is not already up, starting an already running one
        // would just be a wasted 180 second timeout for nothing.
        if (run("npx", "supabase", "status").exitCode != 0) {
            run("npx", "supabase", "start", timeoutSeconds = 180).requireSuccess("supabase start")
        }
        // always reset, even if the stack was already running: a previous test class (or a
        // manual psql session while debugging) could have left the data in a different state.
        run("npx", "supabase", "db", "reset", "--local", timeoutSeconds = 120).requireSuccess("supabase db reset --local")

        // the CLI does not take the url/key as flags, the only way to get them is asking
        // "status" again and parsing its JSON output.
        val status = run("npx", "supabase", "status", "-o", "json").requireSuccess("supabase status -o json")
        val parsed = Json.parseToJsonElement(status.stdout).jsonObject
        apiUrl = parsed["API_URL"]!!.jsonPrimitive.content
        anonKey = parsed["ANON_KEY"]!!.jsonPrimitive.content
    }

    // runCatching here: this runs after the tests already passed or failed, a stop failure
    // (e.g. someone already ran "supabase stop" by hand) should not hide the real test result.
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
     * "authenticated" role can be called. Safe to do on every run.
     */
    suspend fun authenticateAsNewUser(client: SupabaseClient) {
        client.auth.signUpWith(Email) {
            email = "contract-test-${UUID.randomUUID()}@local.test"
            password = "Test1234!"
        }
    }

    // AssumptionViolatedException is JUnit's "skip this, do not fail" signal, for
    // machines without Docker or the CLI.
    private fun requireToolAvailable(vararg command: String) {
        val result = runCatching { run(*command, timeoutSeconds = 10) }.getOrNull()
        if (result == null || result.exitCode != 0) {
            throw AssumptionViolatedException(
                "'${command.joinToString(" ")}' not available: skipping local Supabase contract tests",
            )
        }
    }

    // Bundles the three things that come out of running an external process, so run()
    // has one typed value to return instead of three separate out-parameters.
    private class CommandResult(val exitCode: Int, val stdout: String, val stderr: String) {
        // Fails with a clear message (command label, exit code, stderr) instead of making
        // every caller of run() repeat the same exitCode check by hand.
        fun requireSuccess(label: String): CommandResult {
            check(exitCode == 0) { "$label failed (exit $exitCode):\n$stderr" }
            return this
        }
    }

    // The one place every "npx supabase ..." call above goes through.
    private fun run(vararg command: String, timeoutSeconds: Long = 30): CommandResult {
        val process = ProcessBuilder(*command).directory(projectRoot).start()
        val stdout = StringBuilder()
        val stderr = StringBuilder()
        // Each stream gets read on its own thread, started before waitFor(). The supabase CLI
        // writes a lot of progress output to stderr, and if we only read stdout while waiting
        // for the process to exit, stderr's OS pipe buffer can fill up and the process blocks
        // forever trying to write to it, a deadlock neither side recovers from on its own.
        val outThread = Thread { process.inputStream.bufferedReader().forEachLine { stdout.appendLine(it) } }
        val errThread = Thread { process.errorStream.bufferedReader().forEachLine { stderr.appendLine(it) } }
        outThread.start()
        errThread.start()
        val finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
        // a process stuck past its timeout (e.g. docker hanging) gets killed instead of left
        // running in the background for the rest of the test run.
        if (!finished) process.destroyForcibly()
        outThread.join(5_000)
        errThread.join(5_000)
        return CommandResult(if (finished) process.exitValue() else -1, stdout.toString(), stderr.toString())
    }
}
