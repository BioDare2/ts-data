/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

import ed.robust.dom.data.TimeSeries;
import ed.robust.dom.data.Timepoint;
import ed.robust.error.RobustFormatException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tzielins
 */
public class TSGeneratorTest {
  
    
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testMakeLine() {
        
        double EPS = 1E-6;
        double a = 10;
        double b = 1;
        int N = 1;
        double step = 0.1;
        
        double[] expTimes = {0};
        double[] expValues = {b};
        
        TimeSeries data = TSGenerator.makeLine(N, step, a, b);
        assertArrayEquals(expTimes, data.getTimes(),EPS);
        assertArrayEquals(expValues, data.getValues(),EPS);
        
        N = 3;
        expTimes = new double[]{0,step,2*step};
        expValues = new double[]{b,step*a+b,2*step*a+b};
        
        data = TSGenerator.makeLine(N, step, a, b);
        assertArrayEquals(expTimes, data.getTimes(),EPS);
        assertArrayEquals(expValues, data.getValues(),EPS);        
    }
    
    @Test
    public void testHaveSameTimes() {
        
        TimeSeries t1 = new TimeSeries();
        for (double i = 0; i<10;i+=(10/60.0)) {
            t1.add(i,2*i);
        }
        
        assertTrue(TSGenerator.haveSameTimes(t1, t1));
        
        TimeSeries t2 = new TimeSeries();
        for (double i = 0; i<10;i+=(10/60.0)) {
            t2.add(i,2*(int)i);
        }  
        
        assertTrue(TSGenerator.haveSameTimes(t1, t2));
        
        t2 = new TimeSeries();
        for (double i = 0; i<10+2;i+=(10/60.0)) {
            t2.add(i,2*i);
        }
        
        assertFalse(TSGenerator.haveSameTimes(t1, t2));
        
        TimeSeries t3 = new TimeSeries();
        t3.addAll(t1.getTimepoints());
        
        t3.add(t3.getLast().getTime()+1,1);
        
        assertFalse(TSGenerator.haveSameTimes(t1, t3));
        assertFalse(TSGenerator.haveSameTimes(t2, t3));
       
    }
    
    @Test
    public void testSum() {
        
        TimeSeries t1 = new TimeSeries();
        for (double i = 0; i<10;i+=(10/60.0)) {
            t1.add(i,2*i);
        }
        
        TimeSeries t2 = new TimeSeries();
        for (double i = 0; i<10;i+=(10/60.0)) {
            t2.add(i,2*(int)i);
        }
        
        TimeSeries t3 = TSGenerator.sum(t1, t2);
        
        assertEquals(t1.size(), t3.size());
        assertTrue(TSGenerator.haveSameTimes(t3, t1));
        
        for (int i = 0; i<t1.size();i++) {
            assertEquals(t3.getTimepoints().get(i).getValue(), t1.getTimepoints().get(i).getValue()+t2.getTimepoints().get(i).getValue(),0.00001);
        }
        
        t3.add(100,1);
        try {
            t3 = TSGenerator.sum(t3, t1);
            fail("Exception due to different legnth expected");
        } catch (IllegalArgumentException e) {
            
        }
       
    }
    
    @Test
    public void testConvolute() {
        
        TimeSeries t1 = new TimeSeries();
        for (double i = 0; i<10;i+=(10/60.0)) {
            t1.add(i,2*i);
        }
        
        TimeSeries t2 = new TimeSeries();
        for (double i = 0; i<10;i+=(10/60.0)) {
            t2.add(i,2*(int)i);
        }
        
        TimeSeries t3 = TSGenerator.convolute(t1, t2);
        
        assertEquals(t1.size(), t3.size());
        assertTrue(TSGenerator.haveSameTimes(t3, t1));
        
        for (int i = 0; i<t1.size();i++) {
            assertEquals(t3.getTimepoints().get(i).getValue(), t1.getTimepoints().get(i).getValue()*t2.getTimepoints().get(i).getValue(),0.00001);
        }
        
        t3.add(100,1);
        try {
            t3 = TSGenerator.convolute(t3, t1);
            fail("Exception due to different legnth expected");
        } catch (IllegalArgumentException e) {
            
        }
       
    }
    
    @Test
    public void testHollow() throws IOException {
        
        List<TimeSeries> series = new ArrayList<TimeSeries>();

        TimeSeries t1 = new TimeSeries();
        for (int i=0;i<24;i++) {
            t1.add(i,i);
        }
        series.add(t1);
        
        for (int i = 2;i<5;i++) {
            TimeSeries t2 = TSGenerator.hollow(t1, i);
            series.add(t2);
        }
        
        for (int i = 2;i<5;i++) {
            TimeSeries t2 = TSGenerator.randomHollow(t1, i);
            series.add(t2);
        }
        
        
        File file = new File("E:/Temp/ser.csv");
        
        //TimeSeriesFileHandler.saveToText(series, file, ",");
        
        
       
    }
    
    @Test
    public void testAddNoise() throws IOException {
        
       List<TimeSeries> series = new ArrayList<TimeSeries>();
       File file = new File("E:/Temp/ser.csv");
       
       
       double period = 20;
       double amplitude = 1;
       double phase = 5;
       
       double step = 1;
       int N = (int)(5*period/step);
       
       TimeSeries t = TSGenerator.makeCos(N, step, period, amplitude);
       series.add(t);
       series.add(TSGenerator.addNoise(t, 0.5));
       
       t = TSGenerator.makeCos(N, step, period, amplitude,phase);
       series.add(t);
       series.add(TSGenerator.addNoise(t, 0.5));
       
       //handler.saveToText(series, file, ",");
       
   }
    
    @Test
    public void testFunctions() throws IOException {
        
       List<TimeSeries> series = new ArrayList<TimeSeries>();
       File file = new File("E:/Temp/ser.csv");
       
       
       double period = 25;
       double amplitude = 1;
       double phase = 10;
       
       double step = 1;
       int N = (int)(5*period/step);
       
       
       series.add(TSGenerator.makeCos(N, step, period, phase, amplitude));
       series.add(TSGenerator.makeWave(N, step, period, phase, amplitude));
       
       series.add(TSGenerator.makeStep(N, step, period, phase,amplitude));
       
       series.add(TSGenerator.makeTriangle(N, step, period, phase, amplitude));
       
       series.add(TSGenerator.makePulse(N, step, period, phase,amplitude));
       
       series.add(TSGenerator.makeDblPulse(N, step, period, phase,amplitude));
       
       //TimeSeriesFileHandler.saveToText(series, file, ",");
    }

    //@Test 
    public void testDumpen() throws IOException, RobustFormatException  {
       List<TimeSeries> series = new ArrayList<TimeSeries>();
       File file = new File("E:/Temp/ser-d.csv");
       
       
       double period = 25;
       double amplitude = 1;
       double phase = 10;
       
       double step = 1;
       int N = (int)(5*period/step);
       
       
       TimeSeries data = TSGenerator.makeCos(N, step, period, phase, amplitude);
       TimeSeries dampen = TSGenerator.dampen(data, 0.5);
       
       series.add(data);
       series.add(dampen);
       
       data = TimeSeriesFileHandler.readFromText(new File("E:/Temp/line.csv"), ",").get(0);
       dampen = TSGenerator.dampen(data,0.00001);
       
       series.add(data);
       series.add(dampen);       
       
       TimeSeriesFileHandler.saveToText(series, file, ",");
    }
    
    @Test
    public void testGetPolyTrend() {
        
        TimeSeries data = new TimeSeries();
        double a = 2;
        double b = -1;
        double c = 2;
        double d = 1;
        for (int i = 0;i<50;i++) data.add(i,a*Math.pow(i, 3)+b*Math.pow(i, 2)+c*i+d);
        
        TimeSeries trend = TSGenerator.getPolyTrend(data, 3);
        
        List<Timepoint> expV = data.getTimepoints();
        List<Timepoint> res = trend.getTimepoints();
        
        for (int i = 0; i< expV.size();i++) {
            assertEquals(expV.get(i).getTime(), res.get(i).getTime(),0.0000001);
            assertEquals(expV.get(i).getValue(), res.get(i).getValue(),0.0001);
        }
    }
}
