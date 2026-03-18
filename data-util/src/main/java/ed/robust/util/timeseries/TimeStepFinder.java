/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

import ed.robust.dom.data.TimeSeries;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.ListSelectionEvent;

/**
 *
 * @author tzielins
 */
public class TimeStepFinder {
    
    /**
     * Finds optimal time interval between data points. 
     * For equally spaced data, the optimal time step is same as the existing interval between data points (rounded up to centy).
     * For not equally spaced data, if more than half of the points share the same time interval, this major data step is selected,
     * otherwise the mean data step is returned. That was the optimal time interval when used in interpolation will provide data points
     * which are as close as possible to the original data even if some points are missing.
     * @param data input time series
     * @return optimal data step, which would provide the 
     */
    public double findTimeStep(TimeSeries data) {
        
        return findTimeStep(data.getTimes());
        
        
    }
    
    
    public double findTimeStep(double[] times) {
        if (times.length < 2) throw new IllegalArgumentException("Data must have at least 2 points");
        //steps are preresented as integer intStep =100*step
        Map<Integer,Integer> stepsFrequency = countSteps(times);
        
        int mostFrequent = getMostFrequent(stepsFrequency);
        if (stepsFrequency.get(mostFrequent) > (times.length/2)) return mostFrequent/100.0;
        
        double duration = times[times.length-1]-times[0];
        return duration/(times.length-1);
        
    }

    protected Map<Integer, Integer> countSteps(double[] times) {
        Map<Integer,Integer> stepsFrequency = new HashMap<>();
        
        
        int last = times.length-1;
        for (int i =0;i<last;i++) {
            double step = times[i+1]-times[i];
            int intStep = (int)(step*100);
            Integer count = stepsFrequency.get(intStep);
            if (count == null) count = 0;
            count++;
            stepsFrequency.put(intStep,count);
        }
        return stepsFrequency;
    }

    protected int getMostFrequent(Map<Integer, Integer> stepsFrequency) {
        
        int max = 0;
        int maxCount = 0;
        for (Map.Entry<Integer,Integer> entry : stepsFrequency.entrySet()) {
            if (entry.getValue() > maxCount) {
                max = entry.getKey();
                maxCount = entry.getValue();
            }
        }
        return max;
    }
}
