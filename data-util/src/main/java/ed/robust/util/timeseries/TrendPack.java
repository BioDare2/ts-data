/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

import ed.robust.dom.data.TimeSeries;
import ed.robust.dom.util.Pair;

/**
 *
 * @author tzielins
 */
@Deprecated
class TrendPack {
    
    protected TimeSeries data;
    protected double[] times;
    protected double[] values;
    protected double timeStep;

    protected TimeSeries detrended;
    protected double[] detrendedValues;
    
    protected TimeSeries baselineTrend;
    protected double[] baselineTrendValues;
    
    protected TimeSeries amplitudeTrend;
    protected double[] amplitudeTrendValues;
    
    protected double baselineMean;
    protected double amplitudeMean;
    
    private TrendPack(TimeSeries data,double timeStep) {
        
        this.data = data;
        Pair<double[],double[]> timesValues = TimeSeriesOperations.extractTimeAndValueTables(data);
        this.times = timesValues.getLeft();
        this.values = timesValues.getRight();
        this.timeStep = timeStep;
        if (!verifyTimeStep(times,timeStep)) throw new IllegalArgumentException("Not all the data is evenly spaced with step: "+timeStep);
    }
    
    private TrendPack(double[] times,double[] values,double timeStep) {
        this.times = times;
        this.values = values;
        this.data = TimeSeriesOperations.makeTimeSeries(times, values);
        this.timeStep = timeStep;
        if (!verifyTimeStep(times,timeStep)) throw new IllegalArgumentException("Not all the data is evenly spaced with step: "+timeStep);
    }

    public void setAmplitudeTrend(TimeSeries amplitudeTrend) {
        this.amplitudeTrend = amplitudeTrend;
        setAmplitudeTrendValues(TimeSeriesOperations.extractTimeAndValueTables(amplitudeTrend).getRight());
    }

    public void setAmplitudeTrendValues(double[] amplitudeTrendValues) {
        this.amplitudeTrendValues = amplitudeTrendValues;
        setAmplitudeMean(getMean(this.amplitudeTrendValues));
    }

    public void setBaselineTrend(TimeSeries baselineTrend) {
        this.baselineTrend = baselineTrend;
        setBaselineTrendValues(TimeSeriesOperations.extractTimeAndValueTables(baselineTrend).getRight());        
    }

    public void setBaselineTrendValues(double[] baselineTrendValues) {
        this.baselineTrendValues = baselineTrendValues;
        setBaselineMean(getMean(this.baselineTrendValues));
    }

    public void setDetrended(TimeSeries detrended) {
        this.detrended = detrended;
        setDetrendedValues(TimeSeriesOperations.extractTimeAndValueTables(detrended).getRight());        
    }

    public void setDetrendedValues(double[] detrendedValues) {
        this.detrendedValues = detrendedValues;
    }

    private void setAmplitudeMean(double amplitudeMean) {
        this.amplitudeMean = amplitudeMean;
    }

    private void setBaselineMean(double baselineMean) {
        this.baselineMean = baselineMean;
    }

    
    public double getAmplitudeMean() {
        return amplitudeMean;
    }

    public TimeSeries getAmplitudeTrend() {
        if (amplitudeTrend == null) {
            amplitudeTrend = TimeSeriesOperations.makeTimeSeries(times, amplitudeTrendValues);
        }
        return amplitudeTrend;
    }

    public double[] getAmplitudeTrendValues() {
        return amplitudeTrendValues;
    }

    public double getBaselineMean() {
        return baselineMean;
    }

    public TimeSeries getBaselineTrend() {
        if (baselineTrend == null) {
            baselineTrend = TimeSeriesOperations.makeTimeSeries(times, baselineTrendValues);
        }
        return baselineTrend;
    }

    public double[] getBaselineTrendValues() {
        return baselineTrendValues;
    }

    public TimeSeries getData() {
        return data;
    }

    public TimeSeries getDetrended() {
        if (detrended == null) {
            detrended = TimeSeriesOperations.makeTimeSeries(times, detrendedValues);
        }
        return detrended;
    }

    public double[] getDetrendedValues() {
        return detrendedValues;
    }

    public double[] getTimes() {
        return times;
    }

    public double[] getValues() {
        return values;
    }
    
    public double getTimeStep() {
        return timeStep;
    }
    
    
    protected double getMean(double[] tab) {
        if (tab == null || tab.length == 0) throw new IllegalArgumentException("Table cannot be empty");
        double sum = 0;
        int N = 0;
        
        for (double d : tab) {
            sum+=d;
            N++;
        }
        return sum/N;
    }

    protected static boolean verifyTimeStep(double[] times, double timeStep) {

        double EPS = 0.001;
        
        
        double prev = times[0]-timeStep;
        for (double time : times) {
             double stepError = time-prev-timeStep;
             if (stepError > EPS) return false;
             prev = time;
            
        }
        return true;
    }
    
}
