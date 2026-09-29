package exiledsector;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import exiledsector.effects.CombatXpListener;
import exiledsector.effects.NpcFleetDialogListener;
import exiledsector.effects.NpcFleetInflationListener;
import exiledsector.effects.NpcFleetSweepScript;
import exiledsector.effects.SkillTreeInstaller;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.SkillDataResolver;
import exiledsector.skills.npc.NpcLayout;
import exiledsector.skills.npc.NpcLayouts;
import exiledsector.ui.SkillTreeRefitButton;
import exiledsector.ui.inspect.NpcTreeInspectInput;
import exiledsector.ui.inspect.SkillTreeCodexListener;
import lunalib.lunaRefit.BaseRefitButton;
import lunalib.lunaRefit.LunaRefitManager;
import lunalib.lunaSettings.LunaSettings;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExiledSectorModPluginTest {

    private MockedStatic<Global> globalMock;
    private MockedStatic<LunaSettings> lunaSettingsMock;
    private MockedStatic<LunaSettings.SettingsCreator> settingsCreatorMock;
    private SectorAPI sector;
    private ListenerManagerAPI listenerManager;
    private SettingsAPI settings;

    @BeforeEach
    void setUp() throws Exception {
        Map<String, Object> persistentData = new HashMap<>();
        sector = mock(SectorAPI.class);
        when(sector.getPersistentData()).thenReturn(persistentData);
        listenerManager = mock(ListenerManagerAPI.class);
        when(sector.getListenerManager()).thenReturn(listenerManager);

        settings = mock(SettingsAPI.class);
        when(settings.loadJSON("data/skilltrees/skill_types.json")).thenReturn(new JSONObject("{ \"skillTypes\": [] }"));
        when(settings.loadJSON("data/skilltrees/ship_skill_tree.json")).thenReturn(new JSONObject("{ \"nodes\": [] }"));
        when(settings.getMergedSpreadsheetDataForMod("plugin", "data/config/exiledSector/split_beam_effect_blocklist.csv", "exiledSector"))
                .thenReturn(new JSONArray());
        when(settings.getMergedSpreadsheetDataForMod("weapon", "data/config/exiledSector/energy_chain_blocklist.csv", "exiledSector"))
                .thenReturn(new JSONArray());
        when(settings.getMergedJSON("data/config/exiledSector/npc_layouts.json")).thenReturn(new JSONObject(
                "{ \"layouts\": { \"bulwark\": { \"root\": \"root_low_tech_1\", \"nodes\": [\"a\"] } } }"));

        Logger logger = mock(Logger.class);

        lunaSettingsMock = Mockito.mockStatic(LunaSettings.class, invocation -> null);
        globalMock = Mockito.mockStatic(Global.class);
        globalMock.when(Global::getSector).thenReturn(sector);
        globalMock.when(Global::getSettings).thenReturn(settings);
        globalMock.when(() -> Global.getLogger(any())).thenReturn(logger);
        settingsCreatorMock = Mockito.mockStatic(LunaSettings.SettingsCreator.class);
    }

    @AfterEach
    void tearDown() {
        NpcLayouts.register(Map.of());
        settingsCreatorMock.close();
        globalMock.close();
        lunaSettingsMock.close();
        BaseRefitButton registered = LunaRefitManager.getFirstButtonOfClass(SkillTreeRefitButton.class);
        if (registered != null) {
            LunaRefitManager.INSTANCE.removeButton(registered);
        }
    }

    @Test
    void onApplicationLoadRegistersTheSkillTreeRefitButton() throws Exception {
        new ExiledSectorModPlugin().onApplicationLoad();

        assertTrue(LunaRefitManager.hasButtonOfClass(SkillTreeRefitButton.class));
    }

    @Test
    void onApplicationLoadRefreshesLunaSettingsSoNewSettingsAreWrittenWithTheirDefaults() throws Exception {
        new ExiledSectorModPlugin().onApplicationLoad();

        settingsCreatorMock.verify(() -> LunaSettings.SettingsCreator.refresh("exiledSector"), Mockito.times(2));
    }

    @Test
    void onApplicationLoadLoadsBothCombatBlocklistsMergedAcrossMods() throws Exception {
        new ExiledSectorModPlugin().onApplicationLoad();

        verify(Global.getSettings()).getMergedSpreadsheetDataForMod("plugin", "data/config/exiledSector/split_beam_effect_blocklist.csv", "exiledSector");
        verify(Global.getSettings()).getMergedSpreadsheetDataForMod("weapon", "data/config/exiledSector/energy_chain_blocklist.csv", "exiledSector");
    }

    @Test
    void onGameLoadRegistersTheInstallerScriptAsTransientSoItIsNeverSaved() {
        new ExiledSectorModPlugin().onGameLoad(true);

        verify(sector).addTransientScript(any(SkillTreeInstaller.class));
        verify(sector, never()).addScript(any(SkillTreeInstaller.class));
    }

    @Test
    void onGameLoadRemovesInstallerCopiesPersistedByOlderSavesBeforeAddingTheTransientOne() {
        new ExiledSectorModPlugin().onGameLoad(false);

        InOrder order = inOrder(sector);
        order.verify(sector).removeScriptsOfClass(SkillTreeInstaller.class);
        order.verify(sector).addTransientScript(any(SkillTreeInstaller.class));
    }

    @Test
    void onGameLoadRegistersTheCombatXpListenerAsTransient() {
        new ExiledSectorModPlugin().onGameLoad(true);

        verify(sector).addTransientListener(any(CombatXpListener.class));
    }

    @Test
    void onGameLoadClearsTheNpcTreeCache() {
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        when(variant.getTags()).thenReturn(List.of("exiledSector_npcTree|bulwark|0|missing_root"));
        ShipSkillData before = SkillDataResolver.resolve(null, variant);

        new ExiledSectorModPlugin().onGameLoad(false);

        assertNotSame(before, SkillDataResolver.resolve(null, variant));
    }

    @Test
    void onApplicationLoadLoadsTheNpcLayoutsMergedAcrossMods() throws Exception {
        new ExiledSectorModPlugin().onApplicationLoad();

        assertEquals(List.of("bulwark"), NpcLayouts.all().stream().map(NpcLayout::id).toList());
    }

    @Test
    void onGameLoadRegistersTheNpcFleetHooksAndInspectionListenersAsTransient() {
        new ExiledSectorModPlugin().onGameLoad(false);

        verify(sector).addTransientScript(any(NpcFleetSweepScript.class));
        verify(sector, never()).addScript(any(NpcFleetSweepScript.class));
        verify(sector).addTransientListener(any(NpcFleetDialogListener.class));
        verify(listenerManager).addListener(any(NpcFleetInflationListener.class), eq(true));
        verify(listenerManager).addListener(any(NpcTreeInspectInput.class), eq(true));
        verify(listenerManager).addListener(any(SkillTreeCodexListener.class), eq(true));
    }
}
