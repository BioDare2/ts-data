/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

import ed.robust.dom.data.Timepoint;
import ed.robust.dom.util.Pair;
import java.util.List;

/**
 *
 * @author tzielins
 */
public interface DataSource {
    
    
    public Timepoint getFirst();
    
    public Timepoint getLast();
    
    public double getValue(double time);
    
    public double getFirstTime();
    
    public double getLastTime();
    
    public Pair<double[],double[]> getTimesAndValues(double step,ROUNDING_TYPE x_rounding);
    
    public Pair<double[],double[]> getTimesAndValues(double start,double end,double step,ROUNDING_TYPE x_rounding);
    
    public List<Timepoint> getTimepoints(double step,ROUNDING_TYPE x_rounding);

    public List<Timepoint> getTimepoints(double start,double end,double step,ROUNDING_TYPE x_rounding);
    
}
