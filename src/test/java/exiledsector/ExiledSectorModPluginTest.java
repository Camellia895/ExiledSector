package exiledsector;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import exiledsector.effects.SkillTreeInstaller;
import exiledsector.ui.SkillTreeRefitButton;
import lunalib.lunaRefit.BaseRefitButton;
import lunalib.lunaRefit.LunaRefitManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExiledSectorModPluginTest {

    private MockedStatic<Global> globalMock;
    private SectorAPI sector;

    @BeforeEach
    void setUp() throws Exception {
        Map<String, Object> persistentData = new HashMap<>();
        sector = mock(SectorAPI.class);
        when(sector.getPersistentData()).thenReturn(persistentData);

        SettingsAPI settings = mock(SettingsAPI.class);
        when(settings.loadJSON("data/skilltrees/skill_types.json")).thenReturn(new JSONObject("{ \"skillTypes\": [] }"));
        when(settings.loadJSON("data/skilltrees/ship_skill_tree.json")).thenReturn(new JSONObject("{ \"nodes\": [] }"));
        when(settings.getMergedSpreadsheetDataForMod("plugin", "data/config/exiledSector/split_beam_effect_blocklist.csv", "exiledSector"))
                .thenReturn(new JSONArray());
        when(settings.getMergedSpreadsheetDataForMod("weapon", "data/config/exiledSector/energy_chain_blocklist.csv", "exiledSector"))
                .thenReturn(new JSONArray());

        Logger logger = mock(Logger.class);

        globalMock = Mockito.mockStatic(Global.class);
        globalMock.when(Global::getSector).thenReturn(sector);
        globalMock.when(Global::getSettings).thenReturn(settings);
        globalMock.when(() -> Global.getLogger(any())).thenReturn(logger);
    }

    @AfterEach
    void tearDown() {
        globalMock.close();
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
    void onApplicationLoadLoadsBothCombatBlocklistsMergedAcrossMods() throws Exception {
        new ExiledSectorModPlugin().onApplicationLoad();

        verify(Global.getSettings()).getMergedSpreadsheetDataForMod("plugin", "data/config/exiledSector/split_beam_effect_blocklist.csv", "exiledSector");
        verify(Global.getSettings()).getMergedSpreadsheetDataForMod("weapon", "data/config/exiledSector/energy_chain_blocklist.csv", "exiledSector");
    }

    @Test
    void onGameLoadRegistersTheInstallerScript() {
        new ExiledSectorModPlugin().onGameLoad(true);

        verify(sector).addScript(any(SkillTreeInstaller.class));
    }
}
