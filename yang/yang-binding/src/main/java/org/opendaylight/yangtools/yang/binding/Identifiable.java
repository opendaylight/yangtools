package org.opendaylight.yangtools.yang.binding;


/**
 * Object is uniquely identifiable in its scope by key
 * 
 * 
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
