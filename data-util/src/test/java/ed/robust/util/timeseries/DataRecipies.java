/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

import ed.robust.dom.data.TimeSeries;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;
/**
 *
 * @author tzielins
 */
public class DataRecipies {
   
    
    @Test
    public void generatePeakData() throws Exception {
        
        double period = 24;
        double step = 1;
        int N = 48;
        double[] noises = {0.5, 1, 2, 4};
        
        Map<String,TimeSeries> waves = generatePeakData(period, step, N);
        waves = addNoise(waves, noises);
        
        Path file = Paths.get("E:/Temp/peak_data_"+step+"_"+N+".csv");
        TimeSeriesFileHandler.saveToText(waves, file.toFile(), ",", ROUNDING_TYPE.CENTY);
        
        step = 2;
        N = 24;
        
        waves = generatePeakData(period, step, N);
        waves.putAll(generatePeakData(period+2, step, N));
        waves.putAll(generatePeakData(period+4, step, N));
        waves.putAll(generatePeakData(period-4, step, N));
        waves = addNoise(waves, noises);
        
        file = Paths.get("E:/Temp/peak_data_"+step+"_"+N+".csv");
        TimeSeriesFileHandler.saveToText(waves, file.toFile(), ",", ROUNDING_TYPE.CENTY);   
        
        step = 4;
        N = 12;
        
        waves = generatePeakData(period, step, N);
        waves.putAll(generatePeakData(period+2, step, N));
        waves.putAll(generatePeakData(period+4, step, N));
        waves.putAll(generatePeakData(period-4, step, N));
        waves = addNoise(waves, noises);
        
        file = Paths.get("E:/Temp/peak_data_"+step+"_"+N+".csv");
        TimeSeriesFileHandler.saveToText(waves, file.toFile(), ",", ROUNDING_TYPE.CENTY);         
        assertTrue(true);
    }
    
    
    
    public Map<String,TimeSeries> addNoise(Map<String,TimeSeries> patterns, double[] noises) {
        
        Map<String,TimeSeries> waves = new LinkedHashMap<>();
        
        for (double noise : noises) {
            
            patterns.forEach( (label, p) -> {
            
                TimeSeries ts = TSGenerator.addNoise(p, noise);
                label+="_"+noise;
                waves.put(label, ts);
            });
        }
        
        return waves;
    }
    
    public Map<String,TimeSeries> generatePeakData(double period, double step, int N) {
        
        double amplitude = 2;
        
        double[] phases = {1.0, 2.0, 3.0};
        
        Map<String,TimeSeries> waves = new LinkedHashMap<>();
        
        for (double phase : phases) {
            
            TimeSeries ts = TSGenerator.makePulse(N, step, period, phase, amplitude);
            String label = "PUL_"+period+":"+phase;
            waves.put(label, ts);            
        }
        
        for (double phase : phases) {
            
            TimeSeries ts = TSGenerator.makeDblPulse(N, step, period, phase, amplitude);
            String label = "DBP_"+period+":"+phase;
            waves.put(label, ts); 
        }
        
        TimeSeries ts = TSGenerator.makeLine(N, step, 0, 1);
        ts.add(step*(N), 0);
        ts = TSGenerator.addNoise(ts, 1);
        ts = new TimeSeries(ts.getTimepoints().subList(0, N));
        
        String label = "LIN";
        waves.put(label, ts); 
        
        return waves;        
    }
}
