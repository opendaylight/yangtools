package org.opendaylight.yangtools.concepts;

/**
 * Utility registration handle. It is a convenience for register-style method
 * which can return an AutoCloseable realized by a subclass of this class.
 * Invoking the close() method triggers unregistration of the state the method
 * installed.
 */
public abstract class AbstractObjectRegistration<T> extends AbstractRegistration implements Registration<T> {

    
    private final T instance;

    public AbstractObjectRegistration(T instance) {
        this.instance = instance;
    }

    @Override
    public final T getInstance() {
        return instance;
    }

}
