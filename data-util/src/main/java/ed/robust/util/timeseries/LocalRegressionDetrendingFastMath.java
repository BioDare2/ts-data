/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
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
public class LocalRegressionDetrendingFastMath extends LocalRegressionDetrending {
    
   
    
    
    
    
    
    @Override
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
    
  
    
    
}
