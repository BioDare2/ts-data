/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

import ed.robust.dom.data.TimeSeries;
import ed.robust.dom.data.Timepoint;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.BeforeClass;
import static org.junit.Assert.*;

/**
 *
 * @author tzielins
 */
public class TimeSeriesOverlapperTest {
    
    public TimeSeriesOverlapperTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    protected TimeSeriesOverlapper makeInstance() {
        return new TimeSeriesOverlapper();
    }
    
    
    @Test
    public void testOverlapData() throws Exception {
        System.out.println("overlapData");
        
        double period = 24.6;
        double phase = 5;
        double days = 5;
        
        TimeSeries data = makeData(period,phase,days);

        TimeSeriesOverlapper instance = makeInstance();
        
        TimeSeries wrapped = instance.overlapData(period,data);
        
        List<TimeSeries> list = Arrays.asList(data,wrapped);
        
        TimeSeriesFileHandler.saveToText(list, Configuration.tempFile("p_ovgwave.csv"), ",");
        
    }
    
    @Test
    public void testOverlapAndPropagateData() throws Exception {
        System.out.println("overlapAndPropagateData");
        
        double period = 24.6;
        double phase = 5;
        double days = 5;
        double step = 0.1;
        
        TimeSeries data = makeData(period,phase,days);

        TimeSeriesOverlapper instance = makeInstance();
        
        DataSource fitSource = instance.overlapAndPropagateData(period, data);
        TimeSeries fit = new TimeSeries(fitSource.getTimepoints(step, ROUNDING_TYPE.DECY));
        
        List<TimeSeries> list = Arrays.asList(data,fit);
        
        TimeSeriesFileHandler.saveToText(list, Configuration.tempFile("p_avgfit.csv"), ",");
        
        List<Timepoint> points = fit.getTimepoints();
        int jump = (int)(period/step);
        for (int i = 0;i<points.size();i++) {
            
            Timepoint c = points.get(i);
            int j = i+jump;
            if (j >= points.size()) break;
            Timepoint n = points.get(j);
            assertEquals(c.getValue(),n.getValue(),0.01);
        }
        
    }
    
    public TimeSeries makeData(double period, double phase, double days) {
        
        double freq = 1;
        int N = (int)(days*24/freq);
        double amplitude = 3;
        
        TimeSeries data = TSGenerator.makeDblPulse(N, freq, period, phase, amplitude);
        //data = TSGenerator.dampen(data, 0.8);
        
        
        return data;
    }
    
}
