/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.dom.util;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author tzielins
 */
public class MultiCounterMapImp<K> implements MultiCounter<K> {

    private final Map<K,Counter> map = new HashMap<K,Counter>();
    
    @Override
    public int increase(K key) {
        
        return getVal(key).increase();
    }

    @Override
    public int decrease(K key) {
        return getVal(key).decrease();
    }

    @Override
    public int get(K key) {
        return getVal(key).get();
    }

    protected Counter getVal(K key) {
        if (!map.containsKey(key)) map.put(key, new Counter());
        
        return map.get(key);
    }
    
    
    protected static class Counter {
        int count = 0;
        
        public int increase() {
            return ++count;
        }
        
        public int decrease() {
            return --count;
        }
        
        public int get() {
            return count;
        }
    }
}
