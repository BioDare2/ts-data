/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ed.robust.dom.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author tzielins
 */
@SuppressWarnings("unchecked")
public class Copier {

    public static <V extends Copyable> List<V> copyList(List<V> src) {

	List list = new ArrayList<>();

	for (V element : src)
	    list.add(element.copy());

	return list;
    }

    public static <V extends Copyable> Set<V> copySet(Set<V> src) {

	Set set = new HashSet<>();

	for (V element : src)
	    set.add(element.copy());

	return set;
    }
}
