package exiledsector;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import exiledsector.effects.CombatXpListener;
import exiledsector.effects.NpcFleetDialogListener;
import exiledsector.effects.NpcFleetInflationListener;
import exiledsector.effects.NpcFleetSweepScript;
import exiledsector.effects.SkillTreeInstaller;
import exiledsector.skills.SkillDataResolver;
import exiledsector.skills.SkillTree;
import exiledsector.skills.npc.NpcLayouts;
import exiledsector.skills.skilleffect.CsvIdBlocklist;
import exiledsector.ui.ExiledSectorSettings;
import exiledsector.ui.SkillTreeRefitButton;
import exiledsector.ui.inspect.NpcTreeInspectInput;
import exiledsector.ui.inspect.SkillTreeCodexListener;

public class ExiledSectorModPlugin extends BaseModPlugin {

    public static final String LOG_TAG = "ExiledSector";
    public static final String MOD_ID = "exiledSector";

    @Override
    public void onApplicationLoad() throws Exception {
        Global.getLogger(ExiledSectorModPlugin.class).info(LOG_TAG + " loaded");
        SkillTreeRefitButton.addButton();
        ExiledSectorSettings.register();
        SkillTree.load();
        CsvIdBlocklist.loadAll();
        NpcLayouts.load();
    }

    @Override
    public void onGameLoad(boolean newGame) {
        SkillDataResolver.clearCache();
        Global.getSector().removeScriptsOfClass(SkillTreeInstaller.class);
        Global.getSector().addTransientScript(new SkillTreeInstaller());
        Global.getSector().addTransientListener(new CombatXpListener());
        Global.getSector().addTransientScript(new NpcFleetSweepScript());
        Global.getSector().addTransientListener(new NpcFleetDialogListener());
        Global.getSector().getListenerManager().addListener(new NpcFleetInflationListener(), true);
        Global.getSector().getListenerManager().addListener(new NpcTreeInspectInput(), true);
        Global.getSector().getListenerManager().addListener(new SkillTreeCodexListener(), true);
    }
}
