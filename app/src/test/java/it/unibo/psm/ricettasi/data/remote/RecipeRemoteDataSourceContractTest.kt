package it.unibo.psm.ricettasi.data.remote

import io.github.jan.supabase.SupabaseClient
import it.unibo.psm.ricettasi.testutil.LocalSupabaseRule
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.ClassRule
import org.junit.Test

/**
 * Runs the `search_recipes` RPC and the recipes SELECT against a local Supabase
 * instance (see LocalSupabaseRule), seeded from supabase/seed.sql.
 * Skipped automatically (not failed) on a machine without Docker.
 */
class RecipeRemoteDataSourceContractTest {
    companion object {
        @ClassRule
        @JvmField
        val supabaseRule = LocalSupabaseRule() // Wrapper to start/stop a local supabase stack

        private lateinit var client: SupabaseClient
        private lateinit var dataSource: RecipeRemoteDataSource

        @BeforeClass
        @JvmStatic
        fun setUpClient() = runBlocking {
            client = supabaseRule.newClient()
            supabaseRule.authenticateAsNewUser(client)
            dataSource = RecipeRemoteDataSource(client)
        }

        // ids from supabase/seed.sql
        // ingredients
        private const val POMODORO = "a0000000-0000-0000-0000-000000000001"
        private const val PASTA = "a0000000-0000-0000-0000-000000000002"
        private const val FARINA = "a0000000-0000-0000-0000-000000000004"
        private const val CIOCCOLATO = "a0000000-0000-0000-0000-000000000005"
        // recipes
        private const val PASTA_AL_POMODORO = "b0000000-0000-0000-0000-000000000001"
        private const val PANCAKE_VELOCI = "b0000000-0000-0000-0000-000000000003"
        private const val TORTA_AL_CIOCCOLATO = "b0000000-0000-0000-0000-000000000004"
        private const val EMPTY_RECIPE = "b0000000-0000-0000-0000-000000000005"
        private const val CARBONARA_CREAM = "b0000000-0000-0000-0000-000000000006"
        private const val PASTA_ALLA_CARBONARA = "b0000000-0000-0000-0000-000000000007"
        // categories
        private const val ITALIANA = "c0000000-0000-0000-0000-000000000001"
        private const val IN_PADETTA = "c0000000-0000-0000-0000-000000000002"
        private const val DOLCE = "c0000000-0000-0000-0000-000000000003"
        private const val GOURMET = "c0000000-0000-0000-0000-000000000004"
    }

    @Test
    fun `getById decodes a full recipe together with all its relations`() = runBlocking {
        val recipe = dataSource.getById(PASTA_AL_POMODORO)
        assertNotNull(recipe)
        assertEquals("Pasta al pomodoro", recipe!!.title)
        assertEquals(20, recipe.preparationTime)
        assertEquals("facile", recipe.difficulty)
        assertEquals(3, recipe.steps.size)
        assertEquals(setOf(POMODORO, PASTA), recipe.ingredients.map { it.ingredientId }.toSet())
        assertEquals(setOf("pranzo", "cena"), recipe.mealTypes.map { it.mealType }.toSet())
        assertEquals(setOf(ITALIANA, IN_PADETTA), recipe.categories.map { it.categoryId }.toSet())
    }

    @Test
    fun `getById decodes null optional fields and empty relations`() = runBlocking {
        val recipe = dataSource.getById(EMPTY_RECIPE)

        assertNotNull(recipe)
        assertNull(recipe!!.description)
        assertNull(recipe.servings)
        assertTrue(recipe.steps.isEmpty())
        assertTrue(recipe.ingredients.isEmpty())
        assertTrue(recipe.mealTypes.isEmpty())
        assertTrue(recipe.categories.isEmpty())
    }

    // "torta al cioccolato" is the only difficile recipe in the seed data.
    @Test
    fun `searchRecipes filters by difficulty`() = runBlocking {
        val results = dataSource.searchRecipes(difficulties = listOf("difficile"))

        assertEquals(listOf(TORTA_AL_CIOCCOLATO), results.map { it.id })
    }

    @Test
    fun `searchRecipes filters by query, case insensitive and on a substring`() = runBlocking {
        val results = dataSource.searchRecipes(query = "pomodoro")

        assertEquals(listOf(PASTA_AL_POMODORO), results.map { it.id })
    }

    // "pomodroo" has no letter-by-letter substring in common with "Pasta al pomodoro",
    // but the trigram similarity is high enough to still match it as a typo.
    @Test
    fun `searchRecipes tolerates a typo in the query via trigram similarity`() = runBlocking {
        val results = dataSource.searchRecipes(query = "pomodroo")

        assertEquals(listOf(PASTA_AL_POMODORO), results.map { it.id })
    }

    // both recipes match "pasta carbonara" only via trigram similarity, not as a literal
    // substring, but "Pasta alla carbonara" is the closer match and must come first,
    // not just whichever title is alphabetically first.
    @Test
    fun `searchRecipes ranks the closer title match first, not alphabetically`() = runBlocking {
        val results = dataSource.searchRecipes(query = "pasta carbonara")

        assertEquals(listOf(PASTA_ALLA_CARBONARA, CARBONARA_CREAM), results.map { it.id })
    }

    // "pancake veloci" is the only colazione recipe in the seed data.
    @Test
    fun `searchRecipes filters by meal type`() = runBlocking {
        val results = dataSource.searchRecipes(mealType = "colazione")

        assertEquals(listOf(PANCAKE_VELOCI), results.map { it.id })
    }

    @Test
    fun `searchRecipes filters by categories, requiring all of them not just one`() = runBlocking {
        val results = dataSource.searchRecipes(categories = listOf("Italiana", "In padella"))

        // "risotto ai funghi" only has "Italiana", so it is excluded even though it partially matches.
        assertEquals(listOf(PASTA_AL_POMODORO), results.map { it.id })
    }

    @Test
    fun `searchRecipes filters by ingredient roots, requiring all of them`() = runBlocking {
        val results = dataSource.searchRecipes(ingredientRoots = listOf(POMODORO, PASTA))

        assertEquals(listOf(PASTA_AL_POMODORO), results.map { it.id })
    }

    @Test
    fun `searchRecipes filters by time window, quick means a short prep time`() = runBlocking {
        val results = dataSource.searchRecipes(timeWindows = listOf("quick"))

        assertEquals(setOf(PANCAKE_VELOCI, EMPTY_RECIPE), results.map { it.id }.toSet())
    }

    // both pomodoro and pasta are in the pantry, so the recipe using exactly those two
    // ingredients gets a full 2 out of 2 match and is ranked first.
    @Test
    fun `searchRecipes computes pantry match and orders the best match first`() = runBlocking {
        val results = dataSource.searchRecipes(pantryRoots = listOf(POMODORO, PASTA), orderBy = "match")

        val best = results.first()
        assertEquals(PASTA_AL_POMODORO, best.id)
        assertEquals(2, best.availableCount)
        assertEquals(2, best.requiredCount)
    }

    @Test
    fun `searchRecipes computes expiring ingredient match and orders the most expiring first`() = runBlocking {
        val results = dataSource.searchRecipes(expiringRoots = listOf(FARINA, CIOCCOLATO), orderBy = "expiring")

        val best = results.first()
        assertEquals(TORTA_AL_CIOCCOLATO, best.id)
        assertEquals(2, best.expiringMatchCount)
    }

    @Test
    fun `searchRecipes respects minAvailable even without an orderBy`() = runBlocking {
        val results = dataSource.searchRecipes(pantryRoots = listOf(POMODORO, PASTA), minAvailable = 2)

        assertEquals(listOf(PASTA_AL_POMODORO), results.map { it.id })
    }
}
