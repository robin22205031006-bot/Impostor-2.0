package com.example.data

import kotlin.random.Random

/**
 * Repository of secret "location" words used in the social deduction game.
 * Non-impostors (Crewmates) reference the selected location word during discussion,
 * while Impostors only receive optional metadata or must deduce the location from clues.
 */
object LocationWordRepository {

    val locationWords: List<WordEntry> = listOf(
        WordEntry("Space Station", "Locations", "Sci-Fi", "Orbit", "Airlock"),
        WordEntry("Submarine", "Locations", "Maritime", "Periscope", "Depth"),
        WordEntry("Hospital", "Locations", "Civic", "Ward", "Pulse"),
        WordEntry("Casino", "Locations", "Nightlife", "Chips", "Velvet"),
        WordEntry("Airport", "Locations", "Transit", "Terminal", "Runway"),
        WordEntry("Pirate Ship", "Locations", "Adventure", "Mast", "Plank"),
        WordEntry("Art Museum", "Locations", "Culture", "Gallery", "Frame"),
        WordEntry("Movie Theater", "Locations", "Entertainment", "Projector", "Popcorn"),
        WordEntry("Ski Resort", "Locations", "Alpine", "Gondola", "Powder"),
        WordEntry("Train Station", "Locations", "Transit", "Platform", "Whistle"),
        WordEntry("Volcano Lab", "Locations", "Science", "Magma", "Sensor"),
        WordEntry("Cruise Ship", "Locations", "Voyage", "Deck", "Horizon"),
        WordEntry("University Campus", "Locations", "Academic", "Quad", "Lecture"),
        WordEntry("Police Station", "Locations", "Civic", "Precinct", "Badge"),
        WordEntry("Amusement Park", "Locations", "Leisure", "Coaster", "Ticket"),
        WordEntry("Desert Oasis", "Locations", "Nature", "Mirage", "Palm"),
        WordEntry("Underground Bunker", "Locations", "Stealth", "Hatch", "Ration"),
        WordEntry("Botanical Garden", "Locations", "Nature", "Greenhouse", "Pollen"),
        WordEntry("Film Studio", "Locations", "Media", "Clapper", "Rig"),
        WordEntry("Coral Reef", "Locations", "Ocean", "Lagoon", "Snorkel")
    )

    fun getAllLocations(): List<WordEntry> = locationWords

    fun pickRandomLocationWord(
        excludeWords: Set<String> = emptySet(),
        random: Random = Random.Default
    ): WordEntry {
        val fresh = locationWords.filterNot { it.word in excludeWords }
        val pool = fresh.ifEmpty { locationWords }
        return CryptoRandom.pickOne(pool) ?: pool[random.nextInt(pool.size)]
    }
}
