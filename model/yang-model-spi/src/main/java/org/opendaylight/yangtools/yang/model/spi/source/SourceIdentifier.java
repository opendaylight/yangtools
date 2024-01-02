/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import static java.util.Objects.requireNonNull;

import java.time.format.DateTimeParseException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Identifier;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangConstants;

/**
 * Base class of YANG Schema source identifiers. Source identifiers are designated to be carry only necessary
 * information to look up YANG module (or submodule) source and to be used by various SchemaSourceProviders.
 *
 * <p>
 * For further reference see: <a href="https://www.rfc-editor.org/rfc/rfc6020#section-5.2">RFC6020</a>
 * and <a href="https://www.rfc-editor.org/rfc/rfc6022#section-3.1">RFC6022</a>.
 */
public record SourceIdentifier(@NonNull Unqualified name, @Nullable Revision revision) implements Identifier {
    private static final long serialVersionUID = 3L;

    /**
     * Creates new YANG Schema source identifier for sources with or without a revision.
     *
     * @param name Name of schema
     * @param revision Revision of schema
     * @throws NullPointerException if {@code name} is null
     */
    public SourceIdentifier {
        requireNonNull(name);
    }

    /**
     * Creates new YANG Schema source identifier for sources without a revision.
     *
     * @param name Name of schema
     * @throws NullPointerException if {@code name} is null
     */
    public SourceIdentifier(final @NonNull Unqualified name) {
        this(name, null);
    }

    /**
     * Creates new YANG Schema source identifier for sources without a revision.
     *
     * @param name Name of schema
     * @throws NullPointerException if {@code name} is null
     * @throws IllegalArgumentException if {@code name} is not a valid YANG identifier
     */
    public SourceIdentifier(final @NonNull String name) {
        this(Unqualified.of(name));
    }

    /**
     * Creates new YANG Schema source identifier for sources with or without a revision.
     *
     * @param name Name of schema
     * @param revision Optional schema revision
     * @throws NullPointerException if {@code name} is null
     * @throws IllegalArgumentException if {@code name} is not a valid YANG identifier
     */
    public SourceIdentifier(final @NonNull String name, final @Nullable Revision revision) {
        this(Unqualified.of(name), revision);
    }

    /**
     * Creates new YANG Schema source identifier for sources with or without a revision.
     *
     * @param name Name of schema
     * @param revision Optional schema revision
     * @throws NullPointerException if {@code name} is null
     * @throws IllegalArgumentException if {@code name} is not a valid YANG identifier
     * @throws DateTimeParseException if {@code revision} format does not conform specification.
     */
    public SourceIdentifier(final @NonNull String name, final @Nullable String revision) {
        this(name, revision != null ? Revision.of(revision) : null);
    }

    /**
     * Returns filename for this YANG module as specified in RFC 6020.
     *
     * <p>
     * Returns filename in format <code>name ['@' revision] '.yang'</code>, where revision is date in format YYYY-mm-dd.
     *
     * <p>
     * @see <a href="http://www.rfc-editor.org/rfc/rfc6020#section-5.2">RFC6020</a>
     *
     * @return Filename for this source identifier.
     */
    public @NonNull String toYangFilename() {
        return toYangFileName(name.getLocalName(), revision);
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder("SourceIdentifier [").append(name.getLocalName());
        if (revision != null) {
            sb.append('@').append(revision);
        }
        return sb.append(']').toString();
    }

    /**
     * Returns filename for this YANG module as specified in RFC 6020.
     *
     * <p>
     * Returns filename in format <code>moduleName ['@' revision] '.yang'</code>,
     * where Where revision-date is in format YYYY-mm-dd.
     *
     * <p>
     * See http://www.rfc-editor.org/rfc/rfc6020#section-5.2
     *
     * @param moduleName module name
     * @param revision optional revision
     * @return Filename for this source identifier.
     */
    public static @NonNull String toYangFileName(final @NonNull String moduleName, final @Nullable Revision revision) {
        final StringBuilder sb = new StringBuilder(moduleName);
        if (revision != null) {
            sb.append('@').append(revision);
        }
        return sb.append(YangConstants.RFC6020_YANG_FILE_EXTENSION).toString();
    }
}
