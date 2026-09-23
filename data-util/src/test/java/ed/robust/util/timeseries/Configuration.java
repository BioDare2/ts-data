package ed.robust.util.timeseries;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author tzielins
 */
public class Configuration {

    private static Path tempDir;

    public static Path getTempDir() {
	if (tempDir == null) {
	    try {
		tempDir = Files.createTempDirectory("Temp");
		tempDir.toFile().deleteOnExit();
	    } catch (IOException e) {
		throw new RuntimeException("Failed to create temporary directory", e);
	    }
	}
	return tempDir;
    }
    
    public static File tempFile(String name) {
        return getTempDir().resolve(name).toFile();
    }
}
