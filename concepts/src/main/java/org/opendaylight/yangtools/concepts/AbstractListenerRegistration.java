package org.opendaylight.yangtools.concepts;

import java.util.EventListener;

public abstract class AbstractListenerRegistration<T extends EventListener> extends AbstractObjectRegistration<T>
        implements ListenerRegistration<T> {
    
    public AbstractListenerRegistration(T listener) {
        super(listener);
    }

}
