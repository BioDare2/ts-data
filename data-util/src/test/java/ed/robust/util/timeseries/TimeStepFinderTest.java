/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

import ed.robust.dom.data.TimeSeries;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tzielins
 */
public class TimeStepFinderTest {
    
    static final double EPS = 1E-06;
    
    public TimeStepFinderTest() {
    }

    
    @Test
    public void testEmpty() {
        System.out.println("Test empty");
        
        TimeSeries data = new TimeSeries();
        
        TimeStepFinder finder = makeInstance();
        
        try {
            double step = finder.findTimeStep(data);
            fail("Expected exception");
        } catch (IllegalArgumentException e) {
            
        }
        
        try {
            data.add(1,2);
            double step = finder.findTimeStep(data);
            fail("Expected exception");
        } catch (IllegalArgumentException e) {
            
        }
    }
    
    @Test
    public void testExact() {
        System.out.println("Test exact");
        TimeStepFinder finder = makeInstance();
        
        double[] times = {0,1};
        double[] values = new double[times.length];
        
        TimeSeries data = new TimeSeries(times, values);
        double exp = 1;
        
        double step = finder.findTimeStep(data);
        assertEquals(exp, step,EPS);
        
        times = new double[]{0,1,2,3,4,5};
        values = new double[times.length];        
        data = new TimeSeries(times, values);
        exp = 1;        
        step = finder.findTimeStep(data);
        assertEquals(exp, step,EPS);
        
        times = new double[]{0,0.1,0.2,0.3,0.4,0.5};
        values = new double[times.length];        
        data = new TimeSeries(times, values);
        exp = 0.1;        
        step = finder.findTimeStep(data);
        assertEquals(exp, step,EPS);
        
        times = new double[]{0,1.01,2.02,3.03,4.04,5.05};
        values = new double[times.length];        
        data = new TimeSeries(times, values);
        exp = 1.01;        
        step = finder.findTimeStep(data);
        assertEquals(exp, step,EPS);
        
        times = new double[]{0,1.011,2.02,3.034,4.046,5.059};
        values = new double[times.length];        
        data = new TimeSeries(times, values);
        exp = 1.01;        
        step = finder.findTimeStep(data);
        assertEquals(exp, step,EPS);
    }  
    
    @Test
    public void testMajor() {
        System.out.println("Test major");
        TimeStepFinder finder = makeInstance();
        
        double[] times = {0,1,2,3,3.5};
        double[] values = new double[times.length];
        
        TimeSeries data = new TimeSeries(times, values);
        double exp = 1;
        
        double step = finder.findTimeStep(data);
        assertEquals(exp, step,EPS);
        
        times = new double[]{0,1,2,3,4,5,7,10,20};
        values = new double[times.length];        
        data = new TimeSeries(times, values);
        exp = 1;        
        step = finder.findTimeStep(data);
        assertEquals(exp, step,EPS);
    }  
    
    @Test
    public void testMean() {
        System.out.println("Test major");
        TimeStepFinder finder = makeInstance();
        
        double[] times = {0,1,2,3.5};
        double[] values = new double[times.length];
        
        TimeSeries data = new TimeSeries(times, values);
        double exp = data.getAverageStep();
        
        double step = finder.findTimeStep(data);
        assertEquals(exp, step,EPS);
        
        times = new double[]{0,1,2,3,4,5,7,10,20,30,40};
        values = new double[times.length];        
        data = new TimeSeries(times, values);
        exp = data.getAverageStep();       
        step = finder.findTimeStep(data);
        assertEquals(exp, step,EPS);
        
    }
    
    protected TimeStepFinder makeInstance() {
        return new TimeStepFinder();
    }
}