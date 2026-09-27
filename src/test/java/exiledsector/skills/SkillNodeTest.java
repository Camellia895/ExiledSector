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
        SkillType type = new SkillType.Builder("hull", "Hull", "graphics/hullmods/reinforced_bulkheads.png", SkillTier.SMALL)
                .effects(List.of(new SkillTypeEffect(DefenseSkillEffect.HULL_PERCENT, 10f)))
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .build();
        SkillNode node = new SkillNode("hull_1", type, List.of(), 0f, 0f);

        assertEquals("Increases hull points by 10%.", node.getDescription());
    }

    @Test
    void descriptionStatesHowLongATemporaryAfterDeploymentNodeLasts() {
        SkillType type = new SkillType.Builder("hull", "Hull", "a.png", SkillTier.SMALL)
                .effects(List.of(new SkillTypeEffect(DefenseSkillEffect.HULL_PERCENT, 10f)))
                .temporaryAfterDeploymentSeconds(60f)
                .build();

        assertEquals("Increases hull points by 10%.\n\nThese effects only last for the first 60 seconds after the ship is deployed.",
                SkillNode.describeType(type, null));
    }

    @Test
    void descriptionJoinsMultipleEffectsOnSeparateLines() {
        SkillType type = new SkillType.Builder("heavyarmor", "Heavy Armor", "graphics/icons/notable_hullmods/heavy_armor.png", SkillTier.NOTABLE)
                .effects(List.of(new SkillTypeEffect(DefenseSkillEffect.ARMOR_PERCENT, 15f), new SkillTypeEffect(DefenseSkillEffect.HULL_PERCENT, 5f)))
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .build();
        SkillNode node = new SkillNode("heavyarmor_1", type, List.of(), 0f, 0f);

        assertEquals("Increases armor by 15%.\n\nIncreases hull points by 5%.", node.getDescription());
    }

    @Test
    void descriptionPutsDeallocationWarningsLastRegardlessOfEffectOrder() {
        SkillType type = new SkillType.Builder("converted_hangar", "Converted Hangar", "graphics/icons/notable_hullmods/converted_hangar.png", SkillTier.KEYSTONE)
                .effects(List.of(new SkillTypeEffect(FighterSkillEffect.FIGHTER_BAYS_FLAT, 1f), new SkillTypeEffect(DefenseSkillEffect.HULL_PERCENT, 5f)))
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .build();
        SkillNode node = new SkillNode("converted_hangar_1", type, List.of(), 0f, 0f);

        assertEquals(
                "Increases number of fighter bays by 1.\n\nIncreases hull points by 5%.\n\nCannot be unallocated without at least 1 empty fighter bay.",
                node.getDescription());
    }

    @Test
    void descriptionIsEmptyForATypeWithNoEffectsAndNoOverride() {
        SkillType type = new SkillType.Builder("cosmetic", "Cosmetic", "graphics/hullmods/flux_coil_adjunct.png", SkillTier.SMALL)
                .effects(List.of())
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .build();
        SkillNode node = new SkillNode("cosmetic_1", type, List.of(), 0f, 0f);

        assertEquals("", node.getDescription());
    }

    @Test
    void descriptionOverrideIsFollowedByEffectLines() {
        SkillType type = new SkillType.Builder("hull", "Hull", "graphics/hullmods/reinforced_bulkheads.png", SkillTier.SMALL)
                .effects(List.of(new SkillTypeEffect(DefenseSkillEffect.HULL_PERCENT, 10f)))
                .vanillaHullModId(null)
                .descriptionOverride("Custom flavor text.")
                .todo(null)
                .build();
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

        SkillType type = new SkillType.Builder("escort_package", "Escort Package", "graphics/icons/notable_hullmods/escort_package.png", SkillTier.NOTABLE)
                .effects(List.of())
                .vanillaHullModId("escort_package")
                .descriptionOverride(null)
                .todo("Needs a real mechanic")
                .build();
        SkillNode node = new SkillNode("escort_package_1", type, List.of(), 0f, 0f);

        assertEquals("Mutually exclusive with: Escort Package.", node.getDescription());
    }

    @Test
    void descriptionAppendsExclusivityLineAfterEffectsAndWarnings() {
        SettingsAPI settings = mock(SettingsAPI.class);
        globalMock.when(Global::getSettings).thenReturn(settings);
        HullModSpecAPI armoredCladding = mock(HullModSpecAPI.class);
        when(armoredCladding.getDisplayName()).thenReturn("Armored Cladding");
        when(settings.getHullModSpec("armoredcladding")).thenReturn(armoredCladding);

        SkillType type = new SkillType.Builder("heavyarmor", "Heavy Armor", "graphics/icons/notable_hullmods/heavy_armor.png", SkillTier.NOTABLE)
                .effects(List.of(new SkillTypeEffect(DefenseSkillEffect.ARMOR_PERCENT, 15f)))
                .hullSizeEffects(List.of())
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .optionalOptionIds(List.of())
                .exclusiveHullModIds(List.of("armoredcladding"))
                .build();
        SkillNode node = new SkillNode("heavyarmor_1", type, List.of(), 0f, 0f);

        assertEquals("Increases armor by 15%.\n\nMutually exclusive with: Armored Cladding.", node.getDescription());
    }

    @Test
    void descriptionFallsBackToRawIdWhenHullModSpecIsUnknown() {
        SettingsAPI settings = mock(SettingsAPI.class);
        globalMock.when(Global::getSettings).thenReturn(settings);
        when(settings.getHullModSpec("unknown_hullmod")).thenReturn(null);

        SkillType type = new SkillType.Builder("hull", "Hull", "a.png", SkillTier.SMALL)
                .effects(List.of())
                .hullSizeEffects(List.of())
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .optionalOptionIds(List.of())
                .exclusiveHullModIds(List.of("unknown_hullmod"))
                .build();
        SkillNode node = new SkillNode("hull_1", type, List.of(), 0f, 0f);

        assertEquals("Mutually exclusive with: unknown_hullmod.", node.getDescription());
    }

    @Test
    void descriptionIncludesSkillTypeExclusivityLine() {
        SkillType other = new SkillType.Builder("adaptiveshields", "Shield Conversion - Omni", "b.png", SkillTier.KEYSTONE)
                .effects(List.of())
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .build();
        SkillTree.registerType(other);

        SkillType type = new SkillType.Builder("frontemitter", "Shield Conversion - Front", "a.png", SkillTier.NOTABLE)
                .effects(List.of())
                .hullSizeEffects(List.of())
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .optionalOptionIds(List.of())
                .exclusiveHullModIds(List.of())
                .exclusiveSkillTypeIds(List.of("adaptiveshields"))
                .build();
        SkillNode node = new SkillNode("frontemitter_1", type, List.of(), 0f, 0f);

        assertEquals("Mutually exclusive with Shield Conversion - Omni.", node.getDescription());
    }

    @Test
    void descriptionFallsBackToRawIdWhenExclusiveSkillTypeIsUnknown() {
        SkillType type = new SkillType.Builder("frontemitter", "Shield Conversion - Front", "a.png", SkillTier.NOTABLE)
                .effects(List.of())
                .hullSizeEffects(List.of())
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .optionalOptionIds(List.of())
                .exclusiveHullModIds(List.of())
                .exclusiveSkillTypeIds(List.of("unknown_type"))
                .build();
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

        SkillType other = new SkillType.Builder("adaptiveshields", "Shield Conversion - Omni", "b.png", SkillTier.KEYSTONE)
                .effects(List.of())
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .build();
        SkillTree.registerType(other);

        SkillType type = new SkillType.Builder("frontemitter", "Shield Conversion - Front", "a.png", SkillTier.NOTABLE)
                .effects(List.of())
                .hullSizeEffects(List.of())
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .optionalOptionIds(List.of())
                .exclusiveHullModIds(List.of("escort_package"))
                .exclusiveSkillTypeIds(List.of("adaptiveshields"))
                .build();
        SkillNode node = new SkillNode("frontemitter_1", type, List.of(), 0f, 0f);

        assertEquals("Mutually exclusive with: Escort Package.\n\nMutually exclusive with Shield Conversion - Omni.", node.getDescription());
    }

    @Test
    void descriptionDedupesNamesSharedBetweenHullModAndSkillTypeExclusivity() {
        SettingsAPI settings = mock(SettingsAPI.class);
        globalMock.when(Global::getSettings).thenReturn(settings);
        HullModSpecAPI spec = mock(HullModSpecAPI.class);
        when(spec.getDisplayName()).thenReturn("Shield Shunt");
        when(settings.getHullModSpec("shield_shunt")).thenReturn(spec);

        SkillType other = new SkillType.Builder("shield_shunt", "Shield Shunt", "b.png", SkillTier.NOTABLE)
                .effects(List.of())
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .build();
        SkillTree.registerType(other);

        SkillType type = new SkillType.Builder("frontemitter", "Shield Conversion - Front", "a.png", SkillTier.NOTABLE)
                .effects(List.of())
                .hullSizeEffects(List.of())
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .optionalOptionIds(List.of())
                .exclusiveHullModIds(List.of("shield_shunt"))
                .exclusiveSkillTypeIds(List.of("shield_shunt"))
                .build();
        SkillNode node = new SkillNode("frontemitter_1", type, List.of(), 0f, 0f);

        assertEquals("Mutually exclusive with: Shield Shunt.", node.getDescription());
    }

    @Test
    void resolveEffectiveTypeReturnsItsOwnTypeWhenNotOptional() {
        SkillType type = new SkillType.Builder("hull", "Hull", "graphics/hullmods/reinforced_bulkheads.png", SkillTier.SMALL)
                .effects(List.of(new SkillTypeEffect(DefenseSkillEffect.HULL_PERCENT, 10f)))
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .build();
        SkillNode node = new SkillNode("hull_1", type, List.of(), 0f, 0f);

        assertSame(type, node.resolveEffectiveType(new ShipSkillData()));
    }

    @Test
    void resolveEffectiveTypeReturnsThePlaceholderWhenOptionalAndUnselected() {
        SkillType placeholder = new SkillType.Builder("slot", "Optional Skill", "a.png", SkillTier.SMALL)
                .effects(List.of())
                .hullSizeEffects(List.of())
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .optionalOptionIds(List.of("hull", "armor"))
                .build();
        SkillNode node = new SkillNode("slot_1", placeholder, List.of(), 0f, 0f);

        assertSame(placeholder, node.resolveEffectiveType(new ShipSkillData()));
    }

    @Test
    void resolveEffectiveTypeReturnsTheSelectedOptionsTypeOnceChosen() {
        SkillType placeholder = new SkillType.Builder("slot", "Optional Skill", "a.png", SkillTier.SMALL)
                .effects(List.of())
                .hullSizeEffects(List.of())
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .optionalOptionIds(List.of("hull"))
                .build();
        SkillType hullOption = new SkillType.Builder("hull", "Hull", "graphics/hullmods/reinforced_bulkheads.png", SkillTier.SMALL)
                .effects(List.of(new SkillTypeEffect(DefenseSkillEffect.HULL_PERCENT, 10f)))
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .build();
        SkillTree.registerType(hullOption);
        SkillNode node = new SkillNode("slot_1", placeholder, List.of(), 0f, 0f);
        ShipSkillData data = new ShipSkillData();
        data.selectOption(node, hullOption, 1);

        assertSame(hullOption, node.resolveEffectiveType(data));
    }
}
