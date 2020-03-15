/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ed.robust.dom.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Utility class that represents a map which values are set of elements. That way multiple elements
 * can be stored under one key, but under one key only single instance of the element can be stored.
 * Upon construction using of sorted keys can be chosen, that way iterators and results will be processed
 * using natural order of the keys.
 * @author tzielins
 */
public class SetMap<K,V> implements Map<K, Set<V>> {

    protected Map<K,Set<V>> map;

    public SetMap() {
	map = new HashMap<>();
    }

    public SetMap(boolean keySorted) {
	this();
	if (keySorted) map = new TreeMap<>();
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
    public Set<V> get(Object key) {
	return map.get(key);
    }

    @Override
    public Set<V> put(K key, Set<V> value) {
	return map.put(key, value);
    }

    @Override
    public Set<V> remove(Object key) {
	return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends Set<V>> m) {
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
    public Collection<Set<V>> values() {

	return map.values();
    }

    @Override
    public Set<Entry<K, Set<V>>> entrySet() {
	return map.entrySet();
    }


    /**
     * Adds new element into the set under the given key. If the set was not present
     * a new set is created.
     * @param key under which insert the element
     * @param element to be inserted into the set stored under the key.
     */
    public void add(K key,V element) {
	Set<V> list = map.get(key);
	if (list == null) {
	    list = new HashSet<>();
	    map.put(key, list);
	}
	list.add(element);
    }
    
    public void addAll(K key,Collection<V> elements) {
        Set<V> list = map.get(key);
	if (list == null) {
	    list = new HashSet<>();
	    map.put(key, list);
	}
        list.addAll(elements);
    }

}
