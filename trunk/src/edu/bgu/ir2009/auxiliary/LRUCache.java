package edu.bgu.ir2009.auxiliary;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K, V> {
    private final Map<K, V> map;
    private final Object lock = new Object();

    public LRUCache(final int maxEntries) {
        this.map = new LinkedHashMap<K, V>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > maxEntries;
            }
        };
    }

    public V get(K key) {
        V v;
        synchronized (lock) {
            v = map.remove(key);
            if (v != null) {
                map.put(key, v);
            }
        }
        return v;
    }

    public V put(K key, V value) {
        V res;
        synchronized (lock) {
            res = map.put(key, value);
        }
        return res;
    }
}
