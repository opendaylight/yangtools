package org.opendaylight.yangtools.yang.common;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.OperationResult.Success;

@NonNullByDefault
public interface FutureOperationResult<T> extends ListenableFuture<OperationResult<T>> {

    default void addListener(final Consumer<OperationResult<T>> listener, final Executor executor) {
        Futures.addCallback(this, new FutureCallback<OperationResult<T>>() {
            @Override
            public void onSuccess(final OperationResult<T> result) {
                listener.accept(result);
            }

            @Override
            public void onFailure(final @Nullable Throwable t) {
                // TODO Auto-generated method stub

            }
        }, executor);
    }

    default void addListener(final BiConsumer<Optional<T>, @Nullable PartialCompletionException> listener,
            final Executor executor) {
        Futures.addCallback(this, new FutureCallback<OperationResult<T>>() {
            @Override
            public void onSuccess(final OperationResult<T> result) {
                final Success<T> success;
                try {
                    success = result.assumeSuccess();
                } catch (PartialCompletionException e) {
                    listener.accept(Optional.empty(), e);
                    return;
                }

                listener.accept(success.getValue(), null);
            }

            @Override
            public void onFailure(final @Nullable Throwable t) {
                if (t instanceof PartialCompletionException) {
                    listener.accept(Optional.empty(), (PartialCompletionException) t);
                    return;
                }

            }
        }, executor);
    }
}
