/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ed.robust.dom.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author tzielins
 */
public class ComplexId<V> implements Iterable<V> {

    protected final List<V> ids = new ArrayList<>();

    public ComplexId(List<? extends V> keys) {
	for (V key : keys) ids.add(key);
    }

    public ComplexId(V key) {
	ids.add(key);
    }

    @SafeVarargs
    public ComplexId(V... keys) {
	ids.addAll(Arrays.asList(keys));
    }

    public V getKey(int index) {
	return ids.get(index);
    }
    
    public ComplexId<V> subKey(int start,int end) {
        List<V> list = this.ids.subList(start, end);
        return new ComplexId<>(list);
    }
    
    public int size() {
        return ids.size();
    }

    @Override
    public int hashCode() {
	return ids.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
	if (obj == this)
	    return true;

	if (!(obj instanceof ComplexId))
	    return false;

        ComplexId obj2 = (ComplexId)obj;
        return ids.equals(obj2.ids);

    }

    @Override
    public String toString() {        
        StringBuilder sb = new StringBuilder("[");
        for (V id : ids) sb.append(id).append(",");
        sb.append("]");
        return sb.toString();
                
    }

    @Override
    public Iterator<V> iterator() {
        return ids.iterator();
    }
    
    

}