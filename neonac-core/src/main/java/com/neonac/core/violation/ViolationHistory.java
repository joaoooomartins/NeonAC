package com.neonac.core.violation;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ViolationHistory {

    private final Map<String, Deque<Entry>> entries = new ConcurrentHashMap<>();
    private final long windowMs;

    public ViolationHistory(long windowMs) {
        this.windowMs = windowMs;
    }

    public void record(String checkId, double vl) {
        Deque<Entry> deque = entries.computeIfAbsent(checkId, k -> new ArrayDeque<>());
        synchronized (deque) {
            deque.addLast(new Entry(System.currentTimeMillis(), vl));
        }
    }

    public double count(String checkId) {
        Deque<Entry> deque = entries.get(checkId);
        if (deque == null) return 0.0;
        long cutoff = System.currentTimeMillis() - windowMs;
        double total = 0.0;
        synchronized (deque) {
            Iterator<Entry> it = deque.iterator();
            while (it.hasNext()) {
                Entry e = it.next();
                if (e.timestamp < cutoff) {
                    it.remove();
                } else {
                    total += e.vl;
                }
            }
        }
        return total;
    }

    public double countAll() {
        long cutoff = System.currentTimeMillis() - windowMs;
        double total = 0.0;
        for (Map.Entry<String, Deque<Entry>> me : entries.entrySet()) {
            Deque<Entry> deque = me.getValue();
            synchronized (deque) {
                Iterator<Entry> it = deque.iterator();
                while (it.hasNext()) {
                    Entry e = it.next();
                    if (e.timestamp < cutoff) {
                        it.remove();
                    } else {
                        total += e.vl;
                    }
                }
            }
        }
        return total;
    }

    public void clear() {
        entries.clear();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    private static final class Entry {
        final long timestamp;
        final double vl;

        Entry(long timestamp, double vl) {
            this.timestamp = timestamp;
            this.vl = vl;
        }
    }
}
