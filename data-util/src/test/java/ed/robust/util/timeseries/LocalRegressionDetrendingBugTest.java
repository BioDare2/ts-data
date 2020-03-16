/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

import ed.robust.dom.data.TimeSeries;
import ed.robust.dom.data.Timepoint;
import ed.robust.dom.util.Pair;
import ed.robust.error.RobustFormatException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author tzielins
 */
public class LocalRegressionDetrendingBugTest {
    
    double EPS = 1E-4;
    LocalRegressionDetrending LRDetrending = new LocalRegressionDetrending();
    
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    
    
    //@Test
    public void generateSanityCheckData() throws IOException {
        List<TimeSeries> orgs = new ArrayList<>();
        
        int N = 100;
        double step = 1;
        
        orgs.add(TSGenerator.makeLine(N, step, 0, 2));
        orgs.add(TSGenerator.makeLine(N, step, 0.1, 0));
        orgs.add(TSGenerator.makeLine(N, step, 0.1, 1));
        
        orgs.add(TSGenerator.makeCos(N, step, 24, 0, 2));
        orgs.add(TSGenerator.makeCos(N, step, 24, 6, 2));
        orgs.add(TSGenerator.makeCos(N, step, 24, 12, 2));
        
        orgs.add(TSGenerator.makeDblPulse(N, step, 24, 0, 2));
        orgs.add(TSGenerator.makeDblPulse(N, step, 24, 6, 2));
        orgs.add(TSGenerator.makeDblPulse(N, step, 24, 12, 2));
        
        orgs.add(TSGenerator.makeWave(N, step, 24, 0, 2));
        orgs.add(TSGenerator.makeWave(N, step, 24, 6, 2));
        orgs.add(TSGenerator.makeWave(N, step, 24, 12, 2));
        
        List<TimeSeries> dmp = new ArrayList<>();
        for (TimeSeries ser : orgs) dmp.add(TSGenerator.dampen(ser, 0.3));
        
        orgs.addAll(dmp);
        
        File file = new File("E:/Temp/AMB_BASE.DTR.csv");
        //TimeSeriesFileHandler.saveToText(orgs, file, ",");
        
    }
    
    @Test
    public void debugDificultCases() throws InterruptedException, IOException, RobustFormatException {
        
        File testFile = new File(this.getClass().getResource("diff_detrending_data2.csv").getFile());
        List<TimeSeries> orgs = TimeSeriesFileHandler.readFromText(testFile, ",",1);

        List<TimeSeries> res = new ArrayList<>();
        
        //TimeSeries ser = orgs.get(5);
        //orgs = Arrays.asList(orgs.get(0),orgs.get(3),orgs.get(11));
        orgs = Arrays.asList(orgs.get(11));
        for (TimeSeries org : orgs) {
            
            DataSource source = new SplineTSInterpolator(org, ROUNDING_TYPE.HALF_INT);
            
            //Pair<double[],double[]> timesValues = ser.getTimesAndValues();
            Pair<double[],double[]> timesValues = source.getTimesAndValues(1, ROUNDING_TYPE.DECY);
            
            double[] times = timesValues.getLeft();
            double[] values = timesValues.getRight();

            double[] baselineTrendValues = LRDetrending.getClassicBaselineTrend(times, values,1);
            
            double[] detrended = new double[baselineTrendValues.length];

            for (int i = 0;i<values.length;i++) {
                double val = values[i]-baselineTrendValues[i];
                detrended[i] = val;
            }
            
            double[] abs = new double[detrended.length];

            for (int i = 0;i<detrended.length;i++) {
                if (detrended[i] < 0) {
                    abs[i] = -detrended[i];
                } else {
                     abs[i] = detrended[i];
                }
            }
            
            
            double[] amplitudeTrendValues = LRDetrending.getClassicAmpTrend(times, detrended, 1);
            
            TimeSeries ser = new TimeSeries(times,values);
            TimeSeries baseTrend = new TimeSeries(times,baselineTrendValues);
            TimeSeries bdetr = new TimeSeries(times,detrended);
            TimeSeries absT = new TimeSeries(times,abs);
            TimeSeries ampTrend = new TimeSeries(times,amplitudeTrendValues);
            
            
            res.add(org);
            res.add(ser);
            res.add(baseTrend);
            res.add(bdetr);
            res.add(absT);
            res.add(ampTrend);
            
            LocalRegressionDetrending.TrendPack trend;
            trend = LRDetrending.findClassicTrends(ser, true, false);

            TimeSeries dtr = LRDetrending.removeTrend(ser, trend);
            TimeSeries baseTrend2 = new TimeSeries();
            TimeSeries ampTrend2 = new TimeSeries();

            TimeSeries baseTrend3 = new TimeSeries();
            TimeSeries baseTrend4 = new TimeSeries();
            
            DataSource baselineSource1 = new SplineTSInterpolator(TimeSeriesOperations.makeTimepoints(times, baselineTrendValues),ROUNDING_TYPE.DECY);
            DataSource baselineSource2 = new SplineTSInterpolator(new TimeSeries(times, baselineTrendValues),ROUNDING_TYPE.DECY);

            for (double t : times) {
                baseTrend2.add(t,trend.baselineTrend.getValue(t));
                ampTrend2.add(t,trend.amplitudeTrend.getValue(t));
                baseTrend3.add(t,baselineSource1.getValue(t));
                baseTrend4.add(t,baselineSource2.getValue(t));
            }

            res.add(baseTrend2);
            //res.add(baseTrend3);
            //res.add(baseTrend4);
            res.add(ampTrend2);
            res.add(dtr);
            
            //res.add(dtr);
        }
        
        File out = new File("E:/Temp/diff.dtr.debug.csv");
        //TimeSeriesFileHandler.saveToText(res, out, ",");
        
        //fail("As expected");
    }
    
    @Test
    public void checkDificultCases() throws InterruptedException, IOException, RobustFormatException {
        
        File testFile = new File(this.getClass().getResource("diff_detrending_data1.csv").getFile());
        List<TimeSeries> orgs = TimeSeriesFileHandler.readFromText(testFile, ",",1);

        List<TimeSeries> res = new ArrayList<>();
        
        //TimeSeries ser = orgs.get(5);
        //orgs = Arrays.asList(orgs.get(0),orgs.get(3),orgs.get(11));
        for (TimeSeries ser : orgs) {
            LocalRegressionDetrending.TrendPack trend;
            trend = LRDetrending.findClassicTrends(ser, true, false);

            TimeSeries dtr = LRDetrending.removeTrend(ser, trend);
            TimeSeries baseTrend = new TimeSeries();
            TimeSeries ampTrend = new TimeSeries();

            for (Timepoint tp : ser) {
                baseTrend.add(tp.getTime(),trend.baselineTrend.getValue(tp.getTime()));
                ampTrend.add(tp.getTime(),trend.amplitudeTrend.getValue(tp.getTime()));
            }

            //List<TimeSeries> res = Arrays.asList(ser,baseTrend,ampTrend);
            //res.add(ser);
            //res.add(baseTrend);
            //res.add(ampTrend);
            res.add(dtr);
        }
        
        File out = new File("E:/Temp/diff.dtr.case.res.csv");
        //TimeSeriesFileHandler.saveToText(res, out, ",");
        
        //fail("As expected");
    }
    
    @Test
    public void checkDetrendingSanity() throws InterruptedException, IOException, RobustFormatException {
        
        File testFile = new File(this.getClass().getResource("AMB_BASE.DTR.csv").getFile());
        List<TimeSeries> orgs = TimeSeriesFileHandler.readFromText(testFile, ",");
        
        List<TimeSeries> res = new ArrayList<>();
        for (TimeSeries ser : orgs) {
            LocalRegressionDetrending.TrendPack pack;
            pack = LRDetrending.findSmartTrends(ser,false);
            //pack = LRDetrending.findClassicTrends(ser);
            res.add(ser);
            res.add(LocalRegressionDetrending.removeTrend(ser, pack));
        }
        
        File file = new File("E:/Temp/AMB_BASE.DTR.res2.csv");
        TimeSeriesFileHandler.saveToText(res, file, ",");
        
    }
    
    @Test
    public void testFindSmartTrendsOnZeros() throws InterruptedException {
        
        TimeSeries data = new TimeSeries();
        for (int i = 0;i<100;i++) data.add(i,0);
        
        DataSource source = new SplineTSInterpolator(data, ROUNDING_TYPE.HALF_INT);
        LocalRegressionDetrending.TrendPack pack = LRDetrending.findSmartTrends(source,true,false);
        
        assertEquals(0,pack.baselineMean,EPS);
        assertNotNull(pack.baselineTrend);
        
        double[] vals = pack.baselineTrend.getTimesAndValues(1, ROUNDING_TYPE.NO_ROUNDING).getRight();
        assertEquals(data.size(),vals.length);
        for (double v : vals) {
            assertFalse(Double.isInfinite(v));
            assertFalse(Double.isNaN(v));
            assertEquals(0,v,EPS);
        }
        
        List<Timepoint> ser = pack.baselineTrend.getTimepoints(1, ROUNDING_TYPE.NO_ROUNDING);
        assertEquals(data.size(),ser.size());
        for (Timepoint tp : ser) assertNotNull(tp);
        
        //check baseline
        assertEquals(1,pack.amplitudeMean,EPS);
        assertNotNull(pack.amplitudeTrend);
        
        vals = pack.amplitudeTrend.getTimesAndValues(1, ROUNDING_TYPE.NO_ROUNDING).getRight();
        assertEquals(data.size(),vals.length);
        for (double v : vals) {
            assertFalse(Double.isInfinite(v));
            assertFalse(Double.isNaN(v));
            assertEquals(1,v,EPS);
        }
        
        ser = pack.amplitudeTrend.getTimepoints(1, ROUNDING_TYPE.NO_ROUNDING);
        assertEquals(data.size(),ser.size());
        for (Timepoint tp : ser) assertNotNull(tp);
        
        
    }
    
    @Test
    public void testFindClassicTrendsOnZeros() throws InterruptedException {
        
        TimeSeries data = new TimeSeries();
        for (int i = 0;i<100;i++) data.add(i,0);
        
        DataSource source = new SplineTSInterpolator(data, ROUNDING_TYPE.HALF_INT);
        LocalRegressionDetrending.TrendPack pack = LRDetrending.findClassicTrends(source,true,false);
        
        assertEquals(0,pack.baselineMean,EPS);
        assertNotNull(pack.baselineTrend);
        
        double[] vals = pack.baselineTrend.getTimesAndValues(1, ROUNDING_TYPE.NO_ROUNDING).getRight();
        assertEquals(data.size(),vals.length);
        for (double v : vals) {
            assertFalse(Double.isInfinite(v));
            assertFalse(Double.isNaN(v));
            assertEquals(0,v,EPS);
        }
        
        List<Timepoint> ser = pack.baselineTrend.getTimepoints(1, ROUNDING_TYPE.NO_ROUNDING);
        assertEquals(data.size(),ser.size());
        for (Timepoint tp : ser) assertNotNull(tp);
        
        //check baseline
        assertEquals(1,pack.amplitudeMean,EPS);
        assertNotNull(pack.amplitudeTrend);
        
        vals = pack.amplitudeTrend.getTimesAndValues(1, ROUNDING_TYPE.NO_ROUNDING).getRight();
        assertEquals(data.size(),vals.length);
        for (double v : vals) {
            assertFalse(Double.isInfinite(v));
            assertFalse(Double.isNaN(v));
            assertEquals(1,v,EPS);
        }
        
        ser = pack.amplitudeTrend.getTimepoints(1, ROUNDING_TYPE.NO_ROUNDING);
        assertEquals(data.size(),ser.size());
        for (Timepoint tp : ser) assertNotNull(tp);
        
        
    }
    
    @Test
    public void testZeros() throws InterruptedException {
        
        TimeSeries org = new TimeSeries();
        for (int i = 0;i<100;i++) org.add(i,0);
        
        LocalRegressionDetrending.TrendPack pack = LRDetrending.findSmartTrends(org,false);
        TimeSeries res = LocalRegressionDetrending.removeTrend(org, pack);
        assertNotNull(res);
        assertFalse(res.isEmpty());
        
        for (Timepoint tp : res)
            assertEquals(0,tp.getValue(),EPS);
        
    }
    
    @Test
    public void testZerosOnClassic() throws InterruptedException {
        
        TimeSeries org = new TimeSeries();
        for (int i = 0;i<100;i++) org.add(i,0);
        
        LocalRegressionDetrending.TrendPack pack = LRDetrending.findClassicTrends(org,true,false);
        TimeSeries res = LocalRegressionDetrending.removeTrend(org, pack);
        assertNotNull(res);
        assertFalse(res.isEmpty());
        
        for (Timepoint tp : res)
            assertEquals(0,tp.getValue(),EPS);
        
    }
    
    
    @Test
    public void testEdwardsFailing() throws RobustFormatException, IOException, InterruptedException {
        
        File testFile = new File(this.getClass().getResource("edwards.csv").getFile());
        
        List<TimeSeries> series = TimeSeriesFileHandler.readFromText(testFile, ",", 1);
        
        for (int i = 0;i<series.size();i++) {
            try {
            TimeSeries org = series.get(i);
            LocalRegressionDetrending.TrendPack pack = LRDetrending.findSmartTrends(org,false);
            TimeSeries res = LocalRegressionDetrending.removeTrend(org, pack);
            assertNotNull(res);
            assertFalse(res.isEmpty());
            } catch (RuntimeException e) {
                fail("At: "+i+"; "+e.getMessage());
            }
        }
        
        //fail("As expected");
    }
    
    @Test
    public void testEdwardsFailingOnClassic() throws RobustFormatException, IOException, InterruptedException {
        
        File testFile = new File(this.getClass().getResource("edwards.csv").getFile());
        
        List<TimeSeries> series = TimeSeriesFileHandler.readFromText(testFile, ",", 1);
        
        for (int i = 0;i<series.size();i++) {
            try {
            TimeSeries org = series.get(i);
            LocalRegressionDetrending.TrendPack pack = LRDetrending.findClassicTrends(org,true,false);
            TimeSeries res = LocalRegressionDetrending.removeTrend(org, pack);
            assertNotNull(res);
            assertFalse(res.isEmpty());
            } catch (RuntimeException e) {
                fail("At: "+i+"; "+e.getMessage());
            }
        }
        
        //fail("As expected");
    }
    
    
    @Test
    public void testCorruptedResult() throws Exception {
        
        //File testFile = new File("E:/Temp/blur.std.csv");
        File testFile = new File(this.getClass().getResource("blur.std.csv").getFile());
        List<TimeSeries> series = TimeSeriesFileHandler.readFromText(testFile, ",");
        
        assertFalse(series.isEmpty());
        
        File tmpFile = testFolder.newFile("lrdtmp.ser");
        
        for (int ix = 0;ix<series.size();ix++) {
            TimeSeries data = series.get(ix);
            
            LocalRegressionDetrending.TrendPack pack = LRDetrending.findSmartBaselineTrend(data);
            
            TimeSeries res =  LocalRegressionDetrending.removeTrend(data, pack);
            assertNotNull(res);
            //assertEquals(data.size(),res.size());
            //if (!data.isEmpty()) assertFalse(res.isEmpty());
            try {
                serialize(res,tmpFile);
            } catch (Exception e) {
                System.out.println("Problem with: "+ix);
                e.printStackTrace(System.out);
                throw e;
            }
        }
    }
    
    @Test
    public void testCorruptedResultOnClassic() throws Exception {
        
        //File testFile = new File("E:/Temp/blur.std.csv");
        File testFile = new File(this.getClass().getResource("blur.std.csv").getFile());
        List<TimeSeries> series = TimeSeriesFileHandler.readFromText(testFile, ",");
        
        assertFalse(series.isEmpty());
        
        File tmpFile = testFolder.newFile("lrdtmp.ser");
        
        for (int ix = 0;ix<series.size();ix++) {
            TimeSeries data = series.get(ix);
            
            LocalRegressionDetrending.TrendPack pack = LRDetrending.findClassicTrends(data,false,false);
            
            TimeSeries res =  LocalRegressionDetrending.removeTrend(data, pack);
            assertNotNull(res);
            //assertEquals(data.size(),res.size());
            //if (!data.isEmpty()) assertFalse(res.isEmpty());
            try {
                serialize(res,tmpFile);
            } catch (Exception e) {
                System.out.println("Problem with: "+ix);
                e.printStackTrace(System.out);
                throw e;
            }
        }
    }
    

    private void serialize(Serializable obj, File file) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(obj);
        }
    }
}
