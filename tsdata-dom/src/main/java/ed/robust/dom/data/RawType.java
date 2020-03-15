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
public enum RawType {
    
    RAW_RAW("RR","raw raw"),
    STD_RAW("R","raw"),
    SECONDARY("2nd","secondary"),
    SIMULATED("sim","simulation"),
    CONDITION("cond","condition");
    
    
    RawType(String shortName,String longName) {
        this.shortName = shortName;
        this.longName = longName;
    }
    
    public final String shortName;
    public final String longName;
    
    private static Map<RawType,String> longNames;
    
    static {
        Map<RawType,String> map = new EnumMap<>(RawType.class);
        for (RawType type : values())
            map.put(type,type.longName);
        longNames = Collections.unmodifiableMap(map);        
    }
    
    public static Map<RawType,String> getLongNames() {
        return longNames;        
    }
    
}
