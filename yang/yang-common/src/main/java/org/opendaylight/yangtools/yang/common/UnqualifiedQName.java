/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An unresolved, unqualified {@link QName}. It is guaranteed to hold a valid {@link #getLocalName()}, in the default
 * namespace, which is not resolved. A resolved {@link QName} can be constructed through {@link #bindTo(QNameModule)}.
 */
@Beta
@NonNullByDefault
public final class UnqualifiedQName extends AbstractQName implements Comparable<UnqualifiedQName> {
    private static final long serialVersionUID = 1L;
    private static final Interner<UnqualifiedQName> INTERNER = Interners.newWeakInterner();

    private UnqualifiedQName(final String localName) {
        super(localName);
    }

    public static UnqualifiedQName of(final String localName) {
        return new UnqualifiedQName(checkLocalName(localName));
    }

    /**
     * Read an UnboundQName from a DataInput. The format is expected to match the output format of
     * {@link #writeTo(DataOutput)}.
     *
     * @param in DataInput to read
     * @return An UnboundQName instance
     * @throws IOException if I/O error occurs
     */
    public static UnqualifiedQName readFrom(final DataInput in) throws IOException {
        return of(in.readUTF());
    }

    public QName bindTo(final QNameModule namespace) {
        return new QName(namespace, getLocalName());
    }

    @Override
    @SuppressFBWarnings(value = "ES_COMPARING_STRINGS_WITH_EQ", justification = "Interning identity check")
    public UnqualifiedQName intern() {
        // Make sure to intern the string and check whether it refers to the same name as we are
        final String name = getLocalName();
        final String internedName = name.intern();
        final UnqualifiedQName template = internedName == name ? this : new UnqualifiedQName(internedName);
        return INTERNER.intern(template);
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public int compareTo(final UnqualifiedQName o) {
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
        return this == obj || obj instanceof UnqualifiedQName
                && getLocalName().equals(((AbstractQName) obj).getLocalName());
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
