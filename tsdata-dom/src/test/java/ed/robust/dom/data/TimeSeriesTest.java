/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.dom.data;

import ed.robust.dom.util.Pair;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author tzielins
 */
public class TimeSeriesTest {
    
    
    @Rule
    public TemporaryFolder TEST_DIR = new TemporaryFolder();
    
    static final double EPS = 1E-6;
    Random random = new Random();
    
    public TimeSeriesTest() {
    }

    @Test
    public void testSortedPreservation() {
        
        TimeSeries data = new TimeSeries();
        
        assertTrue(data.isSorted());
        
        data.add(5,1);
        assertTrue(data.isSorted());
        
        data.add(10,0);
        assertTrue(data.isSorted());
        
        data.add(10,-1);
        assertTrue(data.isSorted());
        
        data.add(1,2);
        assertFalse(data.isSorted());
        
        data = new TimeSeries();
        assertTrue(data.isSorted());
        
        data.add(new Timepoint(1,2));
        assertTrue(data.isSorted());
        
        data.add(new Timepoint(1,3));
        assertTrue(data.isSorted());
        
        data.add(new Timepoint(2,3));
        assertTrue(data.isSorted());
        
        data.add(new Timepoint(0,3));
        assertFalse(data.isSorted());
        
    }
    
    /**
     * Test of getTimepoints method, of class TimeSeries.
     */
    @Test
    public void testGetTimepoints() {
        System.out.println("getTimepoints");
        TimeSeries instance = new TimeSeries();
        List<Timepoint> expResult = Collections.EMPTY_LIST;
        List<Timepoint> result = instance.getTimepoints();
        assertEquals(expResult, result);

        instance.add(10, 2);
        expResult = Arrays.asList(new Timepoint(10,2));
        result = instance.getTimepoints();
        assertEquals(expResult, result);        

        instance.add(1, 3);
        instance.add(11, 3);
        instance.add(2, 4);
        expResult = Arrays.asList(new Timepoint(1,3),new Timepoint(2,4),new Timepoint(10,2),new Timepoint(11,3));
        result = instance.getTimepoints();
        assertEquals(expResult, result);        
        
        try {
            expResult.add(new Timepoint(3,5));
            fail("Unmodified excetion expected");
        } catch (Exception e) {
            //System.out.println(e.getClass().getName());
        }
        
        try {
            expResult.clear();
            fail("Unmodified excetion expected");
        } catch (Exception e) {
            //System.out.println(e.getClass().getName());
        }
        
        try {
            expResult.remove(0);
            fail("Unmodified excetion expected");
        } catch (Exception e) {
            //System.out.println(e.getClass().getName());
        }
        
        
    }

    /**
     * Test of getFirst method, of class TimeSeries.
     */
    @Test
    public void testGetFirst() {
        System.out.println("getFirst");
        TimeSeries instance = new TimeSeries();
        Timepoint expResult;
        Timepoint result;
        
        try {
            result = instance.getFirst();
            fail("Illegal Argument exception expected instaed of: "+result);
        } catch (IllegalArgumentException e) {
            
        }
        
        instance.add(10,3);
        expResult = new Timepoint(10,3);
        result = instance.getFirst();
        assertEquals(expResult, result);

        instance.add(1,4);
        expResult = new Timepoint(1,4);
        result = instance.getFirst();
        assertEquals(expResult, result);
        
        instance.add(1.000001,2);
        result = instance.getFirst();
        assertEquals(expResult, result);
        
        
    }

    /**
     * Test of getLast method, of class TimeSeries.
     */
    @Test
    public void testGetLast() {
        System.out.println("getLast");
        TimeSeries instance = new TimeSeries();
        Timepoint expResult;
        Timepoint result;
        
        try {
            result = instance.getLast();
            fail("Illegal Argument exception expected instaed of: "+result);
        } catch (IllegalArgumentException e) {
            
        }
        
        instance.add(10,3);
        expResult = new Timepoint(10,3);
        result = instance.getLast();
        assertEquals(expResult, result);

        instance.add(1,4);
        expResult = new Timepoint(10,3);
        result = instance.getLast();
        assertEquals(expResult, result);
        
        expResult = new Timepoint(11.000001,2);
        instance.add(11.000001,2);
        result = instance.getLast();
        assertEquals(expResult, result);
    }

    /**
     * Test of getMeanValue method, of class TimeSeries.
     */
    @Test
    public void testGetMeanValue() {
        System.out.println("getMeanValue");
        TimeSeries instance = new TimeSeries();
        double expResult;
        double result;
        
        try {
            result = instance.getMeanValue();
            fail("Expected exception not: "+result);
        } catch (IllegalArgumentException e) {
            
        }
        
        instance.add(1,2);
        expResult = 2;
        result = instance.getMeanValue();
        assertEquals(expResult, result, EPS);
        
        instance.add(3,4);
        instance.add(0,0);
        expResult = 2;
        result = instance.getMeanValue();
        assertEquals(expResult, result, EPS);
        
        double sum = 0;
        instance = new TimeSeries();
        
        for (int i = 0;i<100;i++) {
            double v = random.nextDouble()*1000;
            instance.add(i,v);
            sum+=v;
        }
        expResult = sum/instance.size();
        result = instance.getMeanValue();
        assertEquals(expResult, result, EPS);        
    }

    /**
     * Test of getMaxTimePoint method, of class TimeSeries.
     */
    @Test
    public void testGetMaxTimePoint() {
        System.out.println("getMaxTimePoint");
        TimeSeries instance = new TimeSeries();
        Timepoint expResult;
        Timepoint result;
        
        try {
            result = instance.getMaxTimePoint();
            fail("Expected exception not:"+result);
        } catch(IllegalArgumentException e) {
            
        }
        
        expResult = new Timepoint(3,10);
        instance.add(expResult);
        result = instance.getMaxTimePoint();
        assertEquals(expResult, result);
        
        instance.add(1,0);
        instance.add(11,10);
        instance.add(2,1);
        result = instance.getMaxTimePoint();
        assertEquals(expResult, result);
        
        instance = new TimeSeries();
        instance.add(1,0);
        instance.add(3,10);
        instance.add(11,10);
        instance.add(2,1);
        result = instance.getMaxTimePoint();
        assertEquals(expResult, result);
    }

    /**
     * Test of getMaxValue method, of class TimeSeries.
     */
    @Test
    public void testGetMaxValue() {
        System.out.println("getMaxValue");
        TimeSeries instance = new TimeSeries();
        double expResult = 0.0;
        double result;
        
        try {
            result = instance.getMaxValue();
            fail("Expected exception not:"+result);
        } catch(IllegalArgumentException e) {            
        }
        
        instance.add(2,10);
        instance.add(3,15);
        instance.add(4,2);
        
        expResult = 15;
        result = instance.getMaxValue();    
        assertEquals(expResult, result, EPS);
    }

    /**
     * Test of getMinTimePoint method, of class TimeSeries.
     */
    @Test
    public void testGetMinTimePoint() {
        System.out.println("getMinTimePoint");
        TimeSeries instance = new TimeSeries();
        Timepoint expResult;
        Timepoint result;
        
        try {
            result = instance.getMinTimePoint();
            fail("Expected exception not:"+result);
        } catch(IllegalArgumentException e) {
            
        }
        
        expResult = new Timepoint(3,10);
        instance.add(expResult);
        result = instance.getMinTimePoint();
        assertEquals(expResult, result);
        
        instance.add(1,0);
        instance.add(11,10);
        instance.add(2,1);
        expResult = new Timepoint(1,0);
        result = instance.getMinTimePoint();
        assertEquals(expResult, result);
        
        expResult = new Timepoint(11,-1);
        instance = new TimeSeries();
        instance.add(1,0);
        instance.add(3,10);
        instance.add(11,-1);
        instance.add(2,1);
        result = instance.getMinTimePoint();
        assertEquals(expResult, result);
    }

    /**
     * Test of getMinValue method, of class TimeSeries.
     */
    @Test
    public void testGetMinValue() {
        System.out.println("getMinValue");
        TimeSeries instance = new TimeSeries();
        double expResult = 0.0;
        double result;
        
        try {
            result = instance.getMinValue();
            fail("Expected exception not:"+result);
        } catch(IllegalArgumentException e) {            
        }
        
        instance.add(2,10);
        instance.add(3,15);
        instance.add(4,2);
        
        expResult = 2;
        result = instance.getMinValue();    
        assertEquals(expResult, result, EPS);
    }

    
    /**
     * Test of getMinMaxTimePoint method, of class TimeSeries.
     */
    @Test
    public void testGetMinMax() {
        System.out.println("getMinMax");
        TimeSeries instance = new TimeSeries();
        Pair<Timepoint,Timepoint> expResult;
        Pair<Timepoint,Timepoint> result;
        
        try {
            result = instance.getMinMaxTimePoint();
            fail("Expected exception not:"+result);
        } catch(IllegalArgumentException e) {
            
        }
        
        Timepoint tp = new Timepoint(3,10);
        expResult = new Pair(tp,tp);
        
        instance.add(tp);
        result = instance.getMinMaxTimePoint();
        assertEquals(expResult, result);
        
        instance.add(1,0);
        instance.add(11,11);
        instance.add(2,1);
        result = instance.getMinMaxTimePoint();
        expResult = new Pair(new Timepoint(1,0),new Timepoint(11,11));
        assertEquals(expResult, result);
        
   }
    
    
    /**
     * Test of getAmplitude method, of class TimeSeries.
     */
    @Test
    public void testGetAmplitude() {
        System.out.println("getAmplitude");
        TimeSeries instance = new TimeSeries();
        double expResult;
        double result;
        
        try {
            result = instance.getAmplitude();
            fail("Expected exception not:"+result);
        } catch(IllegalArgumentException e) {            
        }
        
        instance.add(2,10);
        expResult = 0;
        result = instance.getAmplitude();    
        assertEquals(expResult, result, EPS);
        
        instance.add(3,15);
        instance.add(4,2);
        
        expResult = (15-2)/2.0;
        result = instance.getAmplitude();    
        assertEquals(expResult, result, EPS);
        
    }
    

    /**
     * Test of subSeries method, of class TimeSeries.
     */
    @Test
    public void testSubSeries() {
        System.out.println("subSeries");
        double min = 1;
        double max = 5;
        TimeSeries instance = new TimeSeries();
        TimeSeries expResult =new TimeSeries();
        TimeSeries result = instance.subSeries(min, max);
        assertEquals(expResult, result);

        instance.add(1,2);        
        expResult.add(1,2);
        
        result = instance.subSeries(min, max);
        assertEquals(expResult, result);
        
        instance.add(0,-1);
        result = instance.subSeries(min, max);
        assertEquals(expResult, result);
        
        instance.add(4,5);
        instance.add(3,-10);
        instance.add(-1,2);
        
        expResult.add(4,5);
        expResult.add(3,-10);
        result = instance.subSeries(min, max);
        assertEquals(expResult, result);
        
        instance.add(40,5);
        instance.add(4.5,-10);
        expResult.add(4.5,-10);
        result = instance.subSeries(min, max);
        assertEquals(expResult, result);
        
        instance.add(6,-1);
        instance.add(5,0);
        expResult.add(5,0);
        result = instance.subSeries(min, max);
        assertEquals(expResult, result);
        
        max = min;
        result = instance.subSeries(min, max);
        assertEquals(1,result.size());
        
        max = 0;
        try {
            result = instance.subSeries(min, max);
            fail("Expected exception not:"+result);
        } catch(IllegalArgumentException e) {            
        } 
        
        min = 0.5;
        max = 5.5;
        result = instance.subSeries(min, max);
        assertEquals(expResult, result); 
        
        result.add(4.5,1);
        result = instance.subSeries(min, max);
        assertEquals(expResult, result); 
        
        assertEquals(expResult.getFirst(), result.getFirst());
        assertEquals(expResult.getLast(), result.getLast());
        assertEquals(expResult.getMaxTimePoint(), result.getMaxTimePoint());
        assertEquals(expResult.getMinTimePoint(), result.getMinTimePoint());
        
        
    }

    /**
     * Test of setTimepoints method, of class TimeSeries.
     */
    @Test
    public void testSetTimepoints() {
        System.out.println("setTimepoints");
        List<Timepoint> timepoints = null;
        TimeSeries instance = new TimeSeries();
        instance.add(1,2);
        instance.add(0,4);
        instance.add(2,1);
        
        try {
            instance.setTimepoints(timepoints);
            fail("Expected exception");
        } catch(IllegalArgumentException e) {            
        } 
        
        timepoints = Collections.EMPTY_LIST;
        instance.setTimepoints(timepoints);
        
        assertTrue(instance.isEmpty());
        try {
            assertNotNull(instance.getMaxTimePoint());
            fail("Expected exception");
        } catch(IllegalArgumentException e) {            
        } 
                
        instance = new TimeSeries();
        instance.add(1,2);
        instance.add(0,4);
        instance.add(2,1);    
        
        TimeSeries templ = new TimeSeries();
        templ.add(10,0);
        templ.add(3,10);
        
        timepoints = new ArrayList(templ.getTimepoints());
        instance.setTimepoints(timepoints);
        
        assertEquals(templ, instance);
        assertEquals(templ.getFirst(), instance.getFirst());
        assertEquals(templ.getLast(), instance.getLast());
        assertEquals(templ.getMaxTimePoint(), instance.getMaxTimePoint());
        assertEquals(templ.getMinTimePoint(), instance.getMinTimePoint());
        assertEquals(templ.getMeanValue(), instance.getMeanValue(),EPS);

        timepoints.add(new Timepoint(12,1));
        assertEquals(templ, instance);
    }
        

    /**
     * Test of add method, of class TimeSeries.
     */
    @Test
    public void testAdd_Timepoint() {
        System.out.println("add");
        Timepoint t = new Timepoint(12,10);
        TimeSeries instance = new TimeSeries();
        instance.add(t);
        
        assertEquals(1,instance.size());
        instance.add(t);
        assertEquals(2,instance.size());
        
        assertEquals(t, instance.getFirst());
        assertEquals(t, instance.getLast());
        
    }


    /**
     * Test of add method, of class TimeSeries.
     */
    @Test
    public void testAdd_double_double() {
        System.out.println("add");
        double time = 0.0;
        double value = 0.0;
        TimeSeries instance = new TimeSeries();
        instance.add(time, value);
        
        assertEquals(1,instance.size());
        
        instance.add(time, value);        
        assertEquals(2,instance.size());
        
        instance.add(-1, 10);        
        assertEquals(3,instance.size());
        
        assertEquals(new Timepoint(-1,10),instance.getFirst());
        assertEquals(10,instance.getMaxValue(),EPS);
    }

    /**
     * Test of addAll method, of class TimeSeries.
     */
    @Test
    public void testAddAll() {
        System.out.println("addAll");
        
        TimeSeries instance = new TimeSeries();
        instance.add(2,3);
        instance.add(1,0);
        
        TimeSeries templ = new TimeSeries();
        templ.add(1,-10);
        templ.add(0,3);
        
        int expSize = instance.size()+templ.size();
        instance.addAll(templ.getTimepoints());
        
        assertEquals(expSize, instance.size());
        assertEquals(0,instance.getFirst().getTime(),EPS);
        assertEquals(templ.getMinValue(),instance.getMinValue(),EPS);
        assertEquals(templ.getMaxValue(),instance.getMaxValue(),EPS);
        assertEquals(2,instance.getLast().getTime(),EPS);
        
    }


    /**
     * Test of size method, of class TimeSeries.
     */
    @Test
    public void testSize() {
        System.out.println("size");
        TimeSeries instance = new TimeSeries();
        int expResult = 0;
        int result = instance.size();
        assertEquals(expResult, result);
        
        int N = 10000;
        for (int i = 0;i<N;i++) instance.add(random.nextInt(200),random.nextDouble());
        
        expResult = N;
        result = instance.size();
        assertEquals(expResult, result);
    }

    /**
     * Test of isEmpty method, of class TimeSeries.
     */
    @Test
    public void testIsEmpty() {
        System.out.println("isEmpty");
        TimeSeries instance = new TimeSeries();
        boolean expResult = true;
        boolean result = instance.isEmpty();
        assertEquals(expResult, result);
        
        instance.addAll(new ArrayList());
        result = instance.isEmpty();
        assertEquals(expResult, result);        
        
        instance.add(2,1);
        expResult = false;
        
        result = instance.isEmpty();
        assertEquals(expResult, result);        
    }

    /**
     * Test of iterator method, of class TimeSeries.
     */
    @Test
    public void testIterator() {
        System.out.println("iterator");
        TimeSeries instance = new TimeSeries();
        
        Iterator<Timepoint> result = instance.iterator();
        assertFalse(result.hasNext());
        
        List<Double> times = new ArrayList<>();
        
        int N =200;
        for (int i = 0;i<N;i++) {
            double t = random.nextInt(N);
            times.add(t);
            instance.add(t,random.nextDouble());
        }
        
        Collections.sort(times);
        
        Iterator<Double> tIter = times.iterator();
        result = instance.iterator();
        while(tIter.hasNext()) {
            assertEquals(tIter.next(),result.next().getTime(),EPS);
        }
        assertFalse(result.hasNext());
    }

    /**
     * Test of getDuration method, of class TimeSeries.
     */
    @Test
    public void testGetDuration() {
        System.out.println("getDuration");
        TimeSeries instance = new TimeSeries();
        double expResult = 0.0;
        double result = instance.getDuration();
        assertEquals(expResult, result, EPS);
        
        instance.add(1,10);
        result = instance.getDuration();
        assertEquals(expResult, result, EPS);
        
        instance.add(0,2);
        instance.add(10,1);
        expResult = 10;
        result = instance.getDuration();
        assertEquals(expResult, result, EPS);
        
        instance.add(1,2);
        result = instance.getDuration();
        assertEquals(expResult, result, EPS);
        
        instance.add(4,5);
        result = instance.getDuration();
        assertEquals(expResult, result, EPS);
        
        instance.add(14,-5);
        expResult = 14;
        result = instance.getDuration();
        assertEquals(expResult, result, EPS);
        
    }

    /**
     * Test of getAverageStep method, of class TimeSeries.
     */
    @Test
    public void testGetAverageStep() {
        System.out.println("getAverageStep");
        TimeSeries instance = new TimeSeries();
        double expResult = Double.NaN;
        double result;
        
            result = instance.getAverageStep();
            assertEquals(expResult, result, EPS);
        
        
        instance.add(1,10);
        result = instance.getAverageStep();
        assertEquals(expResult, result, EPS);
        
        instance.add(0,2);
        instance.add(3,1);
        expResult = 1.5;
        result = instance.getAverageStep();
        assertEquals(expResult, result, EPS);
        
    }

    /**
     * Test of factorise method, of class TimeSeries.
     */
    @Test
    public void testFactorise() {
        System.out.println("factorise");
        double factor = 1;
        TimeSeries instance = new TimeSeries();
        TimeSeries expResult = new TimeSeries();
        TimeSeries result = instance.factorise(factor);
        assertEquals(expResult, result);
        
        instance.add(0,10);
        expResult.add(0,10);
        
        result = instance.factorise(factor);
        assertEquals(expResult, result);
        
        instance = new TimeSeries();
        expResult = new TimeSeries();
        int N = 100;
        factor = 2;
        for (int i = 0;i<N;i++) {
            double v = random.nextDouble()*100;
            instance.add(i,v);
            expResult.add(i,v/factor);
        }
        
        result = instance.factorise(factor);
        assertEquals(expResult, result);        
    }

    /**
     * Test of almostEquals method, of class TimeSeries.
     */
    @Test
    public void testAlmostEquals() {
        System.out.println("almostEquals");
        TimeSeries other = new TimeSeries();
        double precision = EPS;
        TimeSeries instance = new TimeSeries();
        boolean result = instance.almostEquals(other, precision);
        assertTrue(result);
        
        other.add(1,2);
        result = instance.almostEquals(other, precision);
        assertFalse(result);        
        
        instance.add(1,2);
        result = instance.almostEquals(other, precision);
        assertTrue(result);
        
        other.add(2,3);
        instance.add(2.01,3);
        result = instance.almostEquals(other, precision);
        assertFalse(result);
        result = instance.almostEquals(other, 0.1);
        assertTrue(result);
        
        other.add(4,2.01);
        instance.add(4,2);
        result = instance.almostEquals(other, precision);
        assertFalse(result);
        result = instance.almostEquals(other, 0.1);
        assertTrue(result);
        
    }
    
    /**
     * Test of hasSameTimes method, of class TimeSeries.
     */
    @Test
    public void testHasSameTimes() {
        TimeSeries other = null;
        TimeSeries instance = new TimeSeries();
        assertFalse(instance.hasSameTimes(other));

        other = new TimeSeries();
        assertTrue(instance.hasSameTimes(other));
        
        other.add(10,2);
        assertFalse(instance.hasSameTimes(other));
        
        instance.add(10,3);
        assertTrue(instance.hasSameTimes(other));
        
        other.add(2,3);
        instance.add(2,30);
        assertTrue(instance.hasSameTimes(other));
        
        instance.add(4,4);
        assertFalse(instance.hasSameTimes(other));

        other.add(4,0);
        assertTrue(instance.hasSameTimes(other));
        
    }
    
    /**
     * Test of hasSameTimes method, of class TimeSeries.
     */
    @Test
    public void testHasSameTimesWithPrecission() {
        
        double precission = 0.05;
        
        TimeSeries other = null;
        TimeSeries instance = new TimeSeries();
        assertFalse(instance.hasSameTimes(other, precission));

        other = new TimeSeries();
        assertTrue(instance.hasSameTimes(other, precission));
        
        other.add(10,2);
        assertFalse(instance.hasSameTimes(other, precission));
        
        instance.add(10.04,3);
        assertTrue(instance.hasSameTimes(other, precission));
        
        other.add(2.045,3);
        instance.add(2,30);
        assertTrue(instance.hasSameTimes(other, precission));
        
        instance.add(4,4);
        assertFalse(instance.hasSameTimes(other, precission));

        other.add(4.051,0);
        assertFalse(instance.hasSameTimes(other, precission));
        
        instance.add(4.04,4);
        other.add(3.98,0);
        assertTrue(instance.hasSameTimes(other, precission));
    }
    
    

    /**
     * Test of getTimesAndValues method, of class TimeSeries.
     */
    @Test
    public void testGetTimesAndValues() {
        System.out.println("getTimesAndValues");
        TimeSeries instance = new TimeSeries();
        Pair<double[],double[]> expResult = new Pair(new double[0],new double[0]);
        Pair<double[],double[]> result = instance.getTimesAndValues();
        assertArrayEquals(expResult.getLeft(), result.getLeft(),EPS);
        assertArrayEquals(expResult.getRight(), result.getRight(),EPS);
        
        instance.add(1,2);        
        expResult = new Pair(new double[]{1},new double[]{2});
        
        result = instance.getTimesAndValues();
        assertArrayEquals(expResult.getLeft(), result.getLeft(),EPS);
        assertArrayEquals(expResult.getRight(), result.getRight(),EPS);
    
        instance.add(0,0);
        instance.add(10,2);
        expResult = new Pair(new double[]{0,1,10},new double[]{0,2,2});
        
        result = instance.getTimesAndValues();
        assertArrayEquals(expResult.getLeft(), result.getLeft(),EPS);
        assertArrayEquals(expResult.getRight(), result.getRight(),EPS);
        
        
    
    }

    /**
     * Test of getTimesAndValuesLists method, of class TimeSeries.
     */
    @Test
    public void testGetTimesAndValuesLists() {
        System.out.println("getTimesAndValuesLists");
        TimeSeries instance = new TimeSeries();
        Pair<List<Double>,List<Double>> expResult = new Pair(Collections.EMPTY_LIST,Collections.EMPTY_LIST);
        Pair<List<Double>,List<Double>> result = instance.getTimesAndValuesLists();
        assertEquals(expResult, result);

        instance.add(10,1);
        List<Double> eL = Arrays.asList(10.0);
        List<Double> eR = Arrays.asList(1.0);
        
        result = instance.getTimesAndValuesLists();
        assertEquals(eL, result.getLeft());
        assertEquals(eR, result.getRight());
        
        instance.add(0,100);
        instance.add(10.0001,-1);
        eL = Arrays.asList(0.0,10.0,10.0001);
        eR = Arrays.asList(100.0,1.0,-1.0);
        
        result = instance.getTimesAndValuesLists();
        assertEquals(eL, result.getLeft());
        assertEquals(eR, result.getRight());
        
    }

    /**
     * Test of getTimes method, of class TimeSeries.
     */
    @Test
    public void testGetTimes() {
        System.out.println("getTimes");
        TimeSeries instance = new TimeSeries();
        double[] expResult = {};
        double[] result = instance.getTimes();
        assertArrayEquals(expResult, result,EPS);
        
        instance.add(2,-10);
        expResult = new double[]{2};
        result = instance.getTimes();
        assertArrayEquals(expResult, result,EPS);    
    
        instance.add(1,0);
        expResult = new double[]{1,2};
        result = instance.getTimes();
        assertArrayEquals(expResult, result,EPS);    
    }

    /**
     * Test of getValues method, of class TimeSeries.
     */
    @Test
    public void testGetValues() {
        System.out.println("getValues");
        TimeSeries instance = new TimeSeries();
        double[] expResult = {};
        double[] result = instance.getValues();
        assertArrayEquals(expResult, result,EPS);
        
        instance.add(2,-10);
        expResult = new double[]{-10};
        result = instance.getValues();
        assertArrayEquals(expResult, result,EPS);    
    
        instance.add(1,0);
        expResult = new double[]{0,-10};
        result = instance.getValues();
        assertArrayEquals(expResult, result,EPS);    
    }

    /**
     * Test of getTimesList method, of class TimeSeries.
     */
    @Test
    public void testGetTimesList() {
        System.out.println("getTimesList");
        TimeSeries instance = new TimeSeries();
        List<Double> expResult = Collections.EMPTY_LIST;
        List<Double> result = instance.getTimesList();
        assertEquals(expResult, result);
        
        
        instance.add(3,0);
        expResult = Arrays.asList(3.0);
        
        result = instance.getTimesList();
        assertEquals(expResult, result);        
        
        instance.add(0,10);
        expResult = Arrays.asList(0.0,3.0);
        
        result = instance.getTimesList();
        assertEquals(expResult, result);        
    }

    @Test
    public void testAddTrend() {
        
        System.out.println("testAddTrend");
        
        double slope = 1;
        double inter = 1;
        TimeSeries org = new TimeSeries();
        
        TimeSeries exp = new TimeSeries();
        TimeSeries res = org.addTrend(slope, inter);
        
        assertEquals(exp, res);
        assertNotSame(res, org);
        
        org.add(1,2);
        exp.add(1,2+1+1);
        
        res = org.addTrend(slope, inter);
        
        assertEquals(exp, res);
        assertNotSame(res, org);        
        
        org.add(0,0);
        exp.add(0,1);
        
        res = org.addTrend(slope, inter);        
        assertEquals(exp, res);
        assertNotSame(res, org);   
        
        org = new TimeSeries();
        for (int i=0;i<10;i++) org.add(i,random.nextDouble());
        
        slope = 0;
        inter = 0;
        res = org.addTrend(slope, inter);
        assertEquals(org,res);
        
        org = new TimeSeries();
        exp = new TimeSeries();
        
        for (int i=0;i<10;i++) {
            org.add(i,0);
            exp.add(i,i);
        }        
        slope = 1;
        inter = 0;
        
        res = org.addTrend(slope, inter);
        assertEquals(exp, res);
        
        org = new TimeSeries();
        exp = new TimeSeries();
        
        for (int i=0;i<10;i++) {
            org.add(i,0);
            exp.add(i,1);
        }        
        slope = 0;
        inter = 1;
        
        res = org.addTrend(slope, inter);
        assertEquals(exp, res);
        
        org = new TimeSeries();
        org.add(0,1);
        org.add(10,2);
        
        slope = -1;
        inter = 1;
        
        res = org.addTrend(slope, inter);
        assertEquals(2,res.getMaxValue(),EPS);
        assertEquals(-7,res.getMinValue(),EPS);

        //assertTrue(false);
        
    }
    
    /**
     * Test of getValuesLists method, of class TimeSeries.
     */
    @Test
    public void testGetValuesList() {
        System.out.println("getValuesList");
        TimeSeries instance = new TimeSeries();
        List<Double> expResult = Collections.EMPTY_LIST;
        List<Double> result = instance.getValuesList();
        assertEquals(expResult, result);
        
        
        instance.add(3,0);
        expResult = Arrays.asList(0.0);
        
        result = instance.getValuesList();
        assertEquals(expResult, result);        
        
        instance.add(0,10);
        expResult = Arrays.asList(10.0,0.0);
        
        result = instance.getValuesList();
        assertEquals(expResult, result);        
    }

    /**
     * Test of equals method, of class TimeSeries.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object obj = new Double(3);
        TimeSeries instance = new TimeSeries();
        boolean expResult = false;
        boolean result = instance.equals(obj);
        assertEquals(expResult, result);
        
        TimeSeries sc = new TimeSeries();
        
        obj =sc;
        expResult = true;
        result = instance.equals(obj);
        assertEquals(expResult, result);
        
        sc.add(0,1);
        expResult = false;        
        result = instance.equals(obj);
        assertEquals(expResult, result);

        sc.add(10,2);
        sc.add(2,1);
        
        instance.add(new Timepoint(0,1));
        instance.add(new Timepoint(2,1));
        instance.add(new Timepoint(10,2,1.0,null));
        result = instance.equals(obj);
        assertEquals(expResult, result);

        sc = new TimeSeries();
        sc.add(0,1);
        obj = sc;
        
        instance = new TimeSeries();
        instance.add(new Timepoint(0,1,null,null));
        
        expResult = true;
        result = instance.equals(obj);
        assertEquals(expResult, result);
        
        sc.add(new Timepoint(2,1,1.0,2.0));
        instance.add(new Timepoint(2,1,1.0,2.0));
        result = instance.equals(obj);
        assertEquals(expResult, result);
        
        sc.add(new Timepoint(10,1,2.0,null));
        instance.add(new Timepoint(10,1,2.0,null));
        result = instance.equals(obj);
        assertEquals(expResult, result);
        
        sc.add(new Timepoint(11,1,null,2.0));
        instance.add(new Timepoint(11,1,null,2.0));
        result = instance.equals(obj);
        assertEquals(expResult, result);
        
        sc.add(new Timepoint(12,2,1.0,null));
        instance.add(new Timepoint(12,2,null,1.0));
        expResult = false;
        result = instance.equals(obj);
        assertEquals(expResult, result);

        sc = new TimeSeries();
        obj = sc;
        
        instance = new TimeSeries();
        sc.add(1,2);
        result = instance.equals(obj);
        assertEquals(expResult, result);
        
        instance.add(1,2.1);
        result = instance.equals(obj);
        assertEquals(expResult, result);
        
        
    }

    /**
     * Test of hashCode method, of class TimeSeries.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        
        TimeSeries t1 = new TimeSeries();
        TimeSeries t2 = new TimeSeries();
        TimeSeries t3 = new TimeSeries();
        
        t1.add(0,15);
        
        assertEquals(t2.hashCode(), t3.hashCode());
        assertFalse(t1.hashCode() == t2.hashCode());
        
        t2.add(new Timepoint(0,15));
        t3.add(new Timepoint(0, 15, null, null));
        
        assertEquals(t1.hashCode(), t2.hashCode());
        assertEquals(t2.hashCode(), t3.hashCode());

        t1.add(10,1);
        t1.add(1,10);
        
        t2.add(1,10);
        t2.add(10,1);

        t3.add(1,1);
        t3.add(10,10);
        
        assertEquals(t1.hashCode(), t2.hashCode());
        assertFalse(t1.hashCode() == t3.hashCode());
        
        t1.add(2,1);
        t2.add(new Timepoint(2,1,null,1.0));

        assertFalse(t1.hashCode() == t2.hashCode());
        
        t1 = new TimeSeries();
        t2 = new TimeSeries();

        t1.add(10,1);
        t1.add(1,10);
        
        t2.add(1,10);
        t2.add(10,1);
        
        t1.add(new Timepoint(2,1,null,1.0));
        t2.add(new Timepoint(2,1,null,1.0));
        
        assertEquals(t1.hashCode(), t2.hashCode());
        
        t1.add(3,4);
        t1.add(new Timepoint(2,1,1.0,null));
        assertFalse(t1.hashCode() == t2.hashCode());
        
        t1 = new TimeSeries();
        t2 = new TimeSeries();
        t1.add(new Timepoint(1,2,2.0,null));
        t2.add(new Timepoint(1,2,null,2.0));
        
        assertFalse(t1.hashCode() == t2.hashCode());
    }


    /**
     * Test of writeExternal method, of class TimeSeries.
     */
    @Test
    public void testSerialization() throws Exception {
        System.out.println("serialization");
        
        File file = TEST_DIR.newFile();//new File(TEST_DIR,"timeseries."+hashCode()+".ser");
        
        try {
            TimeSeries org = new TimeSeries();
            testSerializationID(org,file);
            
            org.add(1,2);
            testSerializationID(org,file);
            
            org.add(1000,2);
            testSerializationID(org,file);
            
            org = new TimeSeries();
            org.add(10,1);
            org.add(1,2);
            org.add(2,3);
            testSerializationID(org,file);
            
            
            org = new TimeSeries();
            int N = 100;
            for (int i =0;i<N;i++) {
                double t = random.nextInt(50);
                double v = random.nextDouble()*100;
                Double stdE = random.nextDouble() < 0.2 ? random.nextDouble() : null;
                Double stdD = random.nextDouble() < 0.2 ? random.nextDouble() : null;
                Timepoint tp = new Timepoint(t, v, stdE, stdD);
                org.add(tp);
            }
            testSerializationID(org,file);
            
        } catch (AssertionError e) {
            file.delete();
            throw e;
        }
    }
   

    /**
     * Test of writeExternal method, of class TimeSeries.
     */
    @Test
    public void testSerialization2() throws Exception {
        System.out.println("serialization2");
        
        File file = TEST_DIR.newFile();//new File(TEST_DIR,"timeseries."+hashCode()+".ser");
        
        try {
            TimeSeries org = new TimeSeries();
            testSerializationID(org,file);
            
            org = new TimeSeries();
            org.add(new Timepoint(0,4,null,null) );
            testSerializationID(org,file);
            
            org = new TimeSeries();
            org.add(new Timepoint(0,4,null,null) );
            org.add(1000,2);
            testSerializationID(org,file);
            
            
        } catch (AssertionError e) {
            file.delete();
            throw e;
        }
    }
    
    protected void testSerializationID(TimeSeries org,File file) throws IOException, ClassNotFoundException {
        try (ObjectOutput out = new ObjectOutputStream(new FileOutputStream(file))) {            
            out.writeObject(org);
        }
        
        TimeSeries read;
        try (ObjectInput in = new ObjectInputStream(new FileInputStream(file))) {
            read = (TimeSeries) in.readObject();
        }
        
        testExtendedEquality(org,read);
        
    }
    
    /**
     * Test of xml serialization, of class TimeSeries.
     */
    @Test
    public void testXmlSerialization() throws Exception {
        System.out.println("xml serialization");
        
        File file = TEST_DIR.newFile();//new File(TEST_DIR,"timeseries."+hashCode()+".xml");
        
        try {
            TimeSeries org = new TimeSeries();
            testXmlSerializationID(org,file);
            
            org.add(1,2);
            testXmlSerializationID(org,file);
            
            org.add(1000,2);
            testXmlSerializationID(org,file);
            
            org = new TimeSeries();
            org.add(10,1);
            org.add(1,2);
            org.add(2,3);
            testXmlSerializationID(org,file);
            
            
            org = new TimeSeries();
            int N = 100;
            for (int i =0;i<N;i++) {
                double t = random.nextInt(50);
                double v = random.nextDouble()*100;
                Double stdE = random.nextDouble() < 0.2 ? random.nextDouble() : null;
                Double stdD = random.nextDouble() < 0.2 ? random.nextDouble() : null;
                Timepoint tp = new Timepoint(t, v, stdE, stdD);
                org.add(tp);
            }
            testXmlSerializationID(org,file);
            
        } catch (AssertionError e) {
            file.delete();
            throw e;
        }
    }
    
    protected void testXmlSerializationID(TimeSeries org,File file) throws IOException, ClassNotFoundException, JAXBException {
        
        JAXBContext cont = JAXBContext.newInstance(TimeSeries.class);
        Marshaller mar = cont.createMarshaller();
        mar.marshal(org, file);

        Unmarshaller umar = cont.createUnmarshaller();
        
        
        TimeSeries read = (TimeSeries) umar.unmarshal(file);
        
        testExtendedEquality(org,read);
        
    }
    
    protected void testExtendedEquality(TimeSeries org,TimeSeries cpy) {
            assertEquals(org,cpy);
            
            
            if (!org.isEmpty()) {
                assertEquals(org.getFirst(),cpy.getFirst());
                assertEquals(org.getLast(),cpy.getLast());
                assertEquals(org.getAverageStep(),cpy.getAverageStep(),EPS);
                assertEquals(org.getDuration(),cpy.getDuration(),EPS);
                assertEquals(org.getMaxTimePoint(),cpy.getMaxTimePoint());
                assertEquals(org.getMinTimePoint(),cpy.getMinTimePoint());
                assertEquals(org.getMeanValue(),cpy.getMeanValue(),EPS);
                assertEquals(org.getAmplitude(),cpy.getAmplitude(),EPS);
                assertEquals(org.getTimepoints(),cpy.getTimepoints());
            }
                
       
    }
    
    
}
