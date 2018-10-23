/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Identifier;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.YangConstants;

/**
 * Base class of YANG Schema source identifiers.
 *
 * <p>
 * Source identifiers are designated to be carry only necessary information to
 * look-up YANG model source and to be used by various SchemaSourceProviders.
 *
 * <p>
 * (For further reference see: http://tools.ietf.org/html/rfc6020#section-5.2
 * and http://tools.ietf.org/html/rfc6022#section-3.1 ).
 */
@Beta
public abstract class SourceIdentifier implements Identifier, Immutable {
    private static final Interner<SourceIdentifier> INTERNER = Interners.newWeakInterner();
    private static final long serialVersionUID = 2L;

    private final @Nullable Revision revision;
    private final @NonNull String name;

    /**
     * Creates new YANG Schema source identifier for sources without revision.
     *
     * @param name Name of schema
     */
    SourceIdentifier(final @NonNull String name) {
        this(name, (Revision) null);
    }

    /**
     * Creates new YANG Schema source identifier.
     *
     * @param name Name of schema
     * @param revision Revision of source, may be null
     */
    SourceIdentifier(final @NonNull String name, final @Nullable Revision revision) {
        this.name = requireNonNull(name);
        this.revision = revision;
    }

    /**
     * Creates new YANG Schema source identifier.
     *
     * @param name Name of schema
     * @param revision Revision of source, possibly not present
     */
    SourceIdentifier(final @NonNull String name, final Optional<Revision> revision) {
        this(name, revision.orElse(null));
    }

    /**
     * Return an interned reference to a equivalent SemVerSourceIdentifier.
     *
     * @return Interned reference, or this object if it was interned.
     */
    public @NonNull SourceIdentifier intern() {
        return INTERNER.intern(this);
    }

    /**
     * Returns model name.
     *
     * @return model name
     */
    public @NonNull String getName() {
        return name;
    }

    /**
     * Returns revision of source or null if revision was not supplied.
     *
     * @return revision of source or null if revision was not supplied.
     */
    public Optional<Revision> getRevision() {
        return Optional.ofNullable(revision);
    }

    /**
     * Returns filename for this YANG module as specified in RFC 6020.
     *
     * <p>
     * Returns filename in format <code>name ['@' revision] '.yang'</code>, where revision is date in format YYYY-mm-dd.
     *
     * <p>
     * @see <a href="http://tools.ietf.org/html/rfc6020#section-5.2">RFC6020</a>
     *
     * @return Filename for this source identifier.
     */
    public @NonNull String toYangFilename() {
        return toYangFileName(name, Optional.ofNullable(revision));
    }

    /**
     * Returns filename for this YANG module as specified in RFC 6020.
     *
     * <p>
     * Returns filename in format <code>moduleName ['@' revision] '.yang'</code>,
     * where Where revision-date is in format YYYY-mm-dd.
     *
     * <p>
     * See http://tools.ietf.org/html/rfc6020#section-5.2
     *
     * @return Filename for this source identifier.
     */
    public static @NonNull String toYangFileName(final @NonNull String moduleName, final Optional<Revision> revision) {
        StringBuilder filename = new StringBuilder(moduleName);
        if (revision.isPresent()) {
            filename.append('@');
            filename.append(revision.get());
        }
        filename.append(YangConstants.RFC6020_YANG_FILE_EXTENSION);
        return filename.toString();
    }
}
