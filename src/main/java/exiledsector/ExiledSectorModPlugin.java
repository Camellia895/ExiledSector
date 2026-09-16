package exiledsector;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import exiledsector.combat.CombatXpListener;
import exiledsector.effects.SkillTreeInstaller;
import exiledsector.skills.SkillTree;
import exiledsector.ui.SkillTreeRefitButton;

public class ExiledSectorModPlugin extends BaseModPlugin {

    public static final String LOG_TAG = "ExiledSector";

    @Override
    public void onApplicationLoad() throws Exception {
        Global.getLogger(ExiledSectorModPlugin.class).info(LOG_TAG + " loaded");
        SkillTreeRefitButton.addButton();
        SkillTree.load();
    }

    @Override
    public void onGameLoad(boolean newGame) {
        new CombatXpListener();
        Global.getSector().addScript(new SkillTreeInstaller());
    }
}
