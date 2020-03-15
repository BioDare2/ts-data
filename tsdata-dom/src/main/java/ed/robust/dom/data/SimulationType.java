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
public enum SimulationType {
    
    NONE("","none"),
    PPA_FIT("fit","ppa fit"),
    RETROS_RATE("rate","retros rate"),
    RETROS_RNA("RNA","retros RNA"),
    RETROS_PROT("PROT","retros PROT");
    
    
    SimulationType(String shortName,String longName) {
        this.shortName = shortName;
        this.longName = longName;
    }
    
    public final String shortName;
    public final String longName;
    
    private static Map<SimulationType,String> longNames;
    
    static {
        Map<SimulationType,String> map = new EnumMap<>(SimulationType.class);
        for (SimulationType type : values())
            map.put(type,type.longName);
        longNames = Collections.unmodifiableMap(map);        
    }
    
    public static Map<SimulationType,String> getLongNames() {
        return longNames;        
    }
    
}
