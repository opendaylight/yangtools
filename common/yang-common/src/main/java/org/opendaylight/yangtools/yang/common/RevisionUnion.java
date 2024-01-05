/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.WritableObject;

/**
 * A capture of an optional {@code revision-date}. This is a replacement for {@code Optional<Revision>}, with the added
 * benefit of having a non-null string representation in {@link #unionString()}, which is also conveniently returned
 * from {@link #toString()}.
 *
 * <p>
 * This contract is exactly the same as this fragment from {@code ietf-yang-library}:
 * <pre>{@code
 *   type union {
 *     type revision-identifier;
 *     type string {
 *       length "0";
 *     }
 *   }
 * }</pre>
 */
@NonNullByDefault
public sealed interface RevisionUnion extends Comparable<RevisionUnion>, Immutable, Serializable, WritableObject
        permits Revision, NotRevision {
    /**
     * Return empty {@link RevisionUnion}.
     *
     * @return empty {@link RevisionUnion}
     */
    static NotRevision none() {
        return NotRevision.of();
    }

    static RevisionUnion of(final String unionString) {
        return unionString.isEmpty() ? none() : Revision.of(unionString);
    }

    static RevisionUnion of(final @Nullable Revision revision) {
        return revision != null ? revision : none();
    }

    /**
     * A {@code revision-date}-compliant date, or an empty string ({@code ""}).
     *
     * @return A revision-date or empty string
     */
    String unionString();

    /**
     * Return the {@link Revision}, if present.
     *
     * @return the revision, or {@code null} if not present
     */
    @Nullable Revision revision();

    default Optional<Revision> findRevision() {
        return Optional.ofNullable(revision());
    }

    default Revision getRevision() {
        final var revision = revision();
        if (revision == null) {
            throw new NoSuchElementException();
        }
        return revision;
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    default int compareTo(final RevisionUnion o) {
        // Since all strings conform to the format, we can use their comparable property to do the correct thing
        // with respect to temporal ordering.
        return unionString().compareTo(o.unionString());
    }

    @Override
    default void writeTo(final DataOutput out) throws IOException {
        out.writeUTF(unionString());
    }

    static RevisionUnion readFrom(final DataInput in) throws IOException {
        final var unionString = in.readUTF();
        return unionString.isEmpty() ? none() : Revision.ofRead(unionString);
    }

    @Override
    int hashCode();

    @Override
    boolean equals(@Nullable Object obj);

    /**
     * Returns {@link #unionString()}.
     *
     * @return {@link #unionString()}
     */
    @Override
    String toString();
}
