package org.opendaylight.yangtools.util;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import java.util.Iterator;
import javax.annotation.Nullable;

public abstract class ForEachExecutor<E extends Exception> {

    private final boolean removeSuccessful;

    private final static ForEachExecutor<?> FAILFAST = new FailOnFirstException(false);
    private final static ForEachExecutor<?> REMOVING_FAILFAST = new FailOnFirstException(true);

    protected ForEachExecutor(final boolean removeSuccessful) {
        this.removeSuccessful = removeSuccessful;
    }

    public static <E extends Exception> ForEachExecutor<E> exceptionCollecting(final Supplier<E> supplier, final boolean removeSuccessful) {
        return new ContinueAndThrowCompositeException<E>(removeSuccessful, supplier);
    }

    public final <T> void apply(final Iterator<T> elements, final Function<? super T, Void> function) throws E {
        E composite = null;
        while (elements.hasNext()) {
            try {
                function.apply(elements.next());
                if(removeSuccessful) {
                    elements.remove();
                }
            } catch (final Exception thrown) {
                composite = onException(composite,thrown);
            }
        }
        if(composite != null) {
            throw composite;
        }
    }

    abstract E onException(@Nullable final E composite, final Exception thrown) throws E;

    private static class FailOnFirstException extends ForEachExecutor<Exception> {

        protected FailOnFirstException(final boolean removeSuccessful) {
            super(removeSuccessful);
        }

        @Override
        Exception onException(final Exception composite, final Exception thrown) throws Exception {
            throw thrown;
        }
    }

    private static class ContinueAndThrowCompositeException<E extends Exception> extends ForEachExecutor<E> {

        private final Supplier<E> supplier;

        public ContinueAndThrowCompositeException(final boolean removeSuccessful, final Supplier<E> supplier) {
            super(removeSuccessful);
            this.supplier = supplier;
        }

        @Override
        E onException(E composite, final Exception thrown) throws E {
           if(composite == null) {
               composite = supplier.get();
           }
           composite.addSuppressed(thrown);
           return composite;
        }
    }

}
