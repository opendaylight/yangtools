/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.Objects;
import java.util.Optional;

/**
 * YANG Schema revision source identifier.
 *
 * <p>
 * Simple transfer object represents revision identifier of source for YANG
 * schema (module or submodule), which consists of
 * <ul>
 * <li>YANG schema name ({@link #getName()}
 * <li>Module revision (optional) ({link {@link #getRevision()})
 * </ul>
 *
 * <p>
 * Revision source identifier is designated to be carry only necessary
 * information to look-up YANG model source and to be used by various
 * SchemaSourceProviders.
 *
 * <p>
 * <b>Note:</b>On source retrieval layer it is impossible to distinguish between
 * YANG module and/or submodule unless source is present.
 *
 * <p>
 * (For further reference see: http://tools.ietf.org/html/rfc6020#section-5.2
 * and http://tools.ietf.org/html/rfc6022#section-3.1 ).
 */
@Beta
public final class RevisionSourceIdentifier extends SourceIdentifier {
    private static final long serialVersionUID = 1L;

    /**
     * Creates new YANG Schema revision source identifier for sources without
     * a revision.
     *
     * @param name
     *            Name of schema
     */
    RevisionSourceIdentifier(final String name) {
        super(name);
    }

    /**
     * Creates new YANG Schema revision source identifier.
     *
     * @param name
     *            Name of schema
     * @param formattedRevision
     *            Revision of source in format YYYY-mm-dd
     */
    RevisionSourceIdentifier(final String name, final String formattedRevision) {
        super(requireNonNull(name), requireNonNull(formattedRevision));
    }

    /**
     * Creates new YANG Schema revision source identifier.
     *
     * @param name
     *            Name of schema
     * @param formattedRevision
     *            Revision of source in format YYYY-mm-dd. If not present,
     *            default value will be used.
     */
    RevisionSourceIdentifier(final String name, final Optional<String> formattedRevision) {
        super(name, formattedRevision);
    }

    /**
     * Creates new YANG Schema revision source identifier.
     *
     * @param moduleName
     *            Name of schema
     * @param revision
     *            Revision of source in format YYYY-mm-dd. If not present,
     *            default value will be used.
     */
    public static RevisionSourceIdentifier create(final String moduleName,
            final Optional<String> revision) {
        return new RevisionSourceIdentifier(moduleName, revision);
    }

    /**
     * Creates new YANG Schema revision source identifier.
     *
     * @param moduleName
     *            Name of schema
     * @param revision
     *            Revision of source in format YYYY-mm-dd
     */
    public static RevisionSourceIdentifier create(final String moduleName, final String revision) {
        return new RevisionSourceIdentifier(moduleName, revision);
    }

    /**
     * Creates new YANG Schema revision source identifier for sources without
     * a revision.
     *
     * @param moduleName
     *            Name of schema
     */
    public static RevisionSourceIdentifier create(final String moduleName) {
        return new RevisionSourceIdentifier(moduleName);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(getName());
        result = prime * result + Objects.hashCode(getRevision());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RevisionSourceIdentifier)) {
            return false;
        }
        final RevisionSourceIdentifier other = (RevisionSourceIdentifier) obj;
        return Objects.equals(getName(), other.getName()) && Objects.equals(getRevision(), other.getRevision());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RevisionSourceIdentifier [name=");
        sb.append(getName());

        final String rev = getRevision();
        if (rev != null) {
            sb.append('@').append(rev);
        }
        return sb.append(']').toString();
    }
}
