package org.opendaylight.yangtools.util;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import java.util.EventListener;
import java.util.Iterator;
import org.opendaylight.yangtools.concepts.ListenerRegistration;

public class InvocableListenerRegistry<T extends EventListener> extends ListenerRegistry<T> {

    @SuppressWarnings("rawtypes")
    protected static final Function EXTRACT_INSTANCE = new Function<ListenerRegistration<?>, Object>() {

        @Override
        public Object apply(final ListenerRegistration<?> input) {
            return input.getInstance();
        }

    };

    private final ForeachInvoker<T> invoker;

    private InvocableListenerRegistry(final Class<T> iface, final ForEachExecutor<?> executionStrategy) {
        invoker = new ForeachInvoker<T>(iface,executionStrategy) {
            @Override
            protected Iterator<T> getTargetIterator() {
                return FluentIterable.from(getListeners()).transform(InvocableListenerRegistry.<T>instanceExtractor()).iterator();
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static <T extends EventListener> Function<ListenerRegistration<T>, T> instanceExtractor() {
        return EXTRACT_INSTANCE;
    }

}
