package org.opendaylight.yangtools.triemap;

/**
 * Mimic Some in Scala
 *  
 * @author Roman Levenstein <romixlev@gmail.com>
 *
 * @param <V>
 */
class Some<V> extends Option<V>{
    final V value;
    public Some(V v) {
        value = v;
    }
    
    public V get() {
        return value;
    }
    
    public boolean nonEmpty () {
        return value != null;
    }
}
