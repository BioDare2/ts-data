/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

import ed.robust.dom.data.TimeSeries;
import ed.robust.dom.data.Timepoint;
import ed.robust.dom.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.commons.math3.util.FastMath;

/**
 * Class that implements detrending using local regression approach which was implemented in original matlab MFourFit method.
 * For details of the algorithm, see the mFourFit description in the doc folder.
 * <p>
 * The are few variations of the algorithm (see the particular findTrend methods for details, but in all cases:
 * <br/>- are interpolated to 1hour intervals and such are resolution is used during caclulations
 * <br/>- 'fast local regression is used' it means that not all the data points are taken into considerations but only those in some
 * local neighbourhood for which contribution is still significant (determined by the LIN_EPS parameter, for LIN_EPS 1E-5 neighbourhood has radio of 40 points)
 * @author tzielins
 */
public class LocalRegressionDetrending {
    
    /**
     * Default Bandwidth used for local linear regression.
     * The current value 15 was increesed from the original mfourfit code of 10, as noticed
     * that the baseline trends were still "bumpy", so the wider bandwith give more line like curve.
     * Orginaly the mfourfit value of 10 was used.
     */
    protected static final double DEF_BANDWIDTH = 15;
    
    protected static final int SANITIZATION_RANGE = 10;
    /**
     * Marks how many hours of data should be discarded from both ends before checking if there is significant change in amplitude.
     */
    protected static final int SMART_AMP_TIME_CUT = 10;
    /**
     * The cut off for ration between mean and max to above which the amplitude change is treated as insignificant.
     */
    protected static final double SMART_AMP_CHANGE_L_TRESHOLD = 0.84;
    /**
     * The cut off for ration between mean and min to belowe which the amplitude change is treated as insignificant.
     */
    protected static final double SMART_AMP_CHANGE_H_TRESHOLD = 1.20;
    /**
     * Precission for diagonal elements in local regression belowe which they are dismissed (hence reduce computation power). 
     * E-5 gives about 40 elements, E-6 about 50....`
     */
    protected static double LIN_EPS = 1E-5;


    /**
     * Uses linear regression of (time,value) pairs to predict value for requested given time.
     * Only subset of all the values is used as determined by the start and end index
     * @param x time (x) for which values will be predicted using linear regression
     * @param times table with times values
     * @param values table with values for each time
     * @param start start index (0-based, inclusive) of data subset that will be used, ie. htat only data from time[start] till time[end-1] will be used
     * to calculate linear regression
     * @param end end index (0-based,exclusive) of data subset that will be used, ie. that only data from time[start] till time[end-1] will be used
     * @return predicted value using linear regression, or 0 for empty tables, or value[0] for one element table
     */
    protected double predictTrendValue(double x, double[] times,double[] values, int start, int end) {
        if (times.length != values.length) throw new IllegalArgumentException("Times and values have different legnth");
        if (end < start) throw new IllegalArgumentException("End must be larger that start");
        if (end == start || end == (start+1)) return values[0];
        
        
        SimpleRegression reg = new SimpleRegression();
        for (int i = start;i<end;i++) reg.addData(times[i], values[i]);
        return reg.predict(x);
    }

    
    
    /**
     * Container for information about amplitude and baseline trends. 
     */
    public static class TrendPack {
    
        /**
         * Time resolution used for underlying DataSources
         */
        double timeStep;
        /**
         * Mean value of the baseline trend
         */
        double baselineMean;
        /**
         * Mean value of the amplitude trend
         */
        double amplitudeMean;
        /**
         * DataSource that can generate baseline trend
         */        
        DataSource baselineTrend;
        /**
         * DataSoruce that can generate amplitude trend
         */
        DataSource amplitudeTrend;
    }
    
    
    /**
     * Detrends given data using information about amplitude- and baseline-trends stored in the trendPack.
     * Detrending preserves data mean and amplitude mean. Both baseline and amplitude detrending are always performed so if
     * one of them should be omitted the trend values should be set to 0 for lack of baseline and 1 for lack of amplitude trend.
     * @param data data to be detrended
     * @param trendPack information about trends to be used (baseline and amplitude together with their mean)
     * @return Timeseries that contains the detrended data
     */
    public static TimeSeries removeTrend(Iterable<Timepoint> data,TrendPack trendPack) {
        
        TimeSeries detrended = new TimeSeries();
        
        DataSource amplitudeTrend = trendPack.amplitudeTrend;
        double amplitudeMean = trendPack.amplitudeMean;
        
        DataSource baselineTrend = trendPack.baselineTrend;
        double baseLineMean = trendPack.baselineMean;
        
        for (Timepoint tp : data) {
            double time = tp.getTime();
            double val = tp.getValue();
            val = val - baselineTrend.getValue(time);
            double factor = amplitudeMean/amplitudeTrend.getValue(time);
            val = val * factor + baseLineMean;
            Double stdErr = (tp.getStdError() == null ? null : tp.getStdError()*Math.abs(factor));
            Double stdDev = (tp.getStdDev() == null ? null : tp.getStdDev()*Math.abs(factor));
            Timepoint res = new Timepoint(time,val,stdErr,stdDev);
            detrended.add(res);            
        }
        
        
        return detrended;
    }
    
    
   /**
     * Add trends to the given data using information about amplitude- and baseline-trends stored in the trendPack.
     * Adding trend is mirror operation to removeTrend, so the outcome of addTrend(removeTrend) should be same as input data. 
     * Both information about amplitude and baseline trend are used
     * @param data data to be detrended
     * @param trendPack information about trends to be used (baseline and amplitude together with their mean)
     * @return Timeseries that contains the data with the trend added
     */
    public static TimeSeries addTrend(Iterable<Timepoint> data,TrendPack trendPack) {
        
        TimeSeries trended = new TimeSeries();
        
        DataSource amplitudeTrend = trendPack.amplitudeTrend;
        double amplitudeMean = trendPack.amplitudeMean;
        
        DataSource baselineTrend = trendPack.baselineTrend;
        double baseLineMean = trendPack.baselineMean;
        
        for (Timepoint tp : data) {
            double time = tp.getTime();
            double val = tp.getValue();
            val = val - baseLineMean;
            double factor = amplitudeTrend.getValue(time)/amplitudeMean; 
            val = val * factor;
            val = val + baselineTrend.getValue(time);
            Double stdErr = (tp.getStdError() == null ? null : tp.getStdError()*factor);
            Double stdDev = (tp.getStdDev() == null ? null : tp.getStdDev()*factor);
            trended.add(new Timepoint(time,val,stdErr,stdDev));
        }
        
        return trended;
    }
    
    
    
    
    /**
     * Finds baseline trend using the 'classic' adaptation of mfourfit algorithm.
     * Check the findCorrectedClassicTrends method for modification details
     * @param data data for which trend should be found
     * @return pack containing information about baseline trend with "idempotent" amplitude component 
     * @throws InterruptedException 
     */
    public TrendPack findBaselineTrend(TimeSeries data) throws InterruptedException {
        
        return findCorrectedClassicTrends(data, false, false);
    }
    
    
    /**
     * Finds baseline and amplitude trend using the 'classic' adaptation of mfourfit algorithm.
     * Check the findCorrectedClassicTrends method for modification details
     * @param data data for which trend should be found
     * @return pack containing information about baseline and amplitude trend 
     * @throws InterruptedException 
     */
    public TrendPack findBaseAndAmpTrend(TimeSeries data) throws InterruptedException {
        
        return findCorrectedClassicTrends(data, true, false);
    }
    
    /**
     * Finds baseline and conditionally amplitude trend using the 'classic' adaptation of mfourfit algorithm.
     * The amplitude trend is being found only if the data is longer than 30 hours threshold and the amplitude change
     * is significant enought, otherwise amplitude trend is "idempotent"
     * Check the findCorrectedClassicTrends method for modification details
     * @param data data for which trend should be found
     * @return pack containing information about baseline and amplitude trend 
     * @throws InterruptedException 
     */
    public TrendPack findBaseAndConditionalAmpTrend(TimeSeries data) throws InterruptedException {
        
        return findCorrectedClassicTrends(data, true, true);
    }
    
    
    /**
     * Finds amplitude and baseline trend using the most 'classic' adaptation of mfourfit algorithm. 
     * The only difference is that the 'fast' local regression is used (so only part of neighbourhood compared) and
     * that data are interpolated to 1-hour interval and such are used instead of all the points.
     * @param data for which trends should be found
     * @return pack containing information about baseline and amplitude trend and theri means. 
     * @throws InterruptedException 
     */
    protected TrendPack findClassicTrends(TimeSeries data) throws InterruptedException {
        
        return findClassicTrends(data, true, false);
    }

    
     /**
     * Finds amplitude and baseline trend using the most 'classic' adaptation of mfourfit algorithm. 
     * The only difference is that the 'fast' local regression is used (so only part of neighbourhood compared) and
     * that data are interpolated to 1-hour interval and such are used instead of all the points.
     * @param data data source for which trends should be found
     * @return pack containing information about baseline and amplitude trend and theri means. 
     * @throws InterruptedException 
     */   
    protected TrendPack findClassicTrends(DataSource source) throws InterruptedException {

        return findClassicTrends(source, true, false);
    }

    /**
     * Finds amplitude and baseline trend using the most 'classic' adaptation of mfourfit algorithm. 
     * The only difference is that the 'fast' local regression is used (so only part of neighbourhood compared) and
     * that data are interpolated to 1-hour interval and such are used instead of all the points.
     * @param data data source for which trends should be found
     * @param ampDetrending flag to decide if look for the amplitude trend at all, if true than amplitude detrending will be attempted unless
     * the nextParameter or data length will exclude it
     * @param automaticAmplitudeDetrending if set to true, the method tries to establish if amplitude detrending is necessary. For short
     * data the amplitude detrending has little sense so detrending is ignored, for longer the potential fold change of values is calculated
     * (using min, max and mean values of the amplitude trend) and if those are inside treshold (SMART_AMP_CHANGE_L_TRESHOLD, SMART_AMP_CHANGE_H_TRESHOLD) the amplitude trend is ignored (set to 1)
     * @return pack containing information about baseline and amplitude trend and their means. If automaticAmplitudeDetrending was set to true and no amplitude trend was needed
     * this trend will be set to constant 1
     * @throws InterruptedException 
     */
    public TrendPack findClassicTrends(TimeSeries data,boolean ampDetrending,boolean automaticAmplitudeDetrending) throws InterruptedException {
        DataSource source = new SplineTSInterpolator(data, ROUNDING_TYPE.HALF_INT);
        return findClassicTrends(source,ampDetrending,automaticAmplitudeDetrending);
    }
    
    /**
     * Finds amplitude and baseline trend using the most 'classic' adaptation of mfourfit algorithm. 
     * The only difference is that the 'fast' local regression is used (so only part of neighbourhood compared) and
     * that data are interpolated to 1-hour interval and such are used instead of all the points.
     * @param source data source for which trends should be found
     * @param ampDetrending flag to decide if look for the amplitude trend at all, if true than amplitude detrending will be attempted unless
     * the nextParameter or data length will exclude it
     * @param automaticAmplitudeDetrending if set to true, the method tries to establish if amplitude detrending is necessary. For short
     * data the amplitude detrending has little sense so detrending is ignored, for longer the potential fold change of values is calculated
     * (using min, max and mean values of the amplitude trend) and if those are inside treshold (SMART_AMP_CHANGE_L_TRESHOLD, SMART_AMP_CHANGE_H_TRESHOLD) the amplitude trend is ignored (set to 1)
     * @return pack containing information about baseline and amplitude trend and their means. If automaticAmplitudeDetrending was set to true and no amplitude trend was needed
     * this trend will be set to constant 1
     * @throws InterruptedException 
     */
    protected TrendPack findClassicTrends(DataSource source,boolean ampDetrending,boolean automaticAmplitudeDetrending) throws InterruptedException {
        
        double timeStep = 1;
        Pair<double[],double[]> timesValues = source.getTimesAndValues(timeStep, ROUNDING_TYPE.DECY);
        
        double[] times = timesValues.getLeft();
        double[] values = timesValues.getRight();
        
        double[] baselineTrendValues = getClassicBaselineTrend(times, values,timeStep);

        double[] detrended = new double[baselineTrendValues.length];
        
        for (int i = 0;i<values.length;i++) {
            double val = values[i]-baselineTrendValues[i];
            detrended[i] = val;
        }
        
        //lets check if we need amplitude trend
        double duration = times[times.length-1]-times[0];
        
        if (ampDetrending && duration <= 30) ampDetrending = false;
        //boolean ampDetrending = duration > 30;
        
        if (Thread.interrupted())
            throw new InterruptedException(Thread.currentThread().hashCode() + " have been interupted findSmartTrends before checking amplitude trend and producing final data");
        
       
        double[] amplitudeTrendValues = null;
        
        if (ampDetrending) amplitudeTrendValues = getClassicAmpTrend(times, detrended, timeStep);
        
        if (ampDetrending && automaticAmplitudeDetrending)
            ampDetrending = isSevereAmplitudeTrend(times,amplitudeTrendValues);
        
        TrendPack trendPack = new TrendPack();
        trendPack.timeStep = timeStep;
        trendPack.baselineMean = getMean(baselineTrendValues);        
        trendPack.baselineTrend = new SplineTSInterpolator(TimeSeriesOperations.makeTimepoints(times, baselineTrendValues),ROUNDING_TYPE.DECY);
        
        
        if (!ampDetrending) {
            trendPack.amplitudeMean = 1;        
            trendPack.amplitudeTrend = makeLineDataSource(times[0],times[times.length-1],0,1);
        } else {
            trendPack.amplitudeMean = getMean(amplitudeTrendValues); 
            //replace 0 mean amplitude by 1, zero amplitude can happen only in linear data, otherwise it should be > 0
            if (trendPack.amplitudeMean == 0) trendPack.amplitudeMean = 1;
            //replace 0 in treands by the amplitude mean, that way when detrending valuse will not be scaled. As the 0, can happen only
            //in point which lied on the 0 line after abs, so they should probably be not moved
            for (int i =0;i<amplitudeTrendValues.length;i++)
                if (amplitudeTrendValues[i]==0) amplitudeTrendValues[i] = trendPack.amplitudeMean;
            
            trendPack.amplitudeTrend = new SplineTSInterpolator(TimeSeriesOperations.makeTimepoints(times, amplitudeTrendValues),ROUNDING_TYPE.DECY);
        }

        return trendPack;
    }
    
    
    /**
     * Finds amplitude and baseline trend using the most 'classic' adaptation of mfourfit algorithm with sanity check. 
     * The only difference is that the 'fast' local regression is used (so only part of neighbourhood compared) and
     * that data are interpolated to 1-hour interval and such are used instead of all the points.
     * The sanity check, removes zeros from amplitude trend and replaced them by average values from neighbour points. It prevent
     * creation of spike artifacts.
     * @param data data source for which trends should be found
     * @param ampDetrending flag to decide if look for the amplitude trend at all, if true than amplitude detrending will be attempted unless
     * the nextParameter or data length will exclude it
     * @param automaticAmplitudeDetrending if set to true, the method tries to establish if amplitude detrending is necessary. For short
     * data the amplitude detrending has little sense so detrending is ignored, for longer the potential fold change of values is calculated
     * (using min, max and mean values of the amplitude trend) and if those are inside treshold (SMART_AMP_CHANGE_L_TRESHOLD, SMART_AMP_CHANGE_H_TRESHOLD) the amplitude trend is ignored (set to 1)
     * @return pack containing information about baseline and amplitude trend and their means. If automaticAmplitudeDetrending was set to true and no amplitude trend was needed
     * this trend will be set to constant 1
     * @throws InterruptedException 
     */
    public TrendPack findCorrectedClassicTrends(TimeSeries data,boolean ampDetrending,boolean automaticAmplitudeDetrending) throws InterruptedException {
        DataSource source = new SplineTSInterpolator(data, ROUNDING_TYPE.HALF_INT);
        return findCorrectedClassicTrends(source, ampDetrending, automaticAmplitudeDetrending);
    }
    
    /**
     * Finds amplitude and baseline trend using the most 'classic' adaptation of mfourfit algorithm with sanity check. 
     * The only difference is that the 'fast' local regression is used (so only part of neighbourhood compared) and
     * that data are interpolated to 1-hour interval and such are used instead of all the points.
     * The sanity check, removes zeros from amplitude trend and replaced them by average values from neighbour points. It prevent
     * creation of spike artifacts.
     * @param source data source for which trends should be found
     * @param ampDetrending flag to decide if look for the amplitude trend at all, if true than amplitude detrending will be attempted unless
     * the nextParameter or data length will exclude it
     * @param automaticAmplitudeDetrending if set to true, the method tries to establish if amplitude detrending is necessary. For short
     * data the amplitude detrending has little sense so detrending is ignored, for longer the potential fold change of values is calculated
     * (using min, max and mean values of the amplitude trend) and if those are inside treshold (SMART_AMP_CHANGE_L_TRESHOLD, SMART_AMP_CHANGE_H_TRESHOLD) the amplitude trend is ignored (set to 1)
     * @return pack containing information about baseline and amplitude trend and their means. If automaticAmplitudeDetrending was set to true and no amplitude trend was needed
     * this trend will be set to constant 1
     * @throws InterruptedException 
     */
    public TrendPack findCorrectedClassicTrends(DataSource source,boolean ampDetrending,boolean automaticAmplitudeDetrending) throws InterruptedException {
        
        double timeStep = 1;
        Pair<double[],double[]> timesValues = source.getTimesAndValues(timeStep, ROUNDING_TYPE.DECY);
        
        double[] times = timesValues.getLeft();
        double[] values = timesValues.getRight();
        
        double[] baselineTrendValues = getClassicBaselineTrend(times, values,timeStep);

        double[] detrended = new double[baselineTrendValues.length];
        
        for (int i = 0;i<values.length;i++) {
            double val = values[i]-baselineTrendValues[i];
            detrended[i] = val;
        }
        
        //lets check if we need amplitude trend
        double duration = times[times.length-1]-times[0];
        
        if (ampDetrending && duration <= 30) ampDetrending = false;
        
        if (Thread.interrupted())
            throw new InterruptedException(Thread.currentThread().hashCode() + " have been interupted findSmartTrends before checking amplitude trend and producing final data");
        
       
        double[] amplitudeTrendValues = null;
        
        if (ampDetrending) amplitudeTrendValues = getSanitizedClassicAmpTrend(times, detrended, timeStep);
        
        if (ampDetrending && automaticAmplitudeDetrending)
            ampDetrending = isSevereAmplitudeTrend(times,amplitudeTrendValues);
        
        TrendPack trendPack = new TrendPack();
        trendPack.timeStep = timeStep;
        trendPack.baselineMean = getMean(baselineTrendValues);        
        trendPack.baselineTrend = new SplineTSInterpolator(TimeSeriesOperations.makeTimepoints(times, baselineTrendValues),ROUNDING_TYPE.DECY);
        
        
        if (!ampDetrending) {
            trendPack.amplitudeMean = 1;        
            trendPack.amplitudeTrend = makeLineDataSource(times[0],times[times.length-1],0,1);
        } else {
            trendPack.amplitudeMean = getMean(amplitudeTrendValues); 
            //replace 0 mean amplitude by 1, zero amplitude can happen only in linear data, otherwise it should be > 0
            if (trendPack.amplitudeMean == 0) trendPack.amplitudeMean = 1;
            //replace 0 in treands by the amplitude mean, that way when detrending valuse will not be scaled. As the 0, can happen only
            //in point which lied on the 0 line after abs, so they should probably be not moved
            for (int i =0;i<amplitudeTrendValues.length;i++)
                if (amplitudeTrendValues[i]==0) amplitudeTrendValues[i] = trendPack.amplitudeMean;
            
            trendPack.amplitudeTrend = new SplineTSInterpolator(TimeSeriesOperations.makeTimepoints(times, amplitudeTrendValues),ROUNDING_TYPE.DECY);
        }

        return trendPack;
    }
    
    /**
     * Finds amplitude and baseline trend using the smart adaptation of mfourfit algorithm. 
     * The differences are:
     * <br/>- the 'fast' local regression is used (so only part of neighbourhood compared)
     * <br/>- that data are interpolated to 1-hour interval and such are used instead of all the points.
     * <br/>- the ends of data are treated differently than middle. Firstly the trend in the middle of data are found, this
     * trend is then used to interpolated values before and after the given data (upto to 2*BANDWITH, using only first and second half of the trend respectivelly).
     * Those values are than appended before and after the original data and 'normal' linear regression is then performed on both ends.
     * <p>That way ends are more strongly influenced by middle of the data, without it the baseline trend tends to follow directly the ends of data giving
     * resulting in skewed detrending as small amplitude which then buffs up the data during detrending.
     * @param data data source for which trends should be found
     * @param automaticAmplitudeDetrending if set to true, the method tries to establish if amplitude detrending is necessary. For short
     * data the amplitude detrending has little sense so detrending is ignored, for longer the potential fold change of values is calculated
     * (using min, max and mean values of the amplitude trend) and if those are inside treshold (SMART_AMP_CHANGE_L_TRESHOLD, SMART_AMP_CHANGE_H_TRESHOLD) the amplitude trend is ignored (set to 1)
     * @return pack containing information about baseline and amplitude trend and their means. If automaticAmplitudeDetrending was set to true and no amplitude trend was needed
     * this trend will be set to constant 1
     * @throws InterruptedException 
     * @deprecated use CorrectedClassicTrends with MESA extended input data. The MESA/COrrected classic seems to be working better
     */
    @Deprecated
    protected TrendPack findSmartTrends(TimeSeries data,boolean automaticAmplitudeDetrending) throws InterruptedException {
        
        DataSource source = new SplineTSInterpolator(data, ROUNDING_TYPE.HALF_INT);
        if (Thread.interrupted())
            throw new InterruptedException(Thread.currentThread().hashCode() + " have been interupted findSmartTrends while preparing interpolated data");
        
        return findSmartTrends(source,true,automaticAmplitudeDetrending);
    }
    
    /**
     * Finds baseline trend using the smart adaptation of mfourfit algorithm. 
     * The differences are:
     * <br/>- the 'fast' local regression is used (so only part of neighbourhood compared)
     * <br/>- that data are interpolated to 1-hour interval and such are used instead of all the points.
     * <br/>- the ends of data are treated differently than middle. Firstly the trend in the middle of data are found, this
     * trend is then used to interpolated values before and after the given data (upto to 2*BANDWITH, using only first and second half of the trend respectivelly).
     * Those values are than appended before and after the original data and 'normal' linear regression is then performed on both ends.
     * <p>That way ends are more strongly influenced by middle of the data, without it the baseline trend tends to follow directly the ends of data giving
     * resulting in skewed detrending as small amplitude which then buffs up the data during detrending.
     * @param data data source for which trends should be found
     * @return pack containing information about baseline trend, its mean and an amplitude trend which is set to 1. 
     * @throws InterruptedException 
     * @deprecated use CorrectedClassicTrends with MESA extended input data. The MESA/COrrected classic seems to be working better
     */
    @Deprecated
    public TrendPack findSmartBaselineTrend(TimeSeries data) throws InterruptedException {
        
        DataSource source = new SplineTSInterpolator(data, ROUNDING_TYPE.HALF_INT);
        TrendPack pack = findSmartTrends(source,false,false);
        //pack.amplitudeMean = 1;
        //pack.amplitudeTrend = makeLineDataSource(source.getFirstTime(), source.getLastTime(), 0, 1);
        
        return pack;
        //return convertToBaseLineTriend(pack);
    }
    
    /**
     * Creates new trend that represents only baseline part of the given trend.
     * The amplitude trend is set to 1 so it will not affect detrending
     * @param pack TrendPack from which baseline trend should be extracted
     * @return new trend that if applied will remove only baseline trend in the data
     */
    public static TrendPack convertToBaseLineTriend(TrendPack pack) {
        
        TrendPack trend = new TrendPack();
        trend.timeStep = pack.timeStep;
        trend.baselineMean = pack.baselineMean;
        trend.baselineTrend = pack.baselineTrend;
        trend.amplitudeMean = 1;
        trend.amplitudeTrend =  makeLineDataSource(trend.baselineTrend.getFirstTime(), trend.baselineTrend.getLastTime(), 0, 1);
        return trend;
        
    }
    
    
    /**
     * Finds amplitude and baseline trend using the smart adaptation of mfourfit algorithm. 
     * The differences are:
     * <br/>- the 'fast' local regression is used (so only part of neighbourhood compared)
     * <br/>- that data are interpolated to 1-hour interval and such are used instead of all the points.
     * <br/>- the ends of data are treated differently than middle. Firstly the trend in the middle of data are found, this
     * trend is then used to interpolated values before and after the given data (upto to 2*BANDWITH, using only first and second half of the trend respectivelly).
     * Those values are than appended before and after the original data and 'normal' linear regression is then performed on both ends.
     * <p>That way ends are more strongly influenced by middle of the data, without it the baseline trend tends to follow directly the ends of data giving
     * resulting in skewed detrending as small amplitude which then buffs up the data during detrending.
     * @param data data source for which trends should be found
     * @param ampDetrending flag to decide if look for the amplitude trend at all, if true than amplitude detrending will be attempted unless
     * the nextParameter or data length will exclude it
     * @param automaticAmplitudeDetrending if set to true, the method tries to establish if amplitude detrending is necessary. For short
     * data the amplitude detrending has little sense so detrending is ignored, for longer the potential fold change of values is calculated
     * (using min, max and mean values of the amplitude trend) and if those are inside treshold (SMART_AMP_CHANGE_L_TRESHOLD, SMART_AMP_CHANGE_H_TRESHOLD) the amplitude trend is ignored (set to 1)
     * @return pack containing information about baseline and amplitude trend and their means. If automaticAmplitudeDetrending was set to true and no amplitude trend was needed
     * this trend will be set to constant 1
     * @throws InterruptedException 
     * @deprecated use CorrectedClassicTrends with MESA extended input data. The MESA/COrrected classic seems to be working better
     * 
     */
    @Deprecated
    protected TrendPack findSmartTrends(DataSource source,boolean ampDetrending,boolean automaticAmplitudeDetrending) throws InterruptedException {
        
        double timeStep = 1;
        Pair<double[],double[]> timesValues = source.getTimesAndValues(timeStep, ROUNDING_TYPE.DECY);
        
        
        double[] times = timesValues.getLeft();
        double[] values = timesValues.getRight();
        
        
        double[] baselineTrendValues = getSmartBaselineTrend(times, values,timeStep);

        
        
        double[] detrended = new double[baselineTrendValues.length];
        
        for (int i = 0;i<values.length;i++) {
            double val = values[i]-baselineTrendValues[i];
            detrended[i] = val;
        }
        
        
        //lets check if we need amplitude trend
        double duration = times[times.length-1]-times[0];
        
        if (ampDetrending && duration <= 30) ampDetrending = false;
        //boolean ampDetrending = duration > 30;
        
        if (Thread.interrupted())
            throw new InterruptedException(Thread.currentThread().hashCode() + " have been interupted findSmartTrends before checking amplitude trend and producing final data");
        
       
        double[] amplitudeTrendValues = null;
        
        if (ampDetrending) amplitudeTrendValues = getSmartAmpTrend(times, detrended, timeStep);

        
        if (ampDetrending && automaticAmplitudeDetrending)
            ampDetrending = isSevereAmplitudeTrend(times,amplitudeTrendValues);
        
        TrendPack trendPack = new TrendPack();
        trendPack.timeStep = timeStep;
        trendPack.baselineMean = getMean(baselineTrendValues);
        trendPack.baselineTrend = new SplineTSInterpolator(TimeSeriesOperations.makeTimepoints(times, baselineTrendValues),ROUNDING_TYPE.DECY);
        
        
        if (!ampDetrending) {
            //System.out.println("NO AMP TREND");
            trendPack.amplitudeMean = 1;        
            trendPack.amplitudeTrend = makeLineDataSource(times[0],times[times.length-1],0,1);
        } else {
            //System.out.println("AMP TREND");            
            
            trendPack.amplitudeMean = getMean(amplitudeTrendValues); 
            //replace 0 mean amplitude by 1, zero amplitude can happen only in linear data, otherwise it should be > 0
            if (trendPack.amplitudeMean == 0) trendPack.amplitudeMean = 1;
            //replace 0 in treands by the amplitude mean, that way when detrending valuse will not be scaled. As the 0, can happen only
            //in point which lied on the 0 line after abs, so they should probably be not moved
            for (int i =0;i<amplitudeTrendValues.length;i++)
                if (amplitudeTrendValues[i]==0) amplitudeTrendValues[i] = trendPack.amplitudeMean;
            
            trendPack.amplitudeTrend = new SplineTSInterpolator(TimeSeriesOperations.makeTimepoints(times, amplitudeTrendValues),ROUNDING_TYPE.DECY);
        }
        
        

        return trendPack;
    }
    
    /**
     * Creates a data source which represents a linear trend (line y=ax+b)
     * @param start the time of the first data point (data source needs begining and end)
     * @param end the time of the last data point
     * @param a the slope as in y= ax+b
     * @param b the interception as in y = ax+b
     * @return data source that will be returning values for the line y = ax+b
     */
    public static DataSource makeLineDataSource(double start,double end,double a,double b) {
        
            TimeSeries tmp = new TimeSeries();
            tmp.add(start,a*start+b);
            tmp.add(end,a*end+b);
            
            return new BinningLinearTSInterpolator(tmp,ROUNDING_TYPE.CENTY);        
    }

    public static TrendPack makeLinearTrend(double start,double end,double a,double b) {
        
        TrendPack pack = new TrendPack();
        pack.timeStep = 1;
        
        pack.amplitudeMean = 1;
        pack.amplitudeTrend = makeLineDataSource(start,end,0,1);
        
        pack.baselineMean = 0;
        pack.baselineTrend = makeLineDataSource(start,end,a,b);
        
        return pack;        
    }
    
    public static TrendPack makeIdentityTrend(double start,double end) {
        
        TrendPack pack = new TrendPack();
        pack.timeStep = 1;
        
        pack.amplitudeMean = 1;
        pack.amplitudeTrend = makeLineDataSource(start,end,0,1);
        
        pack.baselineMean = 0;
        pack.baselineTrend = makeLineDataSource(start,end,0,0);
        
        return pack;        
    }
    
    /**
     * Checks if the given apmlitude trend should be considered as severe hency will
     * impact the data upon detrending. The check is emperically based after looking at different test
     * data. Firstly the initial and last SMART_AMP_TIME_CUT hours of data are ignored, as the trends
     * tend to be skewed at the ends and at the same time there is no need to fix only amplitude only for those bits.
     * <br/>Then the min and max values in the trend are found and compared with the mean. Since the data are scaled
     * by ampMean/ampTrend(x) then if those ratios are close to 1 (as set by SMART_AMP_CHANGE_L_TRESHOLD and SMART_AMP_CHANGE_V_TRESHOLD)
     * the amplitude detrending will not change much the data so it can be ignored and method returns false.
     * @param times times for trend values (only to ignore the first and last bits of data)
     * @param amplValues trend values
     * @return true if changes in amplitude are severe, false if data are too short or changes are too mild to affect detrending
     */
    protected boolean isSevereAmplitudeTrend(double[] times,double[] amplValues) {
        
        double s = times[0]+SMART_AMP_TIME_CUT;
        double e = times[times.length-1]-SMART_AMP_TIME_CUT;
        
        if ((e - s) < 5 ) return false;
        DescriptiveStatistics stat = new DescriptiveStatistics();
        for (int i = 0;i<times.length;i++) {
            double time = times[i];
            if (time < e || time > e) continue;
            stat.addValue(amplValues[i]);
        }
        
        if (stat.getMin() == 0) return false;
        
        double factor = stat.getMean()/stat.getMin();
        if (factor < SMART_AMP_CHANGE_L_TRESHOLD || factor > SMART_AMP_CHANGE_H_TRESHOLD) return true;
        
        if (stat.getMax() == 0) return false;
        factor = stat.getMean()/stat.getMax();
        if (factor < SMART_AMP_CHANGE_L_TRESHOLD || factor > SMART_AMP_CHANGE_H_TRESHOLD) return true;
        
        return false;
    }
    
    /**
     * Reverses the order of tables values
     * @param tab
     * @return 
     */
    protected double[] reverse(double[] tab) {
        double[] res = new double[tab.length];
        for (int i = 0;i<res.length;i++) {
            res[i] = tab[tab.length-1-i];
        }
        return res;  
    }
    
    
    /**
     * Creates new table which is concatenation of values of tables from the list. join({A,B,C}) = [A|B|C]
     * @param tabs
     * @return 
     */
    protected double[] join(double[] ... tabs) {
        
        int size = 0;
        for (double[] tab : tabs) size+=tab.length;
        
        double[] res = new double[size];
        
        int pos = 0;
        for (double[] tab : tabs) {
            System.arraycopy(tab, 0, res, pos, tab.length);
            pos+=tab.length;
        }
        return res;
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
    
    
    /**
     * Implementation of local linear regression which treates the data ends in different way than the rest of the data in order
     * to remove bias against data ends.
     * <p>The algorithm is:<br/>
     * - first regular local lin regression but on the subset of the data from (start+1.5BANDWIDTH) till (end-1.5BANDWIDTH) to get trend.
     * - the first half of the trend is then aproximate by linear regression and this aproximation is used to predict value at time (start -2*BANDWIDTH)
     * - the second half of the trend is then aproximate by linear regression and this aproximation is used to predict value at time (end +2*BANDWIDTH)
     * - the data are padded with first value from (start-2*BANDWIDTH) till (start) and with second from (end) till (end+2*BANDWIDTH), that
     * way we have extra data points before and after the original data which will reduced the contribution of actual values and the ends of data range
     * - the local lin regression is pefromed on the extended data at points (start till start+3*BANDWIDTH) and (end - 3*BANDWIDTH till end)
     * - the original data trend is replaced at is ends by the values found by the two end interpolation (so points form start till 3BAND will see the extra data before the
     * beginning.
     * 
     * @param times table with times values (the step between times should match the timeStep) as the 'smart end' part assumes evenly spaced data,
     * as well as the fast implementation. It is not validate and it stayed this way for historical reasons.
     * @param values table with values at times from times table
     * @param timeStep time difference between the consecutive points in times table, this value is used during fast implementetion of loc regrssion
     * so it should be real.
     * @param negativeOK true if negative values are acceptable for trend prediction outside the data range if false than the negative values will be replaced by 0.
     * That way it can be assured that the amplitude trend will not become negative and crosses 0 which messes up with divisions.
     * @return values obtained by local linear regression on the given input data with 'moderated' behaviour at the ends
     * @throws InterruptedException 
     */
    protected double[] smartEndsLocalLineRregression(double[] times,double[] values,double timeStep, boolean negativeOK) throws InterruptedException {
        
     
        int oneHalfBand = (int)(DEF_BANDWIDTH*1.5/timeStep);
        int twoBand = (int)(DEF_BANDWIDTH*2/timeStep);
        int threeBand = (int)(DEF_BANDWIDTH*3/timeStep);
        
        
        int start = oneHalfBand;
        int end = times.length - oneHalfBand;
        if (end <= start) {
            start = 0;
            end = times.length;
        }
        
        double[] baselineTrend = localLinRegresionSubset(times, values,timeStep,DEF_BANDWIDTH,start,end);
        double[] baselineTrendExtended = new double[times.length];
        System.arraycopy(baselineTrend, 0, baselineTrendExtended, start, baselineTrend.length);
        //System.out.println("V: "+Arrays.toString(baselineTrend));
        //System.out.println("V: "+Arrays.toString(baselineTrendExtended));
        
        //System.out.println("S:"+start+" E:"+end+" T: "+times.length);
        double leftVal = predictTrendValue(times[0]-DEF_BANDWIDTH*2,times,baselineTrendExtended,start,start+(end-start)/2);
        
        if (!negativeOK && leftVal < 0) leftVal = 0;
        
        double rightVal = predictTrendValue(times[times.length-1]+DEF_BANDWIDTH*2,times,baselineTrendExtended,start+(end-start)/2,baselineTrendExtended.length);
        if (!negativeOK && rightVal < 0) rightVal = 0;
        
        double[] leftVals = new double[twoBand];
        double[] rightVals = new double[twoBand];
        Arrays.fill(leftVals, leftVal);
        Arrays.fill(rightVals,rightVal);
        
        double[] leftTimes = new double[leftVals.length];
        double[] rightTimes = new double[rightVals.length];
        for (int i = 0;i<leftTimes.length;i++) {
            leftTimes[i] = times[0]-timeStep * (i+1);
            rightTimes[i] = times[times.length-1]+timeStep*(i+1);
        }
        reverse(leftTimes);
        
        double[] ntimes = join(leftTimes, times,rightTimes);
        double[] nvalues = join(leftVals,values,rightVals);
        
        
        int lend = threeBand;
        if (lend > times.length) lend = times.length;
        
        double[] leftBaselineTrend = localLinRegresionSubset(ntimes, nvalues,timeStep,DEF_BANDWIDTH,leftTimes.length,leftTimes.length+lend);
        
        int rstart = times.length - threeBand;
        if (rstart < lend) rstart = lend;
        double[] rightBaselineTrend = localLinRegresionSubset(ntimes, nvalues,timeStep,DEF_BANDWIDTH,leftTimes.length+rstart,leftTimes.length+times.length);
      
        double[] ans = new double[times.length];
        
        System.arraycopy(baselineTrend, 0, ans, start, baselineTrend.length);
        
        System.arraycopy(leftBaselineTrend, 0, ans, 0, leftBaselineTrend.length);
        
        System.arraycopy(rightBaselineTrend, 0, ans, ans.length-rightBaselineTrend.length, rightBaselineTrend.length);
        
        //int tmp = leftBaselineTrend.length+baselineTrend.length+rightBaselineTrend.length;
        //System.out.println("Operations: "+leftBaselineTrend.length+"+"+baselineTrend.length+"+"+rightBaselineTrend.length+"="+(tmp)+", -3B ="+(tmp-threeBand)+" org:"+times.length);
        return ans;
        
        //return join(Arrays.asList(leftBaselineTrend,baselineTrend,rightBaselineTrend));
        
    }
    
    /**
     * Finds the baseline trend in the data, using 'smart' implementation which treats data ends in a special manner.
     * The trend is assumed to be local linear regression through the data. 
     * Special manner raises from padding data before start and after end with some predicted values to reduced bias against ends.
     * See smartEndsLocalLineRregression for details
     * @param times times, must be evenly spaced with timeStep distance
     * @param values values for each time
     * @param timeStep distance between timepoints
     * @return trend in the baseline of the data.
     * @throws InterruptedException 
     */
    protected double[] getSmartBaselineTrend(double[] times,double[] values,double timeStep) throws InterruptedException {
        
        return smartEndsLocalLineRregression(times,values,timeStep,true);
        
    }
    
    
    /**
     * Finds the baseline trend in the data.
     * The trend is assumed to be local linear regression through the data. 
     * @param times times, must be evenly spaced with timeStep distance
     * @param values values for each time
     * @param timeStep distance between timepoints
     * @return trend in the baseline of the data.
     * @throws InterruptedException 
     */
    protected double[] getClassicBaselineTrend(double[] times,double[] values,double timeStep) throws InterruptedException {
        
        double[] baselineTrend = localLinRegresion(times, values,timeStep,DEF_BANDWIDTH);
                
        return baselineTrend;
    }
    
    /**
     * Finds the baseline trend in the data, using 'line fix' implementation which treats data ends in a special manner.
     * The trend is assumed to be local linear regression through the data. 
     * The 'line fix' replacing the trend values found by local lin regression, buy the values predicted by linear
     * interpolation of the trend in the pieces following the start/preceding the end.

     * @param times times, must be evenly spaced with timeStep distance
     * @param values values for each time
     * @param timeStep distance between timepoints
     * @param leftLinearDuration how much data at the begining (duration in hour) should be replaced by the linear prediction from the 10 hours after this period
     * @param rightLinearDuration how much data at the end (duration in hour) should be replaced by the linear prediction from the 10 hours before this period
     * @return
     * @throws InterruptedException 
     */
    protected double[] getLineFixBaselineTrend(double[] times,double[] values,double timeStep, double leftLinearDuration,double rightLinearDuration) throws InterruptedException {
        
        double[] baselineTrend = localLinRegresion(times, values,timeStep,DEF_BANDWIDTH);
        
        if (leftLinearDuration > 0) {
            
            int leftReplaceIndexMin = 0;
            int leftReplaceIndexMax = (int)(leftLinearDuration/timeStep);
            int leftTrendIndexMin = (int)((leftLinearDuration-2)/timeStep);
            int leftTrendIndexMax = (int)((leftLinearDuration+8)/timeStep);
                
            fillWithLinTrend(times,baselineTrend,leftReplaceIndexMin,leftReplaceIndexMax,leftTrendIndexMin,leftTrendIndexMax);
        }
        
        if (rightLinearDuration > 0) {
            int rightReplaceIndexMin = baselineTrend.length - (int)((rightLinearDuration)/timeStep);
            int rightReplaceIndexMax = baselineTrend.length;
            int rightTrendIndexMin = baselineTrend.length - (int)((rightLinearDuration+8)/timeStep);
            int rightTrendIndexMax = baselineTrend.length - (int)((rightLinearDuration-2)/timeStep);

            fillWithLinTrend(times,baselineTrend,rightReplaceIndexMin,rightReplaceIndexMax,rightTrendIndexMin,rightTrendIndexMax);
        }
        
        return baselineTrend;
    }
    
    /**
     * Pefroms linear regression on the requested data subset and uses the regression to predict values in the 'replace' range.
     * @param times table with times
     * @param values table with values, the operation will change values of that table
     * @param replaceIndexMin index of first element that which be replaced by the linear prediction
     * @param replaceIndexMax index of last element (exclusive) to which replacement will take place
     * @param trendIndexMin index of first element which value will be used to create the linear interpolation
     * @param trendIndexMax index of last element (exclusive) of the elements for linear interpolation
     */
    protected void fillWithLinTrend(double[] times, double[] values, int replaceIndexMin, int replaceIndexMax, int trendIndexMin, int trendIndexMax) {
        
 
        SimpleRegression reg;
        
        
        reg = new SimpleRegression();
        for (int i = trendIndexMin;i< trendIndexMax;i++) {
            reg.addData(times[i],values[i]);
        }
        for (int i = replaceIndexMin; i< replaceIndexMax;i++) values[i] = reg.predict(times[i]);
        
    }
    
    
    /**
     * Finds trend amplitude in the baseline detrended data, using the 'smart' modification.
     * Since data has been baseline detrended (without preserving mean) the amplitude trend is assumed to be local
     * linear regression of the absolute values. The smart modification treats the data ends in different way as described in 
     * smartEndsLocalLineRregression method.
     * @param times times of data
     * @param data values of data
     * @param step time difference between consecutive points
     * @return
     * @throws InterruptedException 
     */
    protected double[] getSmartAmpTrend(double[] times, double[] data,double step) throws InterruptedException {
        
        double[] abs = new double[data.length];
        
        for (int i = 0;i<data.length;i++) {
            if (data[i] < 0) {
                abs[i] = -data[i];
            } else {
                 abs[i] = data[i];
            }
        }
        
        return smartEndsLocalLineRregression(times,abs,step,false);
        
    }
    
    /**
     * Finds trend amplitude in the baseline detrended data.
     * Since data has been baseline detrended (without preserving mean) the amplitude trend is assumed to be local
     * linear regression of the absolute values. 
     * @param times times of data
     * @param data values of data
     * @param step time difference between consecutive points
     * @return
     * @throws InterruptedException 
     */    
    protected double[] getClassicAmpTrend(double[] times, double[] data,double step) throws InterruptedException {
        
        double[] abs = makeAbsValues(data);
        
        double[] ampTrend = localLinRegresion(times,abs,step,DEF_BANDWIDTH);
        
        
        return ampTrend;
    }
    
    /**
     * Finds trend amplitude in the baseline detrended data but performs their "sanitization" and ends to prevent artifacts.
     * Since data has been baseline detrended (without preserving mean) the amplitude trend is assumed to be local
     * linear regression of the absolute values. 
     * @param times times of data
     * @param data values of data
     * @param step time difference between consecutive points
     * @return
     * @throws InterruptedException 
     */    
    protected double[] getSanitizedClassicAmpTrend(double[] times, double[] data,double step) throws InterruptedException {
        
        double[] abs = makeAbsValues(data);
        
        double[] ampTrend = localLinRegresion(times,abs,step,DEF_BANDWIDTH);
        
        ampTrend = sanitizeAmpTrend(ampTrend,abs,SANITIZATION_RANGE);
        return ampTrend;
    }
    
    /**
     * Corrects amplitude trend to prevent cration of artifacts by removing 0 and below zero values.
     * Method operates only at the starting and end range, leaving middle of the data unchanged as it was observed
     * that artifacts happen only at ends.
     * @param ampTrend predicted trend that should be corrected
     * @param orgAmp original amplitude values (expected to be >=0)
     * @param range correction range: number of first and last points to be checked
     * @return 
     */
    protected double[] sanitizeAmpTrend(double[] ampTrend,double[] orgAmp,int range) {
        
        
        double[] corrected = Arrays.copyOf(ampTrend, ampTrend.length);
        
        int last = Math.min(range, corrected.length);
        for (int ix = 0;ix<last;ix++) {
            
            if (corrected[ix] <= 0) corrected[ix] = correctAmpForZero(ix,ampTrend,orgAmp);
        }
        
        int first = Math.max(0, corrected.length-range);
        for (int ix =first;ix<corrected.length;ix++) {
            if (corrected[ix] <= 0) corrected[ix] = correctAmpForZero(ix,ampTrend,orgAmp);
        }
        
        return corrected;
        
    }
    
    /**
     * Computes new value for amp trend in the given position using the neighbouring points.
     * Looks at points in +-2 positions and averages their values, either using the ampTrend input
     * (if it is larger than zero), or the orginal amp value if the trend one was negative.
     * 
     * @param position index in ampTrend (orgAmp) for which new value should be computed
     * @param ampTrend the predicted ampTrend values, they can in principle go down below 0 for which correction has to be applied
     * @param orgAmp the orginal amplitude values, they are expected to be >=0 as they should be calculated using Math.abs
     * @return new value for the given possitin based on "smart" averaging of the neighbouring data.
     */
    protected final double correctAmpForZero(int position, double[] ampTrend, double[] orgAmp) {
        double val = 0;
        int n = 0;
        
        final int st = Math.max(0, position-2);
        final int en = Math.min(position+3,ampTrend.length);
        for (int i =st;i<en;i++) {
            val +=ampTrend[i]>0 ? ampTrend[i] : orgAmp[i];
            n++;
        }
        
        val = val /n;
        if (val == 0) val = orgAmp[position];
        return val;
    }
    
    
    protected double[] makeAbsValues(double data[]) {
        double[] abs = new double[data.length];
        for (int i = 0;i<data.length;i++) {
            if (data[i] < 0) {
                abs[i] = -data[i];
            } else {
                 abs[i] = data[i];
            }
        }
        return abs;
    }
    
    /**
     * Finds trend amplitude in the baseline detrended data, using the 'line fix modification.
     * Since data has been baseline detrended (without preserving mean) the amplitude trend is assumed to be local
     * linear regression of the absolute values. 
     * The line fix checks the end of the trends and does the weighted averaging with 2 following (at beginning) or 2 proceeding (at the end)
     * data points to slow down the changes, also only positive values are taken into account which prevents the trend from crossing 0 which 
     * messes up with the dividing
     * @param times times of data
     * @param data values of data
     * @param step time difference between consecutive points
     * @param leftAvgDuration duration (hours) of period at the beginning of data in which the averaging will take place
     * @param rightAvgDuration duration (hours) of period at the end of data in which the averaging will take place
     * @return
     * @throws InterruptedException 
     */
    protected double[] getLineFixAmpTrend(double[] times, double[] data,double step,double leftAvgDuration,double rightAvgDuration) throws InterruptedException {
        
        double[] abs = new double[data.length];
        
        for (int i = 0;i<data.length;i++) {
            if (data[i] < 0) {
                abs[i] = -data[i];
            } else {
                 abs[i] = data[i];
            }
        }
        
        double[] ampTrend = localLinRegresion(times,abs,step,DEF_BANDWIDTH);
        
        
        int leftReplaceIndexMin = 0;
        int leftReplaceIndexMax = (int)(leftAvgDuration/step);
        
        for (int i = leftReplaceIndexMax;i >= leftReplaceIndexMin;i--) {
            
            double sum = 0;
            double weight = 0;
            sum+= 0.5*ampTrend[i+1];
            weight+=0.5;
            
            sum+= 0.25*ampTrend[i+2];
            weight+=0.25;
            if (ampTrend[i] > 0) {
                sum+=ampTrend[i];
                weight+=1;
            }
            ampTrend[i] = sum/weight;
        }
        
        int rightReplaceIndexMin = data.length - (int)(rightAvgDuration/step);
        int rightReplaceIndexMax = data.length;
        
        for (int i = rightReplaceIndexMin;i<rightReplaceIndexMax;i++) {
            
            double sum = 0;
            double weight = 0;
            
            sum+=0.25*ampTrend[i-2];
            weight+=0.25;
            sum+=0.5*ampTrend[i-1];
            weight+=0.5;
            
            if (ampTrend[i] > 0) {
                sum+=ampTrend[i];
                weight+=1;
            }
            ampTrend[i] = sum/weight;
        }
        
        
        
        /*for (int i =0;i<ampTrend.length;i++)
            ampTrend[i] *= sign[i];
        */
        return ampTrend;
    }
    
    
    /**
     * Performs 'fast' local linear regression on the requested subset of the given data.
     * The fast implementation assumes that times are evenly spaced with the step distance. It then determines
     * the size of neighbourhood in which local linear regression should be peformed by checking the values of diagonal
     * elements against precision defined by LIN_EPS. <br/>
     * Although the data may be longer the local regression is calculated only for points in the requested range, but such calculation
     * will also take into account the neighbours which falls outside this range!!!.
     * @param times times for data, must be evenly spaced with step distance
     * @param values values for the times
     * @param step distance between consecutive time points
     * @param bandwidth requested bandwith for local linear regression (the larger the value the more points will contribute to local regression)
     * @param start index (0-based, inclusive) of first element for which regression should be caclulated
     * @param end index (0-based) of first element for which regrssion should not be calculated, so the regression is done
     * from start inclusive till end exclusive
     * @return values from local linear regression, first value will be for point at time[start], next at time[start+1] last one for time[end-1].
     * So the result table has reduced size compering with input data as holds only (end-start) points.
     * @throws InterruptedException 
     */
    protected double[] localLinRegresionSubset(double[] times, double[] values,double step,double bandwidth,int start,int end) throws InterruptedException {
 
        
        if (times.length != values.length) throw new IllegalArgumentException("Times and values must have same size");
        if (end <= start ) return new double[0]; //throw new IllegalArgumentException("End must be larger than start");
        
        
        RealMatrix VAL = new Array2DRowRealMatrix(values);
        
        RealMatrix T = new Array2DRowRealMatrix(times.length,2);
        for (int i=0;i<times.length;i++) {
            T.setEntry(i, 0, 1);
            T.setEntry(i,1,times[i]);
        }
        
        

        double wcoeff=FastMath.sqrt(1/(2*FastMath.PI*bandwidth*bandwidth));
        double hv=2*bandwidth*bandwidth;
        
        
        List<Double> diagElements = new ArrayList<Double>();

        for (int i = 0; i<values.length;i++) {
            if (Thread.interrupted())
                throw new InterruptedException(Thread.currentThread().hashCode() + " have been interupted during localLinRegresion, diagSetup");
            
            double v = wcoeff*FastMath.exp(-(i*step)*(i*step)/hv);
            if (v > LIN_EPS)
                diagElements.add(v);
            
        }
        
        Double[] diags = diagElements.toArray(new Double[diagElements.size()]);
            
        //System.out.println("DIAG sizie: "+diags.length);
        
        RealMatrix[] bI = new RealMatrix[times.length];
        for (int i = start;i<end;i++) {
            
            if (Thread.interrupted())
                throw new InterruptedException(Thread.currentThread().hashCode() + " have been interupted during localLinRegresion, inside local coef calculations");
            
            int lowest = i - diags.length+1;
            int highest = i +diags.length-1;
            if (lowest < 0) lowest = 0;
            if (highest >= times.length) highest = times.length-1;
            int size = highest - lowest+1;
            
            //System.out.println("WSize: "+size);
            
            RealMatrix W = new Array2DRowRealMatrix(size,size);
            
            for (int k = 0; k<size;k++) W.setEntry(k,k,diags[FastMath.abs(i-(lowest+k))]);
            
            RealMatrix TCut = T.getSubMatrix(lowest, highest, 0, 1);
            RealMatrix TTCut = TCut.transpose();
            
            //RealMatrix TE = TT.multiply(W); //te=TTW
            RealMatrix TECut = TTCut.multiply(W);
            
            RealMatrix VALCut = VAL.getSubMatrix(lowest, highest, 0, 0);
            
            if (Thread.interrupted())
                throw new InterruptedException(Thread.currentThread().hashCode() + " have been interupted during localLinRegresion, before solving local coeficients matrix");
                        
            //bI[i] = inverse(TE.multiply(T)).multiply(TE.multiply(VAL));
            bI[i] = inverse(TECut.multiply(TCut)).multiply(TECut.multiply(VALCut));
            //System.out.println(bI[i]);
        }
        
        //System.out.println(bI[0]);
        
        
        double[] localTrend = new double[times.length];
        for (int i=start;i<end;i++)
            localTrend[i]=bI[i].getEntry(0, 0)+bI[i].getEntry(1, 0)*times[i];
        
        //System.out.println(Arrays.toString(localTrend));
        return Arrays.copyOfRange(localTrend,start,end);
        
    }
    
    /**
     * Performs 'fast' local linear regression on the requested subset of the given data.
     * The fast implementation assumes that times are evenly spaced with the step distance. It then determines
     * the size of neighbourhood in which local linear regression should be performed by checking the values of diagonal
     * elements against precision defined by LIN_EPS. <br/>
     * @param times times for data, must be evenly spaced with step distance
     * @param values values for the times
     * @param step distance between consecutive time points
     * @param bandwidth requested bandwith for local linear regression (the larger the value the more points will contribute to local regression)
     * @return values of linear regression.
     * @throws InterruptedException 
     */
    protected double[] localLinRegresion(double[] times, double[] values,double step,double bandwidth) throws InterruptedException {
 
        return localLinRegresionSubset(times, values, step, bandwidth, 0, times.length);
        /*
        if (times.length != values.length) throw new IllegalArgumentException("Times and values must have same size");
        
        
        
        RealMatrix VAL = new Array2DRowRealMatrix(values);
        
        RealMatrix T = new Array2DRowRealMatrix(times.length,2);
        for (int i=0;i<times.length;i++) {
            T.setEntry(i, 0, 1);
            T.setEntry(i,1,times[i]);
        }
        
        

        double wcoeff=FastMath.sqrt(1/(2*FastMath.PI*bandwidth*bandwidth));
        double hv=2*bandwidth*bandwidth;
        
        
        List<Double> diagElements = new ArrayList<Double>();

        for (int i = 0; i<values.length;i++) {
            if (Thread.interrupted())
                throw new InterruptedException(Thread.currentThread().hashCode() + " have been interupted during localLinRegresion, diagSetup");
            
            double v = wcoeff*FastMath.exp(-(i*step)*(i*step)/hv);
            if (v > LIN_EPS)
                diagElements.add(v);
            
        }
        
        Double[] diags = diagElements.toArray(new Double[diagElements.size()]);
            

        
        RealMatrix[] bI = new RealMatrix[times.length];
        for (int i = 0;i<times.length;i++) {
            
            if (Thread.interrupted())
                throw new InterruptedException(Thread.currentThread().hashCode() + " have been interupted during localLinRegresion, inside local coef calculations");
            
            int lowest = i - diags.length+1;
            int highest = i +diags.length-1;
            if (lowest < 0) lowest = 0;
            if (highest >= times.length) highest = times.length-1;
            int size = highest - lowest+1;
            
            //System.out.println("WSize: "+size);
            
            RealMatrix W = new Array2DRowRealMatrix(size,size);
            
            for (int k = 0; k<size;k++) W.setEntry(k,k,diags[FastMath.abs(i-(lowest+k))]);
            
            RealMatrix TCut = T.getSubMatrix(lowest, highest, 0, 1);
            RealMatrix TTCut = TCut.transpose();
            
            //RealMatrix TE = TT.multiply(W); //te=TTW
            RealMatrix TECut = TTCut.multiply(W);
            
            RealMatrix VALCut = VAL.getSubMatrix(lowest, highest, 0, 0);
            
            if (Thread.interrupted())
                throw new InterruptedException(Thread.currentThread().hashCode() + " have been interupted during localLinRegresion, before solving local coeficients matrix");
                        
            //bI[i] = inverse(TE.multiply(T)).multiply(TE.multiply(VAL));
            bI[i] = inverse(TECut.multiply(TCut)).multiply(TECut.multiply(VALCut));
            //System.out.println(bI[i]);
        }
        
        //System.out.println(bI[0]);
        
        
        double[] localTrend = new double[times.length];
        for (int i=0;i<times.length;i++)
            localTrend[i]=bI[i].getEntry(0, 0)+bI[i].getEntry(1, 0)*times[i];
        
        //System.out.println(Arrays.toString(localTrend));
        return localTrend;
        */
    }
    
    protected RealMatrix inverse(RealMatrix mat) {
        //return new LUDecomposition(mat).getSolver().getInverse();
        return new QRDecomposition(mat).getSolver().getInverse();
    }
    
    
}
