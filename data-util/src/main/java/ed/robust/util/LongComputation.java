/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util;

import ed.robust.dom.data.TimeSeries;
import ed.robust.dom.data.Timepoint;
import ed.robust.dom.util.Pair;
import ed.robust.util.timeseries.TimeSeriesFileHandler;
import ed.robust.util.timeseries.TimeSeriesOperations;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;

/**
 * Implementation of sleep or wait function that actively occupies processor for the given time.
 * <p>On its first run per day it tries to calibrates itself to determine how many repeats of internal calculations
 * are needed to occupy processor for specific time.
 * @author Zielu
 */
public class LongComputation {
    
    public static final boolean DEBUG = true;
    
    private static UnivariateFunction cycleDurationInterpolation = initCycleDurationInterpolation();
    
    /**
     * Runs the computation for the specific amount of time. It is of course an approximation but it usually runs in about +-10% of 
     * requested value.
     * 
     * @param duration time in millisecond that the run should roughly take
     * @param interruptable if true the computation will monitor interrupted state of the thread and break if such is detected. Otherwise
     * the computation runs till the end.
     * @return how long the run actually took in milliseconds.
     * @throws InterruptedException 
     */
    public static long run(long duration,boolean interruptable) throws InterruptedException {
        
        long cycles = getCyclesForDuration(duration);
        return compute(cycles,duration,interruptable);
    }

    /**
     * Performs time consuming operations for requested amount of cycles.
     * After finishing with computations it can sleep for extra time to match the expDuration.
     * @param cycles how many internal interations should be performed
     * @param expDuration how long it should take in milliseconds, if after computation the ex
     * @param interruptable if true the Thread.interrupt will stop the computation
     * @return how long the computation took including the potential sleep
     * @throws InterruptedException if was interrupted
     */
    protected static long compute(long cycles,long expDuration, boolean interruptable) throws InterruptedException {
        
        long begining = System.currentTimeMillis();
        ArrayList<Double> list = new ArrayList<Double>();
        int N = 1500;
        //int K = 100;
        long loops = cycles;
        Random random = new Random();
        
        
        for (int i = 0;i<(N+1000);i++) {
            list.add(Math.cos(random.nextDouble()*1000)*Math.exp(random.nextLong())/(1+Math.tan(1+random.nextDouble()*1.5)));
        }
        for (int l = 0;l<loops;l++) {
            for (int k = 0;k<N;k++) {
            int i = random.nextInt(list.size());
            list.remove(i);
            i = random.nextInt(list.size());
            list.add(i,Math.cos(random.nextDouble()*1000)*Math.exp(random.nextLong())/(1+Math.tan(1+random.nextDouble()*1.5)));            
            if (interruptable) {
                if (Thread.currentThread().isInterrupted()) throw new InterruptedException("Interrupted in the loop: "+l);
            }
            }
        }
        
        long sleep = expDuration -(System.currentTimeMillis()-begining);
        
        if (sleep > 8)
            Thread.sleep(sleep);
        
        return (System.currentTimeMillis()-begining);
    }

    /**
     * Converts the expected time to computation cycles.
     * @param duration in miliseconds
     * @return number of cycles for compute method
     * @throws InterruptedException 
     */
    protected static long getCyclesForDuration(long duration) throws InterruptedException {
        
        UnivariateFunction cycleFunction = getCycleFunction();
        double cyclesD = cycleFunction.value(duration);
        long cycles = (long)Math.floor(cyclesD);
        if (cycles < 0) cycles = 0;
        return cycles;
    }

    protected static UnivariateFunction getCycleFunction() throws InterruptedException {
         return cycleDurationInterpolation;
    }

    protected static UnivariateFunction initCycleDurationInterpolation() {
        
        UnivariateFunction function = readFunction();
        for (;;) {
            if (function == null) {
                try {
                    function = calculateCycleDurationFunction();
                    saveFunction(function);
                } catch(InterruptedException e) {
                    continue;
                }
            }
            return function;
        }
    }

    /**
     * Calibrates the class by performing series of calculations with different cycles number to get
     * the duration estimate.
     * @return
     * @throws InterruptedException 
     */
    protected static UnivariateFunction calculateCycleDurationFunction() throws InterruptedException {
        
        if (DEBUG) System.out.println("Calculating CycleDuration Function for LongComputationCalibration");
        
        long[] cycles = {0,2,5,10,20,40,80,100,150};
        int N = 4;
        
        Map<Long,Long> cyclesVsDuration = new TreeMap<Long,Long>();
        for (long cycle: cycles) cyclesVsDuration.put(cycle, 0L);
        
        for (int i=0;i<N;i++) {
            
            for (long cycle: cycles) {
                long w = cyclesVsDuration.get(cycle);
                if (DEBUG) System.out.print(".");
                w+=compute(cycle, 0, true);
                cyclesVsDuration.put(cycle,w);
            }
            if (DEBUG) System.out.println(".");
        }
        
        for (long cycle: cycles) cyclesVsDuration.put(cycle, cyclesVsDuration.get(cycle)/N);
        
        if (DEBUG) {
            for (long cycle: cycles) {
                System.out.println("C: "+cycle+":"+cyclesVsDuration.get(cycle));
            }
        }
        
        long last = cycles[cycles.length-1];
        long longest = cyclesVsDuration.get(cycles[cycles.length-1]);
        cyclesVsDuration.put(0L,Math.max(cyclesVsDuration.get(0L), 1L));
        cyclesVsDuration.put(-1L, 0L);
        cyclesVsDuration.put((Long.MAX_VALUE/longest)*last, Long.MAX_VALUE);
        
        //double[] durations = new double[cyclesVsDuration.size()];
        //double[] cyclesD = new double[cyclesVsDuration.size()];
        TimeSeries serie = new TimeSeries();
        
        for (long cycle : cyclesVsDuration.keySet()) {
            serie.add(cyclesVsDuration.get(cycle),cycle);
        }

        Pair<double[],double[]> dc = serie.getTimesAndValues();
        //cyclesD[cycles.length+1] = 2*cyclesD[cycles.length];
        //durations[cycles.length+1] = 2*durations[cycles.length];
        
        SplineInterpolator si = new SplineInterpolator();
        UnivariateFunction cyclesFunction = si.interpolate(dc.getLeft(), dc.getRight());
        
        if (DEBUG) {
            cycles = new long[]{10,20,50,100,200,2000,10000,60*1000};
            for (long cycle: cycles) {
                System.out.println("D: "+cycle+":"+cyclesFunction.value(cycle));
            }
        }
        
        if (DEBUG) System.out.println("Finished CycleDuration Function for LongComputationCalibration");
        return cyclesFunction;
    }

    protected static String getCalibrationFileName() {
        return LongComputation.class.getName()+".calibration.csv";
    }
    
    protected static File getCalibrationFile() {
        
        String tmpPath = System.getProperty("java.io.tmpdir", null);
        File tmpDir = null;
        if (tmpPath != null) {
            tmpDir = new File(tmpPath);
            if (!tmpDir.exists() || !tmpDir.isDirectory() || !tmpDir.canWrite()) tmpDir = null;
        }
        String fName = getCalibrationFileName();
        File file = (tmpDir != null) ? new File(tmpDir,fName) : new File(fName);
        return file;
        
    }
    /**
     * Serialises calibration funciton to temp file.
     * @param function 
     */
    protected static void saveFunction(UnivariateFunction function) {
        
        double[] nodes = {0,10,20,50,100,200,400,800,1000,2000,5000,Long.MAX_VALUE};
        TimeSeries ts = new TimeSeries();
        for (double node: nodes) ts.add(node,function.value(node));
        
        File file = getCalibrationFile();
        if (DEBUG) System.out.println("Saving long computation calibration to: "+file.getAbsolutePath());
        try {
            TimeSeriesFileHandler.saveToText(ts, file, ",");
        } catch (IOException ex) {
            System.out.println("Could not save long computation calibration file: "+ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }

    /**
     * DeSerialises calibration function from the temp file.
     * @return 
     */
    protected static UnivariateFunction readFunction() {
        
        try {
            File file = getCalibrationFile();
            if (!file.exists() || !file.canRead()) return null;
            Calendar modified = Calendar.getInstance();
            modified.setTime(new Date(file.lastModified()));
            Calendar today = Calendar.getInstance();
            
            boolean fresh = true;
            int[] fields = {Calendar.YEAR,Calendar.DAY_OF_YEAR};
            for (int f : fields)
                if (today.get(f) != modified.get(f)) fresh = false;
            
            if (!fresh) {
                if (DEBUG) System.out.println("Ignoring calibration file as is old");
                return null;
            }
            
            List<TimeSeries> list = TimeSeriesFileHandler.readFromText(file, ",");
            if (list.size() != 1) {
                System.out.println("Calibration file wrong content size");
                return null;
            }
            
            TimeSeries ts = list.get(0);
            double[] durations = new double[ts.size()];
            double[] cycles = new double[ts.size()];
            int i = 0;
            for (Timepoint tp : ts) {
                durations[i] = tp.getTime();
                cycles[i] = tp.getValue();
                i++;
            }
            
            
                SplineInterpolator si = new SplineInterpolator();
                UnivariateFunction function = si.interpolate(durations, cycles);
                if (DEBUG) System.out.println("Read calibration function from file: "+file.getAbsolutePath());
                return function;
            
            
            } catch (Exception e) {
                System.out.println("Could not read calibration curve: "+e.getMessage());
                e.printStackTrace(System.out);
                return null;
            }        
    }
    
    
}
