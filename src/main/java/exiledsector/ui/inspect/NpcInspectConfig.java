package exiledsector.ui.inspect;

import lunalib.lunaSettings.LunaSettings;
import org.lwjgl.input.Keyboard;

import static exiledsector.ExiledSectorModPlugin.MOD_ID;

public final class NpcInspectConfig {

    public static final String KEYBIND_FIELD_ID = "exiledSector_inspectNpcTreesKey";
    public static final int DEFAULT_KEY = Keyboard.KEY_X;

    private NpcInspectConfig() {
    }

    public static int key() {
        Integer value = LunaSettings.getInt(MOD_ID, KEYBIND_FIELD_ID);
        return value != null ? value : DEFAULT_KEY;
    }
}
