/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A violation of a {@link CanonicalValue} validation. Contains details as mandated by RFC7950 Section 8.3.1.
 */
@Beta
@NonNullByDefault
public abstract sealed class CanonicalValueViolation<T extends CanonicalValue<T>>
        implements CanonicalValueValidator.ValidationResult<T>, Serializable {
    private static final class Regular<T extends CanonicalValue<T>> extends CanonicalValueViolation<T> {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        private final @Nullable String appTag;
        private final @Nullable String message;

        Regular(final @Nullable String appTag, final @Nullable String message) {
            this.appTag = appTag;
            this.message = message;
        }

        @Override
        public @Nullable String appTag() {
            return appTag;
        }

        @Override
        public @Nullable String message() {
            return message;
        }

        @Override
        ToStringHelper addToString(final ToStringHelper helper) {
            return helper.omitNullValues().add("app-tag", appTag).add("message", message);
        }
    }

    public static final class WithCause<T extends CanonicalValue<T>> extends CanonicalValueViolation<T> {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        private final Exception cause;

        WithCause(final Exception cause) {
            this.cause = requireNonNull(cause);
        }

        @Override
        public @Nullable String appTag() {
            return null;
        }

        @Override
        public @Nullable String message() {
            return cause.getMessage();
        }

        /**
         * {@return the underlying Exception which caused this violation}
         */
        public Exception getCause() {
            return cause;
        }

        @Override
        ToStringHelper addToString(final ToStringHelper helper) {
            return helper.add("cause", cause);
        }
    }

    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private static final CanonicalValueViolation<?> EMPTY = new Regular<>(null, null);

    @SuppressWarnings("unchecked")
    public static <T extends CanonicalValue<T>> CanonicalValueViolation<T> empty() {
        return (CanonicalValueViolation<T>) EMPTY;
    }

    public static <T extends CanonicalValue<T>> CanonicalValueViolation<T> of(final Exception cause) {
        return new WithCause<>(cause);
    }

    public static <T extends CanonicalValue<T>> CanonicalValueViolation<T> of(final @Nullable String appTag,
            final @Nullable String message) {
        return appTag == null && message == null ? empty() : new Regular<>(appTag, message);
    }

    public final Optional<String> getAppTag() {
        return nullableString(appTag());
    }

    public final Optional<String> getMessage() {
        return nullableString(message());
    }

    public abstract @Nullable String appTag();

    public abstract @Nullable String message();

    @Override
    public final int hashCode() {
        return Objects.hash(appTag(), message());
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof CanonicalValueViolation other
            && Objects.equals(appTag(), other.appTag()) && Objects.equals(message(), other.message());
    }

    @Override
    public final String toString() {
        return addToString(MoreObjects.toStringHelper(this)).toString();
    }

    abstract ToStringHelper addToString(ToStringHelper helper);

    private static Optional<String> nullableString(final @Nullable String str) {
        return str != null ? Optional.of(str) : Optional.empty();
    }
}
