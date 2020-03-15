package ed.robust.dom.util;

import java.util.Comparator;

public class PairComparator<L extends Comparable<L>,R> implements Comparator<Pair<L,R>> {

	@Override
	public int compare(Pair<L, R> arg0, Pair<L, R> arg1) {
		if (arg0 == null) return (arg1 == null ? 0 : 1);
		if (arg1 == null) return -1;
		if (arg0.getLeft() == null) return (arg1.getLeft() == null ? 0 : 1);
		return arg0.getLeft().compareTo(arg1.getLeft());
	}

	
}
