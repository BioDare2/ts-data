/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.dom.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread safe and concurrent implementation of the counter. It is based on concurrent package so it should
 * be efficient and scalable.
 * @author tzielins
 */
public class MultiCounterConcurrentImp<K> implements MultiCounter<K> {

    private final ConcurrentMap<K, AtomicInteger> map = new ConcurrentHashMap<K, AtomicInteger>();
    
    @Override
    public int increase(K key) {
        
        return getVal(key).incrementAndGet();
    }

    @Override
    public int decrease(K key) {
        return getVal(key).decrementAndGet();
    }

    @Override
    public int get(K key) {
        return getVal(key).get();
    }

    protected AtomicInteger getVal(K key) {
        if (!map.containsKey(key)) map.putIfAbsent(key, new AtomicInteger(0));
        
        return map.get(key);
    }
    
}
