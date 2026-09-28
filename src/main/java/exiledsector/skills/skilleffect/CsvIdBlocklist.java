package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static exiledsector.ExiledSectorModPlugin.MOD_ID;

public final class CsvIdBlocklist {

    private static final List<CsvIdBlocklist> ALL = new ArrayList<>();

    public static final CsvIdBlocklist SPLIT_BEAM_EFFECTS =
            register("data/config/exiledSector/split_beam_effect_blocklist.csv", "plugin");
    public static final CsvIdBlocklist ENERGY_CHAIN_WEAPONS =
            register("data/config/exiledSector/energy_chain_blocklist.csv", "weapon");

    private final String path;
    private final String idColumn;
    private final AtomicReference<Set<String>> ids = new AtomicReference<>(Set.of());

    private CsvIdBlocklist(String path, String idColumn) {
        this.path = path;
        this.idColumn = idColumn;
    }

    private static CsvIdBlocklist register(String path, String idColumn) {
        CsvIdBlocklist blocklist = new CsvIdBlocklist(path, idColumn);
        ALL.add(blocklist);
        return blocklist;
    }

    public static void loadAll() {
        for (CsvIdBlocklist blocklist : ALL) {
            blocklist.load();
        }
    }

    private void load() {
        try {
            JSONArray rows = Global.getSettings().getMergedSpreadsheetDataForMod(idColumn, path, MOD_ID);
            Set<String> loaded = new HashSet<>();
            for (int i = 0; i < rows.length(); i++) {
                String id = rows.getJSONObject(i).optString(idColumn, "").trim();
                if (!id.isEmpty()) {
                    loaded.add(id);
                }
            }
            ids.set(Set.copyOf(loaded));
        } catch (IOException | JSONException e) {
            Logger.getLogger(CsvIdBlocklist.class).error("Failed to load " + path, e);
        }
    }

    public boolean contains(String id) {
        return id != null && ids.get().contains(id);
    }
}
