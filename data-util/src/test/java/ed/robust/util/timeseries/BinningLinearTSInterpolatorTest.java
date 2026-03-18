/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

import java.util.List;
import ed.robust.dom.data.TimeSeries;
import ed.robust.dom.data.Timepoint;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Zielu
 */
public class BinningLinearTSInterpolatorTest {
    
    public BinningLinearTSInterpolatorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void testOutOfRange() {
        
        System.out.println("Test out of range");
        DataSource ds = LocalRegressionDetrending.makeLineDataSource(0, 90, 0, 1);
        double value = ds.getValue(90);
        assertEquals(1, value,0.00001);
        value = ds.getValue(90.5);
        assertEquals(1, value,0.00001);        
        value = ds.getValue(80.5);
        assertEquals(1, value,0.00001);        
    }
    /**
     * Test of getValue method, of class BinningLinearTSInterpolator.
     */
    @Test
    public void testGetValue1() {
        System.out.println("getValue1");
        
        
        TimeSeries data = new TimeSeries();
        
        Timepoint first = new Timepoint(1.0111,1);
        Timepoint last = new Timepoint(10.1231,10);
        data.add(first);
        data.add(last);
        data.add(10.34,0);                                
        data.add(5,5);
        
        BinningLinearTSInterpolator instance = new BinningLinearTSInterpolator(data,ROUNDING_TYPE.INTEGER);                

        double time = 1;

        double expResult = 1;
        double result = instance.getValue(time);
        assertEquals(expResult, result, 0.0000001);
        
        time = 0;
        expResult = 1;
        
        result = instance.getValue(time);
        assertEquals(expResult, result, 0.0000001);        
        
        time = 2.5;
        expResult = 2.5;
        result = instance.getValue(time);
        assertEquals(expResult, result, 0.0000001);
        
        
        time = 6;
        expResult = 5;
        result = instance.getValue(time);
        assertEquals(expResult, result, 0.0000001);
        
        time = 15;
        expResult = 5;
        result = instance.getValue(time);
        assertEquals(expResult, result, 0.0000001);
        
    }

    /**
     * Test of getFirst method, of class BinningLinearTSInterpolator.
     */
    @Test
    public void testGetFirstAndLast() {
        System.out.println("getFirst");
        
        TimeSeries data = new TimeSeries();
        
        Timepoint first = new Timepoint(1.0111,1);
        Timepoint last = new Timepoint(10.1231,10);
        data.add(first);
        data.add(last);

        BinningLinearTSInterpolator instance = new BinningLinearTSInterpolator(data,ROUNDING_TYPE.NO_ROUNDING);                
        
        Timepoint expResult = first;
        Timepoint result = instance.getFirst();
        assertEquals(expResult, result);
        
        expResult = last;
        result = instance.getLast();
        assertEquals(expResult, result);
        
        instance = new BinningLinearTSInterpolator(data,ROUNDING_TYPE.CENTY);                
        
        first = new Timepoint(1.01,first.getValue());
        expResult = first;
        result = instance.getFirst();
        assertEquals(expResult, result);
        
        last = new Timepoint(10.12,last.getValue());
        expResult = last;
        result = instance.getLast();
        assertEquals(expResult, result);        

        data.add(10.34,0);
        instance = new BinningLinearTSInterpolator(data,ROUNDING_TYPE.INTEGER);  
        
        expResult = new Timepoint(10,5);
        result = instance.getLast();
        assertEquals(expResult, result);        
        
    }

    /**
     * Test of getLast method, of class BinningLinearTSInterpolator.
     */
    //@Test
    public void testGetLast() {
        System.out.println("getLast");
        BinningLinearTSInterpolator instance = null;
        Timepoint expResult = null;
        Timepoint result = instance.getLast();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of interpolate method, of class BinningLinearTSInterpolator.
     */
    @Test
    public void testInterpolate() {
        System.out.println("interpolate");
        double t = 5.0;
        Timepoint left = new Timepoint(1,1);
        Timepoint right = new Timepoint(10,1);
        
        double expResult = 1;
        double result = BinningLinearTSInterpolator.interpolate(5, left, right);
        assertEquals(expResult, result, 0.00001);
        
        left = new Timepoint(1,1);
        right = new Timepoint(10,10);
        
        expResult = 5;
        result = BinningLinearTSInterpolator.interpolate(5, left, right);
        assertEquals(expResult, result, 0.00001);
        
    }
    
    @Test
    public void testGetValue2() {
        System.out.println("getValue2");
        
        TimeSeries data = TSGenerator.makeStep(50,2, 25, 5,2);
        
        TimeSeriesInterpolator instance = new BinningLinearTSInterpolator(data,ROUNDING_TYPE.MIL);

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
        expResult = (data.getTimepoints().get(i).getValue()+data.getTimepoints().get(i+1).getValue())/2;
        result = instance.getValue(time);
        
        double l = data.getTimepoints().get(i).getValue();
        double r = data.getTimepoints().get(i+1).getValue();
        double e = l+(time-data.getTimepoints().get(i).getTime())*(r-l)/(data.getTimepoints().get(i+1).getTime()-data.getTimepoints().get(i).getTime());
        
        assertEquals(e, result, 0.0001);
        assertEquals(expResult, result, 0.0001);
        
        assertTrue(result > data.getTimepoints().get(i).getValue());
        assertTrue(result < data.getTimepoints().get(i+1).getValue());
        
        time = instance.getFirst().getTime()-1;
        expResult = instance.getFirst().getValue();
        result = instance.getValue(time);
        assertEquals(expResult, result, 0.0001);
        
        time = instance.getLast().getTime()+1;
        expResult = instance.getLast().getValue();
        result = instance.getValue(time);
        assertEquals(expResult, result, 0.0001);
        
        time = Double.MAX_VALUE/2;
        expResult = instance.getLast().getValue();
        result = instance.getValue(time);
        assertEquals(expResult, result, 0.0001);
    }
    
    
    @Test
    public void testMakeInterpolation() {
        System.out.println("make Interpolation");
        
        TimeSeries data = new TimeSeries();
        data.add(1.012,1);
        data.add(5,5);
        data.add(10,5);
        data.add(10.030098,-5);
        data.add(10.2,0);
        
        TimeSeries exp = new TimeSeries();
        for (double i = 1;i<=5;i+=0.5) exp.add(i,i);
        
        for (double i = 5.5;i<=10;i+=0.5) exp.add(i,10-i);
        
        BinningLinearTSInterpolator instance = new BinningLinearTSInterpolator(data, ROUNDING_TYPE.DECY);
        
        List<Timepoint> result = instance.makeInterpolation(0.5,ROUNDING_TYPE.CENTY);
        List<Timepoint> expResult = exp.getTimepoints();
        
        for(int i = 0;i<result.size();i++) {
            assertEquals(expResult.get(i), result.get(i));
        }
         
        // */
        
        /*
        for (Timepoint tp : result) {
            System.out.println(tp.getTime()+"\t"+tp.getValue());
        }*/
    }
    
}
