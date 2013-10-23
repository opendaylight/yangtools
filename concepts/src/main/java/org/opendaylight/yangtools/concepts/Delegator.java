package org.opendaylight.yangtools.concepts;

/**
 * Implementation of this interface delegates all it's calls
 * to the delegator if not specified otherwise.
 * 
 *
 * @param <T> Type of delegate
 */
public interface Delegator<T> {

    T getDelegate();

}
