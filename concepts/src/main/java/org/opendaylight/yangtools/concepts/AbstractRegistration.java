package org.opendaylight.yangtools.concepts;

/**
 * Utility registration handle. It is a convenience for register-style method
 * which can return an AutoCloseable realized by a subclass of this class.
 * Invoking the close() method triggers unregistration of the state the method
 * installed.
 */
public abstract class AbstractRegistration<T> implements Registration<T> {

    private boolean closed = false;
    private final T instance;

    public AbstractRegistration(T instance) {
        this.instance = instance;
    }

    @Override
    public T getInstance() {
        return instance;
    }

    /**
     * Remove the state referenced by this registration. This method is
     * guaranteed to be called at most once. The referenced state must be
     * retained until this method is invoked.
     */
    protected abstract void removeRegistration();

    @Override
    public void close() throws Exception {
        if (!closed) {
            closed = true;
            removeRegistration();
        }
    }

}
