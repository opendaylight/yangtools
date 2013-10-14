package org.opendaylight.yangtools.concepts;

public interface ClassBasedPropertyBuilder<P,T extends ClassBasedPropertyBuilder<P,T>> extends Builder<P> {

    /**
     * Sets a value of property uniquely identified by it's
     * class.
     * 
     * @param type Type of property to set
     * @param value Value of property 
     * @return
     */
    <V> T set(Class<V> type,V value);
    
    /**
     * Gets a value of property based on it's type.
     * 
     * @param type
     * @return
     */
    <V> V get(Class<V> type);

}
