package com.example.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

enum class GameMode(
    val title: String,
    val subtitle: String,
    val defaultTurnSeconds: Int
) {
    CLASSIC(
        title = "Classic Mode",
        subtitle = "Bangladeshi, Bollywood & trendy global words, tough 1-word clues, 45s turns",
        defaultTurnSeconds = 45
    ),
    SPEED_ROUND(
        title = "Speed Round",
        subtitle = "Rapid-fire 10s turns — think and guess fast!",
        defaultTurnSeconds = 10
    ),
    EXPERT(
        title = "Expert Mode",
        subtitle = "Cryptic 1-word clues for sharp sleuths, 60s turns",
        defaultTurnSeconds = 60
    );

    companion object {
        fun fromRaw(raw: String): GameMode =
            entries.firstOrNull { it.name == raw } ?: CLASSIC
    }
}

data class WordEntry(
    val word: String,
    val category: String,
    val genre: String,
    val hint: String,
    val expertHint: String
) {
    fun hintForMode(mode: GameMode): String =
        if (mode == GameMode.EXPERT) expertHint else hint
}

object WordBank {
    const val CATEGORY_ALL = "ALL"
    const val CATEGORY_CUSTOM = "Custom"

    private const val PREFS_NAME = "impostor_prefs"
    private const val KEY_CUSTOM_WORDS = "custom_words_json"
    private const val KEY_CUSTOM_CATEGORY_NAME = "custom_category_name"

    private val fallbackEntries: List<WordEntry> = listOf(
        // Food
        WordEntry("Fuchka", "Food", "Savory", "Hollow", "Crackle"),
        WordEntry("Kacchi", "Food", "Banquet", "Seal", "Layer"),
        WordEntry("Singara", "Food", "Pastry", "Fold", "Crust"),
        WordEntry("Jilapi", "Food", "Confection", "Coil", "Glaze"),
        WordEntry("Khichuri", "Food", "Comfort", "Pot", "Thunder"),
        WordEntry("Panta", "Food", "Tradition", "Clay", "Dawn"),
        WordEntry("Cha", "Food", "Brew", "Leaf", "Ceramic"),
        WordEntry("Rosogolla", "Food", "Sweet", "Sponge", "Curd"),
        WordEntry("Mango", "Food", "Orchard", "Pit", "Sap"),
        WordEntry("Jackfruit", "Food", "Harvest", "Resin", "Rind"),
        WordEntry("Haleem", "Food", "Stew", "Grain", "Dusk"),
        WordEntry("Muri", "Food", "Crunch", "Hull", "Toss"),
        WordEntry("Bakorkhani", "Food", "Bakery", "Flake", "Alley"),
        WordEntry("Pizza", "Food", "Cuisine", "Crust", "Box"),
        WordEntry("Sushi", "Food", "Cuisine", "Roll", "Vinegar"),
        WordEntry("Burger", "Food", "Cuisine", "Stack", "Sesame"),
        WordEntry("Boba", "Food", "Beverage", "Straw", "Pearl"),
        WordEntry("Coffee", "Food", "Beverage", "Roast", "Grind"),

        // Nature
        WordEntry("Tiger", "Nature", "Predator", "Paw", "Dusk"),
        WordEntry("Ilish", "Nature", "Catch", "Bones", "Tide"),
        WordEntry("Doel", "Nature", "Avian", "Note", "Branch"),
        WordEntry("Crow", "Nature", "Avian", "Wire", "Echo"),
        WordEntry("Cobra", "Nature", "Reptile", "Hiss", "Reed"),
        WordEntry("Kingfisher", "Nature", "Avian", "Dive", "Flash"),
        WordEntry("Dolphin", "Nature", "Aquatic", "Echo", "Wake"),
        WordEntry("Jackal", "Nature", "Fauna", "Howl", "Stalk"),
        WordEntry("Goat", "Nature", "Livestock", "Tether", "Horn"),
        WordEntry("Sundarbans", "Nature", "Wetland", "Silt", "Roots"),
        WordEntry("Monsoon", "Nature", "Climate", "Squall", "Tin"),
        WordEntry("Banyan", "Nature", "Botany", "Shade", "Trunk"),

        // Culture
        WordEntry("Pohela Boishakh", "Culture", "Festival", "Procession", "Chalk"),
        WordEntry("Shaheed Minar", "Culture", "Memorial", "Pillar", "Petal"),
        WordEntry("Eid", "Culture", "Celebration", "Sighting", "Feast"),
        WordEntry("Jamdani", "Culture", "Textile", "Thread", "Loom"),
        WordEntry("Nakshi Kantha", "Culture", "Craft", "Patch", "Quilt"),
        WordEntry("Boi Mela", "Culture", "Gathering", "Spine", "Ink"),
        WordEntry("Baul", "Culture", "Music", "String", "Wanderer"),
        WordEntry("Cricket", "Culture", "Sport", "Over", "Crease"),
        WordEntry("Carrom", "Culture", "Tabletop", "Pocket", "Powder"),
        WordEntry("Ludo", "Culture", "Tabletop", "Home", "Roll"),
        WordEntry("Kabaddi", "Culture", "Sport", "Breath", "Line"),
        WordEntry("Rickshaw Art", "Culture", "Visual", "Enamel", "Motif"),

        // Landmarks
        WordEntry("Cox's Bazar", "Landmarks", "Coast", "Dune", "Salt"),
        WordEntry("Sajek", "Landmarks", "Highland", "Ridge", "Mist"),
        WordEntry("Saint Martin", "Landmarks", "Atoll", "Reef", "Drift"),
        WordEntry("Jaflong", "Landmarks", "Foothills", "Pebble", "Current"),
        WordEntry("Lalbagh Fort", "Landmarks", "Heritage", "Arch", "Bastion"),
        WordEntry("Hatirjheel", "Landmarks", "Urban", "Loop", "Reflection"),
        WordEntry("TSC", "Landmarks", "Campus", "Lawn", "Chatter"),
        WordEntry("Padma Bridge", "Landmarks", "Structure", "Span", "Current"),
        WordEntry("Sadarghat", "Landmarks", "Terminal", "Pier", "Barge"),
        WordEntry("Ahsan Manzil", "Landmarks", "Heritage", "Dome", "Stair"),

        // Everyday
        WordEntry("Rickshaw", "Everyday", "Transit", "Spoke", "Tassel"),
        WordEntry("CNG", "Everyday", "Transit", "Meter", "Cage"),
        WordEntry("Launch", "Everyday", "Voyage", "Cabin", "Wake"),
        WordEntry("Lungi", "Everyday", "Apparel", "Check", "Knot"),
        WordEntry("Saree", "Everyday", "Apparel", "Border", "Drape"),
        WordEntry("Mosquito Net", "Everyday", "Household", "Mesh", "Corner"),
        WordEntry("Loadshedding", "Everyday", "Utility", "Darkness", "Silence"),
        WordEntry("Traffic Jam", "Everyday", "Urban", "Gridlock", "Brake"),
        WordEntry("bKash", "Everyday", "Fintech", "Agent", "Ledger"),
        WordEntry("Umbrella", "Everyday", "Gear", "Spoke", "Latch"),

        // World Trends
        WordEntry("Netflix", "World Trends", "Streaming", "Queue", "Skip"),
        WordEntry("Instagram", "World Trends", "Social", "Filter", "Grid"),
        WordEntry("ChatGPT", "World Trends", "Software", "Prompt", "Token"),
        WordEntry("iPhone", "World Trends", "Hardware", "Notch", "Glass"),
        WordEntry("Spotify", "World Trends", "Audio", "Shuffle", "Wrapped"),
        WordEntry("YouTube", "World Trends", "Media", "Thumbnail", "Bell"),
        WordEntry("TikTok", "World Trends", "Social", "Trend", "Loop"),
        WordEntry("Bitcoin", "World Trends", "Finance", "Hash", "Block"),
        WordEntry("PlayStation", "World Trends", "Gaming", "Trigger", "Console"),
        WordEntry("Minecraft", "World Trends", "Gaming", "Voxel", "Torch"),
        WordEntry("Uber", "World Trends", "Mobility", "Surge", "Pin"),
        WordEntry("Passport", "World Trends", "Travel", "Stamp", "Chip"),
        WordEntry("Sneakers", "World Trends", "Fashion", "Lace", "Drop"),
        WordEntry("Airport", "World Trends", "Travel", "Gate", "Carousel"),
        WordEntry("Gym", "World Trends", "Fitness", "Rep", "Bench"),

        // Bollywood
        WordEntry("Shah Rukh Khan", "Bollywood", "Actor", "Dimple", "Stardom"),
        WordEntry("Salman Khan", "Bollywood", "Actor", "Bracelet", "Muscle"),
        WordEntry("Aamir Khan", "Bollywood", "Actor", "Perfection", "Chess"),
        WordEntry("Amitabh Bachchan", "Bollywood", "Legend", "Baritone", "Podium"),
        WordEntry("Hrithik Roshan", "Bollywood", "Actor", "Silhouette", "Pace"),
        WordEntry("Ranbir Kapoor", "Bollywood", "Actor", "Melody", "Journey"),
        WordEntry("Deepika Padukone", "Bollywood", "Actress", "Poise", "Crown"),
        WordEntry("Alia Bhatt", "Bollywood", "Actress", "Spark", "Frame"),
        WordEntry("Katrina Kaif", "Bollywood", "Actress", "Rhythm", "Grace"),
        WordEntry("Arijit Singh", "Bollywood", "Singer", "Heartbreak", "Acoustic"),
        WordEntry("3 Idiots", "Bollywood", "Cinema", "Campus", "Pen"),
        WordEntry("DDLJ", "Bollywood", "Romance", "Mustard", "Platform"),
        WordEntry("Sholay", "Bollywood", "Classic", "Coin", "Quarry"),
        WordEntry("PK", "Bollywood", "Satire", "Radio", "Sticker"),
        WordEntry("Dangal", "Bollywood", "Drama", "Mat", "Dawn"),
        WordEntry("Pathaan", "Bollywood", "Action", "Vault", "Freeze"),
        WordEntry("Jawan", "Bollywood", "Action", "Bandage", "Signal"),
        WordEntry("K3G", "Bollywood", "Cinema", "Chopper", "Mansion")
    ) + LocationWordRepository.locationWords

    @Volatile
    private var loadedEntries: List<WordEntry>? = null

    private val _customCategoryNameFlow = MutableStateFlow(CATEGORY_CUSTOM)
    val customCategoryNameFlow: StateFlow<String> = _customCategoryNameFlow.asStateFlow()

    private val _customWordsFlow = MutableStateFlow<List<WordEntry>>(emptyList())
    val customWordsFlow: StateFlow<List<WordEntry>> = _customWordsFlow.asStateFlow()

    val customEntries: List<WordEntry>
        get() = _customWordsFlow.value

    val customCategoryName: String
        get() = _customCategoryNameFlow.value

    val entries: List<WordEntry>
        get() = (loadedEntries ?: fallbackEntries) + _customWordsFlow.value

    val builtInCount: Int
        get() = (loadedEntries ?: fallbackEntries).size

    val builtInCategories: List<String>
        get() = (loadedEntries ?: fallbackEntries).map { it.category }.distinct().sorted()

    val categories: List<String>
        get() = (listOf(CATEGORY_ALL) + builtInCategories + _customCategoryNameFlow.value).distinct()

    fun isCustomCategory(category: String): Boolean {
        val trimmed = category.trim()
        return trimmed.equals(CATEGORY_CUSTOM, ignoreCase = true) ||
            trimmed.equals(_customCategoryNameFlow.value, ignoreCase = true)
    }

    fun loadFromAssets(context: Context) {
        if (loadedEntries == null) {
            runCatching {
                val rawJson = context.assets.open("words.json").bufferedReader().use { it.readText() }
                val array = JSONArray(rawJson)
                val parsed = buildList(array.length()) {
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        val word = obj.optString("word").trim()
                        val category = obj.optString("category").trim()
                        val genre = obj.optString("genre").trim()
                        val hint = obj.optString("hint").trim()
                        val expertHint = obj.optString("expertHint").trim()
                        if (word.isNotEmpty() && category.isNotEmpty()) {
                            add(
                                WordEntry(
                                    word = word,
                                    category = category,
                                    genre = genre.ifEmpty { category },
                                    hint = hint,
                                    expertHint = expertHint.ifEmpty { hint }
                                )
                            )
                        }
                    }
                }
                if (parsed.isNotEmpty()) {
                    loadedEntries = parsed
                }
            }
        }
        loadCustomWords(context)
    }

    private fun loadCustomWords(context: Context) {
        runCatching {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedCat = prefs.getString(KEY_CUSTOM_CATEGORY_NAME, CATEGORY_CUSTOM)
                ?.trim()
                ?.take(30)
                ?.ifEmpty { CATEGORY_CUSTOM }
                ?: CATEGORY_CUSTOM
            _customCategoryNameFlow.value = savedCat

            val raw = prefs.getString(KEY_CUSTOM_WORDS, null) ?: return
            val array = JSONArray(raw)
            val parsed = buildList(array.length()) {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val word = obj.optString("word").trim()
                    val genre = obj.optString("genre").trim()
                    val hint = obj.optString("hint").trim()
                    if (word.isNotEmpty()) {
                        add(
                            WordEntry(
                                word = word,
                                category = savedCat,
                                genre = genre.ifEmpty { savedCat },
                                hint = hint,
                                expertHint = hint
                            )
                        )
                    }
                }
            }
            _customWordsFlow.value = parsed
        }
    }

    private fun saveCustomWords(context: Context?, list: List<WordEntry>, categoryName: String) {
        if (context == null) return
        runCatching {
            val array = JSONArray()
            list.forEach { entry ->
                array.put(
                    JSONObject().apply {
                        put("word", entry.word)
                        put("genre", entry.genre)
                        put("hint", entry.hint)
                    }
                )
            }
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
                putString(KEY_CUSTOM_CATEGORY_NAME, categoryName)
                putString(KEY_CUSTOM_WORDS, array.toString())
            }
        }
    }

    fun setCustomCategoryName(context: Context?, rawName: String): String {
        val cleanName = rawName.trim().take(30).ifEmpty { CATEGORY_CUSTOM }
        val previousName = _customCategoryNameFlow.value
        _customCategoryNameFlow.value = cleanName
        val updated = _customWordsFlow.value.map { entry ->
            entry.copy(
                category = cleanName,
                genre = if (entry.genre == previousName) cleanName else entry.genre
            )
        }
        _customWordsFlow.value = updated
        saveCustomWords(context, updated, cleanName)
        return cleanName
    }

    fun addCustomWord(
        context: Context?,
        rawWord: String,
        rawGenre: String,
        rawHint: String,
        rawCategoryName: String? = null
    ): WordEntry? {
        val word = rawWord.trim().take(40)
        if (word.isEmpty()) return null
        val activeCategory = if (!rawCategoryName.isNullOrBlank()) {
            setCustomCategoryName(context, rawCategoryName)
        } else {
            _customCategoryNameFlow.value
        }
        val genre = rawGenre.trim().take(40).ifEmpty { activeCategory }
        val hint = rawHint.trim().take(60)
        val newEntry = WordEntry(
            word = word,
            category = activeCategory,
            genre = genre,
            hint = hint,
            expertHint = hint
        )
        val updated = (_customWordsFlow.value.filterNot { it.word.equals(word, ignoreCase = true) } + newEntry)
        _customWordsFlow.value = updated
        saveCustomWords(context, updated, activeCategory)
        return newEntry
    }

    fun removeCustomWord(context: Context?, word: String) {
        val updated = _customWordsFlow.value.filterNot { it.word.equals(word, ignoreCase = true) }
        _customWordsFlow.value = updated
        saveCustomWords(context, updated, _customCategoryNameFlow.value)
    }

    fun pickRandomWord(
        selectedCategory: String,
        usedWords: Set<String> = emptySet(),
        random: Random = Random.Default
    ): WordEntry {
        val pool = entries
        val categoryPool = when {
            selectedCategory == CATEGORY_ALL -> pool
            isCustomCategory(selectedCategory) -> _customWordsFlow.value.ifEmpty { pool }
            else -> pool.filter { it.category.equals(selectedCategory, ignoreCase = true) }.ifEmpty { pool }
        }

        val freshPool = categoryPool.filterNot { it.word in usedWords }
        val finalPool = freshPool.ifEmpty { categoryPool }
        return CryptoRandom.pickOne(finalPool) ?: finalPool[random.nextInt(finalPool.size)]
    }

    fun wordsForCategory(category: String): List<WordEntry> = when {
        category == CATEGORY_ALL -> entries
        isCustomCategory(category) -> _customWordsFlow.value
        else -> entries.filter { it.category.equals(category, ignoreCase = true) }
    }

    fun isGuessMatch(guess: String, secretWord: String): Boolean {
        val normalizedGuess = guess.trim().lowercase().replace(Regex("[^a-z0-9]"), "")
        val normalizedTarget = secretWord.trim().lowercase().replace(Regex("[^a-z0-9]"), "")
        if (normalizedGuess.isEmpty() || normalizedTarget.isEmpty()) return false
        return normalizedGuess == normalizedTarget
    }
}
