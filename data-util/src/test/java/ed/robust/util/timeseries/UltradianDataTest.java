/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

import ed.robust.dom.data.TimeSeries;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import ed.robust.util.timeseries.LocalRegressionDetrending.TrendPack;

/**
 *
 * @author Zielu
 */
public class UltradianDataTest {
    
    
    //@Test
    public void testDetrendData() throws Exception
    {
        System.out.println("TEST detreding ultradian");
        
        File file = new File("D:/Performance/ultradian data.csv");
        
        List<TimeSeries> series = TimeSeriesFileHandler.readFromText(file, ",", 1);
        
        List<String> ids = TimeSeriesFileHandler.readLabels(file, ",", 1);
        ids = ids.subList(1,ids.size());
        
        List<TrendPack> trends = findTrends(series);
        
        List<TimeSeries> baseDetrended = new ArrayList<TimeSeries>();
        List<TimeSeries> ampDetrended = new ArrayList<TimeSeries>();
        List<TimeSeries> all = new ArrayList<TimeSeries>();
        List<String> allIds = new ArrayList<String>();
        
        for (int i =0;i<series.size();i++) {
            TimeSeries data = series.get(i);
            TrendPack trend = trends.get(i);
            
            TimeSeries ampDet = LocalRegressionDetrending.removeTrend(data, trend);            
            
            trend = LocalRegressionDetrending.convertToBaseLineTriend(trend);
            TimeSeries basDet = LocalRegressionDetrending.removeTrend(data, trend);            
            
            baseDetrended.add(basDet);
            ampDetrended.add(ampDet);
            all.add(data);
            all.add(basDet);
            all.add(ampDet);
            all.add(TimeSeriesOperations.lineDetrended(data));
            String id = ids.get(i);
            allIds.add(id);
            allIds.add("base "+id);
            allIds.add("b+a "+id);
            allIds.add("lin "+id);
        }
        
        file = new File("D:/Performance/ultradian data basedtr2.csv");
        TimeSeriesFileHandler.saveToText(baseDetrended, file, ",", ids);
        
        file = new File("D:/Performance/ultradian data baseampdtr2.csv");
        TimeSeriesFileHandler.saveToText(ampDetrended, file, ",", ids);

        file = new File("D:/Performance/ultradian data comp2.csv");
        TimeSeriesFileHandler.saveToText(all, file, ",", allIds);
        
        
    }
    
    public List<TrendPack> findTrends(List<TimeSeries> series) throws InterruptedException {
        
        List<TrendPack> trends = new ArrayList<TrendPack>();
        
        LocalRegressionDetrending finder = new LocalRegressionDetrending();
        
        for (TimeSeries data : series)
            trends.add(finder.findSmartTrends(data, false));
        
        return trends;
    }
}
