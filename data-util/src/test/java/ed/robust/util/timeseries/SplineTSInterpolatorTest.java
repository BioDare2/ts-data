/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

import ed.robust.dom.data.TimeSeries;
import ed.robust.dom.data.Timepoint;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author tzielins
 */
public class SplineTSInterpolatorTest {
    
    public SplineTSInterpolatorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }


    @Test
    public void testMakeInterpolation() throws IOException {
        System.out.println("makeInterpolation");
        
        TimeSeries data = TSGenerator.makeCos(50, 2, 25, 2);
        
        double step = 0.0;
        ROUNDING_TYPE xOutputRound = null;
        SplineTSInterpolator instance = new SplineTSInterpolator(data);
        
        TimeSeries expResult = new TimeSeries();
        expResult.addAll(data.getTimepoints());
        
        
        List result = instance.makeInterpolation(2, ROUNDING_TYPE.MIL);
        TimeSeries rTS = new TimeSeries();
        rTS.addAll(result);
        
        assertTrue(expResult.almostEquals(rTS, 0.001));
        
        result = instance.makeInterpolation(1, ROUNDING_TYPE.MIL);
        rTS = new TimeSeries();
        rTS.addAll(result);  
        
        List<TimeSeries> list = new ArrayList<>();
        list.add(data);
        list.add(rTS);
        
        TimeSeriesFileHandler.saveToText(list, Configuration.tempFile("inter.csv"), ",");
        
    }

    @Test
    public void testGetValue() {
        System.out.println("getValue");
        
        TimeSeries data = TSGenerator.makeStep(50,0.05, 25, 5,2);
        
        SplineTSInterpolator instance = new SplineTSInterpolator(data);

       //System.out.println("AVG: "+instance.getAverageStep());
        
        Timepoint tp = data.getFirst();
        
        double time = tp.getTime();
        double expResult = tp.getValue();
        double result = instance.getValue(time);
        assertEquals(expResult, result, 0.0001);
        
        tp = data.getTimepoints().get(10);
        time = tp.getTime();
        expResult = tp.getValue();
        result = instance.getValue(time);
        assertEquals(expResult, result, 0.0001);
        
        int i = 12;
        time = (data.getTimepoints().get(i).getTime()+data.getTimepoints().get(i+1).getTime())/2;
        result = instance.getValue(time);
        
        
        assertTrue(result > data.getTimepoints().get(i).getValue());
        assertTrue(result < data.getTimepoints().get(i+1).getValue());
        
        time = instance.getFirst().getTime()-1;
        result = instance.getValue(time);
        
        time = instance.getLast().getTime()+1;
        result = instance.getValue(time);
        
        time = Double.MAX_VALUE/2;
        result = instance.getValue(time);
    }

    @Test
    public void testGetFirst() {
        System.out.println("getFirst");
        TimeSeries data = TSGenerator.makeStep(50,2, 25, 5,2);
        
        SplineTSInterpolator instance = new SplineTSInterpolator(data);

        Timepoint tp = data.getFirst();
        
        double time = tp.getTime();
        double expResult = tp.getValue();
        double result = instance.getValue(time);
        assertEquals(expResult, result, 0.0001);
        
        
    }

    @Test
    public void testGetLast() {
        System.out.println("getLast");
         TimeSeries data = TSGenerator.makeStep(50,2, 25, 5,2);
        
        SplineTSInterpolator instance = new SplineTSInterpolator(data);

        Timepoint tp = data.getLast();
        
        double time = tp.getTime();
        double expResult = tp.getValue();
        double result = instance.getValue(time);

        assertEquals(expResult, result, 0.0001);
        
         
    }
    
    
    @Test
    public void testRepeatedData() {
        System.out.println("repeated data");
        
        TimeSeries data = new TimeSeries();
        data.add(0,0);
        data.add(0,10);
        data.add(1,1);
        data.add(0,5);
        data.add(2,0);
        data.add(2,4);
        
        SplineTSInterpolator instance = new SplineTSInterpolator(data);
        List<Timepoint> result = instance.makeInterpolation(1);
        
        for (Timepoint tp : result) {
            System.out.println(tp.getTime()+"\t"+tp.getValue());
        }
        
        
    }
    
}
