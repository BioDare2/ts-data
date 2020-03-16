/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import org.junit.Test;
/**
 *
 * @author tzielins
 */
public class LongComputationTest {
    

    
         
    //checks how long the computations last
    //@Test
    public void testLongTime() throws InterruptedException {
        
        System.out.println("\nTest LongComputation Times\n");
        long[] times = {20,50,100,500,1000,5000};
        Map<Long,Long> avgTimes = new HashMap<Long,Long>();
       
        System.out.println("Warmup 20: "+LongComputation.run(20, true));
        for (long t: times) {
            avgTimes.put(t, LongComputation.run(t, true));
        }

        int wrong = 0;
        for (long t: times) {
            long w = avgTimes.get(t);
            long e = (t-w);
            boolean bad = false;
            if (e < 0) bad = (-1*e > 0.15*t);
            else bad = (e > 0.15*t);
            if (bad)
                System.out.println("BAD T: "+t+", e:"+e+", w:"+w);
            else
                System.out.println("OK  T: "+t+", e:"+e+", w:"+w);
            if (bad) wrong++;
            
        }
        
        assertEquals(0,wrong);
        assertTrue(wrong==0);
    }
    
    
    
    
    
    
  
    
    
    
    
    
    
     
}
