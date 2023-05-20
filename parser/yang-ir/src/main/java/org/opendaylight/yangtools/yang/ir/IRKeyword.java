/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.ir;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A YANG keyword, as defined, as defined by section 6.1.2 of both
 * <a href="https://www.rfc-editor.org/rfc/rfc6020#section-6.1.2">RFC6020</a> and
 * <a href="https://www.rfc-editor.org/rfc/rfc7950#section-6.1.2">RFC7950</a>. The two options are discerned by nullability
 * of {@link #prefix()} method's return, as hinted by the ABNF for {@code node-identifier} -- and while a keyword is a
 * semantically different construct, it shares the same value space.
 *
 * <p>
 * Naming in this class prefers the formal ABNF specification and draws value-space and type-safety implications from
 * that connection, rather than following the RFC-assigned names.
 */
@Beta
public abstract sealed class IRKeyword extends AbstractIRObject {
    public static final class Qualified extends IRKeyword {
        private final @NonNull String prefix;

        private Qualified(final String prefix, final String localName) {
            super(localName);
            this.prefix = requireNonNull(prefix);
        }

        public static @NonNull Qualified of(final String prefix, final String localName) {
            return new Qualified(prefix, localName);
        }

        @Override
        public @NonNull String prefix() {
            return prefix;
        }

        @Override
        public String asStringDeclaration() {
            return prefix + ':' + identifier();
        }

        @Override
        StringBuilder toYangFragment(final StringBuilder sb) {
            return sb.append(prefix).append(':').append(identifier());
        }
    }

    public static final class Unqualified extends IRKeyword {
        private Unqualified(final String localName) {
            super(localName);
        }

        public static @NonNull Unqualified of(final String localName) {
            return new Unqualified(localName);
        }

        @Override
        public String prefix() {
            return null;
        }

        @Override
        public String asStringDeclaration() {
            return identifier();
        }

        @Override
        StringBuilder toYangFragment(final StringBuilder sb) {
            return sb.append(identifier());
        }
    }

    private final @NonNull String identifier;

    IRKeyword(final String localName) {
        identifier = requireNonNull(localName);
    }

    /**
     * This keyword's 'identifier' part. This corresponds to what the RFCs refer to as {@code YANG keyword} or as
     * {@code language extension keyword}.
     *
     * <p>
     * Note the returned string is guaranteed to conform to rules of {@code identifier} ABNF..
     *
     * @return This keyword's identifier part.
     */
    public final @NonNull String identifier() {
        return identifier;
    }

    /**
     * This keyword's 'prefix' part. This corresponds to {@code prefix identifier}. For {@code YANG keyword}s this is
     * null. For language extension references this is the non-null prefix which references the YANG module defining
     * the language extension.
     *
     * <p>
     * Note the returned string, if non-null, is guaranteed to conform to rules of {@code identifier} ABNF.
     *
     * @return This keyword's prefix, or null if this keyword references a YANG keyword.
     */
    public abstract @Nullable String prefix();

    /**
     * Helper method to re-create the string which was used to declared this keyword.
     *
     * @return Declaration string.
     */
    public abstract @NonNull String asStringDeclaration();

    @Override
    public final int hashCode() {
        return Objects.hashCode(prefix()) * 31 + identifier.hashCode();
    }

    @Override
    public final boolean equals(final Object obj) {
        return this == obj || obj instanceof IRKeyword other && identifier.equals(other.identifier)
            && Objects.equals(prefix(), other.prefix());
    }
}
