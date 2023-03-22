package techbit.snow.proxy.service.stream;

import com.google.common.collect.Maps;
import jakarta.annotation.Nonnull;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
public class BlockingBag<K, V> {

    private final Map<K, V> map = Maps.newConcurrentMap();
    private final Map<K, Object> locks = Maps.newConcurrentMap();


    public void put(@Nonnull K key, @Nonnull V value) {
        final Object lock = lockFor(key);
        synchronized (lock) {
            map.put(key, value);
            lock.notifyAll();
        }
    }

    public @Nonnull V take(@Nonnull K key) throws InterruptedException {
        final Object lock = lockFor(key);
        final V result;
        synchronized (lock) {
            if (!map.containsKey(key)) {
                lock.wait();
            }
            result = map.get(key);
            Objects.requireNonNull(result);
        }
        return result;
    }

    public void remove(@Nonnull K key) {
        final Object lock = lockFor(key);
        synchronized (lock) {
            map.remove(key);
            lock.notifyAll();
            locks.remove(key);
        }
    }

    public void removeAll() {
        for (final K key : locks.keySet()) {
            remove(key);
        }
    }

    private Object lockFor(@Nonnull K key) {
        return locks.computeIfAbsent(key, k -> new Object());
    }
}
