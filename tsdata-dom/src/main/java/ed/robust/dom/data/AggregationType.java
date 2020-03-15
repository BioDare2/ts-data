/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.dom.data;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 *
 * @author tzielins
 */
public enum AggregationType {
    
    NO_AGG("","none"),
    AVG("AVG","avg."),
    AVG_NO_LINES("LAVG","(nolines) avg.");
    
    AggregationType(String shortName,String longName) {
        this.shortName = shortName;
        this.longName = longName;
    }
    
    public final String shortName;
    public final String longName;

    private static Map<AggregationType,String> longNames;
    
    public static Map<AggregationType,String> getLongNames() {
        if (longNames == null) {
            Map<AggregationType,String> map = new EnumMap<>(AggregationType.class);
            for (AggregationType type : values())
                map.put(type,type.longName);
            longNames = Collections.unmodifiableMap(map);
        }
        return longNames;        
    }
    
}
