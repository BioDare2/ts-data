/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

import ed.robust.dom.data.TimeSeries;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author tzielins
 */
public class GoodnessOfFitFinderTest {
    
    public GoodnessOfFitFinderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    
    protected GoodnessOfFitFinder makeInstance(GoodnessOfFitFinder.REF_TYPE type) {
        return makeInstance(type,GoodnessOfFitFinder.GOF_METHOD.ABS_MEAN_PL_STD);
    }
    
    protected GoodnessOfFitFinder makeInstance(GoodnessOfFitFinder.REF_TYPE type,GoodnessOfFitFinder.GOF_METHOD method) {
        return new GoodnessOfFitFinder(type, method);
    }
    
    
    @Test
    public void testGOFMonotonity() {
        System.out.println("chekcing monotonous gof values");
        
        double period = 24.5;
        double phase = 5;
        double amplitude = 2;
        double step = 1;
        int N = 100;
        
        
        TimeSeries data = TSGenerator.makeDblPulse(N, step, period, phase, amplitude);
        TimeSeries fit = data;
        
        GoodnessOfFitFinder instance = makeInstance(GoodnessOfFitFinder.REF_TYPE.POLY_3);
        
        double EPS = 0.01;
        double exp = 0;
        double gof = instance.findGOF(data, fit);
        
        assertEquals(exp, gof,EPS);
        
        double prev = 0;
        double[] noises = {0.1,0.2,0.4,0.8,1.6,3.2};
        
        for (double noise : noises) {
            fit = TSGenerator.addNoise(data, noise);
            gof = instance.findGOF(data, fit);
            System.out.println("GOF: "+gof+"; noise: "+noise);
            if (prev == instance.VERY_BAD)
                assertEquals(instance.VERY_BAD, gof,EPS);
            else
                assertTrue(gof > prev);
            prev = gof;
        }
        
    }
    
    
    @Test
    public void testGOFMonotonity2() {
        System.out.println("chekcing monotonous gof values2");
        
        double period = 24.5;
        
        double amplitude = 2;
        double step = 1;
        int N = 100;
        
        
        TimeSeries data = TSGenerator.makeDblPulse(N, step, period, 0, amplitude);
        TimeSeries fit = data;
        
        GoodnessOfFitFinder instance = makeInstance(GoodnessOfFitFinder.REF_TYPE.POLY_3);
        
        double EPS = 0.01;
        double exp = 0;
        double gof = instance.findGOF(data, fit);
        
        assertEquals(exp, gof,EPS);
        
        double prev = 0;
        
        
        for (double phase = 1;phase < period/2;phase+=1) {
            fit = TSGenerator.makeDblPulse(N, step, period, phase, amplitude);
            gof = instance.findGOF(data, fit);
            System.out.println("GOF: "+gof+"; phase: "+(phase));
            if (prev == instance.VERY_BAD)
                assertEquals(instance.VERY_BAD, gof,EPS);
            else
                assertTrue(gof > prev);
            prev = gof;
        }
        
    }
    
    @Test
    public void testGOFWrongness() {
        System.out.println("chekcing wrong fits");
        
        double period = 24.5;
        double phase = 5;
        double amplitude = 2;
        double step = 1;
        int N = 100;
        
        TimeSeries data = TSGenerator.makeLine(N, step, 0.01, 2);
        TimeSeries fit = TSGenerator.makeDblPulse(N, step, period, phase, amplitude);
        
        GoodnessOfFitFinder instance = makeInstance(GoodnessOfFitFinder.REF_TYPE.POLY_3);

        double EPS = 0.01;
        double exp = GoodnessOfFitFinder.VERY_BAD;
        double gof = instance.findGOF(data, fit);
        
        assertEquals(exp, gof,EPS);
        
        exp = 0;//Math.min(1, GoodnessOfFitFinder.VERY_BAD);
        gof = instance.findGOF(data, data);
        
        assertEquals(exp, gof,EPS);
        
        data = TSGenerator.makeDblPulse(N, step, period, phase, amplitude);
        fit = TSGenerator.makeDblPulse(N, step, period, phase, amplitude);
        
        exp = 0;
        gof = instance.findGOF(data, fit);        
        assertEquals(exp, gof,EPS);
        
        fit = TSGenerator.makeDblPulse(N, step, period, phase+period/2, amplitude);
        exp = GoodnessOfFitFinder.VERY_BAD;
        gof = instance.findGOF(data, fit);        
        assertEquals(exp, gof,EPS);
        
    }
    
    
    //@Test
    public void testFindGOF_TimeSeries_TimeSeries() {
        System.out.println("findGOF timeseries "+GoodnessOfFitFinder.ALMOST_ZERO);
        
        double period = 24.5;
        double phase = 5;
        double amplitude = 2;
        double step = 1;
        int N = 100;
        
        
        TimeSeries data = TSGenerator.makeDblPulse(N, step, period, phase, amplitude);
        TimeSeries fit = data;
        
        GoodnessOfFitFinder instance = makeInstance(GoodnessOfFitFinder.REF_TYPE.LINEAR);
        
        double EPS = 0.01;
        double expResult = 0.0;
        double result = instance.findGOF(data, fit);
                
        System.out.println(result);
        assertEquals(expResult, result, EPS);
        
        fit = TSGenerator.getLinTrend(data);
        expResult = 1;
        result = instance.findGOF(data, fit);
                
        System.out.println(result);
        assertEquals(expResult, result, EPS);
    
        fit = TSGenerator.getLinTrend(data);
        expResult = instance.VERY_BAD;
        result = instance.findGOF(fit,data);
                
        System.out.println(result);
        assertEquals(expResult, result, EPS);
        
        
        fit = TSGenerator.makeDblPulse(N/2, 2*step, period, phase, amplitude);
        expResult = 0;
        result = instance.findGOF(data, fit);
                
        System.out.println(result);
        assertEquals(expResult, result, EPS);
        
        fit = TSGenerator.makeDblPulse(N, step, period, phase+0.5, amplitude);
        expResult = 0;
        double result1 = instance.findGOF(data, fit);
                
        System.out.println(result1);
        
        fit = TSGenerator.makeDblPulse(N, step, period, phase+1, amplitude);
        expResult = 0;
        double result2 = instance.findGOF(data, fit);
                
        System.out.println(result2);
        
        assertTrue(result1<result2);
        
        fit = TSGenerator.makeDblPulse(N, step, period, phase+period/2, amplitude);
        expResult = 0;
        result1 = instance.findGOF(data, fit);
                
        System.out.println(result1);
        
        assertTrue(result1>=instance.VERY_BAD);
        
        GoodnessOfFitFinder instance2 = makeInstance(GoodnessOfFitFinder.REF_TYPE.POLY_3);
        result2 = instance2.findGOF(data, fit);
                
        System.out.println(result2);
        
        assertTrue(result2>=instance.VERY_BAD);
        assertTrue(result2>=result1);
        
        
        double[] t = new double[100];
        double[] v = new double[t.length];
        
        for (int i = 0;i<t.length;i++) {
            t[i] = i;
            v[i] = i*i-i+1;
        }
        
        v[0] = 1.1;
        data = new TimeSeries(t, v);
        
        v[0] = 1;
        fit = new TimeSeries(t, v);
        
        result1 = instance.findGOF(data, fit);                
        System.out.println(result1);       
        assertTrue(result1<0.5);
        
        result2 = instance2.findGOF(data, fit);                
        System.out.println(result2);       
        assertTrue(result2>=instance.VERY_BAD);
    }

}
