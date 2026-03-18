/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

import ed.robust.dom.data.TimeSeries;
import ed.robust.dom.data.Timepoint;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

/**
 * Overlaps input data on top of each other using data window of given length.
 * @author tzielins
 */
public class TimeSeriesOverlapper {
    
    
    /**
     * Overlaps input data on top of each other using data window of given length.
     * This implementation interpolates data firstly into 0.1 hour data, overlaps them using
     * data window, averages, and return averaged weveform with 0.5 hour resolution.
     * @param window length of the overlapping window in hours
     * @param orgData time series with data
     * @return average waveform from time0 tile time0+window with timepoints having std error set
     */
    public static TimeSeries overlapData(double window, TimeSeries orgData) {
        
        DataRounder DECY = new DataRounder(ROUNDING_TYPE.DECY);
        window = DECY.round(window);
        
        TimeSeriesInterpolator interpolator = new SplineTSInterpolator(orgData,ROUNDING_TYPE.CENTY);
        
        List<Timepoint> points = interpolator.makeInterpolation(0.1, ROUNDING_TYPE.DECY);
        
        Map<Double,SummaryStatistics> waveform = new HashMap<Double,SummaryStatistics>();
        
        double first = points.get(0).getTime();
        
        for(Timepoint tp : points) {
            
            double time = DECY.round((tp.getTime()-first) % window);
            if (time ==window) time = 0;
            //System.out.println(time+"\t"+DECY.round(tp.getTime()-first));
            SummaryStatistics stat = waveform.get(time);
            if (stat == null) {
                stat = new SummaryStatistics();
                waveform.put(time,stat);
            }
            stat.addValue(tp.getValue());
            
        }
        
        TimeSeries fit = new TimeSeries();
        
        for (Map.Entry<Double,SummaryStatistics> entry : waveform.entrySet()) {
            
            double time = DECY.round(entry.getKey()+first);
            SummaryStatistics stat = entry.getValue();
            double value = stat.getMean();
            double error = stat.getStandardDeviation();
            
            Timepoint tp = new Timepoint(time, value, error, Timepoint.STD_DEV);
            fit.add(tp);
            
        }
        
        return fit;
    }
    
    
    public static PeriodicDataSource overlapAndPropagateData(double window,TimeSeries data) {

        TimeSeries overlapped = overlapData(window, data);
        
        DataSource interpolator = new SplineTSInterpolator(overlapped,ROUNDING_TYPE.DECY);

        return new PeriodicDataSource(interpolator, window, data.getLast().getTime());
        
        /*
       if (DataRounder.round(step,xOutputRound) != step) throw new IllegalArgumentException("Output roudning must be wider than precission of the step");
        
        TimeSeries overlapped = overlapData(window, data);
        
        TimeSeriesInterpolator interpolator = new SplineTSInterpolator(overlapped,ROUNDING_TYPE.DECY);
        
        DataRounder ROUND = new DataRounder(xOutputRound);
        double first = ROUND.round(data.getFirst().getTime());
        double last = ROUND.round(data.getLast().getTime());

        window = ROUND.round(window);
        double time = first;
        
        TimeSeries fit = new TimeSeries();
        while (time <= last) {
            double localTime = ROUND.round((time-first)%window);
            if (localTime == window) localTime = 0;
            
            double value = interpolator.getValue(localTime);
            fit.add(time,value);
            time = ROUND.round(time+step);
        }
        
        return fit;
        * 
        */
    }
    
    
}
