/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

import ed.robust.dom.data.TimeSeries;
import ed.robust.dom.data.Timepoint;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author tzielins
 */
public class PeriodicDataSourceTest {
    
    public PeriodicDataSourceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    protected PeriodicDataSource makeInstance(DataSource source,double period, double last)  {
        return new PeriodicDataSource(source, period, last);
    }

    @Test
    public void testGetFirst() {
        System.out.println("getFirst");
        
        double phase = 4;
        double period = 25;
        double amp = 2;
        
        double step = 1;
        int N = (int)(1*period/step);
        
        TimeSeries data = TSGenerator.makeDblPulse(N, step, period, phase, amp);
        
        DataSource source = new SplineTSInterpolator(data);
        
        PeriodicDataSource instance = makeInstance(source, period, 100);
        
        Timepoint expResult = source.getFirst();
        Timepoint result = instance.getFirst();
        
        assertEquals(expResult, result);
    }

    @Test
    public void testGetLast() {
        System.out.println("getLastTime");
        
        double phase = 4;
        double period = 25;
        double amp = 2;
        
        double step = 1;
        int N = (int)(1*period/step);
        
        TimeSeries data = TSGenerator.makeDblPulse(N, step, period, phase, amp);
        
        DataSource source = new SplineTSInterpolator(data);
        
        PeriodicDataSource instance = makeInstance(source, period, 100);
        
        double expResult = 100;
        double result = instance.getLastTime();
        
        assertEquals(expResult, result,0.0001);
    }
    

    @Test
    public void testGetValue() {
        System.out.println("getValue");
        
        
        double phase = 4;
        double period = 24.6;
        double amp = 2;
        
        double step = 1;
        int N = (int)(2*period/step);
        
        TimeSeries data = TSGenerator.makeDblPulse(N, step, period, phase, amp);
        
        DataSource source = new SplineTSInterpolator(data);
        
        PeriodicDataSource instance = makeInstance(source, period, 100);
        
        
        double EPS = 0.00001;
        for (double time = 0;time < period; time+=1.23) {
            double expResult = source.getValue(time);
            double result = instance.getValue(time);
        
            assertEquals(expResult, result,EPS);
            result = instance.getValue(time+2*period);
            assertEquals(expResult, result,EPS);
            result = instance.getValue(time-period);
            assertEquals(expResult, result,EPS);
            
        }
        
    }



    @Test
    public void testGetTimepoints() throws IOException {
        System.out.println("getTimepoints");
        
        double phase = 4;
        double period = 24.6;
        double amp = 2;
        
        double step = 1;
        int N = (int)(1.5*period/step);
        
        TimeSeries data = TSGenerator.makeDblPulse(N, step, period, phase, amp);
        
        DataSource sourceShort = new SplineTSInterpolator(data);
        
        N = (int)(5*period/step);
        data = TSGenerator.makeDblPulse(N, step, period, phase, amp);
        
        DataSource sourceLong = new SplineTSInterpolator(data);
        
        PeriodicDataSource instance = makeInstance(sourceShort, period, sourceLong.getLastTime());
        
        TimeSeries expResult = new TimeSeries(sourceLong.getTimepoints(1, ROUNDING_TYPE.DECY));
        TimeSeries result = new TimeSeries(instance.getTimepoints(1, ROUNDING_TYPE.DECY));

        List<TimeSeries> list = Arrays.asList(expResult,result);
        TimeSeriesFileHandler.saveToText(list, Configuration.tempFile("perds.csv"), ",");
        
        double EPS = 0.1;
        assertTrue(expResult.almostEquals(result, EPS));
        
    }

}
