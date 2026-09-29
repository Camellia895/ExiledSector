package exiledsector.i18n;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModManagerAPI;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import exiledsector.skills.npc.RealSkillData;
import lunalib.lunaSettings.LunaSettings;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PseudoLocaleLeakTest {

    private static final String EXTERNAL_NAME = "0";

    private static MockedStatic<LunaSettings> lunaSettingsMock;
    private static MockedStatic<Global> globalMock;

    @BeforeAll
    static void setUp() throws Exception {
        lunaSettingsMock = Mockito.mockStatic(LunaSettings.class, invocation -> null);
        SettingsAPI settings = mock(SettingsAPI.class);
        HullModSpecAPI hullMod = mock(HullModSpecAPI.class);
        when(hullMod.getDisplayName()).thenReturn(EXTERNAL_NAME);
        when(settings.getHullModSpec(Mockito.anyString())).thenReturn(hullMod);
        CommoditySpecAPI commodity = mock(CommoditySpecAPI.class);
        when(commodity.getName()).thenReturn(EXTERNAL_NAME);
        when(settings.getCommoditySpec(Mockito.anyString())).thenReturn(commodity);
        ModManagerAPI modManager = mock(ModManagerAPI.class);
        when(modManager.isModEnabled(Mockito.anyString())).thenReturn(true);
        when(settings.getModManager()).thenReturn(modManager);
        globalMock = Mockito.mockStatic(Global.class);
        globalMock.when(Global::getSettings).thenReturn(settings);
        RealSkillData.load();
    }

    @AfterAll
    static void tearDown() {
        RealSkillData.clear();
        globalMock.close();
        lunaSettingsMock.close();
    }

    @Test
    void everyDescriptionComesFromTheCatalogueOrData() {
        I18n.install(RealCatalogue.of(PseudoLocale.LOCALE));
        List<String> leaks = new ArrayList<>();
        for (String line : DescriptionGoldenTest.generate()) {
            String[] columns = line.split("\t", 3);
            String markup = columns[2].replace("\\n", "\n").replace("\\t", "\t").replace("\\\\", "\\");
            if (PseudoLeaks.hasLeak(markup)) {
                leaks.add(columns[0] + ": " + PseudoLeaks.unbracketed(markup));
            }
        }
        assertEquals(List.of(), leaks);
    }

    @Test
    void theLeakCheckSpotsTextOutsideThePseudoBrackets() {
        assertFalse(PseudoLeaks.hasLeak("[Increases [armor~] by <good>10%</good>.~]"));
        assertFalse(PseudoLeaks.hasLeak("12, 0 - 3%"));
        assertEquals(" leaked", PseudoLeaks.unbracketed("[ok~] leaked"));
    }
}
