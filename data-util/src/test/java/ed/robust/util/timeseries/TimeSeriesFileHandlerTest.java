/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

import ed.robust.dom.data.TimeSeries;
import ed.robust.dom.data.Timepoint;
import ed.robust.error.RobustFormatException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author tzielins
 */
public class TimeSeriesFileHandlerTest {
    
    public TimeSeriesFileHandlerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test 
    public void testMultipleEntries() throws Exception {
        
        TimeSeries data = new TimeSeries();
        data.add(0,1);
        data.add(1,2);
        data.add(1,3);
        data.add(1,4);
        data.add(2,2);
        
        
        File cF = Configuration.tempFile("me.csv");
        //assertTrue("cF file: "+cF.getAbsolutePath(),false);
        
        TimeSeriesFileHandler.saveToText(data, cF, ",");
        
        data = TimeSeriesFileHandler.readFromText(cF, ",").get(0);
        
        TimeSeries expected = new TimeSeries();
        expected.add(0,1);
        expected.add(1,3);
        expected.add(2,2);
        
        
        checkSame(data,expected);
        
        List<TimeSeries> ser = makeTimeSeries();
        List<TimeSeries> expSer = copyTS(ser);
        
        data = new TimeSeries();
        data.add(0,1);
        data.add(1,2);
        data.add(1,3);
        data.add(1,4);
        data.add(2,2);        
        
        ser.add(1,data);
        expSer.add(1,expected);
        
        TimeSeriesFileHandler.saveToText(ser, cF, ",");
        ser = TimeSeriesFileHandler.readFromText(cF, ",");
        
        checkSame(ser, expSer);  
        
    }
    
    @Test
    public void testHandler() {
        
        try {
        File dir = Files.createTempDirectory("bdutil").toFile(); //new File("tmp")).getParentFile();
        
        List<TimeSeries> data = makeTimeSeries();
        List<TimeSeries> data1 = copyTS(data);
        
         checkSame(data, data1);
        
        
       
        
        
        File cF = new File(dir,"c.csv");
        //assertTrue("cF file: "+cF.getAbsolutePath(),false);
        
        TimeSeriesFileHandler.saveToText(data, cF, ",",ROUNDING_TYPE.NO_ROUNDING);
        
        File bF = new File(dir,"bra.txt");
        TimeSeriesFileHandler.saveToText(data,bF,"[ ]\t",ROUNDING_TYPE.NO_ROUNDING);

        

        
        List<TimeSeries> read = null;
        
        try {
            read = TimeSeriesFileHandler.readFromText(new File(dir,"niemamnie.txt"), ",");
            fail("IOException expected");
        } catch (IOException e) {};
        
        
         try {
            read = TimeSeriesFileHandler.readFromText(cF, " ");
            fail("FormatException expected");
        } catch (RobustFormatException e) {};
        
        
        checkSame(data,data1);
        
        read = TimeSeriesFileHandler.readFromText(cF, ",");
        
        checkSame(data,read);
        
        read = TimeSeriesFileHandler.readFromText(bF, "[ ]\t");
        
        checkSame(data1,read);
         
        
        } catch(Exception e) {
            fail("Got exception: "+e.getMessage()+", "+e.getClass().getName());
        }
        
    }
    

 
 
    protected List<TimeSeries> makeTimeSeries() {
        
        List<TimeSeries> list = new ArrayList<TimeSeries>();
        
        TimeSeries ts =null;
        
        ts = new TimeSeries();
        
        for (int i=0;i<10;i++) {
            ts.add(i, (int)(Math.random()*100));
        }
        for (int i=0;i<5;i++) {
            ts.add((Math.random()*100), (Math.random()*100));
        }
        list.add(ts);
        
        ts = new TimeSeries();
        
        for (int i=0;i<10;i++) {
            ts.add(i, (int)(Math.random()*100));
        }
        for (int i=0;i<5;i++) {
            ts.add((Math.random()*100), (Math.random()*100));
        }
        list.add(ts);
        
        ts = new TimeSeries();
        
        for (int i=0;i<15;i++) {
            ts.add((Math.random()*100), (Math.random()*100));
        }
        list.add(ts);
        
        
        
        return list;
    }

    protected void printOut(List<TimeSeries> list) {
        for (TimeSeries t : list) {
            System.out.println("T");
            for (Timepoint p : t) {
                System.out.println(p.getTime()+"\t"+p.getValue());
            }
        }        
    }
    
    protected List<TimeSeries> copyTS(List<TimeSeries> data) {
        
        List<TimeSeries> list = new ArrayList<TimeSeries>();
        
        for (TimeSeries t : data) {
            TimeSeries n = new TimeSeries();
            for (Timepoint p:t) n.add(p.getTime(),p.getValue());
            
            list.add(n);
        }
        return list;
    }

    public static void checkSame(List<TimeSeries> data1, List<TimeSeries> data2) {
        
        if (data1.size() != data2.size())
            fail("Not equal lists, different length");
        
        for (int i = 0;i<data1.size();i++) {
            
            checkSame(data1.get(i),data2.get(i));
        }
        
    }

    public static void checkSame(TimeSeries t1, TimeSeries t2) {
        
        List<Timepoint> l1 = t1.getTimepoints();
        List<Timepoint> l2 = t2.getTimepoints();
        
        if (l1.size() != l2.size()) fail("TS of different length");
        
        for (int i=0;i<l1.size();i++) {
            Timepoint p1 = l1.get(i);
            Timepoint p2 = l2.get(i);            
            
            assertEquals(p1.getTime(), p2.getTime(),0.0001);
            assertEquals(p1.getValue(), p2.getValue(),0.0001);
            
        }
        
        
    }
}
