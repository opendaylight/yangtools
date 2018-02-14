package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;

@NonNullByDefault
public final class PartialCompletionException extends Exception implements Immutable {
    private static final long serialVersionUID = 1L;

    private final Collection<@NonNull OperationError> events;
    private final @Nullable Object value;

    private PartialCompletionException(final Collection<? extends @NonNull OperationError> events,
            final @Nullable Object value) {
        this.events = ImmutableList.copyOf(events);
        this.value = value;
    }

    public static PartialCompletionException withoutResult(final OperationError event) {
        return new PartialCompletionException(ImmutableList.of(event), null);
    }

    public static PartialCompletionException withResult(final @NonNull Object value,
            final @NonNull OperationError event) {
        return new PartialCompletionException(ImmutableList.of(event), requireNonNull(value));
    }

    public static PartialCompletionException withResult(final @NonNull Object value,
            final @NonNull OperationError... events) {
        return new PartialCompletionException(ImmutableList.copyOf(events), requireNonNull(value));
    }

    public static PartialCompletionException withResult(final @NonNull Object value,
            final @NonNull Collection<? extends @NonNull OperationError> events) {
        return new PartialCompletionException(events, requireNonNull(value));
    }

    @SuppressWarnings("null")
    // XXX: we are promising a non-null optional implicitly, Eclipse barfs because it does not understand Optional
    public Optional<Object> getValue() {
        return (Optional<Object>) Optional.ofNullable(value);
    }

    public Collection<@NonNull OperationError> getEvents() {
        return events;
    }

    @Override
    public String toString() {
        final ToStringHelper helper = MoreObjects.toStringHelper(this).omitNullValues().add("value", value);
        if (events.isEmpty()) {
            helper.add("events", events);
        }
        return helper.toString();
    }
}
