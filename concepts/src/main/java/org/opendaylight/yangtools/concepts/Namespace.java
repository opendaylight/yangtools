package org.opendaylight.yangtools.concepts;

import java.util.Set;

public interface Namespace<K,V> {
    
    V get(K key);
    
    Namespace<K,V> getParent();
    Set<Namespace<K,V>> getSubnamespaces();
    Namespace<K,V> getSubnamespace(V key);
}
