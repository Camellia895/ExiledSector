package exiledsector.skills;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import exiledsector.skills.skilleffect.DefenseSkillEffect;
import exiledsector.skills.skilleffect.FighterSkillEffect;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SkillNodeTest {

    private MockedStatic<Global> globalMock;

    @BeforeEach
    void setUp() {
        SkillTree.getAllTypes().clear();
        globalMock = Mockito.mockStatic(Global.class);
    }

    @AfterEach
    void tearDown() {
        globalMock.close();
        SkillTree.getAllTypes().clear();
    }

    @Test
    void descriptionDelegatesToTheTypesEffect() {
        SkillType type = new SkillType("hull", "Hull", "graphics/hullmods/reinforced_bulkheads.png", 2, List.of(new SkillTypeEffect(DefenseSkillEffect.HULL_PERCENT, 10f)), SkillTier.SMALL, null, null, null);
        SkillNode node = new SkillNode("hull_1", type, List.of(), 0f, 0f);

        assertEquals("Increases hull points by 10%.", node.getDescription());
    }

    @Test
    void descriptionJoinsMultipleEffectsOnSeparateLines() {
        SkillType type = new SkillType("heavyarmor", "Heavy Armor", "graphics/icons/notable_hullmods/heavy_armor.png", 4, List.of(new SkillTypeEffect(DefenseSkillEffect.ARMOR_PERCENT, 15f), new SkillTypeEffect(DefenseSkillEffect.HULL_PERCENT, 5f)),
                SkillTier.NOTABLE, null, null, null);
        SkillNode node = new SkillNode("heavyarmor_1", type, List.of(), 0f, 0f);

        assertEquals("Increases armor rating by 15%.\n\nIncreases hull points by 5%.", node.getDescription());
    }

    @Test
    void descriptionPutsDeallocationWarningsLastRegardlessOfEffectOrder() {
        SkillType type = new SkillType("converted_hangar", "Converted Hangar", "graphics/icons/notable_hullmods/converted_hangar.png", 4, List.of(new SkillTypeEffect(FighterSkillEffect.FIGHTER_BAYS_FLAT, 1f), new SkillTypeEffect(DefenseSkillEffect.HULL_PERCENT, 5f)),
                SkillTier.KEYSTONE, null, null, null);
        SkillNode node = new SkillNode("converted_hangar_1", type, List.of(), 0f, 0f);

        assertEquals(
                "Increases number of fighter bays by 1.\n\nIncreases hull points by 5%.\n\nCannot be unallocated without at least 1 empty fighter bay.",
                node.getDescription());
    }

    @Test
    void descriptionIsEmptyForATypeWithNoEffectsAndNoOverride() {
        SkillType type = new SkillType("cosmetic", "Cosmetic", "graphics/hullmods/flux_coil_adjunct.png", 2, List.of(), SkillTier.SMALL, null, null, null);
        SkillNode node = new SkillNode("cosmetic_1", type, List.of(), 0f, 0f);

        assertEquals("", node.getDescription());
    }

    @Test
    void descriptionOverrideIsFollowedByEffectLines() {
        SkillType type = new SkillType("hull", "Hull", "graphics/hullmods/reinforced_bulkheads.png", 2, List.of(new SkillTypeEffect(DefenseSkillEffect.HULL_PERCENT, 10f)), SkillTier.SMALL, null, "Custom flavor text.", null);
        SkillNode node = new SkillNode("hull_1", type, List.of(), 0f, 0f);

        assertEquals("Custom flavor text.\n\nIncreases hull points by 10%.", node.getDescription());
    }

    @Test
    void descriptionForAVanillaPassthroughTypeWithNoEffectsYetIsJustTheExclusivityLine() {
        SettingsAPI settings = mock(SettingsAPI.class);
        globalMock.when(Global::getSettings).thenReturn(settings);
        HullModSpecAPI spec = mock(HullModSpecAPI.class);
        when(spec.getDisplayName()).thenReturn("Escort Package");
        when(settings.getHullModSpec("escort_package")).thenReturn(spec);

        SkillType type = new SkillType("escort_package", "Escort Package", "graphics/icons/notable_hullmods/escort_package.png", 4, List.of(), SkillTier.NOTABLE, "escort_package", null, "Needs a real mechanic");
        SkillNode node = new SkillNode("escort_package_1", type, List.of(), 0f, 0f);

        assertEquals("Mutually exclusive with hullmod(s): Escort Package.", node.getDescription());
    }

    @Test
    void descriptionAppendsExclusivityLineAfterEffectsAndWarnings() {
        SettingsAPI settings = mock(SettingsAPI.class);
        globalMock.when(Global::getSettings).thenReturn(settings);
        HullModSpecAPI armoredCladding = mock(HullModSpecAPI.class);
        when(armoredCladding.getDisplayName()).thenReturn("Armored Cladding");
        when(settings.getHullModSpec("armoredcladding")).thenReturn(armoredCladding);

        SkillType type = new SkillType("heavyarmor", "Heavy Armor", "graphics/icons/notable_hullmods/heavy_armor.png", 4,
                List.of(new SkillTypeEffect(DefenseSkillEffect.ARMOR_PERCENT, 15f)), List.of(), SkillTier.NOTABLE, null, null, null,
                List.of(), List.of("armoredcladding"));
        SkillNode node = new SkillNode("heavyarmor_1", type, List.of(), 0f, 0f);

        assertEquals("Increases armor rating by 15%.\n\nMutually exclusive with hullmod(s): Armored Cladding.", node.getDescription());
    }

    @Test
    void descriptionFallsBackToRawIdWhenHullModSpecIsUnknown() {
        SettingsAPI settings = mock(SettingsAPI.class);
        globalMock.when(Global::getSettings).thenReturn(settings);
        when(settings.getHullModSpec("unknown_hullmod")).thenReturn(null);

        SkillType type = new SkillType("hull", "Hull", "a.png", 2, List.of(), List.of(), SkillTier.SMALL, null, null, null,
                List.of(), List.of("unknown_hullmod"));
        SkillNode node = new SkillNode("hull_1", type, List.of(), 0f, 0f);

        assertEquals("Mutually exclusive with hullmod(s): unknown_hullmod.", node.getDescription());
    }

    @Test
    void descriptionIncludesSkillTypeExclusivityLine() {
        SkillType other = new SkillType("adaptiveshields", "Shield Conversion - Omni", "b.png", 1, List.of(), SkillTier.KEYSTONE, null, null, null);
        SkillTree.registerType(other);

        SkillType type = new SkillType("frontemitter", "Shield Conversion - Front", "a.png", 1, List.of(), List.of(), SkillTier.NOTABLE, null, null, null,
                List.of(), List.of(), List.of("adaptiveshields"));
        SkillNode node = new SkillNode("frontemitter_1", type, List.of(), 0f, 0f);

        assertEquals("Mutually exclusive with Shield Conversion - Omni.", node.getDescription());
    }

    @Test
    void descriptionFallsBackToRawIdWhenExclusiveSkillTypeIsUnknown() {
        SkillType type = new SkillType("frontemitter", "Shield Conversion - Front", "a.png", 1, List.of(), List.of(), SkillTier.NOTABLE, null, null, null,
                List.of(), List.of(), List.of("unknown_type"));
        SkillNode node = new SkillNode("frontemitter_1", type, List.of(), 0f, 0f);

        assertEquals("Mutually exclusive with unknown_type.", node.getDescription());
    }

    @Test
    void descriptionCombinesHullModAndSkillTypeExclusivityAsSeparateLines() {
        SettingsAPI settings = mock(SettingsAPI.class);
        globalMock.when(Global::getSettings).thenReturn(settings);
        HullModSpecAPI spec = mock(HullModSpecAPI.class);
        when(spec.getDisplayName()).thenReturn("Escort Package");
        when(settings.getHullModSpec("escort_package")).thenReturn(spec);

        SkillType other = new SkillType("adaptiveshields", "Shield Conversion - Omni", "b.png", 1, List.of(), SkillTier.KEYSTONE, null, null, null);
        SkillTree.registerType(other);

        SkillType type = new SkillType("frontemitter", "Shield Conversion - Front", "a.png", 1, List.of(), List.of(), SkillTier.NOTABLE, null, null, null,
                List.of(), List.of("escort_package"), List.of("adaptiveshields"));
        SkillNode node = new SkillNode("frontemitter_1", type, List.of(), 0f, 0f);

        assertEquals("Mutually exclusive with hullmod(s): Escort Package.\n\nMutually exclusive with Shield Conversion - Omni.", node.getDescription());
    }

    @Test
    void descriptionDedupesNamesSharedBetweenHullModAndSkillTypeExclusivity() {
        SettingsAPI settings = mock(SettingsAPI.class);
        globalMock.when(Global::getSettings).thenReturn(settings);
        HullModSpecAPI spec = mock(HullModSpecAPI.class);
        when(spec.getDisplayName()).thenReturn("Shield Shunt");
        when(settings.getHullModSpec("shield_shunt")).thenReturn(spec);

        SkillType other = new SkillType("shield_shunt", "Shield Shunt", "b.png", 1, List.of(), SkillTier.NOTABLE, null, null, null);
        SkillTree.registerType(other);

        SkillType type = new SkillType("frontemitter", "Shield Conversion - Front", "a.png", 1, List.of(), List.of(), SkillTier.NOTABLE, null, null, null,
                List.of(), List.of("shield_shunt"), List.of("shield_shunt"));
        SkillNode node = new SkillNode("frontemitter_1", type, List.of(), 0f, 0f);

        assertEquals("Mutually exclusive with hullmod(s): Shield Shunt.", node.getDescription());
    }

    @Test
    void resolveEffectiveTypeReturnsItsOwnTypeWhenNotOptional() {
        SkillType type = new SkillType("hull", "Hull", "graphics/hullmods/reinforced_bulkheads.png", 2, List.of(new SkillTypeEffect(DefenseSkillEffect.HULL_PERCENT, 10f)), SkillTier.SMALL, null, null, null);
        SkillNode node = new SkillNode("hull_1", type, List.of(), 0f, 0f);

        assertSame(type, node.resolveEffectiveType(new ShipSkillData()));
    }

    @Test
    void resolveEffectiveTypeReturnsThePlaceholderWhenOptionalAndUnselected() {
        SkillType placeholder = new SkillType("slot", "Optional Skill", "a.png", 0, List.of(), List.of(), SkillTier.SMALL, null, null, null, List.of("hull", "armor"));
        SkillNode node = new SkillNode("slot_1", placeholder, List.of(), 0f, 0f);

        assertSame(placeholder, node.resolveEffectiveType(new ShipSkillData()));
    }

    @Test
    void resolveEffectiveTypeReturnsTheSelectedOptionsTypeOnceChosen() {
        SkillType placeholder = new SkillType("slot", "Optional Skill", "a.png", 0, List.of(), List.of(), SkillTier.SMALL, null, null, null, List.of("hull"));
        SkillType hullOption = new SkillType("hull", "Hull", "graphics/hullmods/reinforced_bulkheads.png", 2, List.of(new SkillTypeEffect(DefenseSkillEffect.HULL_PERCENT, 10f)), SkillTier.SMALL, null, null, null);
        SkillTree.registerType(hullOption);
        SkillNode node = new SkillNode("slot_1", placeholder, List.of(), 0f, 0f);
        ShipSkillData data = new ShipSkillData();
        data.selectOption(node, hullOption);

        assertSame(hullOption, node.resolveEffectiveType(data));
    }
}
