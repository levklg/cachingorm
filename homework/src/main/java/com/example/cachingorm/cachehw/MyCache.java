package com.example.cachingorm.cachehw;


import java.util.*;

public class MyCache<K, V> implements HwCache<K, V> {
    private final Map<K, V> cache = new WeakHashMap<>();
    private final Set<HwListener<K, V>> listeners = new HashSet<>();

    @Override
    public void put(K key, V value) {
        notifyListeners(key, value, "put");
        cache.put(key, value);
    }

    @Override
    public void remove(K key) {
        notifyListeners(key, cache.get(key), "remove");
        cache.remove(key);
    }

    @Override
    public V get(K key) {
        V value = cache.get(key);
        if (value != null) {
            notifyListeners(key, value, "get");
        }
        return value;
    }

    @Override
    public void addListener(HwListener<K, V> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(HwListener<K, V> listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(K key, V value, String action) {
        for (HwListener<K, V> listener : listeners) {
            listener.notify(key, value, action);
        }
    }
}

