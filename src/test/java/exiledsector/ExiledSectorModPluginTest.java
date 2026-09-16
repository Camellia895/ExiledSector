package exiledsector;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorAPI;
import exiledsector.combat.CombatXpListener;
import exiledsector.effects.SkillTreeInstaller;
import exiledsector.ui.SkillTreeRefitButton;
import lunalib.lunaRefit.BaseRefitButton;
import lunalib.lunaRefit.LunaRefitManager;
import org.apache.log4j.Logger;
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

/**
 * Global is statically mocked throughout since the plugin calls
 * Global.getLogger()/Global.getSector() directly and there's no running
 * game session to back those in a test.
 */
class ExiledSectorModPluginTest {

    private MockedStatic<Global> globalMock;
    private SectorAPI sector;

    @BeforeEach
    void setUp() {
        Map<String, Object> persistentData = new HashMap<>();
        sector = mock(SectorAPI.class);
        when(sector.getPersistentData()).thenReturn(persistentData);

        globalMock = Mockito.mockStatic(Global.class);
        globalMock.when(Global::getSector).thenReturn(sector);
        globalMock.when(() -> Global.getLogger(any())).thenReturn(mock(Logger.class));
    }

    @AfterEach
    void tearDown() {
        globalMock.close();
        // onApplicationLoad() registers into LunaRefitManager's static
        // registry - clean it up so it doesn't leak into other tests.
        BaseRefitButton registered = LunaRefitManager.getFirstButtonOfClass(SkillTreeRefitButton.class);
        if (registered != null) {
            // removeButton() isn't @JvmStatic, so it's reached through the
            // Kotlin object's singleton INSTANCE field from Java.
            LunaRefitManager.INSTANCE.removeButton(registered);
        }
    }

    @Test
    void onApplicationLoadRegistersTheSkillTreeRefitButton() throws Exception {
        new ExiledSectorModPlugin().onApplicationLoad();

        assertTrue(LunaRefitManager.hasButtonOfClass(SkillTreeRefitButton.class));
    }

    @Test
    void onGameLoadRegistersTheCombatListenerAndInstallerScript() {
        new ExiledSectorModPlugin().onGameLoad(true);

        verify(sector).addListener(any(CombatXpListener.class));
        verify(sector).addScript(any(SkillTreeInstaller.class));
    }
}
