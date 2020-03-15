package ed.robust.dom.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for mapping pairs of values.
 * Main usage of this class is in JAX-WS mapping of a Map as a List of MapEntry values, as the maps are not supported in JAXB.
 * So instead of using Map<K,V> obj in a method, one can use List<MapEntry<K,V>> obj.
 *
 * @author tzielins
 */
public class MapEntry<K,V> {

    K key;
    V value;

    public MapEntry() {

    }

    public MapEntry(K k, V v)
    {
	this.key = k;
	this.value = v;
    }

    /**
     * Converts a map into a list of MapEntry that way the list can be easily used
     * where map is not supported.
     * @param <KT> key type
     * @param <VT> value type
     * @param map map to be converted
     * @return list of pairs key,value
     */
    public static <KT,VT> List<MapEntry<KT,VT>> toList(Map<KT,VT> map)
    {
	List<MapEntry<KT,VT>> list = new ArrayList<>();
	for (KT k : map.keySet())
	{
	    list.add(new MapEntry<>(k,map.get(k)));
	}
	return list;
    }

    /**
     * Converts a list of MapEntry into a map.
     * @param <KT> key type
     * @param <VT> value type
     * @param list to be converted, contains pairs of key,values
     * @return map that maps keys into values, if the list had multiple entries for same key
     * than only one value will be preserved
     */
    public static <KT,VT> Map<KT,VT> toMap(List<MapEntry<KT,VT>> list)
    {
	Map<KT,VT> map = new HashMap<KT, VT>();
	for (MapEntry<KT,VT> en : list)
	    map.put(en.getKey(), en.getValue());
	return map;
    }


    public K getKey() {
	return key;
    }

    public void setKey(K key) {
	this.key = key;
    }

    public V getValue() {
	return value;
    }

    public void setValue(V value) {
	this.value = value;
    }


}
