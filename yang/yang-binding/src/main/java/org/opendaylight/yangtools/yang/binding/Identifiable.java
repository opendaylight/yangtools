package org.opendaylight.yangtools.yang.binding;


/**
 * Identifiable object, which could be identified by it's key
 * 
 * @author ttkacik
 *
 * @param <T> Identifier class for this object
 */
public interface Identifiable<T extends Identifier<? extends Identifiable<T>>> {
    
    /**
     * Returns an unique key for the object
     * 
     * @return Key for the object
     */
    T getKey();
}
