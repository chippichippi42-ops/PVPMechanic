package org.twightlight.PVPMechanic.util;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

public class ObjectPool<T> {
    private final ConcurrentLinkedQueue<T> pool = new ConcurrentLinkedQueue<>();
    private final Supplier<T> supplier;

    public ObjectPool(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T acquire() {
        T obj = pool.poll();
        return (obj != null) ? obj : supplier.get();
    }

    public void release(T obj) {
        pool.offer(obj);
    }
}
