/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

import ed.robust.dom.data.Timepoint;
import ed.robust.dom.util.Pair;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tzielins
 */
public class PeriodicDataSource implements DataSource {
    
    
    protected DataSource source;
    double period;
    double lastTime;
    double firstTime;
    
    public PeriodicDataSource(DataSource source,double period,double lastTime) {
        if (period < 1) throw new IllegalArgumentException("Period must be >= 1");
        if (lastTime < source.getFirstTime()) throw new IllegalArgumentException("LastTime must be > than source first point");
        
        this.source = source;
        this.period = period;
        this.lastTime = lastTime;
        this.firstTime = source.getFirstTime();
    }

    @Override
    public Timepoint getFirst() {
        return source.getFirst();
    }

    @Override
    public Timepoint getLast() {
        return new Timepoint(lastTime,getValue(lastTime));
    }

    @Override
    public double getValue(double time) {
        
        time = time - firstTime;
        time = time % period;
        if (time < 0) time+= period;
        time = time + firstTime;
        return source.getValue(time);
        
    }

    @Override
    public double getFirstTime() {
        return firstTime;
    }

    @Override
    public double getLastTime() {
        return lastTime;
    }

    @Override
    public Pair<double[], double[]> getTimesAndValues(double step, ROUNDING_TYPE x_rounding) {
        return getTimesAndValues(firstTime, lastTime, step, x_rounding);
    }

    @Override
    public Pair<double[], double[]> getTimesAndValues(double start, double end, double step, ROUNDING_TYPE x_rounding) {
        
        List<Timepoint> timepoints = getTimepoints(start, end, step, x_rounding);
        double[] times = new double[timepoints.size()];
        double[] values = new double[timepoints.size()];
        
        for (int i =0;i<timepoints.size();i++) {
            times[i] = timepoints.get(i).getTime();
            values[i] = timepoints.get(i).getValue();
        }
        
        return new Pair<double[],double[]>(times,values);
    }

    @Override
    public List<Timepoint> getTimepoints(double step, ROUNDING_TYPE x_rounding) {
        return getTimepoints(firstTime, lastTime, step, x_rounding);
    }

    @Override
    public List<Timepoint> getTimepoints(double start, double end, double step, ROUNDING_TYPE x_rounding) {
        
       if (DataRounder.round(step,x_rounding) != step) throw new IllegalArgumentException("Output roudning must be wider than presission of the step");
       
       List<Timepoint> interpol = new ArrayList<Timepoint>();
       
       double fin = end;
       
       double beg = start;

       int i = 0;
       while (true) {
           double time = DataRounder.round(beg+(step*i++),x_rounding);
           if (time > fin) break;
           
           double value = getValue(time);
           
           interpol.add(new Timepoint(time,value));
       }
       
       return interpol;
        
    }
    
    
}
