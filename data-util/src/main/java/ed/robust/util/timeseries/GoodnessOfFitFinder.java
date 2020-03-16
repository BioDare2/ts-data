/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

import ed.robust.dom.data.TimeSeries;
import ed.robust.dom.data.Timepoint;
import ed.robust.dom.util.Pair;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.util.FastMath;

/**
 * Class that provides measure for goodness of fit for periodic data. The goodness of fit is defined as the ratio between fit-data discrepancy
 * and polynomial-data discrepancy. Depending on the configuration polynomial fit can be either linear or cubic, the cubic is prefered and the 
 * default. 
 * <p>There different approaches to measuring the discrepancy between fit and the data. The most common is based
 * on sum of the square errors (difference between value for data and the fit) in each data point: (SQR_SUM), however this method penalases too strongly periodic fits without trend for
 * trended data, as the fit oscilations may follow the oscilations of the data, but due to the trend the actual values do not, while polynomial trend
 * will follow only the trend and may have smaller error. ABS_SUM is using absolute values of errors instead of square, and is less influenced by an outlier.
 * ABS_MEAN_PL_STD is the recommended extension of this approach, firstly notice the ration between sum of abs errors is the same as reation between mean abs errors 
 * as the number of points is the same. The ABS_MEAN_PL_STD is hence using the MEAN value plus its standard deviation, thus penalising additionally
 * for high variance in the errors, most likely due to 'oscilating' patterns in those.</p>
 * <p>The fit and the data do not to have the same time points, as the fit will be spline interpolated to coincide with the data.</p>
 * <p>The implementation is thread safe</p>
 * @author tzielins
 */
public class GoodnessOfFitFinder {

    /**
     * Constant that defines the close to 0 values to prevent division by zero.
     */
    protected static final double ALMOST_ZERO = 1E-300;
    /**
     * Upper limit for GOF of values, if the GOF is worse than the constant ie higher, it is truncated to this value.
     */
    public static final double VERY_BAD = 1;
    
    /**
     * Type of polynomial fit to be used.
     */
    public enum REF_TYPE {
        /**
         * Linear regression
         */
        LINEAR,
        /**
         * Cubis polynomial (recommended)
         */
        POLY_3;
    }
    
    /**
     * The way how the discrepancy between fit and data is measured.
     */
    public enum GOF_METHOD {
        /**
         * Is based on sum of square errors in each points.
         */
        SQR_SUM,
        /**
         * Is based on sum of abs errors in each points.
         */
        ABS_SUM,
        /**
         * Is based on the mean abs error plus its standard deviation.
         */
        ABS_MEAN_PL_STD;
    }
    
    protected final REF_TYPE refType;
    protected final GOF_METHOD gofMethod;
    
    /**
     * Creates the instance of GOF fineder with the default configations which is cubic reference fit, 
     * and mean+std errors ratio.
     */
    public GoodnessOfFitFinder() {
        this(REF_TYPE.POLY_3,GOF_METHOD.ABS_MEAN_PL_STD);
    }
    
    /**
     * Creates new instance of the finder which is based on the provided configuration options: the reference fit and method for error estimation.
     * @param refType the reference fit type
     * @param gofMethod the method for calculating errors between fit and the data
     */
    protected GoodnessOfFitFinder(REF_TYPE refType,GOF_METHOD gofMethod) {
        if (refType == null || gofMethod == null) throw new IllegalArgumentException("GOF method and ref type cannot be null");
        this.refType = refType;
        this.gofMethod = gofMethod;
    }
    
    /**
     * Calculates GOF value as defined by the configuration using the data and the fit. The data and fit do not have to
     * coincide as the fit is spline interpolated to overlap with  the data.
     * @param data the reference data for which the fit was obtained
     * @param fit the fit, which goodness should be calculated
     * @return the calculated GOF value in the range [0,VERY_BAD].
     */ 
    public double findGOF(TimeSeries data,TimeSeries fit) {
        
        if (fit.isEmpty()) return VERY_BAD;
        
        DataSource fitSource = new SplineTSInterpolator(fit,ROUNDING_TYPE.CENTY);
        
        return findGOF(data,fitSource);
    }

    
    /**
     * Calculates GOF value as defined by the configuration using the data and the fit.
     * @param data the reference data for which the fit was obtained
     * @param fit the fit, for which goodness should be calculated
     * @return the calculated GOF value in the range [0,VERY_BAD].
     */
    public double findGOF(TimeSeries data, DataSource fit) {
        
        double[] dataValues = data.getValues();//TimeSeriesOperations.extractTimeAndValueTables(data).getRight();
        
        double[] refValues = findReferenceValues(data);
        double[] fitValues = getFitValues(data,fit);
        
        return calculateGOF(dataValues,refValues,fitValues);
        
    }
    
    /**
     * Calculates GOF value as defined by the configuration using the data and the fit. 
     * The reference polynomial fit is obtained assuming evenly spaced points.
     * @param dataValues the values from original data
     * @param fitValues the vaules of the fit
     * @return the calculated GOF value in the range [0,VERY_BAD].
     */
    public double findGOF(double[] dataValues, double[] fitValues) {
        if (dataValues.length != fitValues.length) throw new IllegalArgumentException("Mismatch between data and fit lenght: "+dataValues.length+"!="+fitValues.length);
        double[] refValues = findReferenceValues(dataValues);
        
        return calculateGOF(dataValues,refValues,fitValues);
        
    }
    
    /**
     * Performs the calculations based on the class configuarion
     * @param dataValues org values at each point
     * @param refValues the polynomial fit values at each point
     * @param fitValues the fit values for which GOF must be calculated
     * @return the calculated GOF value in the range [0,VERY_BAD].
     */
    protected double calculateGOF(double[] dataValues,double[] refValues,double[] fitValues) {
        
        if (dataValues.length == 0) return 0;
        
        switch(gofMethod) {
            case ABS_SUM: return calculateAbsGOF(dataValues,refValues,fitValues);
            case ABS_MEAN_PL_STD: return calculateStatAbsGOF(dataValues,refValues,fitValues);
            case SQR_SUM: return calculateSqrGOF(dataValues,refValues,fitValues);
            default: throw new IllegalArgumentException("Unsuported GOF type: "+gofMethod);
        }        
    }
    
    
    protected double calculateSqrGOF(double[] dataValues,double[] refValues,double[] fitValues) {
        
        double refSE = calculateSumSqrError(dataValues,refValues);
        double fitSE = calculateSumSqrError(dataValues,fitValues);
        
        return normalizeGOF(fitSE, refSE);        
    }
    
    protected double calculateAbsGOF(double[] dataValues,double[] refValues,double[] fitValues) {
        
        double refSE = calculateSumAbsError(dataValues,refValues);
        double fitSE = calculateSumAbsError(dataValues,fitValues);
        
        return normalizeGOF(fitSE, refSE);        
    }
    
    protected double calculateStatAbsGOF(double[] dataValues,double[] refValues,double[] fitValues) {
        
        double refSE = calculateStatAbsError(dataValues,refValues);
        double fitSE = calculateStatAbsError(dataValues,fitValues);
        
        return normalizeGOF(fitSE, refSE);        
    }
    
    protected double normalizeGOF(double fitVal,double refVal) {
        
        //System.out.println("REFSE "+refSE+" FSE:"+fitSE);
        if (refVal <= ALMOST_ZERO) {
            if (fitVal <= ALMOST_ZERO) return 0;
            else return VERY_BAD;
        }
        
        double gof = fitVal/refVal;
        if (gof > VERY_BAD) return VERY_BAD;
        gof = SmartDataRounder.round(gof);
        return gof;
    }

    protected double calculateSumSqrError(double[] data, double[] ref) {
        
        double sum = 0;
        for (int i = 0;i<data.length;i++) {
            double dif = data[i]-ref[i];
            sum+=dif*dif; 
        }
        return sum;
    }
    
    protected double calculateSumAbsError(double[] data, double[] ref) {
        
        double sum = 0;
        for (int i = 0;i<data.length;i++) {
            double dif = data[i]-ref[i];
            sum+= FastMath.abs(dif); 
        }
        return sum;
    }
    
    protected double calculateStatAbsError(double[] data, double[] ref) {
        
        if (data.length == 0) return 0;
        SummaryStatistics stat = new SummaryStatistics();
        
        for (int i = 0;i<data.length;i++) {
            double dif = data[i]-ref[i];
            stat.addValue(FastMath.abs(dif)); 
        }
        return stat.getMean()+stat.getStandardDeviation();
    }
    
    

    protected double[] getFitValues(TimeSeries data, DataSource fit) {
        
        double[] fitValues = new double[data.size()];
        int i = 0;
        for (Timepoint tp : data) fitValues[i++] = fit.getValue(tp.getTime());
        
        return fitValues;
    }

    
    protected double[] findReferenceValues(double[] values) {
        
        double[] times = new double[values.length];
        for (int i = 0;i<times.length;i++) times[i]=i;
        return findReferenceValues(new TimeSeries(times, values));
        
    }
    
    protected double[] findReferenceValues(TimeSeries data) {
        
        return findReferenceValues(data,refType);
    }
    
    protected double[] findReferenceValues(TimeSeries data, REF_TYPE refType) {
        
        switch (refType) {
            case LINEAR: {
                    Pair<Double,Double> slopeInter = TimeSeriesOperations.getLinTrendParams(data);
                    double a = slopeInter.getLeft();
                    double b = slopeInter.getRight();
                    double[] values = new double[data.size()];
                    int i = 0;
                    for (Timepoint tp: data) values[i++] = a*tp.getTime()+b;
                    return values; }
            case POLY_3: {
                    PolynomialFunction trend = TimeSeriesOperations.getPolyTrendFunction(data, 3);
                    double[] values = new double[data.size()];
                    int i = 0;
                    for (Timepoint tp: data) values[i++] = trend.value(tp.getTime());
                    return values; }
            default: throw new IllegalArgumentException("Not supported refType: "+refType.name());
        }
    }
    
}
