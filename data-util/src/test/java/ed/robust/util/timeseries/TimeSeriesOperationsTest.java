/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

import ed.robust.dom.data.TimeSeries;
import ed.robust.dom.data.Timepoint;
import ed.robust.dom.util.Pair;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author tzielins
 */
public class TimeSeriesOperationsTest {
    
    static final double EPS = 1E-6;
    final Random rand = new Random();
    
    public TimeSeriesOperationsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }


    @Test
    public void testFindHighestLowestPeaks() {
        System.out.println("findHighestLowestPeaks");
        TimeSeries data = new TimeSeries();
        data.add(5,1);
        data.add(6,2);
        data.add(7,1);
        data.add(8,0);
        data.add(9,0);
        data.add(10,1);
        data.add(11,2);
        data.add(13,3);
        data.add(14,0);
        data.add(15,-1);
        data.add(16,1);
        
        List<Timepoint> points = data.getTimepoints();
        
        Pair<Timepoint,Timepoint> highestAndLowestPeaks = TimeSeriesOperations.findHighestLowestPeaks(points);
        
        Timepoint highest = highestAndLowestPeaks.getLeft();
        Timepoint lowest = highestAndLowestPeaks.getRight();
        
        Timepoint expHighest = new Timepoint(13,3);
        Timepoint expLowest = new Timepoint(15,-1);
        
        
        assertEquals(expHighest, highest);
        assertEquals(expLowest, lowest);
        
        data = new TimeSeries();
        data.add(5,6);
        data.add(6,5);
        data.add(7,4);
        data.add(8,3);
        data.add(9,2);
        data.add(10,1);
        data.add(11,0);
        data.add(13,-1);
        data.add(14,-1);
        data.add(15,-1);
        data.add(16,-2);        
        
        points = data.getTimepoints();
        
        highestAndLowestPeaks = TimeSeriesOperations.findHighestLowestPeaks(points);
        
        highest = highestAndLowestPeaks.getLeft();
        lowest = highestAndLowestPeaks.getRight();
        
        expHighest = null;
        expLowest = null;
        
        
        assertEquals(expHighest, highest);
        assertEquals(expLowest, lowest);        
    }

    @Test
    public void testFindPeaksAndValleys() {
        System.out.println("findPeaksAndValleys");
        
        TimeSeries data = new TimeSeries();
        data.add(5,1);
        data.add(6,2);
        data.add(7,1);
        data.add(8,1);
        data.add(9,0);
        data.add(10,1);
        data.add(11,2.5);
        data.add(13,3);
        data.add(14,0);
        data.add(15,-1);
        data.add(16,-2);
        
        List<Timepoint> points = data.getTimepoints();
        
        Pair<List<Timepoint>,List<Timepoint>> peaksAndValleys = TimeSeriesOperations.findPeaksAndValleys(points);
        
        List<Timepoint> peaks = peaksAndValleys.getLeft();        
        List<Timepoint> valleys = peaksAndValleys.getRight();

        List<Timepoint> expPeaks = Arrays.asList(new Timepoint(6,2),new Timepoint(13,3));
        List<Timepoint> expValleys = Arrays.asList(new Timepoint(9,0));
        
        assertEquals(expPeaks, peaks);
        assertEquals(expValleys, valleys);
    }
    
    @Test
    public void testAddFuntion() {
        System.out.println("Testing adding function");
        
        TimeSeries data = new TimeSeries();
        TimeSeries exp = new TimeSeries();
        UnivariateFunction fun = new PolynomialFunction(new double[]{1});
        
        TimeSeries res = TimeSeriesOperations.addFunction(data, fun);
        assertEquals(exp,res);
        
        data.add(0,1);
        data.add(1,2);
        
        exp.add(0,2);
        exp.add(1,3);
        
        res = TimeSeriesOperations.addFunction(data, fun);
        assertEquals(exp,res); 
        
        fun = new PolynomialFunction(new double[]{2,1});
        exp = new TimeSeries();
        exp.add(0,3);
        exp.add(1,5);
        res = TimeSeriesOperations.addFunction(data, fun);
        assertEquals(exp,res); 
        
        
    }
    
    
    @Test
    public void testSubFuntion() {
        System.out.println("Testing sub function");
        
        TimeSeries data = new TimeSeries();
        TimeSeries exp = new TimeSeries();
        UnivariateFunction fun = new PolynomialFunction(new double[]{1});
        
        TimeSeries res = TimeSeriesOperations.substractFunction(data, fun);
        assertEquals(exp,res);
        
        data.add(0,1);
        data.add(1,2);
        
        exp.add(0,0);
        exp.add(1,1);
        
        res = TimeSeriesOperations.substractFunction(data, fun);
        assertEquals(exp,res); 
        
        fun = new PolynomialFunction(new double[]{2,1});
        exp = new TimeSeries();
        exp.add(0,-1);
        exp.add(1,-1);
        res = TimeSeriesOperations.substractFunction(data, fun);
        assertEquals(exp,res); 
        
        
    }
    
    @Test
    public void testGetPolyTrend() {
        
        TimeSeries data = new TimeSeries();
        double a = 1;
        double b = 1.5;
        
        for (int i =0;i<10;i++) {
            data.add(i,a*i+b);
        }
        
        TimeSeries trend = TimeSeriesOperations.getPolyTrend(data, 1);
        assertEquals(data,trend);
        
        data = new TimeSeries();
        a = 1;
        b = 2;
        double c = 3;
        
        for (int i = 0;i<10;i++) {
            data.add(i,a*i*i+b*i+c);
        }
        
        trend = TimeSeriesOperations.getPolyTrend(data, 2);
        assertTrue(data.almostEquals(trend,EPS));        
        
        trend = TimeSeriesOperations.getPolyTrend(data, 3);
        assertTrue(data.almostEquals(trend,EPS));        
        
        data = new TimeSeries();
        a = -1;
        b = 1;
        c = 2;
        double d = -1;
        
        for (int i = 0;i<10;i++) {
            data.add(i,a*i*i*i+b*i*i+c*i+d);
        }

        trend = TimeSeriesOperations.getPolyTrend(data, 3);
        assertTrue(data.almostEquals(trend,EPS));  
        
        trend = TimeSeriesOperations.getPolyTrend(data, 2);
        assertFalse(data.almostEquals(trend,EPS));  
    }
    
    @Test
    public void testSubtract() {
        
        TimeSeries s1 = new TimeSeries();
        s1.add(new Timepoint(1.0,1.0,2.0,3.0));
        s1.add(2.0,2.0);
        
        TimeSeries s2 = new TimeSeries();
        s2.add(1.0,1.0);
        s2.add(2.0,1.0);
        
        TimeSeries exp = new TimeSeries();
        exp.add(new Timepoint(1.0,0.0,2.0,3.0));
        exp.add(2.0,1.0);
        
        TimeSeries res = TimeSeriesOperations.substract(s1, s2);
        assertEquals(exp,res);
    }

    @Test
    public void testCastTimes() {
        System.out.println("Testing cast");
        
        TimeSeries source = new TimeSeries();
        TimeSeries dest = new TimeSeries();
        TimeSeries exp = new TimeSeries();
        
        TimeSeries res = TimeSeriesOperations.castTime(source, dest);
        assertEquals(exp,res);
        
        dest.add(1,1);
        
        try {
            res = TimeSeriesOperations.castTime(source, dest);
            fail("Expected illegal argument exception not: "+res);
        } catch (IllegalArgumentException e) {
            
        }
        
        
        source = new TimeSeries();
        source.add(0,2);        
        dest = new TimeSeries();
        dest.add(1,1);        
        exp = new TimeSeries();
        exp.add(1,2);
        
        res = TimeSeriesOperations.castTime(source, dest);
        assertEquals(exp,res);        

        source = new TimeSeries();
        source.add(0,2);        
        source.add(1,3);        
        dest = new TimeSeries();
        dest.add(1,1);        
        exp = new TimeSeries();
        exp.add(1,3);
        
        res = TimeSeriesOperations.castTime(source, dest);
        assertEquals(exp,res);        

        source = new TimeSeries();
        source.add(0,2);        
        source.add(2,4);        
        dest = new TimeSeries();
        dest.add(1,1);        
        exp = new TimeSeries();
        exp.add(1,3);
        
        res = TimeSeriesOperations.castTime(source, dest);
        assertTrue("Got: "+res+" instad of: "+exp,exp.almostEquals(res,1E-2));        

        source = new TimeSeries();
        source.add(1,2);        
        source.add(3,4);        
        dest = new TimeSeries();
        dest.add(0,0);        
        dest.add(2,2);        
        dest.add(4,4);        
        exp = new TimeSeries();
        exp.add(0,1);
        exp.add(2,3);
        exp.add(4,5);
        
        res = TimeSeriesOperations.castTime(source, dest);
        assertTrue("Got: "+res+" instad of: "+exp,exp.almostEquals(res,1E-1));        
        
        source = new TimeSeries();
        dest = new TimeSeries();
        
        for (int i = 1;i<100;i++) {
            source.add(rand.nextDouble()*100,rand.nextDouble()*100);
            dest.add(rand.nextDouble()*100,rand.nextDouble()*100);
        }
        
        res = TimeSeriesOperations.castTime(source, dest);
        assertArrayEquals(dest.getTimes(), res.getTimes(),EPS);
        
    }
}
