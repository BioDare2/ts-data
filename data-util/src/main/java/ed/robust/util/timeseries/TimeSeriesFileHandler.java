/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

import ed.robust.dom.data.TimeSeries;
import ed.robust.dom.data.Timepoint;
import ed.robust.dom.util.ListMap;
import ed.robust.error.RobustFormatException;
import java.io.*;
import java.util.*;

/**
 * Handles file operations on timeseries sets (reading, writing, labelling).
 * @author tzielins
 */
public class TimeSeriesFileHandler {

    
    /**
     * Saves time series to text file. First column is time column, second column is data column separated by
     * the separator text
     * @param data time series to be saved
     * @param file destination file
     * @param separator text that should be used to separate the columns with
     * @throws IOException 
     */
    public static void saveToText(TimeSeries data,File file,String separator) throws IOException {
        saveToText(Arrays.asList(data),file,separator);
    }
    
    /**
     * Saves time series to text file.
     * TimeSereies are saved in a tabular formal, first column is the time values, subsequent columns contains
     * values for each time series for those times, columns (numbers) are separated by the text from separator parameter.
     * If timeserie does not contain value for particular time (row) there is no value in this column for this row (so to column separators after each other).
     * @param series list of timeseries to be save to a file. TimeSeries can have different time values, in such a case as 'unify' time column
     * will be produced, and the data columns will have gaps for missing values.
     * @param file destination file
     * @param separator text that should be used to separate the columns with
     * @throws IOException 
     */
    public static void saveToText(List<TimeSeries> series,File file, String separator) throws IOException {
        saveToText(series, null, file, separator);
    }
    
    /**
     * Saves time series to text file.
     * TimeSereies are saved in a tabular formal, first column is the time values, subsequent columns contains
     * values for each time series for those times, columns (numbers) are separated by the text from separator parameter.
     * If timeserie does not contain value for particular time (row) there is no value in this column for this row (so to column separators after each other).
     * @param series list of timeseries to be save to a file. TimeSeries can have different time values, in such a case as 'unify' time column
     * will be produced, and the data columns will have gaps for missing values.
     * @param file destination file
     * @param separator text that should be used to separate the columns with
     * @param rounding rounding precission for time values
     * @throws IOException 
     */
    public static void saveToText(List<TimeSeries> series,File file, String separator,ROUNDING_TYPE rounding) throws IOException {
        saveToText(series, null, file, separator,rounding);
    }
    
    
    /**
     * Saves time series to text file and proceeds them with requested columns ids.
     * TimeSereies are saved in a tabular formal, first column is the time values, subsequent columns contains
     * values for each time series for those times, columns (numbers) are separated by the text from separator parameter.
     * If timeserie does not contain value for particular time (row) there is no value in this column for this row (so to column separators after each other).
     * <p>Before the table with time and data columns columns ids are inserted. 
     * (as each is going to proceed particular column)
     * @param series list of timeseries to be save to a file. TimeSeries can have different time values, in such a case as 'unify' time column
     * will be produced, and the data columns will have gaps for missing values.
     * @param file destination file
     * @param separator text that should be used to separate the columns with
     * @param ids list of id to mark the data columns with, it must be the same length as series list
     * @throws IOException 
     */
    public static void saveToText(List<TimeSeries> series, File file, String separator,List<String> ids) throws IOException {
        
        List<List<String>> headers = new ArrayList<List<String>>();
        List<String> list = new ArrayList<String>(ids);
        list.add(0,"id");
        headers.add(list);
        saveToText(series, headers, file, separator);
    }

    /**
     * Saves time series to text file and proceeds them with requested columns headers.
     * TimeSereies are saved in a tabular formal, first column is the time values, subsequent columns contains
     * values for each time series for those times, columns (numbers) are separated by the text from separator parameter.
     * If timeserie does not contain value for particular time (row) there is no value in this column for this row (so to column separators after each other).
     * <p>Before the table with time and data columns is printed rows description is inserted. Description consists of rows of 
     * column headers which length must match the number of timeseries being saved
     * (as each is going to proceed particular column)
     * @param series list of timeseries to be save to a file. TimeSeries can have different time values, in such a case as 'unify' time column
     * will be produced, and the data columsn will have gaps for missing values.
     * @param headers list of list of labels that should annotate the data columns (list of rows of cells with text). each header (list of string)
     * must start with name of this header (row) followed by labels for each time series (so the length of hear must be series.size()+1
     * @param file destination file
     * @param separator text that should be used to separate the columns with
     * @throws IOException if writing is not possible
     */
    public static void saveToText(List<TimeSeries> series, List<List<String>> headers,File file, String separator) throws IOException {
       saveToText(series,headers,null,file,separator);
    }
    
    /**
     * Saves time series to text file and proceeds them with requested columns headers.
     * TimeSereies are saved in a tabular formal, first column is the time values, subsequent columns contains
     * values for each time series for those times, columns (numbers) are separated by the text from separator parameter.
     * If timeserie does not contain value for particular time (row) there is no value in this column for this row (so to column separators after each other).
     * <p>Before the table with time and data columns is printed rows description is inserted. Description consists of rows of 
     * column headers which length must match the number of timeseries being saved
     * (as each is going to proceed particular column)
     * @param series list of timeseries to be save to a file. TimeSeries can have different time values, in such a case as 'unify' time column
     * will be produced, and the data columsn will have gaps for missing values.
     * @param headers list of list of labels that should annotate the data columns (list of rows of cells with text). each header (list of string)
     * must start with name of this header (row) followed by labels for each time series (so the length of hear must be series.size()+1
     * @param file destination file
     * @param separator text that should be used to separate the columns with
     * @param rounding the precission to which times should be rounded
     * @throws IOException if writing is not possible
     */
    public static void saveToText(List<TimeSeries> series, List<List<String>> headers,File file, String separator,ROUNDING_TYPE rounding) throws IOException {
       saveToText(series,headers,null,file,separator,rounding);
    }
    

    /**
     * Saves time series to text file and proceeds them with requested columns headers and free text.
     * TimeSereies are saved in a tabular formal, first column is the time values, subsequent columns contains
     * values for each time series for those times, columns (numbers) are separated by the text from separator parameter.
     * If timeserie does not contain value for particular time (row) there is no value in this column for this row (so to column separators after each other).
     * <p>Before the table with time and data columns is printed rows description is inserted. Description consists of rows of free text (each
     * row can have multiple cells/columns), and with column headers which differs from free text that their length must match the number of timeseries being saved
     * (as each is going to proceed particular column)
     * @param series list of timeseries to be save to a file. TimeSeries can have different time values, in such a case as 'unify' time column
     * will be produced, and the data columsn will have gaps for missing values.
     * @param headers list of list of labels that should annotate the data columns (list of rows of cells with text). each header (list of string)
     * must start with name of this header (row) followed by labels for each time series (so the length of hear must be series.size()+1
     * @param freeText list of list of text that should appear at begginign of the file, each list of text is one row which can span over many cells/columns
     * @param file destination file
     * @param separator text that should be used to separate the columns with
     * @throws IOException if writing is not possible
     */
    public static void saveToText(List<TimeSeries> series, List<List<String>> headers,List<List<String>> freeText,File file, String separator) throws IOException {
        saveToText(series,headers,freeText,file,separator,ROUNDING_TYPE.NO_ROUNDING);
    }
    
    /**
     * Saves time series to text file and proceeds them with requested columns headers and free text.
     * TimeSereies are saved in a tabular formal, first column is the time values, subsequent columns contains
     * values for each time series for those times, columns (numbers) are separated by the text from separator parameter.
     * If timeserie does not contain value for particular time (row) there is no value in this column for this row (so to column separators after each other).
     * <p>Before the table with time and data columns is printed rows description is inserted. Description consists of rows of free text (each
     * row can have multiple cells/columns), and with column headers which differs from free text that their length must match the number of timeseries being saved
     * (as each is going to proceed particular column)
     * @param series list of timeseries to be save to a file. TimeSeries can have different time values, in such a case as 'unify' time column
     * will be produced, and the data columsn will have gaps for missing values.
     * @param headers list of list of labels that should annotate the data columns (list of rows of cells with text). each header (list of string)
     * must start with name of this header (row) followed by labels for each time series (so the length of hear must be series.size()+1
     * @param freeText list of list of text that should appear at begginign of the file, each list of text is one row which can span over many cells/columns
     * @param file destination file
     * @param separator text that should be used to separate the columns with
     * @param rounding the precission to which times should be rounded
     * @throws IOException if writing is not possible
     */
    public static void saveToText(List<TimeSeries> series, List<List<String>> headers,List<List<String>> freeText,File file, String separator,ROUNDING_TYPE rounding) throws IOException {
        
        
        ListMap<Double,Double> listMap = makeListMap(series,rounding);
        {
            int S = listMap.values().iterator().next().size();
            for (List<Double> list : listMap.values())
                if (list.size() != S) throw new IOException("Error when assembling table of values, one entry has wrong legnth: "+S+"!="+list.size());
        }
        Set<Double> times = new TreeSet<Double>(listMap.keySet());
        
        BufferedWriter out = null;
        
        if (headers == null) headers = new ArrayList<List<String>>();
        if (freeText == null) freeText = new ArrayList<List<String>>();
        
        for(List<String> labels : headers) 
            if (labels.size() != (series.size()+1))
                throw new IllegalArgumentException("Each header list must start with its name followed by the values for each time series, got "+labels.size()+" values, expected "+(series.size()+1));
        
        try {
            out = new BufferedWriter(new FileWriter(file));

            for (List<String> text : freeText) {
                if (text == null || text.isEmpty()) {
                    out.newLine(); 
                    continue;
                };
                
                out.write(text.get(0));
                for (String label : text.subList(1, text.size())) {
                    out.write(separator);
                    out.write(label);
                }
                
                out.newLine();
                
            }
            
            for (List<String> labels : headers) {
            
                out.write(labels.get(0));
                
                for (String label : labels.subList(1, labels.size())) {
                    out.write(separator);
                    out.write(label);
                }
                
                out.newLine();
            }
            
            
            for (Double time : times) {
                out.write(time.toString());
                List<Double> values = listMap.get(time);
                for (Double val : values) {
                    out.write(separator);
                    if (val != null) out.write(val.toString());
                }
                out.newLine();
            
            }
        } finally {
            if (out != null) out.close();
        }
        
        
    }
    
    public static void saveToText(Map<String,TimeSeries> series,File file, String separator,ROUNDING_TYPE rounding) throws IOException {

        List<TimeSeries> data = new ArrayList<>();
        
        List<String> labels = new ArrayList<>();
        labels.add("Label");

        List<List<String>> headers = new ArrayList<>();
        headers.add(labels);

        List<List<String>> freeText = Collections.emptyList();
        
        series.forEach((label, ts) -> {
        
            data.add(ts);
            labels.add(label);
            
        });
        
        saveToText(data, headers, freeText, file, separator, rounding);
    }    

    /**
     * Reads time series data save in a text file in tabular format. 
     * Tabular format means that first column holds the time values, the subsequent columns have data for each timeseries, missing
     * cells in the table (two separators behind each other) denotes missing value for this time in particular column (timeseries) such time
     * will not apear in the resulting timeseries.
     * @param file text file to read from
     * @param columnSeparator text which is used to separted the columns with, usually ','
     * @return list of timeseries that have been read from the file (each can have different time values)
     * @throws RobustFormatException if the data table has wrong format, for example columns are not separtated with separator, or in some cells non numerical values are found
     * @throws IOException 
     */
    public static List<TimeSeries> readFromText(File file, String columnSeparator) throws RobustFormatException, IOException {
        return readFromText(file, columnSeparator, 0);
    }
    
    /**
     * Reads time series data save in a text file in tabular format. 
     * Tabular format means that first column holds the time values, the subsequent columns have data for each timeseries, missing
     * cells in the table (two separators behind each other) denotes missing value for this time in particular column (timeseries) such time
     * will not apear in the resulting timeseries.
     * @param file text file to read from
     * @param columnSeparator text which is used to separted the columns with, usually ','
     * @param skipLines how many lines from the file should be skipped before reading of data table is commenced. It helps reading from files
     * which contains some column description (metadata) for the table data, that way initial lines of text can be ignored and after them tne table
     * with numbers is read
     * @return list of timeseries that have been read from the file (each can have different time values)
     * @throws RobustFormatException if the data table has wrong format, for example columns are not separtated with separator, or in some cells non numerical values are found
     * @throws IOException 
     */
    public static List<TimeSeries> readFromText(File file, String columnSeparator,int skipLines) throws RobustFormatException, IOException {
        
        if (skipLines < 0) throw new IllegalArgumentException("Lines to skip must be >=0");
        
        
        BufferedReader input = null;
        try {
            input = new BufferedReader(new FileReader(file));            
            String line;
            for (int i = 0;i<skipLines;i++) input.readLine();
            
            String separator = escapeSeparator(columnSeparator);
            //System.out.println("Separator: "+separator);
            List<TimeSeries> series = new ArrayList<TimeSeries>();

            while ((line = input.readLine())!= null) {

                String tokens[] = line.split(separator);
                if (tokens.length < 1) throw new RobustFormatException("Row has to contains at least time column");

                while (series.size() < (tokens.length)) series.add(new TimeSeries());

                String s ="";
                int col = 0;
                try {
                    s = tokens[col];
                    double time = Double.parseDouble(s);

                    for (col=1;col<tokens.length;col++) {
                        s = tokens[col];
                        if (s == null || s.trim().isEmpty()) continue;
                        double val = Double.parseDouble(s);
                        series.get(col).add(time, val);
                    }
                } catch (NumberFormatException e) {
                    throw new RobustFormatException("Expected number but found: "+s+", in col:"+col+", row: "+line);
                }

            }

            return series.subList(1, series.size());
        } finally {
            if (input != null) 
                input.close();
        }
        
    }
    
    /**
     * Reads one line from text files and converts it into list of labels using columnSeparator to split the labels with.
     * @param file file to read from
     * @param columnSeparator text that separates columns
     * @param labelRow nr of line to read from (1-based)
     * @return list of labels
     * @throws IOException
     * @throws RobustFormatException 
     */
    public static List<String> readLabels(File file, String columnSeparator,int labelRow) throws IOException, RobustFormatException {    
        
        if (labelRow <= 0) throw new IllegalArgumentException("Label row must be > 0");
        
        BufferedReader input = new BufferedReader(new FileReader(file));
        
        int skipLines = labelRow - 1;
        try {
            for (int i = 0;i<skipLines;i++) input.readLine();
            
            String separator = escapeSeparator(columnSeparator);
            String line = input.readLine();
            if (line == null) throw new RobustFormatException("There was no label row "+labelRow+"in the file");
            
            String tokens[] = line.split(separator);
            
            return Arrays.asList(tokens);

            

        } finally {
            if (input != null) 
                input.close();
        }
        
    }

    /**
     * Escapes illegal characters in columnSeparator text which would affect the reading of rows and splitting them into columns.
     * Method is needed cause splitting rows into columns is done using regular expression for which some characeters are reserved
     * @param columnSeparator text with column separator
     * @return text that separates columns but will not interfer with reading regular expressions.
     */
    public static String escapeSeparator(String columnSeparator) {
        
        String[] reserved = {"\\","[","]","|","*","+"};
        
        String sep = columnSeparator;
        for (String res : reserved) {
            if (sep.contains(res)) sep = sep.replace(res, "\\"+res);
        }
        return sep;
    }

    protected static ListMap<Double, Double> makeListMap(List<TimeSeries> series,ROUNDING_TYPE rounding) {
        
        DataRounder rounder = new DataRounder(rounding);
        
        ListMap<Double, Double> map = new ListMap<Double, Double>();
        
        for (int i = 0;i<series.size();i++) {
            TimeSeries seria = series.get(i);
            
            ListMap<Double, Double> multipleValues = new ListMap<Double, Double>();
            
            for (Timepoint tp : seria) {
        
                double time = rounder.round(tp.getTime());
                List<Double> vals = map.get(time);
                if (vals == null) {
                    vals = new ArrayList<Double>();
                    map.put(time, vals);
                }
                while(vals.size() < i) vals.add(null);
                
                if (vals.size() == i) //just new entry
                    vals.add(tp.getValue());
                else {//there was entry already so we have multipe entries
                    List<Double> multiVals = multipleValues.get(time);
                    if (multiVals == null) //2nd value to be added, so the existing values has to go there 
                    {
                        multipleValues.add(time, vals.get(i));
                    }
                    //we adding ourselved
                    multipleValues.add(time, tp.getValue());
                    vals.remove(i);
                    vals.add(i,averageVal(multipleValues.get(time)));
                }
            }
        }
        for (List<Double> list : map.values()) {
            while (list.size() < series.size()) list.add(null);
        }
        return map;
    }

    protected static double averageVal(List<Double> values) {
        double sum = 0;
        int N = 0;
        for (Double d : values) {
            if (d == null) continue;
            sum+=d;
            N++;
        }
        if (N != 0) return sum/N;
        return 0;
    }
    
}
