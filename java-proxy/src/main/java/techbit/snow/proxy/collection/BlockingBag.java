package techbit.snow.proxy.collection;

import com.google.common.collect.Maps;

import java.util.Map;

public class BlockingBag<K, V> {

    private final Map<K, V> map = Maps.newConcurrentMap();

    private final Map<K, Object> locks = Maps.newConcurrentMap();

    public void put(K key, V value) {
        map.put(key, value);

        Object lock = lockFor(key);
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public V take(K key) throws InterruptedException {
        Object lock = lockFor(key);
        synchronized (lock) {
            if (!map.containsKey(key)) {
                lock.wait();
            }
        }
        return map.get(key);
    }

    public void remove(K key) {
        map.remove(key);
        Object lock = lockFor(key);
        synchronized (lock) {
            lock.notifyAll();
            locks.remove(key);
        }
    }

    public void removeAll() {
        map.clear();
        for (K key : locks.keySet()) {
            Object lock = lockFor(key);
            synchronized (lock) {
                lock.notifyAll();
            }
        }
        locks.clear();
    }

    private Object lockFor(K key) {
        return locks.computeIfAbsent(key, k -> new Object());
    }
}
