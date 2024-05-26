/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ed.robust.util.timeseries;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author tzielins
 */
public class Configuration {
    
    public static Path tempDir = Paths.get("C:/Temp");
    
    public static File tempFile(String name) {
        
        return tempDir.resolve(name).toFile();
    }
}
