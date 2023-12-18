import java.util.HashMap;
import java.util.Map;

interface Cache<K, V> {
    V get(K key);

    void put(K key, V value);

    void invalidateAfterTime(long expirationTimeMillis);

    void evictByLFU();

    void printCacheContents();
}

class LFUCache<K, V> implements Cache<K, V> {
    private final int maxSize;
    private final long expirationTimeMillis;

    private final Map<K, CacheEntry<V>> cacheMap = new HashMap<>();

    public LFUCache(int maxSize, long expirationTimeMillis) {
        this.maxSize = maxSize;
        this.expirationTimeMillis = expirationTimeMillis;
    }

    @Override
    public V get(K key) {
        CacheEntry<V> entry = cacheMap.get(key);
        if (entry != null) {
            // Check if the entry has expired
            if (System.currentTimeMillis() - entry.creationTime > expirationTimeMillis) {
                evict(key);
                return null;
            }

            entry.incrementFrequency();
            return entry.value;
        }
        return null;
    }

    @Override
    public void put(K key, V value) {
        if (cacheMap.size() >= maxSize) {
            evictByLFU();
        }

        cacheMap.put(key, new CacheEntry<>(value));
    }

    @Override
    public void invalidateAfterTime(long expirationTimeMillis) {
        long currentTime = System.currentTimeMillis();
        cacheMap.entrySet().removeIf(entry -> currentTime - entry.getValue().creationTime > expirationTimeMillis);
    }

    @Override
    public void evictByLFU() {
        int minFrequency = Integer.MAX_VALUE;
        K keyToRemove = null;

        for (Map.Entry<K, CacheEntry<V>> entry : cacheMap.entrySet()) {
            int frequency = entry.getValue().frequency;
            if (frequency < minFrequency) {
                minFrequency = frequency;
                keyToRemove = entry.getKey();
            }
        }

        if (keyToRemove != null) {
            evict(keyToRemove);
        }
    }

    @Override
    public void printCacheContents() {
        System.out.println("Cache Contents:");
        for (Map.Entry<K, CacheEntry<V>> entry : cacheMap.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue().value);
        }
        System.out.println();
    }

    private void evict(K key) {
        cacheMap.remove(key);
    }
}

class CacheEntry<V> {
    V value;
    long creationTime;
    int frequency;

    public CacheEntry(V value) {
        this.value = value;
        this.creationTime = System.currentTimeMillis();
        this.frequency = 1;
    }

    public void incrementFrequency() {
        frequency++;
    }
}

public class Solution {
    public static void main(String[] args) {
        // Creating an LFU Cache with a maximum size of 3 and expiration time of 5000 milliseconds
        Cache<String, Integer> cache = new LFUCache<>(3, 5000);

        // Adding entries to the cache
        cache.put("one", 1);
        cache.put("two", 2);
        cache.put("three", 3);

        // Accessing entries to simulate frequency of use
        cache.get("one");
        cache.get("two");
        cache.get("three");

        // Adding a new entry, should evict the least frequently used entry ("one")
        cache.put("four", 4);

        // Printing cache contents and testing cache functionalities
        cache.printCacheContents();
        System.out.println("After invalidating entries older than 3000 milliseconds:");
        cache.invalidateAfterTime(3000);
        cache.printCacheContents();
    }
}
