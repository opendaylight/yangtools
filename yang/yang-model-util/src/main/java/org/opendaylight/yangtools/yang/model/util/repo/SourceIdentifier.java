/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.repo;

import com.google.common.base.Optional;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 *
 * YANG Schema source identifier
 *
 * Simple transfer object represents identifier of source for YANG schema (module or submodule),
 * which consists of
 * <ul>
 * <li>YANG schema name ({@link #getName()}
 * <li>Module revision (optional) ({link {@link #getRevision()})
 * </ul>
 *
 * Source identifier is designated to be carry only necessary information
 * to look-up YANG model source and to be used by {@link AdvancedSchemaSourceProvider}
 * and similar.
 *
 * <b>Note:</b>On source retrieval layer it is impossible to distinguish
 * between YANG module and/or submodule unless source is present.
 *
 * <p>
 * (For further reference see: http://tools.ietf.org/html/rfc6020#section-5.2 and
 * http://tools.ietf.org/html/rfc6022#section-3.1 ).
 *
 *
 * @deprecated Replaced with {@link org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier}
 */
@Deprecated
public final class SourceIdentifier implements Immutable {

    private final String name;
    private final String revision;

    /**
     *
     * Creates new YANG Schema source identifier.
     *
     * @param name Name of schema
     * @param formattedRevision Revision of source in format YYYY-mm-dd
     */
    public SourceIdentifier(final String name, final Optional<String> formattedRevision) {
        super();
        this.name = name;
        this.revision = formattedRevision.orNull();
    }

    /**
     * Returns model name
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
    public String getRevision() {
        return revision;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((revision == null) ? 0 : revision.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SourceIdentifier other = (SourceIdentifier) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (revision == null) {
            if (other.revision != null) {
                return false;
            }
        } else if (!revision.equals(other.revision)) {
            return false;
        }
        return true;
    }

    public static SourceIdentifier create(final String moduleName, final Optional<String> revision) {
        return new SourceIdentifier(moduleName, revision);
    }

    /**
     * Returns filename for this YANG module as specified in RFC 6020.
     *
     * Returns filename in format
     * <code>name ['@' revision] '.yang'</code>
     * <p>
     * Where revision is  date in format YYYY-mm-dd.
     * <p>
     * See
     * http://tools.ietf.org/html/rfc6020#section-5.2
     *
     * @return Filename for this source identifier.
     */
    public String toYangFilename() {
        return toYangFileName(name, Optional.fromNullable(revision));
    }

    @Override
    public String toString() {
        return "SourceIdentifier [name=" + name + "@" + revision + "]";
    }

    /**
     * Returns filename for this YANG module as specified in RFC 6020.
     *
     * Returns filename in format
     * <code>moduleName ['@' revision] '.yang'</code>
     *
     * Where Where revision-date is in format YYYY-mm-dd.
     *
     * <p>
     * See
     * http://tools.ietf.org/html/rfc6020#section-5.2
     *
     * @return Filename for this source identifier.
     */
    public static String toYangFileName(final String moduleName, final Optional<String> revision) {
        StringBuilder filename = new StringBuilder(moduleName);
        if (revision.isPresent()) {
            filename.append('@');
            filename.append(revision.get());
        }
        filename.append(".yang");
        return filename.toString();
    }

}
