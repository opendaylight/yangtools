/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.zip.DataFormatException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Identifier;
import org.opendaylight.yangtools.concepts.WritableObject;

/**
 * Base encapsulation of a <href="https://www.rfc-editor.org/rfc/rfc8342#page-22">NMDA datastore-ref</a>value. We do not
 * use that name because we want that identifier to be available to code generators for ease of use by high-level users.
 *
 * @apiNote
 *   The design here is fixed to our current best knowledge of published models.
 *
 * @since 14.0.21
 */
// FIXME: reconsider interface design when we have JEP-401 in reasonable shape. We do not care about identity, we are
//        just encapsulating a QName. That should allow us to have non-public constructors with immutability
//        of a record, and make DatastoreIdentity.Unknown a value class without needing the corresponding record.
@NonNullByDefault
public sealed interface DatastoreIdentity extends Identifier, WritableObject
        permits DatastoreIdentity.Known, DatastoreIdentity.Unknown {
    /**
     * A reference to a {@code datastore} which has known semantics.
     */
    sealed interface Known extends DatastoreIdentity permits Known.Conventional, Known.Dynamic, Operational {
        sealed interface Conventional extends Known permits Candidate, Intended, Running, Startup {
            // Just a marker
        }

        /**
         * A datastore which is known to be dynamic.
         */
        sealed interface Dynamic extends Known permits InvalidDatastore {
            // Just a marker
        }
    }

    /**
     * A reference to a {@code datastore} which has unknown semantics.
     */
    sealed interface Unknown extends DatastoreIdentity permits UnknownDatastore {
        // just a marker
    }

    /**
     * A reference to the {@code operational} datastore.
     */
    sealed interface Operational extends Known permits OperationalDatastore {
        // Just a marker, the implementation is hidden
    }

    /**
     * A reference to the {@code candidate} datastore.
     */
    sealed interface Candidate extends Known.Conventional permits CandidateDatastore {
        // Just a marker, the implementation is hidden
    }

    /**
     * A reference to the {@code intended} datastore.
     */
    sealed interface Intended extends Known.Conventional permits IntendedDatastore {
        // Just a marker, the implementation is hidden
    }

    /**
     * A reference to the {@code running} datastore.
     */
    sealed interface Running extends Known.Conventional permits RunningDatastore {
        // Just a marker, the implementation is hidden
    }

    /**
     * A reference to the {@code startup} datastore.
     */
    sealed interface Startup extends Known.Conventional permits StartupDatastore {
        // Just a marker, the implementation is hidden
    }

    static DatastoreIdentity of(final QName value) {
        return DatastoreIdentityMethods.of(value);
    }

    static DatastoreIdentity ofInterned(final QName value) {
        return DatastoreIdentityMethods.ofInterned(value);
    }

    /**
     * Read a {@link DatastoreIdentity} written by {@link #writeTo(DataOutput)}.
     *
     * @param in the {@link DataInput}
     * @return a {@link DataFormatException}
     * @throws IOException if an I/O error occurs
     */
    static DatastoreIdentity readFrom(final DataInput in) throws IOException {
        return ofInterned(QName.readFrom(in));
    }

    @Override
    default void writeTo(final DataOutput out) throws IOException {
        value().writeTo(out);
    }

    /**
     * {@return leaf value {@code QName}}
     */
    QName value();
}
