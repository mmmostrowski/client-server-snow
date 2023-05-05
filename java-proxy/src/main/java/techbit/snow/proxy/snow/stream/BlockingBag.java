package techbit.snow.proxy.snow.stream;

import com.google.common.collect.Maps;
import lombok.experimental.StandardException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
public final class BlockingBag<K, V> {

    @StandardException
    public static class ItemNoLongerExistsException extends Exception {}

    private final Map<K, V> map = Maps.newConcurrentMap();
    private final Map<K, Object> locks = Maps.newConcurrentMap();


    public void put(K key, V value) {
        final Object lock = lockFor(key);
        synchronized (lock) {
            map.put(key, value);
            lock.notifyAll();
        }
    }

    public V get(K key) {
        return map.get(key);
    }

    public V take(K key) throws InterruptedException, ItemNoLongerExistsException {
        final Object lock = lockFor(key);
        final V result;
        synchronized (lock) {
            if (!map.containsKey(key)) {
                lock.wait();
            }
            result = map.get(key);
            if (result == null) {
                throw new ItemNoLongerExistsException("Item was present but has been removed: " + key);
            }
        }
        return result;
    }

    public void remove(K key) {
        final Object lock = lockFor(key);
        synchronized (lock) {
            map.remove(key);
            lock.notifyAll();
            locks.remove(key);
        }
    }

    public void removeAll() {
        locks.keySet().forEach(this::remove);
    }

    private Object lockFor(K key) {
        return locks.computeIfAbsent(key, k -> new Object());
    }
}
