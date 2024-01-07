/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.source;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.common.YangConstants.RFC6020_YANG_FILE_EXTENSION;
import static org.opendaylight.yangtools.yang.common.YangConstants.RFC6020_YIN_FILE_EXTENSION;

import java.time.format.DateTimeParseException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Identifier;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class of YANG Schema source identifiers. Source identifiers are designated to be carry only necessary
 * information to look up YANG module (or submodule) source and to be used by various SchemaSourceProviders.
 *
 * <p>
 * For further reference see: <a href="https://www.rfc-editor.org/rfc/rfc6020#section-5.2">RFC6020</a>
 * and <a href="https://www.rfc-editor.org/rfc/rfc6022#section-3.1">RFC6022</a>.
 */
public record SourceIdentifier(@NonNull Unqualified name, @Nullable Revision revision) implements Identifier {
    @java.io.Serial
    private static final long serialVersionUID = 3L;

    private static final Logger LOG = LoggerFactory.getLogger(SourceIdentifier.class);

    /**
     * Creates new YANG Schema source identifier for sources with or without a revision.
     *
     * @param name Name of schema
     * @param revision Revision of schema
     * @throws NullPointerException if {@code name} is {@code null}
     */
    public SourceIdentifier {
        requireNonNull(name);
    }

    /**
     * Creates new YANG Schema source identifier for sources without a revision.
     *
     * @param name Name of schema
     * @throws NullPointerException if {@code name} is {@code null}
     */
    public SourceIdentifier(final @NonNull Unqualified name) {
        this(name, null);
    }

    /**
     * Creates new YANG Schema source identifier for sources without a revision.
     *
     * @param name Name of schema
     * @throws NullPointerException if {@code name} is {@code null}
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
     * @throws NullPointerException if {@code name} is {@code null}
     * @throws IllegalArgumentException if {@code name} is not a valid YANG identifier
     */
    public SourceIdentifier(final @NonNull String name, final @Nullable Revision revision) {
        this(Unqualified.of(name), revision);
    }

    public static @NonNull SourceIdentifier ofYinFileName(final String fileName) {
        if (fileName.endsWith(RFC6020_YIN_FILE_EXTENSION)) {
            return ofFileName(fileName.substring(0, fileName.length() - RFC6020_YIN_FILE_EXTENSION.length()));
        }
        throw new IllegalArgumentException("Filename " + fileName + " does not end with '.yin'");
    }

    public static @NonNull SourceIdentifier ofYangFileName(final String fileName) {
        if (fileName.endsWith(RFC6020_YANG_FILE_EXTENSION)) {
            return ofFileName(fileName.substring(0, fileName.length() - RFC6020_YANG_FILE_EXTENSION.length()));
        }
        throw new IllegalArgumentException("Filename '" + fileName + "' does not end with '.yang'");
    }

    private static @NonNull SourceIdentifier ofFileName(final String fileName) {
        final var parsed = YangNames.parseFilename(fileName);
        return new SourceIdentifier(parsed.getKey(), parsed.getValue());
    }

    /**
     * Creates new YANG Schema source identifier for sources with or without a revision.
     *
     * @param name Name of schema
     * @param revision Optional schema revision
     * @throws NullPointerException if {@code name} is {@code null}
     * @throws IllegalArgumentException if {@code name} is not a valid YANG identifier
     * @throws DateTimeParseException if {@code revision} format does not conform specification.
     */
    public SourceIdentifier(final @NonNull String name, final @Nullable String revision) {
        this(name, revision != null ? Revision.of(revision) : null);
    }

    /**
     * Returns filename for this YANG module as specified in
     * <a href="https://www.rfc-editor.org/rfc/rfc6020#section-5.2">RFC 6020</a>.
     *
     * <p>
     * Returns filename formatted as {@code moduleName ['@' revision] '.yang'}, where revision-date is in format
     * {@code YYYY-mm-dd}.
     *
     * @return Filename for this source identifier.
     */
    public @NonNull String toYangFilename() {
        return toYangFileName(name.getLocalName(), revision);
    }

    /**
     * Returns filename for this YANG module as specified in
     * <a href="https://www.rfc-editor.org/rfc/rfc6020#section-5.2">RFC 6020</a>.
     *
     * <p>
     * Returns filename formatted as {@code moduleName ['@' revision] '.yin'}, where revision-date is in format
     * {@code YYYY-mm-dd}.
     *
     * @return Filename for this source identifier.
     */
    public @NonNull String toYinFilename() {
        return toYinFileName(name.getLocalName(), revision);
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
     * Returns filename for this YANG module as specified in
     * <a href="https://www.rfc-editor.org/rfc/rfc6020#section-5.2">RFC 6020</a>.
     *
     * <p>
     * Returns filename formatted as {@code moduleName ['@' revision] '.yang'}, where revision-date is in format
     * {@code YYYY-mm-dd}.
     *
     * @param moduleName module name
     * @param revision optional revision
     * @return Filename for this source identifier.
     */
    public static @NonNull String toYangFileName(final @NonNull String moduleName, final @Nullable Revision revision) {
        return toFileName(moduleName, revision, RFC6020_YANG_FILE_EXTENSION);
    }

    /**
     * Returns filename for this YANG module as specified in
     * <a href="https://www.rfc-editor.org/rfc/rfc6020#section-5.2">RFC 6020</a>.
     *
     * <p>
     * Returns filename formatted as {@code moduleName ['@' revision] '.yin'}, where Where revision-date is in format
     * {@code YYYY-mm-dd}.
     *
     * @param moduleName module name
     * @param revision optional revision
     * @return Filename for this source identifier.
     */
    public static @NonNull String toYinFileName(final @NonNull String moduleName, final @Nullable Revision revision) {
        return toFileName(moduleName, revision, RFC6020_YIN_FILE_EXTENSION);
    }

    private static @NonNull String toFileName(final @NonNull String moduleName, final @Nullable Revision revision,
            final @NonNull String extension) {
        final var sb = new StringBuilder(moduleName);
        if (revision != null) {
            sb.append('@').append(revision);
        }
        return sb.append(extension).toString();
    }
}
