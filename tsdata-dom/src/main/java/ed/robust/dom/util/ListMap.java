/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ed.robust.dom.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * Utility class that represents a map containing list of elements as its entries.
 * Everytime new element is put for a key, the element is added to the list under the key.
 * That way multiple elements can be stored under way key. Upon construction a key sorted version
 * can be creates, which means that the key and values iterators will be processed using natural
 * ordering of the keys.
 * @author tzielins
 */
public class ListMap<K,V> implements Map<K, List<V>>{

    protected Map<K,List<V>> map;

    public ListMap()
    {
	map = new HashMap<K, List<V>>();
    }

    /**
     * Creates new instance of listMap, which is going to be based on TreeMap, hence
     * the iterator will preserve natural ordering of the keys.
     * @param keySorted
     */
    public ListMap(boolean keySorted) {
	this();
	if (keySorted) map = new TreeMap<K, List<V>>();
    }


    @Override
    public int size() {
	return map.size();
    }

    @Override
    public boolean isEmpty() {
	return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
	return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
	return map.containsValue(value);
    }

    @Override
    public List<V> get(Object key) {
	return map.get(key);
    }

    @Override
    public List<V> put(K key, List<V> value) {
	return map.put(key, value);
    }

    @Override
    public List<V> remove(Object key) {
	return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends List<V>> m) {
	map.putAll(m);
    }

    @Override
    public void clear() {
	map.clear();
    }

    @Override
    public Set<K> keySet() {
	return map.keySet();
    }

    @Override
    public Collection<List<V>> values() {
	
	return map.values();
    }

    @Override
    public Set<Entry<K, List<V>>> entrySet() {
	return map.entrySet();
    }


    /**
     * Adds the element into the map. The element is added into the list under the key, so
     * multiple elements are stored under same key.
     * @param key to be stored ad
     * @param element to be added into the list under the key
     */
    public void add(K key,V element) {
	List<V> list = map.get(key);
	if (list == null) {
	    list = new ArrayList<V>();
	    map.put(key, list);
	}
	list.add(element);	
    }
    
    /**
     * Adds the elements into the map. The elements are added into the list under the given key.
     * @param key to be stored at
     * @param values collections of values to stored under this key
     */
    public void addAll(K key,Collection<? extends V> values) {
	List<V> list = map.get(key);
	if (list == null) {
	    list = new ArrayList<V>();
	    map.put(key, list);
	}
	list.addAll(values);	        
    }
    
    /**
     * Adds the content of the given argument into the map. Values under each key of the argument map
     * are added to the list under the corresponding key, so that both maps are merged
     * @param map collection of key,values to be added
     */
    public void addAll(Map<K, List<V>> map) {
        for (Map.Entry<K, List<V>> entry : map.entrySet())
            addAll(entry.getKey(),entry.getValue());
    }
    
    
    
}
