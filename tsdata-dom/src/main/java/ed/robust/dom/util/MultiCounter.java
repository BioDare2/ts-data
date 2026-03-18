/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.dom.util;

/**
 * A utility class for counting numbers of multiple objects identified by its key. It is a simple container that 
 * keeps track of values identified by their keys. The value for each keep can increased or decreased. 
 * @author tzielins
 */
public interface MultiCounter<K> {
    
    /**
     * Increases count for the given key and returns its updated value.
     * @param key key for which counter should be increased
     * @return the value after the increase 
     */
    public int increase(K key);
    
    /**
     * Decreases count for the given key and returns its updated value.
     * @param key key for which counter should be increased
     * @return the value after the decrease 
     */
    public int decrease(K key);
    
    /**
     * Gives current count for the given key. 
     * @param key key which counter is queried
     * @return the current value of the conter for this key or 0 if the key is not present
     */
    public int get(K key);
    
}
