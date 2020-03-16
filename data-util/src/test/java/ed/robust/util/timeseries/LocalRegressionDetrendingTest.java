/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

import ed.robust.dom.util.Pair;
import ed.robust.dom.data.TimeSeries;
import ed.robust.dom.data.Timepoint;
import ed.robust.error.RobustFormatException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.BeforeClass;
import static org.junit.Assert.*;

/**
 *
 * @author tzielins
 */
public class LocalRegressionDetrendingTest {
    
    static final double EPS = 1E-6;
    public LocalRegressionDetrendingTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    
    @Test
    public void testSanitizeAmpTrend() {
    
        LocalRegressionDetrending instance = new LocalRegressionDetrending();
        double[] ampTrend = {-1,0,1,-1,2,2,3,1,0,-1,-2};
        double[] orgAmp =   new double[ampTrend.length];
        for (int i=0;i<orgAmp.length;i++) orgAmp[i]=i;
     
        int range = 4;
        double[] sane = instance.sanitizeAmpTrend(ampTrend, orgAmp, range);
        for (double d : sane) assertTrue(d >0);
        
        range = 20;
        sane = instance.sanitizeAmpTrend(ampTrend, orgAmp, range);
        for (double d : sane) assertTrue(d >0);
        
        range = 2;
        sane = instance.sanitizeAmpTrend(ampTrend, orgAmp, range);
        for (int i =0;i<range;i++) assertTrue(sane[i] > 0);
        for (int i = range;i<ampTrend.length-range;i++) assertEquals(ampTrend[i],sane[i],EPS);
        for (int i =ampTrend.length-range;i<ampTrend.length;i++) assertTrue(sane[i] > 0);
        
    }
    
    @Test
    public void testCorrectForZero() {
    
        LocalRegressionDetrending instance = new LocalRegressionDetrending();
        double[] ampTrend = {-1,0,1,2,-1,0};
        double[] orgAmp =   { 0,1,2,3, 4,5};
        
        int position = 0;
        double exp = 0;
        double res = 0;
        
        position = 0;
        exp = (0.0+1+1)/3.0;
        res = instance.correctAmpForZero(position, ampTrend, orgAmp);
        assertEquals(exp,res,EPS);
        
        position = 1;
        exp = (0.0+1+1+2)/4.0;
        res = instance.correctAmpForZero(position, ampTrend, orgAmp);
        assertEquals(exp,res,EPS);
        
        position = 2;
        exp = (0.0+1+1+2+4)/5.0;
        res = instance.correctAmpForZero(position, ampTrend, orgAmp);
        assertEquals(exp,res,EPS);
        
        position = 3;
        exp = (1+1+2+4+5)/5.0;
        res = instance.correctAmpForZero(position, ampTrend, orgAmp);
        assertEquals(exp,res,EPS);
        
        position = 4;
        exp = (1+2+4+5)/4.0;
        res = instance.correctAmpForZero(position, ampTrend, orgAmp);
        assertEquals(exp,res,EPS);
        
        position = 5;
        exp = (2+4+5)/3.0;
        res = instance.correctAmpForZero(position, ampTrend, orgAmp);
        assertEquals(exp,res,EPS);
        
    }
    
    @Test
    public void testLIN_EPS_VALUES() throws Exception {
        System.out.println("test line eps");
        
        double step = 1;
        List<TimeSeries> testData = makeTestSeries(24.3,step);
        
        List<TimeSeries> all = new ArrayList<TimeSeries>();
        List<TimeSeries> detrended1 = new ArrayList<TimeSeries>();
        List<TimeSeries> detrended2 = new ArrayList<TimeSeries>();

        LocalRegressionDetrending instance = new LocalRegressionDetrending();
        
        long sT = System.currentTimeMillis();
        for (TimeSeries data : testData) {
            
            all.add(data);
            LocalRegressionDetrending.TrendPack pack = instance.findSmartTrends(data,false);
            TimeSeries dtr = instance.removeTrend(data, pack);
            
            detrended1.add(dtr);
        }
        System.out.println(LocalRegressionDetrending.LIN_EPS+ "took :"+ (System.currentTimeMillis()-sT));
        
        LocalRegressionDetrending.LIN_EPS = 1E-9;
        sT = System.currentTimeMillis();
        for (TimeSeries data : testData) {
            
            all.add(data);
            LocalRegressionDetrending.TrendPack pack = instance.findSmartTrends(data,false);
            TimeSeries dtr = instance.removeTrend(data, pack);
            
            detrended2.add(dtr);
        }
        System.out.println(LocalRegressionDetrending.LIN_EPS+ "took :"+ (System.currentTimeMillis()-sT));
        
        
        for (int i = 0;i<testData.size();i++) {
            TimeSeries tr1 = detrended1.get(i);
            TimeSeries tr2 = detrended2.get(i);
            for (int j = 0;j<tr1.size();j++) {
                boolean stop = false;
                double val1 = tr1.getTimepoints().get(j).getValue();
                double val2 = tr2.getTimepoints().get(j).getValue();
                if (val1 == 0) continue;
                double err = Math.abs(val1-val2)/val1;
            
            for (double e=0.1;e>1E-4;e=e/10) {
                if (err > e) {
                    System.out.println("Diff: "+e+", "+err+", j"+j+"-"+i);
                    //if (j > 20 && j < (tr1.size()-20)) stop = true;
                    break;
                }
            };
            if (stop) break;
            };
            /*
            assertTrue("0.01", tr1.almostEquals(tr2, 0.01));
            assertTrue("0.001", tr1.almostEquals(tr2, 0.001));
            assertTrue("0.0001", tr1.almostEquals(tr2, 0.0001));
            //assertTrue("0.00001", tr1.almostEquals(tr2, 0.00001));
            * 
            */
        }
        
    }
    
    @Test
    public void testJoin() {
        
        System.out.println("check joins stats");
        double[] a = {0,1,2,3};
        double[] b = {};
        double[] c = {4,5};
        double[] d = {6};
        
        LocalRegressionDetrending instance = new LocalRegressionDetrending();
        double[] tab = instance.join(a,b,c,d);
        
        for (int i =0;i<tab.length;i++)
            assertEquals(i,tab[i],0.0001);
            
    }
    
    @Test 
    public void predictTrendValueTest() throws Exception {
        System.out.println("predict trend value");
    
        double[] trend = {-1,0,1,1,2};
        double[] times = {-1,0,1,2,3};
        
        
        LocalRegressionDetrending instance = new LocalRegressionDetrending();
        int start = 2;
        int end = 4;
        double x = -1;
        
        double res = instance.predictTrendValue(x, times, trend, start, end);
        double EPS = 1e-6;
        assertEquals(1, res,EPS);
        
        start = 0;
        end = 3;
        res = instance.predictTrendValue(x, times, trend, start, end);
        assertEquals(-1, res,EPS);
        
        start = 3;
        end = 5;
        x = 4;
        res = instance.predictTrendValue(x, times, trend, start, end);
        assertEquals(3, res,EPS);
        
        
    }
    
    @Test
    public void checkLinRegres() throws Exception {
        System.out.println("check lin reg1");
        
        double timeStep = 1;
        TimeSeries data = TSGenerator.makeDblPulse(100, timeStep, 24.2, 5, 2);
        data = TSGenerator.addTrend(data,0.05,1);
        
        Pair<double[],double[]> tv = TSGenerator.extractTimeAndValueTables(data);
        double[] times = tv.getLeft();
        double[] values = tv.getRight();
        
        double band = 10;
        int start = 0;
        int end = times.length;
        
        LocalRegressionDetrending instance = new LocalRegressionDetrending();
        double[] trend1 = instance.localLinRegresionSubset(times, values,timeStep,band,start,end);
        double[] trend2 = instance.localLinRegresion(times, values,timeStep,band);
        
        double EPS = 1e-6;
        assertEquals(trend2.length,times.length);
        assertEquals(trend1.length, trend2.length);
        for (int i =0;i<trend1.length;i++) {
            assertEquals(trend2[i], trend1[i],EPS);
        }
        
        start = 10;
        //end = times.length/2;
        trend1 = instance.localLinRegresionSubset(times, values,timeStep,band,start,end);
        assertEquals(times.length-start, trend1.length);
        
        start = 0;
        end = times.length/2;
        trend1 = instance.localLinRegresionSubset(times, values,timeStep,band,start,end);
        assertEquals(end, trend1.length);
        
        start = 10;
        end = times.length/2;
        
        trend1 = instance.localLinRegresionSubset(times, values,timeStep,band,start,end);
        for (int i =0;i<trend1.length;i++) {
            assertEquals(trend2[i+start], trend1[i],EPS);
        }
        
        start = 0;
        end = 2;
        
        trend1 = instance.localLinRegresionSubset(times, values,timeStep,band,start,end);
        assertEquals(2, trend1.length);
        
    }
    
    //@Test
    public void checkTrendsStats() throws Exception {
        System.out.println("check trends stats");
        
        File dataDir = new File("D:/Performance/trends");
        File outDir = new File("D:/Performance/trends-anal");
        
        
        if (!outDir.exists()) outDir.mkdir();
        
        //List<File> files = getFiles(dataDir);
        
        List<File> files = Arrays.asList(new File(dataDir,"trd_4.0_gen_5d_f1.0.csv"));
        //List<File> files = Arrays.asList(new File(dataDir,"dmpMEAN_0.8gen_5d_f1.0.csv"));
        //List<File> files = Arrays.asList(new File(dataDir,"trd_4.0_dmpMEAN_0.4gen_5d_f1.0.csv"));
        
        for (File file : files) {
            
            doTrendsStats(file,outDir);
        }
    }    
    
    protected void doTrendsStats(File file,File outDir) throws IOException, RobustFormatException, InterruptedException {
        
        List<String> ids = TimeSeriesFileHandler.readLabels(file, ",", 1);
        ids = ids.subList(1,ids.size());
        
        List<TimeSeries> series = TimeSeriesFileHandler.readFromText(file, ",", 5);
        
        String fName = file.getName();
        
        List<TimeSeries> detrended = new ArrayList<TimeSeries>();
        List<TimeSeries> baselineTrends = new ArrayList<TimeSeries>();;
        List<TimeSeries> ampTrends = new ArrayList<TimeSeries>();
        
        List<DescriptiveStatistics> stats = new ArrayList<DescriptiveStatistics>();
        //DescriptiveStatistics globaal = new DescriptiveStatistics();
        
        LocalRegressionDetrending instance = new LocalRegressionDetrending();
        
        
        int i = 1;
        for(TimeSeries data : series) {
            
            System.out.println(i++ + "/"+series.size()+" in "+fName);
            LocalRegressionDetrending.TrendPack trendPack = instance.findSmartTrends(data,false);
            TimeSeries dtr = instance.removeTrend(data, trendPack);
            dtr = TimeSeriesOperations.addTrend(dtr, 0, -TimeSeriesOperations.getMeanValue(dtr));
            
            double step = DataRounder.round(data.getAverageStep(),ROUNDING_TYPE.DECY);
            
            detrended.add(dtr);
            
            TimeSeries basT = buildTS(trendPack.baselineTrend,step);
            baselineTrends.add(basT);
            
            TimeSeries ampT = buildTS(trendPack.amplitudeTrend,step);
            ampTrends.add(ampT);
            
            stats.add(buildStats(ampT));
        }
        
        TimeSeriesFileHandler.saveToText(detrended, new File(outDir,fName+".detrended1.csv"), ",",ids);
        TimeSeriesFileHandler.saveToText(baselineTrends, new File(outDir,fName+".base1.csv"), ",",ids);
        TimeSeriesFileHandler.saveToText(ampTrends, new File(outDir,fName+".amp1.csv"), ",",ids);
        
        saveToText(stats,ids,new File(outDir,fName+".stats.csv"),",");
        
    }
    
    protected void saveToText(List<DescriptiveStatistics> stats,List<String> ids,File file,String SEP) throws IOException {
    
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        
        out.write("ID"+SEP+"MIN"+SEP+"MAX"+SEP+"AMP"+SEP+"MEAN"+SEP+"STD"+SEP+"MIN/MAX");
        out.newLine();
        
        for (int i =0;i<stats.size();i++) {
            
            DescriptiveStatistics stat = stats.get(i);
            StringBuilder sb = new StringBuilder();
            sb.append(ids.get(i)).append(SEP);
            
            sb.append(stat.getMin()).append(SEP);
            sb.append(stat.getMax()).append(SEP);
            sb.append(stat.getMax()-stat.getMin()).append(SEP);
            sb.append(stat.getMean()).append(SEP);
            sb.append(stat.getStandardDeviation()).append(SEP);
            sb.append(stat.getMin()/stat.getMax());
            
            out.write(sb.toString());
            out.newLine();
        }
        
        out.close();
    }
    
    protected DescriptiveStatistics buildStats(TimeSeries data) {
        
        DescriptiveStatistics stat = new DescriptiveStatistics();
        double s = data.getFirst().getTime()+10;
        double e = data.getLast().getTime()-10;
        for (Timepoint tp : data) {
            if (tp.getTime() >= s && tp.getTime()<= e)
                stat.addValue(tp.getValue());
        }
        return stat;
    }
    
    
    protected TimeSeries buildTS(DataSource source, double step) {
        
        return new TimeSeries(source.getTimepoints(step, ROUNDING_TYPE.DECY));
    }
    
    
    protected static List<File> getFiles(File dir) {
        
        List<File> list = new ArrayList<File>();
        
        for (File file : dir.listFiles()) {
            if (file.isFile()) list.add(file);
        }
        
        return list;
    }
    
    @Test
    public void testDetrend() throws Exception {
        System.out.println("test detrend");
        
        double step = 1;
        List<TimeSeries> testData = makeTestSeries(25,step);
        //testData.addAll(makeTestSeries(25,step));
        
        List<TimeSeries> results = new ArrayList<TimeSeries>();

        LocalRegressionDetrending instance = new LocalRegressionDetrending();
        
        for (TimeSeries data : testData) {
            
            LocalRegressionDetrending.TrendPack pack = instance.findSmartTrends(data,true);
            TimeSeries detrended = instance.removeTrend(data, pack);
            detrended = TimeSeriesOperations.addTrend(detrended, 0, -TimeSeriesOperations.getMeanValue(detrended));
            
            data = TimeSeriesOperations.addTrend(data, 0, -TimeSeriesOperations.getMeanValue(data));
            results.add(data);
            results.add(detrended);
            TimeSeries basT = buildTS(pack.baselineTrend,step);
            results.add(basT);
            
            TimeSeries ampT = buildTS(pack.amplitudeTrend,step);
            results.add(ampT);
            
        }
        TimeSeriesFileHandler.saveToText(results, new File("E:/Temp/smart_detrend4.csv"), ",");
    }
    
    
    
    @Test
    public void testDoBaselineDetrending() throws Exception {
        System.out.println("doBaselineDetrending");
        
        double step = 1;
        List<TimeSeries> testData = makeTestSeries(15,step);
        testData.addAll(makeTestSeries(25,step));
        
        List<TimeSeries> results = new ArrayList<TimeSeries>();

        LocalRegressionDetrending instance = new LocalRegressionDetrending();
        
        for (TimeSeries data : testData) {
            
            LocalRegressionDetrending.TrendPack pack = instance.findSmartBaselineTrend(data);
            TimeSeries detrended = instance.removeTrend(data, pack);
            
            results.add(data);
            results.add(detrended);
        }
        TimeSeriesFileHandler.saveToText(results, new File("E:/Temp/trends.csv"), ",");
        
    }

    @Test
    public void testDoAmplitudeAndBaselineDetrending() throws Exception {
        System.out.println("doAmplitudeAndBaselineDetrending");
        
        double step = 1;
        List<TimeSeries> testData = makeTestSeries(15,step);
        testData.addAll(makeTestSeries(25,step));
        
        List<TimeSeries> results = new ArrayList<TimeSeries>();

        LocalRegressionDetrending instance = new LocalRegressionDetrending();
        
        for (TimeSeries data : testData) {
            
            LocalRegressionDetrending.TrendPack pack = instance.findSmartTrends(data, false);
            TimeSeries detrended = instance.removeTrend(data, pack);
            results.add(data);
            results.add(detrended);
        }
        TimeSeriesFileHandler.saveToText(results, new File("E:/Temp/trends_amp2.csv"), ",");
    }
    
    @Test
    public void testAddTrend() throws Exception {
        System.out.println("addTrend");
        
        double step = 1;
        List<TimeSeries> testData = makeTestSeries(15,step);
        testData.addAll(makeTestSeries(25,step));
        
        List<TimeSeries> all = new ArrayList<>();
        List<TimeSeries> detrended = new ArrayList<>();
        List<TimeSeries> trended = new ArrayList<>();

        LocalRegressionDetrending instance = new LocalRegressionDetrending();
        
        for (TimeSeries data : testData) {
            
            all.add(data);
            LocalRegressionDetrending.TrendPack pack = instance.findSmartTrends(data,false);
            TimeSeries dtr = instance.removeTrend(data, pack);
            
            detrended.add(dtr);
            all.add(dtr);
            TimeSeries tr = instance.addTrend(dtr, pack);
            trended.add(tr);
            all.add(tr);
        }
        TimeSeriesFileHandler.saveToText(all, new File("E:/Temp/back_trends.csv"), ",");
        
        for (int i = 0;i<testData.size();i++) {
            TimeSeries data = testData.get(i);
            TimeSeries tr = trended.get(i);
            assertTrue(data.almostEquals(tr, 1E-6));
        }
        
    }
    
    @Test
    public void testClassicSmartDetrending() throws Exception {
        System.out.println("compareSmartDetrending");
        
        double step = 1;
        int N = 100;
        
        TimeSeries data = TSGenerator.makePulse(N, step, 24, 6,2);
        
        LocalRegressionDetrending instance = new LocalRegressionDetrending();
        
        LocalRegressionDetrending.TrendPack oldT = instance.findClassicTrends(data);
        LocalRegressionDetrending.TrendPack newT = instance.findClassicTrends(data,true,false);
        
        TimeSeries oldR = instance.removeTrend(data, oldT);
        TimeSeries newR = instance.removeTrend(data, newT);
        
        assertTrue(oldR.almostEquals(newR, 1E-9));
        
        newT = instance.findClassicTrends(data,false,false);
        newR = instance.removeTrend(data, newT);
        assertFalse(oldR.almostEquals(newR, 1E-3));
        
        LocalRegressionDetrending.TrendPack newT2 = instance.findClassicTrends(data,true,true);
        TimeSeries newR2 = instance.removeTrend(data, newT2);
        
        assertTrue(newR2.almostEquals(newR, 1E-9));
    }

    
    protected List<TimeSeries> makeTestSeries(double period,double step) {
        
        double duration = 5*24;
        int N = (int)(duration/step);
        double amp = 3;
        
        TimeSeries data = TSGenerator.makeDblPulse(N, step, period, 5, amp);
        
        List<TimeSeries> list = new ArrayList<TimeSeries>();
        list.add(data);
        
        TimeSeries withTrend = TimeSeriesOperations.addTrend(data, 2*amp/(duration), amp);
        
        list.add(withTrend);
        
        withTrend = TimeSeriesOperations.sum(data,TSGenerator.makeSin(N, step, duration*4, 0, 2*amp));
        list.add(withTrend);

        withTrend = TimeSeriesOperations.sum(data,TSGenerator.makeSin(N, step, duration*2.5, 0, 2*2*amp));
        list.add(withTrend);
        
        withTrend = TimeSeriesOperations.sum(data,TSGenerator.makeSin(N, step, duration*1.7, 0, 2*2*amp));
        list.add(withTrend);
        
        withTrend = TimeSeriesOperations.convolute(data,TSGenerator.makeSin(N, step, duration*4, 0, 4));
        list.add(withTrend);
        
        withTrend = TimeSeriesOperations.convolute(data,TSGenerator.makeCos(N, step, duration*5, 0, 4));
        list.add(withTrend);
        
        withTrend = TimeSeriesOperations.convolute(data,TSGenerator.makeCos(N, step, duration*5, 0, 4));
        withTrend = TimeSeriesOperations.sum(withTrend,TSGenerator.makeSin(N, step, duration*3, 0, 4*amp));
        list.add(withTrend);
        
        
        return list;
    }

    

}
