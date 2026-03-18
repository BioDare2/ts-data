/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.dom.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author tzielins
 */
public class ListsUtil {
 
    public static <E> List<List<E>> split(Collection<E> col,int size) {
        
        if (size < 1) throw new IllegalArgumentException("Size must be > 0");
        if (col == null) return new ArrayList<>();
        
        List<E> list;
        if (col instanceof List ) list = (List<E>)col;
        else list = new ArrayList<>(col);
        
        List<List<E>> split = new ArrayList<>(list.size() / size +1);
        
        int offset = 0;
        while (offset < list.size()) {
            int last = offset+size;
            if (last > list.size()) last = list.size();
            split.add(list.subList(offset, last));
            offset = last;
        }
        return split;
    }
    
    
}
