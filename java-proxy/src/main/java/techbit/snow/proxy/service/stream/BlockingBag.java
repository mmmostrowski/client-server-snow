package techbit.snow.proxy.service.stream;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
public class BlockingBag<K, V> {

    private final Map<K, V> map = Maps.newConcurrentMap();

    private final Map<K, Object> locks = Maps.newConcurrentMap();

    public void put(K key, V value) {
        Object lock = lockFor(key);
        synchronized (lock) {
            map.put(key, value);
            lock.notifyAll();
        }
    }

    public @NotNull V take(K key) throws InterruptedException {
        Object lock = lockFor(key);
        V result;
        synchronized (lock) {
            if (!map.containsKey(key)) {
                lock.wait();
            }
            result = map.get(key);
            Objects.requireNonNull(result);
        }
        return result;
    }

    public void remove(K key) {
        Object lock = lockFor(key);
        synchronized (lock) {
            map.remove(key);
            lock.notifyAll();
            locks.remove(key);
        }
    }

    public void removeAll() {
        for (K key : locks.keySet()) {
            remove(key);
        }
    }

    private Object lockFor(K key) {
        return locks.computeIfAbsent(key, k -> new Object());
    }
}
