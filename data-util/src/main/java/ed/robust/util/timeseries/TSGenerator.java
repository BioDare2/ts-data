/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

import ed.robust.dom.data.TimeSeries;

/**
 * Utility class that generates timeseries
 * @author Zielu
 */
public class TSGenerator extends TimeSeriesOperations {
    
    
    

    
        

    /**
     * Creates data with linear function.
     * @param N number of timepoints to generate
     * @param step distance between time points in hours
     * @param a line slope
     * @param b line interception
     * @return 
     */
    public static TimeSeries makeLine(int N,double step,double a,double b) {
        
        if (N <=0 || step <=0) throw new IllegalArgumentException("N, Step must be > 0");
        
        TimeSeries result = new TimeSeries();
        
        for (int i = 0;i<N;i++) {
            double x = i*step;
            double y = a*x+b;
            result.add(x,y);
        }
        
        return result;
        
    }

    
    /**
     * Creates data with cos function of given period, phase and amplitude. Generates N points of data which are evenly distibuted
     * every 'step' starting from 0 
     * @param N number of timepoints to generate
     * @param step distance between time points in hours
     * @param period requested period in hour
     * @param phase requested phase as cos(w(x-phase))
     * @param amplitude requested amplituded
     * @return timeseries representing requested cos
     */
    public static TimeSeries makeCos(int N,double step,double period,double phase,double amplitude) {
        
        if (N <=0 || step <=0 || period <=0 || amplitude <=0) throw new IllegalArgumentException("N, Step, period, amplitude must be > 0");
        
	double omega = 2*Math.PI/period;
        TimeSeries result = new TimeSeries();
        
        for (int i = 0;i<N;i++) {
            double x = i*step;
            double y = amplitude*Math.cos(omega*(x-phase));
            result.add(x,y);
        }
        
        return result;
        
    }
    
    /**
     * Creates data with cos function of given period and amplitude. Generates N points of data which are evenly distibuted
     * every 'step' starting from 0 
     * @param N number of timepoints to generate
     * @param step distance between time points in hours
     * @param period requested period in hour
     * @param amplitude requested amplituded
     * @return timeseries representing requested cos
     */
    public static TimeSeries makeCos(int N,double step,double period,double amplitude) {
        return makeCos(N, step, period,0, amplitude);
    }
    
    
    /**
     * Creates data with sin function of given period, phase and amplitude. Generates N points of data which are evenly distibuted
     * every 'step' starting from 0 
     * @param N number of timepoints to generate
     * @param step distance between time points in hours
     * @param period requested period in hour
     * @param phase requested phase as sin(w(x-phase))
     * @param amplitude requested amplituded
     * @return timeseries representing requested cos
     */
    public static TimeSeries makeSin(int N,double step,double period,double phase,double amplitude) {
        
        if (N <=0 || step <=0 || period <=0 || amplitude <=0) throw new IllegalArgumentException("N, Step, period, amplitude must be > 0");
        
	double omega = 2*Math.PI/period;
        TimeSeries result = new TimeSeries();
        
        for (int i = 0;i<N;i++) {
            double x = i*step;
            double y = amplitude*Math.sin(omega*(x-phase));
            result.add(x,y);
        }
        
        return result;
        
    }
       

    /**
     * Creates data with wave function of the given period, amplitude. Wave function, raises smothly (like sin)
     * till period is reached and then drops to 0.
     * @param N number of timepoints to generate
     * @param step distance between time points in hours
     * @param period requested period in hour
     * @param amplitude requested amplitude
     * @return 
     */
    public static TimeSeries makeWave(int N,double step,double period,double amplitude) {
        
        return makeWave(N, step, period, 0, amplitude);
    }
    
    /**
     * Creates data with wave function of the given period, amplitude. Wave function, raises smothly (like sin)
     * till period is reached and then drops to 0.
     * @param N number of timepoints to generate
     * @param step distance between time points in hours
     * @param period requested period in hour
     * @param phase requested phase (peak time in hour)
     * @param amplitude requested amplitude
     * @return 
     */
    public static TimeSeries makeWave(int N,double step,double period,double phase,double amplitude) {
        amplitude *=2; //cause we go only from 0 till amplitude
        double omega = 2*Math.PI/period/4;
        TimeSeries result = new TimeSeries();
        //double shift = period-phase;
        for (int i=0;i<N;i++) {
            double x = step*i;
            double x2 = x%period;
            x2 = x2 + period - phase;
            if (x2 > period) x2 -= period;
            double y = amplitude*Math.sin(omega*x2);
            result.add(x, y);
        }
        
        return result;
    }

    /**
     * Creates data with squre pulse repeating with the given period, amplitude. 
     * Generates N points from 0 with step distance between
     * @param N number of timepoints to generate
     * @param step distance between time points in hours
     * @param period requested period in hour
     * @param phase requested period in hour
     * @param amplitude requested amplitude
     * @return 
     */
    public static TimeSeries makeStep(int N,double step,double period,double phase,double amplitude) {
 
	double amp = amplitude;
        double shift = phase-period/4.0;
        TimeSeries result = makeSin(N,step,period,shift,amp);
        
        for (int i = 3;i<15;i+=2) {
            result = sum(result,makeSin(N,step,period/i,shift,amp/i));
        }
        
        return result;
        
    }
 
    /**
     * Creates data with triangle pulse repeating with the given period, amplitude. 
     * @param N number of timepoints to generate
     * @param step distance between time points in hours
     * @param period requested period in hour
     * @param phase peak time in hours
     * @param amplitude requested amplitude
     * @return 
     */
    public static TimeSeries makeTriangle(int N,double step,double period,double phase,double amplitude) {
        
        TimeSeries result = new TimeSeries();
        for (int i = 0;i<N;i++) {
            double x = step*i;
            double locX = x % period;
            locX += period/3-phase;
            if (locX > period) locX -= period;
            double y = bendPiece(locX,period,amplitude);
            result.add(x,y);
        }
        
        return result;         
    }
    
      
    
    /**
     * Calculates value of triangle function. The triangle function raised from 0 to 2*amp in the range 0 till end/3 and then drop back to 0
     * (but 2 times slower)
     * @param x value of x (from 0 till end)
     * @param end end of the range
     * @param amp amplitude (defined as half of values range)
     * @return 
     */
    protected static double bendPiece(double x,double end, double amp) {
        
        double bendPoint = end/3;
        amp*=2; //cause we go from 0 till amp only
        
        if (x<=bendPoint) return (amp*x/bendPoint);
        
        x = x - bendPoint;
        double a = amp/(end-bendPoint);
        double y = amp-x*a;
        if (y <0) return 0;
        return y;
    }
    
    
    /**
     * Creates data with one peak which has gaussian shape, height = 2*amplitude and it is centered around peak.
     * @param N number of timepoints to generate
     * @param step distance between time points in hours
     * @param period requested period in hour
     * @param phase peak time in hours
     * @param amplitude requested amplitude which is half of the peak height (casue data goes from 0 till peak hight)
     * @return 
     */
    public static TimeSeries makePulse(int N, double step, double period,double phase,double amplitude) {
        
        double height = amplitude * 2; //cause we go from 0 to amp only
         
        
        double width = period/7;
        
        return makeGausian(N, step, period, height, phase, width);
        
       
    }
    
    public static TimeSeries makePulse(int N,double step,double period,double amplitude) {
        
        return makePulse(N,step,period,0,amplitude);
        
    }
    
    /**
     * Created data with a gausian pulse which has a shoulder band
     * @param N number of timepoints to generate
     * @param step distance between time points in hours
     * @param period requested period in hour
     * @param phase peak time in hours
     * @param amplitude requested amplitude which is half of the peak height (casue data goes from 0 till peak hight)
     * @return 
     */
    public static TimeSeries makeDblPulse(int N,double step,double period,double phase,double amplitude) {
        
        double height = amplitude * 2; //cause we go from 0 to amp only
        double width = period / 9;
        
        TimeSeries t1 = makeGausian(N,step,period,height,phase,width);
        
        height = height/2.5;
        phase = phase + period/3;
        if (phase > period) phase -= period;
        width = period / 10;

        TimeSeries t2 = makeGausian(N,step,period,height,phase,width);
        
        //return t2;
        return sum(t1,t2);
        
    }
    
    /**
     * Creates data wich periodic gausian pulse, of the given height, phase and peak width
     * @param N number of timepoints to generate
     * @param step distance between time points in hours
     * @param period requested period in hour
     * @param height how tall is the peak (2*amplitude)
     * @param phase where the peak should be centered
     * @param width width of the peak
     * @return 
     */
    public static TimeSeries makeGausian(int N,double step,double period,double height, double phase, double width) {
        
        TimeSeries result = new TimeSeries();
        
        double omega = -2*width*width;
       
        for (int i = 0;i<N;i++) {
            double x = i*step;
            double xS = (x-phase) % period;
            if (xS > period/2) xS-= period;
            
            double b = xS*xS;
        
            double y = height*Math.exp(b/omega);
            
            result.add(x,y);
        }
        return result;
    }
    
    


     
    
     
}
