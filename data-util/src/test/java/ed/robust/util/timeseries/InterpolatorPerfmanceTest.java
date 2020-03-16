/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

import java.util.List;
import ed.robust.dom.data.TimeSeries;
import ed.robust.dom.data.Timepoint;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;

/**
 *
 * @author tzielins
 */
public class InterpolatorPerfmanceTest {
    
    
    
    @Test
    public void testPerformance()
    {
        List<TimeSeries> series = makeTestData();
        System.out.println("Test data size: "+series.size());
        List<TimeSeriesInterpolator> interpolators = makeInterpolators(series);

        System.out.println("interpolators size: "+interpolators.size());
        
        for (TimeSeriesInterpolator interpolator : interpolators) {
            
            double val = interpolator.getValue(10);
            
            long sTime = System.currentTimeMillis();
            
            List<Double> steps = Arrays.asList(1.0,1.5,0.1,interpolator.getOptimalStep());
            
            for (Double step : steps) {
                List<Timepoint> list = interpolator.makeInterpolation(step);
                if (list.size() <1) System.out.println("Small list");
            }
            
            System.out.println(interpolator.getClass().getSimpleName()+" making lists: "+(System.currentTimeMillis()-sTime));
            
            double span = interpolator.getLast().getTime()-interpolator.getFirst().getTime();
            double beg = interpolator.getFirst().getTime();
            
            sTime = System.currentTimeMillis();
            double sum = 0;
            for (int i = 0;i<10000;i++) {
                double time = beg+span*Math.random();
                double value = interpolator.getValue(time);
                sum+=value/(i+1);
            }
            
            System.out.println(interpolator.getClass().getSimpleName()+" random access: "+(System.currentTimeMillis()-sTime));
        }
        
    }

    protected List<TimeSeries> makeTestData() {
        
        List<TimeSeries> series = new ArrayList<>();
        
        series.add(TSGenerator.makeDblPulse(50000, 1, 24, 5, 2));
        series.add(TSGenerator.makeStep(10000, 2.1, 24, 5, 2));
        series.add(TSGenerator.makeWave(10000, 0.5, 24, 5, 2));
        series.add(TSGenerator.makeTriangle(20000, 0.5, 24, 5, 2));

        return series;
    }

    protected List<TimeSeriesInterpolator> makeInterpolators(List<TimeSeries> series) {
        List<TimeSeriesInterpolator> inters = new ArrayList<>();
        
        for (TimeSeries data : series) {
            
            TimeSeriesInterpolator inter = new SplineTSInterpolator(data,ROUNDING_TYPE.DECY);
            inters.add(inter);
        }
        
        for (TimeSeries data : series) {
            
            TimeSeriesInterpolator inter = new BinningLinearTSInterpolator(data,ROUNDING_TYPE.DECY);            
            inters.add(inter);
        }
        
        for (TimeSeries data : series) {
            
            TimeSeriesInterpolator inter = new SplineLinearTSInterpolator(data,ROUNDING_TYPE.DECY);            
            inters.add(inter);
        }
        
        return inters;
        
    }
}
