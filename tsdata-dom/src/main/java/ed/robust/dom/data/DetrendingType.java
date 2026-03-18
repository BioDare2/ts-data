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
public enum DetrendingType {
    
    NO_DTR("","no dtr"),
    LIN_DTR("L","linear dtr"),
    POLY_DTR("P","cubic dtr"),
    BASE_DTR("B","baseline dtr"),
    BAMP_DTR("AB","amp&baseline dtr");
    
    DetrendingType(String shortName,String longName) {
        this.shortName = shortName;
        this.longName = longName;
    }
    
    public final String shortName;
    public final String longName;

    
    private static Map<DetrendingType,String> longNames;
    
    public static Map<DetrendingType,String> getLongNames() {
        if (longNames == null) {
            Map<DetrendingType,String> map = new EnumMap<>(DetrendingType.class);
            for (DetrendingType type : DetrendingType.values())
                map.put(type,type.longName);
            longNames = Collections.unmodifiableMap(map);
        }
        return longNames;        
    }
}
