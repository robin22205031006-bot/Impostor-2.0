package com.example.data

import kotlin.random.Random

/**
 * Role-scoped view of a generated round word.
 * Agents receive the secret word and optional Agent hint, while the Impostor's
 * secret word and Agent hint are strictly redacted.
 */
data class PlayerWordBriefing(
    val role: PlayerRole,
    val secretWord: String?,
    val agentHint: String?,
    val category: String?,
    val genre: String?,
    val isSecretWordHidden: Boolean
)

/**
 * Result produced by [CategoryWordGenerator] for a single game session/round.
 */
data class GeneratedWordRound(
    val entry: WordEntry,
    val selectedCategory: String,
    val gameMode: GameMode = GameMode.CLASSIC,
    val hintsEnabled: Boolean = true,
    val categoryCluesEnabled: Boolean = true
) {
    val agentHintText: String
        get() = if (hintsEnabled) entry.hintForMode(gameMode) else ""

    fun briefingFor(role: PlayerRole): PlayerWordBriefing {
        val cat = if (categoryCluesEnabled) entry.category else null
        val gen = if (categoryCluesEnabled) entry.genre else null
        return if (role == PlayerRole.IMPOSTOR) {
            PlayerWordBriefing(
                role = PlayerRole.IMPOSTOR,
                secretWord = null,
                agentHint = null,
                category = cat,
                genre = gen,
                isSecretWordHidden = true
            )
        } else {
            PlayerWordBriefing(
                role = PlayerRole.NORMAL,
                secretWord = entry.word,
                agentHint = agentHintText.ifBlank { null },
                category = cat,
                genre = gen,
                isSecretWordHidden = false
            )
        }
    }
}

/**
 * Category-based word generator that picks a secret word from the requested category
 * and exposes targeted hints for Agents while hiding the secret word from the Impostor.
 */
object CategoryWordGenerator {

    fun availableCategories(): List<String> = WordBank.categories

    fun generate(
        category: String = WordBank.CATEGORY_ALL,
        usedWords: Set<String> = emptySet(),
        mode: GameMode = GameMode.CLASSIC,
        hintsEnabled: Boolean = true,
        categoryCluesEnabled: Boolean = true,
        random: Random = Random.Default
    ): GeneratedWordRound {
        val picked = WordBank.pickRandomWord(
            selectedCategory = category,
            usedWords = usedWords,
            random = random
        )
        return GeneratedWordRound(
            entry = picked,
            selectedCategory = category,
            gameMode = mode,
            hintsEnabled = hintsEnabled,
            categoryCluesEnabled = categoryCluesEnabled
        )
    }
}
