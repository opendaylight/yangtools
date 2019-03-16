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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.Variant;

/**
 * A violation of a {@link CanonicalValue} validation. Contains details as mandated by RFC7950 Section 8.3.1.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public abstract class CanonicalValueViolation implements Immutable, Serializable {
    public static class Regular extends CanonicalValueViolation {
        private static final long serialVersionUID = 1L;

        private final @Nullable String appTag;
        private final @Nullable String message;

        Regular(final @Nullable String appTag, final @Nullable String message) {
            this.appTag = appTag;
            this.message = message;
        }

        @Override
        @Nullable String appTag() {
            return appTag;
        }

        @Override
        @Nullable String message() {
            return message;
        }

        @Override
        ToStringHelper addToString(final ToStringHelper helper) {
            return helper.omitNullValues().add("app-tag", appTag).add("message", message);
        }
    }

    @SuppressFBWarnings("NM_CLASS_NOT_EXCEPTION")
    public static class WithException extends CanonicalValueViolation {
        private static final long serialVersionUID = 1L;

        private final Exception cause;

        WithException(final Exception cause) {
            this.cause = requireNonNull(cause);
        }

        @Override
        @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
        @Nullable String appTag() {
            return null;
        }

        @Override
        @Nullable String message() {
            return cause.getMessage();
        }

        public final Exception getCause() {
            return cause;
        }

        @Override
        ToStringHelper addToString(final ToStringHelper helper) {
            return helper.add("cause", cause);
        }
    }

    private static final CanonicalValueViolation EMPTY = new Regular(null, null);
    private static final Variant<?, CanonicalValueViolation> EMPTY_VARIANT = Variant.ofSecond(EMPTY);
    private static final long serialVersionUID = 1L;

    public static CanonicalValueViolation empty() {
        return EMPTY;
    }

    public static CanonicalValueViolation of(final Exception cause) {
        return new WithException(cause);
    }

    public static CanonicalValueViolation of(final @Nullable String appTag, final @Nullable String message) {
        return appTag == null && message == null ? EMPTY : new Regular(appTag, message);
    }

    @SuppressWarnings("unchecked")
    public static <T> Variant<T, CanonicalValueViolation> emptyVariant() {
        return (Variant<T, CanonicalValueViolation>) EMPTY_VARIANT;
    }

    public static <T> Variant<T, CanonicalValueViolation> variantOf(final Exception cause) {
        return Variant.ofSecond(CanonicalValueViolation.of(cause));
    }

    public static <T> Variant<T, CanonicalValueViolation> variantOf(final String message) {
        return variantOf(null, message);
    }

    public static <T> Variant<T, CanonicalValueViolation> variantOf(final @Nullable String appTag,
            final String message) {
        return Variant.ofSecond(CanonicalValueViolation.of(appTag, message));
    }

    public final Optional<String> getAppTag() {
        return nullableString(appTag());
    }

    public final Optional<String> getMessage() {
        return nullableString(appTag());
    }

    abstract @Nullable String appTag();

    abstract @Nullable String message();

    @Override
    public final int hashCode() {
        return Objects.hash(appTag(), message());
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CanonicalValueViolation)) {
            return false;
        }
        final CanonicalValueViolation other = (CanonicalValueViolation) obj;
        return Objects.equals(appTag(), other.appTag()) && Objects.equals(message(), other.message());
    }

    @Override
    public final String toString() {
        return addToString(MoreObjects.toStringHelper(this)).toString();
    }

    abstract ToStringHelper addToString(ToStringHelper helper);

    private static Optional<String> nullableString(@Nullable final String str) {
        return str != null ? Optional.of(str) : Optional.empty();
    }
}
