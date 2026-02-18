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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Identifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangNames;

/**
 * The identifier of a schema source. This class is designed to carry the bare minimum information to look up a YANG
 * source, be it a module or a submodule.
 *
 * <p>For further reference see: <a href="https://www.rfc-editor.org/rfc/rfc6020#section-5.2">RFC6020</a>
 * and <a href="https://www.rfc-editor.org/rfc/rfc6022#section-3.1">RFC6022</a>.
 *
 * <p>This object should always be used in its {#link Identifier} capacity, which is to say parsed from the file, or
 * derived from {@code module}/{@code submodule} and {@code revvision-date} statements.
 *
 * <p>SourceIdentifier identifiers have a defined natural order, which is the product of comparing its parts:
 * {@link #name} is compared first, followed by {@link Revision#compare(Revision, Revision)}.
 */
// TODO: this class and its design will need to be re-visited once
//       https://datatracker.ietf.org/doc/draft-ietf-netmod-yang-module-filename/ is published so that the concept is
//       compatible with, and useful for implementation of,
//       https://datatracker.ietf.org/doc/draft-ietf-netmod-yang-module-versioning/ in the overall context of
//       https://datatracker.ietf.org/doc/draft-ietf-netmod-yang-semver/
//
//       When we do that, we should be mixing in optionality and consistency with the source body into the class design:
//       - the file name is a hint, where both revision and version are optional or read, but should be normalized into
//         either of the two formats
//         - here we should how these files are managed on the filesystem:
//           - the canonical name is with revision
//           - the versioned name is a symlink to the corresponding revision
//           - the revisionless name is a symlink to the latest available revision
//       - the SourceIdentifier extracted from the body is accurate value space item to which the source body is laying
//         claim
//       - there may be supplemental information available -- such as all previously-declared revisions and versions,
//         which is useful during source linkage -- but that part is a model.spi.spi.source concern
@NonNullByDefault
public record SourceIdentifier(Unqualified name, @Nullable Revision revision)
        implements Comparable<SourceIdentifier>, Identifier {
    @java.io.Serial
    private static final long serialVersionUID = 3L;

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
    public SourceIdentifier(final Unqualified name) {
        this(name, null);
    }

    /**
     * Creates new YANG Schema source identifier for sources without a revision.
     *
     * @param name Name of schema
     * @throws NullPointerException if {@code name} is {@code null}
     * @throws IllegalArgumentException if {@code name} is not a valid YANG identifier
     */
    public SourceIdentifier(final String name) {
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
    public SourceIdentifier(final String name, final @Nullable Revision revision) {
        this(Unqualified.of(name), revision);
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
    public SourceIdentifier(final String name, final @Nullable String revision) {
        this(name, revision != null ? Revision.of(revision) : null);
    }

    public static SourceIdentifier ofYangFileName(final String fileName) {
        if (fileName.endsWith(RFC6020_YANG_FILE_EXTENSION)) {
            return ofFileName(fileName.substring(0, fileName.length() - RFC6020_YANG_FILE_EXTENSION.length()));
        }
        throw new IllegalArgumentException("Filename '" + fileName + "' does not end with '.yang'");
    }

    public static SourceIdentifier ofYinFileName(final String fileName) {
        if (fileName.endsWith(RFC6020_YIN_FILE_EXTENSION)) {
            return ofFileName(fileName.substring(0, fileName.length() - RFC6020_YIN_FILE_EXTENSION.length()));
        }
        throw new IllegalArgumentException("Filename " + fileName + " does not end with '.yin'");
    }

    private static SourceIdentifier ofFileName(final String fileName) {
        final var parsed = YangNames.parseFilename(fileName);
        return new SourceIdentifier(parsed.getKey(), parsed.getValue());
    }

    /**
     * Construct a new {@link SourceIdentifier} from a {@link QName} by using its {@code localName} as our {@code name}
     * and its {@code revision} as our revision.
     *
     * <p>This is useful in intra-domain conversion, where {@link QName} was chosen as the representation of basic
     * YANG module/submodule metadata. Such representation has the benefit in that it also carries the corresponding
     * {@link XMLNamespace}, which {@link SourceIdentifier} does not.
     *
     * @param qname the {@link QName}
     * @return the {@link SourceIdentifier}
     */
    public static SourceIdentifier ofQName(final QName qname) {
        return new SourceIdentifier(qname.unbind(), qname.getModule().revision());
    }

    /**
     * Returns filename for this YANG module as specified in
     * <a href="https://www.rfc-editor.org/rfc/rfc6020#section-5.2">RFC 6020</a>.
     *
     * <p>Returns filename formatted as {@code moduleName ['@' revision] '.yang'}, where revision-date is in format
     * {@code YYYY-mm-dd}.
     *
     * @return Filename for this source identifier.
     */
    public String toYangFilename() {
        return toYangFileName(name.getLocalName(), revision);
    }

    /**
     * Returns filename for this YANG module as specified in
     * <a href="https://www.rfc-editor.org/rfc/rfc6020#section-5.2">RFC 6020</a>.
     *
     * <p>Returns filename formatted as {@code moduleName ['@' revision] '.yin'}, where revision-date is in format
     * {@code YYYY-mm-dd}.
     *
     * @return Filename for this source identifier.
     */
    public String toYinFilename() {
        return toYinFileName(name.getLocalName(), revision);
    }

    /**
     * {@return a {@code StatementSourceReference} loosely identifying the same source as this identifier}
     * @since 15.0.0
     */
    public DeclarationInSource toReference() {
        return new DeclarationInSource(this);
    }

    /**
     * {@inheritDoc}
     * @since 15.0.0
     */
    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public int compareTo(final SourceIdentifier o) {
        final int cmp = name.compareTo(o.name);
        return cmp != 0 ? cmp : Revision.compare(revision, o.revision());
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
     * <p>Returns filename formatted as {@code moduleName ['@' revision] '.yang'}, where revision-date is in format
     * {@code YYYY-mm-dd}.
     *
     * @param moduleName module name
     * @param revision optional revision
     * @return Filename for this source identifier.
     */
    public static String toYangFileName(final String moduleName, final @Nullable Revision revision) {
        return toFileName(RFC6020_YANG_FILE_EXTENSION, moduleName, revision);
    }

    /**
     * Returns filename for this YANG module as specified in
     * <a href="https://www.rfc-editor.org/rfc/rfc6020#section-5.2">RFC 6020</a>.
     *
     * <p>Returns filename formatted as {@code moduleName ['@' revision] '.yin'}, where Where revision-date is in format
     * {@code YYYY-mm-dd}.
     *
     * @param moduleName module name
     * @param revision optional revision
     * @return Filename for this source identifier.
     */
    public static String toYinFileName(final String moduleName, final @Nullable Revision revision) {
        return toFileName(RFC6020_YIN_FILE_EXTENSION, moduleName, revision);
    }

    private static String toFileName(final String extension, final String moduleName,
            final @Nullable Revision revision) {
        final var sb = new StringBuilder(moduleName);
        if (revision != null) {
            sb.append('@').append(revision);
        }
        return sb.append(extension).toString();
    }
}
