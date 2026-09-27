package exiledsector;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import exiledsector.effects.ShipLevelScript;
import exiledsector.effects.SkillTreeInstaller;
import exiledsector.skills.SkillTree;
import exiledsector.skills.skilleffect.CsvIdBlocklist;
import exiledsector.ui.ExiledSectorSettings;
import exiledsector.ui.SkillTreeRefitButton;

public class ExiledSectorModPlugin extends BaseModPlugin {

    public static final String LOG_TAG = "ExiledSector";

    @Override
    public void onApplicationLoad() throws Exception {
        Global.getLogger(ExiledSectorModPlugin.class).info(LOG_TAG + " loaded");
        SkillTreeRefitButton.addButton();
        ExiledSectorSettings.register();
        SkillTree.load();
        CsvIdBlocklist.loadAll();
    }

    @Override
    public void onGameLoad(boolean newGame) {
        Global.getSector().addScript(new SkillTreeInstaller());
        Global.getSector().addScript(new ShipLevelScript());
    }
}
