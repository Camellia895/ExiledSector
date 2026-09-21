package exiledsector.ui.util;

import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public final class SpriteCache {

    private final Logger logger;
    private final Set<String> loadedSprites = new HashSet<>();
    private final Set<String> failedSprites = new HashSet<>();

    public SpriteCache(Class<?> owner) {
        this.logger = Logger.getLogger(owner);
    }

    public boolean ensureLoaded(String path) {
        if (loadedSprites.contains(path)) return true;
        if (failedSprites.contains(path)) return false;
        try {
            Global.getSettings().loadTexture(path);
            loadedSprites.add(path);
            return true;
        } catch (IOException e) {
            logger.error("Failed to load texture " + path, e);
            failedSprites.add(path);
            return false;
        }
    }
}
