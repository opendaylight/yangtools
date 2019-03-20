/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An unresolved, qualified {@link QName}. It is guaranteed to hold a valid {@link #getLocalName()} bound to a namespace
 * identified through a prefix string, but remains unresolved. A resolved {@link QName} can be obtained through
 * {@link #bindTo(YangNamespaceContext)}.
 */
@Beta
@NonNullByDefault
public final class QualifiedQName extends AbstractQName implements Comparable<QualifiedQName> {
    private static final long serialVersionUID = 1L;
    private static final Interner<QualifiedQName> INTERNER = Interners.newWeakInterner();

    private final String prefix;

    private QualifiedQName(final String prefix, final String localName) {
        super(localName);
        this.prefix = requireNonNull(prefix);
    }

    public static QualifiedQName of(final String prefix, final String localName) {
        return new QualifiedQName(checkLocalName(prefix), checkLocalName(localName));
    }

    /**
     * Read an UnboundQName from a DataInput. The format is expected to match the output format of
     * {@link #writeTo(DataOutput)}.
     *
     * @param in DataInput to read
     * @return An UnboundQName instance
     * @throws IOException if I/O error occurs
     */
    public static QualifiedQName readFrom(final DataInput in) throws IOException {
        return of(in.readUTF(), in.readUTF());
    }

    public Optional<QName> bindTo(final YangNamespaceContext namespaceContext) {
        return namespaceContext.findNamespaceForPrefix(prefix).map(this::bindTo);
    }

    private QName bindTo(final QNameModule namespace) {
        return new QName(namespace, getLocalName());
    }

    @Override
    public QualifiedQName intern() {
        return INTERNER.intern(getTemplate());
    }

    private QualifiedQName getTemplate() {
        // Make sure to intern the string and check whether it refers to the same name as we are
        final String name = getLocalName();
        final String internedName = name.intern();
        final QualifiedQName template = internedName == name ? this : new QualifiedQName(prefix.intern(), internedName);
        return INTERNER.intern(template);
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public int compareTo(final QualifiedQName o) {
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
        return this == obj || obj instanceof QualifiedQName
                && getLocalName().equals(((AbstractQName) obj).getLocalName());
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
