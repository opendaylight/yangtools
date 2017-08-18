/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.RegEx;
import org.opendaylight.yangtools.concepts.Identifier;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
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
    /**
     * Default revision for sources without specified revision. This should be used for comparisons
     * based on revision when a SourceIdentifier does not have a revision.
     */
    public static final String NOT_PRESENT_FORMATTED_REVISION = "0000-00-00";

    @RegEx
    private static final String REVISION_PATTERN_STR = "\\d\\d\\d\\d-\\d\\d-\\d\\d";

    /**
     * Simplified compiled revision pattern in format YYYY-mm-dd, which checks
     * only distribution of number elements.
     *
     * <p>
     * For checking if supplied string is real date, use {@link SimpleDateFormatUtil} instead.
     */
    public static final Pattern REVISION_PATTERN = Pattern.compile(REVISION_PATTERN_STR);

    private static final Interner<SourceIdentifier> INTERNER = Interners.newWeakInterner();

    private static final long serialVersionUID = 1L;
    private final String revision;
    private final String name;

    /**
     * Creates new YANG Schema source identifier for sources without revision.
     *
     * @param name
     *            Name of schema
     */
    SourceIdentifier(final String name) {
        this.name = Preconditions.checkNotNull(name);
        this.revision = null;
    }

    /**
     * Creates new YANG Schema source identifier.
     *
     * @param name
     *            Name of schema
     * @param formattedRevision
     *            Revision of source in format YYYY-mm-dd
     */
    SourceIdentifier(final String name, final String formattedRevision) {
        this.name = Preconditions.checkNotNull(name);
        this.revision = Preconditions.checkNotNull(formattedRevision);
    }

    /**
     * Creates new YANG Schema source identifier.
     *
     * @param name
     *            Name of schema
     * @param formattedRevision
     *            Revision of source in format YYYY-mm-dd. If not present,
     *            default value will be used.
     */
    SourceIdentifier(final String name, final Optional<String> formattedRevision) {
        this.name = Preconditions.checkNotNull(name);
        this.revision = formattedRevision.orElse(null);
    }

    /**
     * Return an interned reference to a equivalent SemVerSourceIdentifier.
     *
     * @return Interned reference, or this object if it was interned.
     */
    public SourceIdentifier intern() {
        return INTERNER.intern(this);
    }

    /**
     * Returns model name.
     *
     * @return model name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns revision of source or null if revision was not supplied.
     *
     * @return revision of source or null if revision was not supplied.
     */
    // FIXME: version 2.0.0: this should return Optional<String>
    public String getRevision() {
        return revision;
    }

    /**
     * Since we've got two ways of model versioning (revision &amp; semantic version),
     * this method shouldn't be called directly anymore. Eventually, callers of this method
     * should be notified before method gets deleted.
     * @deprecated use either
     * <ul>
     * <li>{@link SemVerSourceIdentifier#create(String, SemVer)}</li>
     * <li>{@link SemVerSourceIdentifier#create(String, Optional, SemVer)}</li>
     * <li>{@link SemVerSourceIdentifier#create(String, String, SemVer)}</li>
     * </ul>or
     * <ul>
     * <li>{@link RevisionSourceIdentifier#create(String)}</li>
     * <li>{@link RevisionSourceIdentifier#create(String, String)}</li>
     * <li>{@link RevisionSourceIdentifier#create(String, Optional)}</li>
     * </ul>
     *
     * @param moduleName
     *            Name of schema
     * @param revision
     *            Revision of source in format YYYY-mm-dd. If not present,
     *            default value will be used.
     * @return particular SourceIdentifier instance
     */
    @Deprecated
    public static SourceIdentifier create(final String moduleName, final Optional<String> revision) {
        return new RevisionSourceIdentifier(moduleName, revision);
    }

    /**
     * Returns filename for this YANG module as specified in RFC 6020.
     *
     * <p>
     * Returns filename in format <code>name ['@' revision] '.yang'</code>,
     * where revision is date in format YYYY-mm-dd.
     *
     * <p>
     * @see <a href="http://tools.ietf.org/html/rfc6020#section-5.2">RFC6020</a>
     *
     * @return Filename for this source identifier.
     */
    public String toYangFilename() {
        final String rev = NOT_PRESENT_FORMATTED_REVISION.equals(revision) ? null : revision;
        return toYangFileName(name, Optional.ofNullable(rev));
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
    public static String toYangFileName(final String moduleName, final Optional<String> revision) {
        StringBuilder filename = new StringBuilder(moduleName);
        if (revision.isPresent()) {
            filename.append('@');
            filename.append(revision.get());
        }
        filename.append(YangConstants.RFC6020_YANG_FILE_EXTENSION);
        return filename.toString();
    }
}
