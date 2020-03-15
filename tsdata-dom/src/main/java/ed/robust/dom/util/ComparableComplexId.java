/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.dom.util;

import java.util.List;

/**
 *
 * @author tzielins
 */
public class ComparableComplexId<K extends Comparable> extends ComplexId<K> implements Comparable<ComparableComplexId> {

    @SafeVarargs
    public ComparableComplexId(K ... keys) {
        super(keys);
    }
    
    public ComparableComplexId(List<? extends K> keys) {
        super(keys);
    }
    
    
    @Override
    @SuppressWarnings("unchecked")
    public int compareTo(ComparableComplexId o) {
        
        if (o == null) throw new NullPointerException("Comparing with null object");
        
        if (this.size() != o.size()) return Integer.compare(this.size(), o.size());
        
        for (int i = 0;i<ids.size();i++) {
            Comparable c1 = ids.get(i);
            Comparable c2 = (Comparable)o.ids.get(i);
        
            int v = c1.compareTo(c2);
            if (v == 0) continue;
            return v;
        }
        return 0;
    }
    
}
