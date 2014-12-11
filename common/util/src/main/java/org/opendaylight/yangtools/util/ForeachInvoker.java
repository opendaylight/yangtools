package org.opendaylight.yangtools.util;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Iterator;

abstract class ForeachInvoker<T>  {

    private final ForEachExecutor<?> executor;
    private final T invoker;
    private final InvocationHandler handler = new InvocationHandler() {

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            Preconditions.checkState(invoker == proxy);
            // Composite invocation is not supported on methods with return value.
            Preconditions.checkArgument(Void.class.equals(method.getReturnType()));
            invokeOnAll(method,args);
            return null;
        }
    };

    @SuppressWarnings("unchecked")
    ForeachInvoker(final Class<T> iface, final ForEachExecutor<?> executionStrategy) {
        this.executor = executionStrategy;
        this.invoker = (T) Proxy.newProxyInstance(iface.getClassLoader(),new Class<?>[]{iface},handler);
    }

    public final T getInvoker() {
        return invoker;
    }

    abstract protected Iterator<T> getTargetIterator();

    private void invokeOnAll(final Method method, final Object[] args) throws Exception {
        executor.apply(getTargetIterator(), new InvokeFunction(method,args));
    }

    private static class InvokeFunction implements Function<Object,Void> {

        private final Method method;
        private final Object[] args;

        public InvokeFunction(final Method method, final Object[] args) {
            this.method = method;
            this.args = args;
        }

        @Override
        public Void apply(final Object input) {
            try {
                method.invoke(input, args);
            } catch (final IllegalAccessException e) {
                throw new IllegalStateException(e);
            } catch (final InvocationTargetException e) {
                throw Throwables.propagate(e);
            }
            return null;
        }

    }

    private static final class IterableForeachInvoker<T>  extends ForeachInvoker<T> {

        private final Iterable<T> iterable;

        public IterableForeachInvoker(final Class<T> iface, final ForEachExecutor<?> executionStrategy, final Iterable<T> elements) {
            super(iface, executionStrategy);
            this.iterable = elements;
        }

        @Override
        protected Iterator<T> getTargetIterator() {
            return iterable.iterator();
        }

    }
}
