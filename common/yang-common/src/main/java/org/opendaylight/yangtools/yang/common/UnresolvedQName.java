/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serial;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A {@link QName} equivalent which has not been resolved. There are two subclasses:
 * <ol>
 *   <li>{@link Unqualified}, which holds only a local name available through {@link #getLocalName()}.<li>
 *   <li>{@link Qualified}, which also holds a string prefix available via {@link Qualified#getPrefix()}.</li>
 * </ol>
 */
@NonNullByDefault
public abstract sealed class UnresolvedQName extends AbstractQName {
    /**
     * An unresolved, qualified {@link QName}. It is guaranteed to hold a valid {@link #getLocalName()} bound to a
     * namespace identified through a prefix string, but remains unresolved. A resolved {@link QName} can be obtained
     * through {@link #bindTo(YangNamespaceContext)}.
     */
    public static final class Qualified extends UnresolvedQName implements Comparable<Qualified> {
        @Serial
        private static final long serialVersionUID = 1L;
        private static final Interner<Qualified> INTERNER = Interners.newWeakInterner();

        private final String prefix;

        private Qualified(final String prefix, final String localName) {
            super(localName);
            this.prefix = requireNonNull(prefix);
        }

        /**
         * Create a new qualified unresolved QName.
         *
         * @param prefix The prefix on this qualified QName
         * @param localName The local name of this qualified QName
         * @return An UnqualifiedQName instance
         * @throws NullPointerException if any argument is {@code null}
         * @throws IllegalArgumentException if {@code localName} is not a valid YANG identifier
         */
        public static Qualified of(final String prefix, final String localName) {
            return new Qualified(checkLocalName(prefix), checkLocalName(localName));
        }

        /**
         * Read an QualifiedQName from a DataInput. The format is expected to match the output format of
         * {@link #writeTo(DataOutput)}.
         *
         * @param in DataInput to read
         * @return An QualifiedQName instance
         * @throws IOException if I/O error occurs
         */
        public static Qualified readFrom(final DataInput in) throws IOException {
            return of(in.readUTF(), in.readUTF());
        }

        @Override
        public @NonNull String getPrefix() {
            return prefix;
        }

        public Optional<QName> bindTo(final YangNamespaceContext namespaceContext) {
            return namespaceContext.findNamespaceForPrefix(prefix).map(this::bindTo);
        }

        @Override
        @SuppressFBWarnings(value = "ES_COMPARING_STRINGS_WITH_EQ", justification = "Interning identity check")
        public Qualified intern() {
            // Make sure to intern the string and check whether it refers to the same name as we are
            final String name = getLocalName();
            final String internedName = name.intern();
            final Qualified template = internedName == name ? this : new Qualified(prefix.intern(), internedName);
            return INTERNER.intern(template);
        }

        @Override
        public Qualified withPrefix(final String newPrefix) {
            return prefix.equals(newPrefix) ? this : new Qualified(newPrefix, getLocalName());
        }

        @Override
        @SuppressWarnings("checkstyle:parameterName")
        public int compareTo(final Qualified o) {
            return getLocalName().compareTo(o.getLocalName());
        }

        @Override
        public void writeTo(final DataOutput out) throws IOException {
            out.writeUTF(getLocalName());
        }

        @Override
        public int hashCode() {
            return getLocalName().hashCode();
        }

        @Override
        public boolean equals(final @Nullable Object obj) {
            return this == obj || obj instanceof Qualified other && getLocalName().equals(other.getLocalName());
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("localName", getLocalName()).toString();
        }

        @Override
        Object writeReplace() {
            return new QQNv1(this);
        }
    }

    /**
     * An unresolved, unqualified {@link QName}. It is guaranteed to hold a valid {@link #getLocalName()}, in the
     * default namespace, which is not resolved. A resolved {@link QName} can be constructed through
     * {@link #bindTo(QNameModule)}.
     */
    public static final class Unqualified extends UnresolvedQName implements Comparable<Unqualified> {
        @Serial
        private static final long serialVersionUID = 1L;
        private static final Interner<Unqualified> INTERNER = Interners.newWeakInterner();

        private Unqualified(final String localName) {
            super(localName);
        }

        /**
         * Create a new unqualified unresolved QName.
         *
         * @param localName The local name of this unqualified QName
         * @return An UnqualifiedQName instance
         * @throws NullPointerException if localName is {@code null}
         * @throws IllegalArgumentException if {@code localName} is not a valid YANG identifier
         */
        public static Unqualified of(final String localName) {
            return new Unqualified(checkLocalName(localName));
        }

        /**
         * Read an UnqualifiedQName from a DataInput. The format is expected to match the output format of
         * {@link #writeTo(DataOutput)}.
         *
         * @param in DataInput to read
         * @return An UnqualifiedQName instance
         * @throws IOException if I/O error occurs
         */
        public static Unqualified readFrom(final DataInput in) throws IOException {
            return of(in.readUTF());
        }

        @Override
        @SuppressFBWarnings(value = "ES_COMPARING_STRINGS_WITH_EQ", justification = "Interning identity check")
        public Unqualified intern() {
            // Make sure to intern the string and check whether it refers to the same name as we are
            final String name = getLocalName();
            final String internedName = name.intern();
            final Unqualified template = internedName == name ? this : new Unqualified(internedName);
            return INTERNER.intern(template);
        }

        @Override
        public @Nullable String getPrefix() {
            return null;
        }

        @Override
        public Qualified withPrefix(final String newPrefix) {
            return new Qualified(newPrefix, getLocalName());
        }

        @Override
        @SuppressWarnings("checkstyle:parameterName")
        public int compareTo(final Unqualified o) {
            return getLocalName().compareTo(o.getLocalName());
        }

        @Override
        public void writeTo(final DataOutput out) throws IOException {
            out.writeUTF(getLocalName());
        }

        @Override
        public int hashCode() {
            return getLocalName().hashCode();
        }

        @Override
        public boolean equals(final @Nullable Object obj) {
            return this == obj || obj instanceof Unqualified other && getLocalName().equals(other.getLocalName());
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("localName", getLocalName()).toString();
        }

        @Override
        Object writeReplace() {
            return new UQNv1(this);
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private UnresolvedQName(final String localName) {
        super(localName);
    }

    /**
     * Try to create a new unqualified QName.
     *
     * @param localName The local name of this unqualified QName
     * @return An UnqualifiedQName instance, or null if localName is not valid
     */
    public static @Nullable Unqualified tryLocalName(final String localName) {
        return isValidLocalName(localName) ? new Unqualified(localName) : null;
    }

    @Override
    public abstract UnresolvedQName intern();

    /**
     * Return the prefix of this unresolved QName.
     *
     * @return This QName's prefix
     */
    public abstract @Nullable String getPrefix();

    /**
     * Return a {@link Qualified} object bound to specified {@code prefix}.
     *
     * @return a {@link Qualified} object bound to specified {@code prefix}
     * @throws NullPointerException if {@code newPrefix} is null
     */
    public abstract Qualified withPrefix(String newPrefix);
}
