/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biodare.data.json;

import tools.jackson.core.JacksonException;
import tools.jackson.core.util.DefaultIndenter;
import tools.jackson.core.util.DefaultPrettyPrinter;
import tools.jackson.core.util.Separators;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.SerializationFeature;
import ed.robust.dom.data.TimeSeries;
import ed.robust.dom.data.Timepoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Zielu
 */
public class TimeSeriesModuleTest {
    
    public TimeSeriesModuleTest() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void serializedTS() throws JacksonException {
        
	ObjectMapper mapper = JsonMapper.builder()
	    .addModule(new TimeSeriesModule())
	    .defaultPrettyPrinter(
				  new DefaultPrettyPrinter()
				  .withSeparators(
						  Separators.createDefaultInstance()
						  .withObjectEntrySeparator(',')
						  )
				  .withArrayIndenter(DefaultPrettyPrinter.NopIndenter.instance())
				  .withObjectIndenter(new DefaultIndenter(" ", "\n"))
				  )
	    .enable(SerializationFeature.INDENT_OUTPUT)
	    .build();

        TimeSeries data = new TimeSeries();
        data.add(1,2);
        data.add(3,5);
        data.add(new Timepoint(4, 6, 0.1, 0.2));
        data.add(new Timepoint(5,7,0.2, Timepoint.STD_DEV));
        
        String json = mapper.writeValueAsString(data);
        
        assertNotNull(json);
        
        TimeSeries cpy = mapper.readValue(json, TimeSeries.class);
        assertEquals(data,cpy);
    }

    
}
