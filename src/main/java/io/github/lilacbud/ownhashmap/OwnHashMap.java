package io.github.lilacbud.ownhashmap;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

public class OwnHashMap<K, V> extends AbstractMap<K, V> implements Map<K, V> {
    //default values copied from actual HashMap
    private static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;
    private static final int MAXIMUM_CAPACITY = 1 << 30;
    private static final float DEFAULT_LOAD_FACTOR = 0.75F;
    
    private final float loadFactor;
    private Node<K, V>[] table;
    private int threshold;
    private int size;
    private Set<Map.Entry<K,V>> entrySet;
    
    public OwnHashMap() {
        loadFactor = DEFAULT_LOAD_FACTOR;
    }
    
    public OwnHashMap(Map<? extends K, ? extends V> map) {
        loadFactor = DEFAULT_LOAD_FACTOR;
        final int mapSize = Objects.requireNonNull(map).size();
        if (mapSize > 0) {
            final int rawCapacity = (int)(mapSize / loadFactor);
            int capacity;
            if (rawCapacity > MAXIMUM_CAPACITY) {
                 capacity = MAXIMUM_CAPACITY;
            } else {
                for (capacity = 1; capacity < rawCapacity; capacity *= 2) {}
                if (capacity > MAXIMUM_CAPACITY) {
                    capacity = MAXIMUM_CAPACITY;
                }
            }
            threshold = capacity;
            for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
                K key = entry.getKey();
                putValue(Objects.hashCode(key), key, entry.getValue(), false);
            }
        }
    }
    
    @Override
    public Set<Entry<K, V>> entrySet() {
        if (entrySet == null) {
            entrySet = new EntrySet();
        }
        return entrySet;
    }

    @Override
    public V get(Object key) {
        if (table == null || table.length <= 0) {
            return null;
        }
        final int hash = Objects.hashCode(key);
        Node<K, V> node = table[hash % table.length];
        while (node != null) {
            if (node.hash == hash && Objects.equals(node.key, key)) {
                return node.value;
            }
            node = node.next;
        }
        return null;
    }

    @Override
    public V put(K key, V value) {
        return putValue(Objects.hashCode(key), key, value, false);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return putValue(Objects.hashCode(key), key, value, true);
    }

    @Override
    public V remove(Object key) {
        Node<K, V> node = removeNode(Objects.hashCode(key), key, null, false);
        return node == null ? null : node.value;
    }

    @Override
    public boolean remove(Object key, Object value) {
        return removeNode(Objects.hashCode(key), key, value, true) != null;
    }

    @Override
    public void clear() {
        if (table != null && size > 0) {
            size = 0;
            for (int i = 0; i < table.length; ++i) {
                table[i] = null;
            }
        }
    }
    
    private V putValue(int hash, K key, V value, boolean putIfAbsent) {
        final int capacity = table != null && table.length != 0 ? table.length : resize().length;
        final int index = hash % capacity;
        Node<K, V> node = table[index];
        if (node == null) {
            table[index] = new Node<>(hash, key, value);
        } else {
            while (true) {
                if (node.hash == hash && Objects.equals(node.key, key)) {
                    return putIfAbsent ? node.value : node.setValue(value);
                } else if (node.next == null) {
                    node.next = new Node<>(hash, key, value);
                    break;
                }
                node = node.next;
            }
        }
        ++size;
        if (size > threshold) {
            resize();
        }
        return null;
    }
    
    private Node<K, V> removeNode(int hash, Object key, Object value, boolean matchValue) {
        if (table == null || table.length <= 0) {
            return null;
        }
        final int index = hash % table.length;
        Node<K, V> node = table[index];
        Node<K, V> prevNode = null;
        while (node != null) {
            if (node.hash == hash && Objects.equals(node.key, key)) {
                break;
            }
            prevNode = node;
            node = node.next;
        }
        if (node != null && (!matchValue || Objects.equals(node.value, value))) {
            if (prevNode == null) {
                table[index] = node.next;
            } else {
                prevNode.next = node.next;
            }
            --size;
            return node;
        }
        return null;
    }
    
    @SuppressWarnings({"rawtypes","unchecked"})
    private Node<K, V>[] resize() {
        final Node<K, V>[] oldTable = table;
        final int oldCapacity = oldTable == null ? 0 : oldTable.length;
        int newCapacity, newThreshold = 0;
        if (oldCapacity > 0) {
            if (oldCapacity >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return table;
            }
            newCapacity = oldCapacity * 2;
            if (newCapacity < MAXIMUM_CAPACITY && oldCapacity >= DEFAULT_INITIAL_CAPACITY) {
                newThreshold = threshold * 2;
            }
        } else if (threshold > 0) {
            newCapacity = threshold;
        } else {
            newCapacity = DEFAULT_INITIAL_CAPACITY;
        }
        if (newThreshold <= 0) {
            newThreshold = newCapacity < MAXIMUM_CAPACITY ? (int)(newCapacity * loadFactor) : Integer.MAX_VALUE;
        }
        threshold = newThreshold;
        table = (Node<K, V>[])new Node[newCapacity];
        if (oldTable != null) {
            for (int i = 0; i < oldCapacity; ++i) {
                Node<K, V> node = oldTable[i];
                if (node == null) {
                    continue;
                }
                oldTable[i] = null;
                if (node.next == null) {
                    table[node.hash % newCapacity] = node;
                    continue;
                }
                Node<K, V> firstNode = null, secondNode = null;
                while (node != null) {
                    if (node.hash % newCapacity < oldCapacity) {
                        if (firstNode == null) {
                            table[i] = node;
                        } else {
                            firstNode.next = node;
                        }
                        firstNode = node;
                    } else {
                        if (secondNode == null) {
                            table[i + oldCapacity] = node;
                        } else {
                            secondNode.next = node;
                        }
                        secondNode = node;
                    }
                    node = node.next;
                }
                if (firstNode != null) {
                    firstNode.next = null;
                }
                if (secondNode != null) {
                    secondNode.next = null;
                }
            }
        }
        return table;
    }
    
    private static class Node<K, V> implements Map.Entry<K, V> {
        final int hash;
        final K key;
        V value;
        Node<K, V> next = null;
        
        Node(int hash, K key, V value) {
            this.hash = hash;
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }
    }
    
    //this implementation is not fail-fast
    private class EntryIterator implements Iterator<Entry<K, V>> {
        Node<K, V> current, next;
        int index;

        public EntryIterator() {
            if (table != null && table.length > 0) {
                for (index = 0; next == null && index < table.length; ++index) {
                    next = table[index];
                } 
            }
        }
        
        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public Entry<K, V> next() {
            if (next == null) {
                throw new NoSuchElementException();
            }
            current = next;
            next = current.next;
            if (table != null) {
                for (; next == null && index < table.length; ++index) {
                    next = table[index];
                } 
            }
            return current;
        }

        @Override
        public void remove() {
            if (current == null) {
                throw new IllegalStateException();
            }
            removeNode(current.hash, current.key, null, false);
            current = null;
        }
    }
    
    private class EntrySet extends AbstractSet<Map.Entry<K, V>> {
        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        @Override
        public int size() {
            return size;
        } 
    }
}
