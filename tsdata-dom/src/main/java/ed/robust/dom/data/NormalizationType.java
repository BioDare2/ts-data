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
public enum NormalizationType {
 
    NO_NORM("","none"),
    MEAN_NORM("M","to mean"),
    MAX_NORM("MX","to max");
    
    NormalizationType(String shortName,String longName) {
        this.shortName = shortName;
        this.longName = longName;
    }
    
    public final String shortName;
    public final String longName;

    
    private static Map<NormalizationType,String> longNames;
    
    public static Map<NormalizationType,String> getLongNames() {
        if (longNames == null) {
            Map<NormalizationType,String> map = new EnumMap<>(NormalizationType.class);
            for (NormalizationType type : values())
                map.put(type,type.longName);
            longNames = Collections.unmodifiableMap(map);
        }
        return longNames;        
    }
    
    
}
