package com.example.data

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.R

enum class AvatarPose(
    val label: String,
    val badgeCode: String,
    val description: String
) {
    ORIGINAL(
        label = "Original",
        badgeCode = "REF",
        description = "Cleaned original reference pose"
    ),
    ACTION_STANCE(
        label = "Combat Pose",
        badgeCode = "ACT",
        description = "Dynamic combat & kinetic strike stance"
    ),
    TACTICAL_BUST(
        label = "Skill Pose",
        badgeCode = "SKL",
        description = "Signature skill & energy resonance stance"
    ),
    CREST_AURA(
        label = "Stance Pose",
        badgeCode = "STN",
        description = "Poised signature guard & crest aura"
    );

    fun next(): AvatarPose {
        val all = entries
        return all[(ordinal + 1) % all.size]
    }
}

enum class AvatarGraphicMotif(val displayName: String) {
    INK_WASH("Ink Dragon"),
    EMERALD_RUNES("Emerald Sigil"),
    CRIMSON_EMBERS("Ember Storm"),
    CYBER_HUD("Tactical Grid"),
    LUNAR_RIPPLES("Moonlit Tide"),
    SOLAR_RAYS("Solar Flare"),
    ARCANE_TIDE("Water Vortex"),
    BLADE_SLASH("Blade Arc"),
    FEATHER_PLUME("Peacock Aura"),
    FOREST_WIND("Gale Leaves")
}

enum class AvatarCategoryFilter(val label: String) {
    ALL("All (22)"),
    BLADES("Blades & Shadow"),
    MYSTIC("Mystic & Arcane"),
    VANGUARD("Vanguard & Tactician")
}

@Immutable
data class AvatarCharacter(
    val id: String,
    val code: String,
    val name: String,
    val title: String,
    val archetype: String,
    val filterGroup: AvatarCategoryFilter,
    val motif: AvatarGraphicMotif,
    val accentColor: Color,
    val glowColor: Color,
    val surfaceTint: Color,
    @DrawableRes val baseDrawableRes: Int,
    @DrawableRes val actionDrawableRes: Int,
    @DrawableRes val skillDrawableRes: Int,
    @DrawableRes val stanceDrawableRes: Int,
    val bustBiasX: Float = 0f,
    val bustBiasY: Float = -0.78f,
    val bustZoom: Float = 2.05f
) {
    val hasGeneratedAltPose: Boolean
        get() = true

    fun drawableFor(pose: AvatarPose): Int = when (pose) {
        AvatarPose.ORIGINAL -> baseDrawableRes
        AvatarPose.ACTION_STANCE -> actionDrawableRes
        AvatarPose.TACTICAL_BUST -> skillDrawableRes
        AvatarPose.CREST_AURA -> stanceDrawableRes
    }
}

object AvatarCatalog {

    val all: List<AvatarCharacter> = listOf(
        AvatarCharacter(
            id = "avatar_01",
            code = "AV-01",
            name = "Kuroha",
            title = "Ink Sovereign",
            archetype = "Xianxia Mystic",
            filterGroup = AvatarCategoryFilter.MYSTIC,
            motif = AvatarGraphicMotif.INK_WASH,
            accentColor = Color(0xFFE11D48),
            glowColor = Color(0xFFFF6B8B),
            surfaceTint = Color(0xFF1F1218),
            baseDrawableRes = R.drawable.avatar_01_ink_maiden,
            actionDrawableRes = R.drawable.avatar_01_kuroha_p1,
            skillDrawableRes = R.drawable.avatar_01_kuroha_p2,
            stanceDrawableRes = R.drawable.avatar_01_kuroha_p3,
            bustBiasX = 0.0f,
            bustBiasY = -0.78f,
            bustZoom = 2.10f
        ),
        AvatarCharacter(
            id = "avatar_02",
            code = "AV-02",
            name = "Sylph",
            title = "Emerald Tactician",
            archetype = "Arcane Strategist",
            filterGroup = AvatarCategoryFilter.VANGUARD,
            motif = AvatarGraphicMotif.EMERALD_RUNES,
            accentColor = Color(0xFF10B981),
            glowColor = Color(0xFF5EEAD4),
            surfaceTint = Color(0xFF0A241E),
            baseDrawableRes = R.drawable.avatar_02_emerald_tactician,
            actionDrawableRes = R.drawable.avatar_02_sylph_p1,
            skillDrawableRes = R.drawable.avatar_02_sylph_p2,
            stanceDrawableRes = R.drawable.avatar_02_sylph_p3,
            bustBiasX = -0.04f,
            bustBiasY = -0.82f,
            bustZoom = 2.15f
        ),
        AvatarCharacter(
            id = "avatar_03",
            code = "AV-03",
            name = "Vaelen",
            title = "Scarred Brawler",
            archetype = "Close-Quarters Enforcer",
            filterGroup = AvatarCategoryFilter.VANGUARD,
            motif = AvatarGraphicMotif.BLADE_SLASH,
            accentColor = Color(0xFF94A3B8),
            glowColor = Color(0xFFE2E8F0),
            surfaceTint = Color(0xFF181C26),
            baseDrawableRes = R.drawable.avatar_03_scarred_brawler,
            actionDrawableRes = R.drawable.avatar_03_vaelen_p1,
            skillDrawableRes = R.drawable.avatar_03_vaelen_p2,
            stanceDrawableRes = R.drawable.avatar_03_vaelen_p3,
            bustBiasX = -0.06f,
            bustBiasY = -0.80f,
            bustZoom = 2.10f
        ),
        AvatarCharacter(
            id = "avatar_04",
            code = "AV-04",
            name = "Aarohi",
            title = "Ember Dancer",
            archetype = "Flame Illusionist",
            filterGroup = AvatarCategoryFilter.MYSTIC,
            motif = AvatarGraphicMotif.CRIMSON_EMBERS,
            accentColor = Color(0xFFF97316),
            glowColor = Color(0xFFFDBA74),
            surfaceTint = Color(0xFF2B120C),
            baseDrawableRes = R.drawable.avatar_04_ember_dancer,
            actionDrawableRes = R.drawable.avatar_04_aarohi_p1,
            skillDrawableRes = R.drawable.avatar_04_aarohi_p2,
            stanceDrawableRes = R.drawable.avatar_04_aarohi_p3,
            bustBiasX = 0.04f,
            bustBiasY = -0.80f,
            bustZoom = 2.10f
        ),
        AvatarCharacter(
            id = "avatar_05",
            code = "AV-05",
            name = "Nyx",
            title = "Obsidian Valkyrie",
            archetype = "Armored Duelist",
            filterGroup = AvatarCategoryFilter.BLADES,
            motif = AvatarGraphicMotif.BLADE_SLASH,
            accentColor = Color(0xFFEF4444),
            glowColor = Color(0xFFFCA5A5),
            surfaceTint = Color(0xFF1E1016),
            baseDrawableRes = R.drawable.avatar_05_obsidian_valkyrie,
            actionDrawableRes = R.drawable.avatar_05_nyx_p1,
            skillDrawableRes = R.drawable.avatar_05_nyx_p2,
            stanceDrawableRes = R.drawable.avatar_05_nyx_p3,
            bustBiasX = 0.0f,
            bustBiasY = -0.84f,
            bustZoom = 2.15f
        ),
        AvatarCharacter(
            id = "avatar_06",
            code = "AV-06",
            name = "Elena",
            title = "Silver Duelist",
            archetype = "Steel Vanguard",
            filterGroup = AvatarCategoryFilter.BLADES,
            motif = AvatarGraphicMotif.BLADE_SLASH,
            accentColor = Color(0xFFCBD5E1),
            glowColor = Color(0xFFF8FAFC),
            surfaceTint = Color(0xFF1A1E28),
            baseDrawableRes = R.drawable.avatar_06_silver_duelist,
            actionDrawableRes = R.drawable.avatar_06_elena_p1,
            skillDrawableRes = R.drawable.avatar_06_elena_p2,
            stanceDrawableRes = R.drawable.avatar_06_elena_p3,
            bustBiasX = 0.0f,
            bustBiasY = -0.82f,
            bustZoom = 2.10f
        ),
        AvatarCharacter(
            id = "avatar_07",
            code = "AV-07",
            name = "Marina",
            title = "Ivory Mystic",
            archetype = "Seer Operative",
            filterGroup = AvatarCategoryFilter.MYSTIC,
            motif = AvatarGraphicMotif.ARCANE_TIDE,
            accentColor = Color(0xFF38BDF8),
            glowColor = Color(0xFFBAE6FD),
            surfaceTint = Color(0xFF101D2E),
            baseDrawableRes = R.drawable.avatar_07_ivory_mystic,
            actionDrawableRes = R.drawable.avatar_07_marina_p1,
            skillDrawableRes = R.drawable.avatar_07_marina_p2,
            stanceDrawableRes = R.drawable.avatar_07_marina_p3,
            bustBiasX = 0.0f,
            bustBiasY = -0.80f,
            bustZoom = 2.10f
        ),
        AvatarCharacter(
            id = "avatar_08",
            code = "AV-08",
            name = "Usagi",
            title = "Bunny Ronin",
            archetype = "Masked Assassin",
            filterGroup = AvatarCategoryFilter.BLADES,
            motif = AvatarGraphicMotif.CYBER_HUD,
            accentColor = Color(0xFF06B6D4),
            glowColor = Color(0xFF67E8F9),
            surfaceTint = Color(0xFF0B1F2A),
            baseDrawableRes = R.drawable.avatar_08_bunny_ronin,
            actionDrawableRes = R.drawable.avatar_08_usagi_p1,
            skillDrawableRes = R.drawable.avatar_08_usagi_p2,
            stanceDrawableRes = R.drawable.avatar_08_usagi_p3,
            bustBiasX = 0.0f,
            bustBiasY = -0.76f,
            bustZoom = 2.05f
        ),
        AvatarCharacter(
            id = "avatar_09",
            code = "AV-09",
            name = "Gama",
            title = "Jade Frog Monk",
            archetype = "Chi Martial Master",
            filterGroup = AvatarCategoryFilter.VANGUARD,
            motif = AvatarGraphicMotif.EMERALD_RUNES,
            accentColor = Color(0xFF22C55E),
            glowColor = Color(0xFF86EFAC),
            surfaceTint = Color(0xFF0D2516),
            baseDrawableRes = R.drawable.avatar_09_jade_frog_monk,
            actionDrawableRes = R.drawable.avatar_09_gama_p1,
            skillDrawableRes = R.drawable.avatar_09_gama_p2,
            stanceDrawableRes = R.drawable.avatar_09_gama_p3,
            bustBiasX = 0.04f,
            bustBiasY = -0.82f,
            bustZoom = 2.05f
        ),
        AvatarCharacter(
            id = "avatar_10",
            code = "AV-10",
            name = "Kiri",
            title = "Ink-Blade Phantom",
            archetype = "Silent Executioner",
            filterGroup = AvatarCategoryFilter.BLADES,
            motif = AvatarGraphicMotif.INK_WASH,
            accentColor = Color(0xFF14B8A6),
            glowColor = Color(0xFF5EEAD4),
            surfaceTint = Color(0xFF0D2121),
            baseDrawableRes = R.drawable.avatar_10_ink_blade_phantom,
            actionDrawableRes = R.drawable.avatar_10_kiri_p1,
            skillDrawableRes = R.drawable.avatar_10_kiri_p2,
            stanceDrawableRes = R.drawable.avatar_10_kiri_p3,
            bustBiasX = 0.0f,
            bustBiasY = -0.78f,
            bustZoom = 2.10f
        ),
        AvatarCharacter(
            id = "avatar_11",
            code = "AV-11",
            name = "Liora",
            title = "Peacock Empress",
            archetype = "Celestial Aristocrat",
            filterGroup = AvatarCategoryFilter.MYSTIC,
            motif = AvatarGraphicMotif.FEATHER_PLUME,
            accentColor = Color(0xFF0EA5E9),
            glowColor = Color(0xFF7DD3FC),
            surfaceTint = Color(0xFF0A2033),
            baseDrawableRes = R.drawable.avatar_11_peacock_empress,
            actionDrawableRes = R.drawable.avatar_11_liora_p1,
            skillDrawableRes = R.drawable.avatar_11_liora_p2,
            stanceDrawableRes = R.drawable.avatar_11_liora_p3,
            bustBiasX = 0.0f,
            bustBiasY = -0.74f,
            bustZoom = 2.10f
        ),
        AvatarCharacter(
            id = "avatar_12",
            code = "AV-12",
            name = "Ren",
            title = "Monochrome Rogue",
            archetype = "Shadow Broker",
            filterGroup = AvatarCategoryFilter.BLADES,
            motif = AvatarGraphicMotif.INK_WASH,
            accentColor = Color(0xFFA1A1AA),
            glowColor = Color(0xFFF4F4F5),
            surfaceTint = Color(0xFF17181E),
            baseDrawableRes = R.drawable.avatar_12_monochrome_rogue,
            actionDrawableRes = R.drawable.avatar_12_ren_p1,
            skillDrawableRes = R.drawable.avatar_12_ren_p2,
            stanceDrawableRes = R.drawable.avatar_12_ren_p3,
            bustBiasX = 0.0f,
            bustBiasY = -0.82f,
            bustZoom = 2.10f
        ),
        AvatarCharacter(
            id = "avatar_13",
            code = "AV-13",
            name = "Surya",
            title = "Sun Sovereign",
            archetype = "Royal Blade",
            filterGroup = AvatarCategoryFilter.VANGUARD,
            motif = AvatarGraphicMotif.SOLAR_RAYS,
            accentColor = Color(0xFFF59E0B),
            glowColor = Color(0xFFFDE68A),
            surfaceTint = Color(0xFF2B1D07),
            baseDrawableRes = R.drawable.avatar_13_sun_sovereign,
            actionDrawableRes = R.drawable.avatar_13_surya_p1,
            skillDrawableRes = R.drawable.avatar_13_surya_p2,
            stanceDrawableRes = R.drawable.avatar_13_surya_p3,
            bustBiasX = 0.0f,
            bustBiasY = -0.80f,
            bustZoom = 2.10f
        ),
        AvatarCharacter(
            id = "avatar_14",
            code = "AV-14",
            name = "Zara",
            title = "Violet Duelist",
            archetype = "Whip & Sword Ace",
            filterGroup = AvatarCategoryFilter.BLADES,
            motif = AvatarGraphicMotif.BLADE_SLASH,
            accentColor = Color(0xFFA855F7),
            glowColor = Color(0xFFD8B4FE),
            surfaceTint = Color(0xFF1F1133),
            baseDrawableRes = R.drawable.avatar_14_violet_duelist,
            actionDrawableRes = R.drawable.avatar_14_zara_p1,
            skillDrawableRes = R.drawable.avatar_14_zara_p2,
            stanceDrawableRes = R.drawable.avatar_14_zara_p3,
            bustBiasX = 0.0f,
            bustBiasY = -0.80f,
            bustZoom = 2.10f
        ),
        AvatarCharacter(
            id = "avatar_15",
            code = "AV-15",
            name = "Kora",
            title = "Street Striker",
            archetype = "Kinetic Brawler",
            filterGroup = AvatarCategoryFilter.VANGUARD,
            motif = AvatarGraphicMotif.FOREST_WIND,
            accentColor = Color(0xFFFB923C),
            glowColor = Color(0xFFFED7AA),
            surfaceTint = Color(0xFF1D241E),
            baseDrawableRes = R.drawable.avatar_15_street_striker,
            actionDrawableRes = R.drawable.avatar_15_kora_p1,
            skillDrawableRes = R.drawable.avatar_15_kora_p2,
            stanceDrawableRes = R.drawable.avatar_15_kora_p3,
            bustBiasX = 0.0f,
            bustBiasY = -0.78f,
            bustZoom = 2.05f
        ),
        AvatarCharacter(
            id = "avatar_16",
            code = "AV-16",
            name = "Torin",
            title = "Greataxe Vanguard",
            archetype = "Heavy Breaker",
            filterGroup = AvatarCategoryFilter.VANGUARD,
            motif = AvatarGraphicMotif.SOLAR_RAYS,
            accentColor = Color(0xFFD97706),
            glowColor = Color(0xFFFCD34D),
            surfaceTint = Color(0xFF261A0E),
            baseDrawableRes = R.drawable.avatar_16_greataxe_vanguard,
            actionDrawableRes = R.drawable.avatar_16_torin_p1,
            skillDrawableRes = R.drawable.avatar_16_torin_p2,
            stanceDrawableRes = R.drawable.avatar_16_torin_p3,
            bustBiasX = 0.0f,
            bustBiasY = -0.76f,
            bustZoom = 2.05f
        ),
        AvatarCharacter(
            id = "avatar_17",
            code = "AV-17",
            name = "Nila",
            title = "Moonlit Enchantress",
            archetype = "Lantern Oracle",
            filterGroup = AvatarCategoryFilter.MYSTIC,
            motif = AvatarGraphicMotif.LUNAR_RIPPLES,
            accentColor = Color(0xFF60A5FA),
            glowColor = Color(0xFFBFDBFE),
            surfaceTint = Color(0xFF0E1B33),
            baseDrawableRes = R.drawable.avatar_17_moonlit_enchantress,
            actionDrawableRes = R.drawable.avatar_17_nila_p1,
            skillDrawableRes = R.drawable.avatar_17_nila_p2,
            stanceDrawableRes = R.drawable.avatar_17_nila_p3,
            bustBiasX = 0.0f,
            bustBiasY = -0.78f,
            bustZoom = 2.05f
        ),
        AvatarCharacter(
            id = "avatar_18",
            code = "AV-18",
            name = "Zero",
            title = "White-Ops Agent",
            archetype = "Spec-Ops Infiltrator",
            filterGroup = AvatarCategoryFilter.VANGUARD,
            motif = AvatarGraphicMotif.CYBER_HUD,
            accentColor = Color(0xFF8B5CF6),
            glowColor = Color(0xFFC4B5FD),
            surfaceTint = Color(0xFF1C1036),
            baseDrawableRes = R.drawable.avatar_18_white_ops_agent,
            actionDrawableRes = R.drawable.avatar_18_zero_p1,
            skillDrawableRes = R.drawable.avatar_18_zero_p2,
            stanceDrawableRes = R.drawable.avatar_18_zero_p3,
            bustBiasX = 0.0f,
            bustBiasY = -0.82f,
            bustZoom = 2.15f
        ),
        AvatarCharacter(
            id = "avatar_19",
            code = "AV-19",
            name = "Mary",
            title = "Tidal Mage",
            archetype = "Hydro Arcanist",
            filterGroup = AvatarCategoryFilter.MYSTIC,
            motif = AvatarGraphicMotif.ARCANE_TIDE,
            accentColor = Color(0xFF0284C7),
            glowColor = Color(0xFF7DD3FC),
            surfaceTint = Color(0xFF0C2238),
            baseDrawableRes = R.drawable.avatar_19_tidal_mage,
            actionDrawableRes = R.drawable.avatar_19_mary_p1,
            skillDrawableRes = R.drawable.avatar_19_mary_p2,
            stanceDrawableRes = R.drawable.avatar_19_mary_p3,
            bustBiasX = 0.0f,
            bustBiasY = -0.80f,
            bustZoom = 2.10f
        ),
        AvatarCharacter(
            id = "avatar_20",
            code = "AV-20",
            name = "Dante",
            title = "Imperial Shadow",
            archetype = "Eclipse Commander",
            filterGroup = AvatarCategoryFilter.BLADES,
            motif = AvatarGraphicMotif.CRIMSON_EMBERS,
            accentColor = Color(0xFFDC2626),
            glowColor = Color(0xFFFCA5A5),
            surfaceTint = Color(0xFF260D11),
            baseDrawableRes = R.drawable.avatar_20_imperial_shadow,
            actionDrawableRes = R.drawable.avatar_20_dante_p1,
            skillDrawableRes = R.drawable.avatar_20_dante_p2,
            stanceDrawableRes = R.drawable.avatar_20_dante_p3,
            bustBiasX = 0.0f,
            bustBiasY = -0.80f,
            bustZoom = 2.10f
        ),
        AvatarCharacter(
            id = "avatar_21",
            code = "AV-21",
            name = "Akane",
            title = "Scarlet Shinobi",
            archetype = "Shadow Striker",
            filterGroup = AvatarCategoryFilter.BLADES,
            motif = AvatarGraphicMotif.CRIMSON_EMBERS,
            accentColor = Color(0xFFF43F5E),
            glowColor = Color(0xFFFDA4AF),
            surfaceTint = Color(0xFF260B15),
            baseDrawableRes = R.drawable.avatar_21_scarlet_shinobi,
            actionDrawableRes = R.drawable.avatar_21_akane_p1,
            skillDrawableRes = R.drawable.avatar_21_akane_p2,
            stanceDrawableRes = R.drawable.avatar_21_akane_p3,
            bustBiasX = 0.0f,
            bustBiasY = -0.80f,
            bustZoom = 2.10f
        ),
        AvatarCharacter(
            id = "avatar_22",
            code = "AV-22",
            name = "Aelar",
            title = "Elf Ranger",
            archetype = "Forest Marksman",
            filterGroup = AvatarCategoryFilter.VANGUARD,
            motif = AvatarGraphicMotif.FOREST_WIND,
            accentColor = Color(0xFF10B981),
            glowColor = Color(0xFF6EE7B7),
            surfaceTint = Color(0xFF0D211B),
            baseDrawableRes = R.drawable.avatar_22_elf_ranger,
            actionDrawableRes = R.drawable.avatar_22_aelar_p1,
            skillDrawableRes = R.drawable.avatar_22_aelar_p2,
            stanceDrawableRes = R.drawable.avatar_22_aelar_p3,
            bustBiasX = 0.0f,
            bustBiasY = -0.78f,
            bustZoom = 2.10f
        )
    )

    private val byId = all.associateBy { it.id }

    fun findById(id: String?): AvatarCharacter =
        if (id != null) byId[id] ?: all.first() else all.first()

    fun defaultForIndex(index: Int): AvatarCharacter {
        val safeIdx = ((index % all.size) + all.size) % all.size
        return all[safeIdx]
    }

    fun defaultIdForSeed(seed: String): String {
        val idx = (seed.hashCode() and 0x7FFFFFFF) % all.size
        return all[idx].id
    }

    fun nextAvailableAvatar(usedIds: Set<String>, preferredIndex: Int = 0): AvatarCharacter {
        return all.firstOrNull { it.id !in usedIds } ?: defaultForIndex(preferredIndex)
    }
}
