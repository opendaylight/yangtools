package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;

@NonNullByDefault
@ThreadSafe
public abstract class OperationResult<T> implements Immutable {
    public static final class Success<T> extends OperationResult<T> {
        final @Nullable T value;

        Success(final @Nullable T value) {
            this.value = value;
        }

        @Override
        public Success<T> assumeSuccess() {
            return this;
        }

        @Override
        public List<OperationError> getErrors() {
            return ImmutableList.of();
        }

        @Override
        public Optional<T> getValue() {
            return Optional.ofNullable(value);
        }
    }

    public static final class Partial<T> extends OperationResult<T> {

        @Override
        public Optional<T> getValue() throws PartialCompletionException {
            throw new PartialCompletionException(this);
        }

        @Override
        public Success<T> assumeSuccess() throws PartialCompletionException {
            throw new PartialCompletionException(this);
        }
    }

    private static final Success<?> EMPTY_SUCCESS = new Success<>(null);

    @SuppressWarnings("unchecked")
    public static <T> Success<T> success() {
        return (Success<T>) EMPTY_SUCCESS;
    }

    public static <T> Success<T> success(final T value) {
        return new Success<>(requireNonNull(value));
    }

    public abstract Optional<T> getValue() throws PartialCompletionException;

    public abstract List<OperationError> getErrors();

    /**
     * Convenience method for turning an OperationResult into a success. If the operation did not complete cleanly,
     * a {@link PartialCompletionException} will be raised containing all warnings/errors.
     *
     * @return Successful value
     * @throws PartialCompletionException if there were any warnings/errors raised.
     */
    public abstract Success<T> assumeSuccess() throws PartialCompletionException;
}
