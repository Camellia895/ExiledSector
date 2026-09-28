package exiledsector;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import exiledsector.effects.CombatXpListener;
import exiledsector.effects.EnemyFleetDialogListener;
import exiledsector.effects.EnemyFleetInflationListener;
import exiledsector.effects.EnemyFleetSweepScript;
import exiledsector.effects.SkillTreeInstaller;
import exiledsector.skills.SkillDataResolver;
import exiledsector.skills.SkillTree;
import exiledsector.skills.CsvIdBlocklist;
import exiledsector.skills.enemy.EnemyLayouts;
import exiledsector.ui.ExiledSectorSettings;
import exiledsector.ui.SkillTreeRefitButton;
import exiledsector.ui.inspect.EnemyTreeInspectInput;
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
        EnemyLayouts.load();
    }

    @Override
    public void onGameLoad(boolean newGame) {
        SkillDataResolver.clearCache();
        Global.getSector().removeScriptsOfClass(SkillTreeInstaller.class);
        Global.getSector().addTransientScript(new SkillTreeInstaller());
        Global.getSector().addTransientListener(new CombatXpListener());
        Global.getSector().addTransientScript(new EnemyFleetSweepScript());
        Global.getSector().addTransientListener(new EnemyFleetDialogListener());
        Global.getSector().getListenerManager().addListener(new EnemyFleetInflationListener(), true);
        Global.getSector().getListenerManager().addListener(new EnemyTreeInspectInput(), true);
        Global.getSector().getListenerManager().addListener(new SkillTreeCodexListener(), true);
    }
}
