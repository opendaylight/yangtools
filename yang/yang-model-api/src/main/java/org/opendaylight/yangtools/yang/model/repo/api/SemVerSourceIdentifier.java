/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import com.google.common.annotations.Beta;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.common.Revision;

/**
 * YANG Schema source identifier with specified semantic version.
 *
 * <p>
 * Simple transfer object represents identifier of source for YANG schema (module or submodule), which consists of
 * <ul>
 * <li>YANG schema name {@link #getName()}
 * <li>Semantic version of yang schema {@link #getSemanticVersion()}
 * <li>(Optional) Module revision ({link {@link #getRevision()}
 * </ul>
 *
 * <p>
 * Source identifier is designated to be carry only necessary information to look-up YANG model source and to be used
 * by various SchemaSourceProviders.
 *
 * <p>
 * <b>Note:</b>On source retrieval layer it is impossible to distinguish between YANG module and/or submodule unless
 * source is present.
 *
 * <p>
 * (For further reference see: http://tools.ietf.org/html/rfc6020#section-5.2
 * and http://tools.ietf.org/html/rfc6022#section-3.1 ).
 */
@Beta
public final class SemVerSourceIdentifier extends SourceIdentifier {
    private static final long serialVersionUID = 1L;
    private final SemVer semVer;

    /**
     * Creates new YANG Schema semVer source identifier.
     *
     * @param name Name of schema
     * @param revision Revision of source, possibly not present
     * @param semVer semantic version of source
     */
    SemVerSourceIdentifier(final String name, final Optional<Revision> revision,
            final @Nullable SemVer semVer) {
        super(name, revision);
        this.semVer = semVer;
    }

    /**
     * Creates new YANG Schema semVer source identifier.
     *
     * @param name Name of schema
     * @param semVer semantic version of source
     */
    SemVerSourceIdentifier(final String name, final @Nullable SemVer semVer) {
        this(name, Optional.empty(), semVer);
    }

    /**
     * Returns semantic version of source if it was specified.
     *
     * @return revision of source.
     */
    public Optional<SemVer> getSemanticVersion() {
        return Optional.ofNullable(semVer);
    }

    /**
     * Creates new YANG Schema semVer source identifier.
     *
     * @param moduleName Name of schema
     * @param semVer semantic version of source
     */
    public static @NonNull SemVerSourceIdentifier create(final String moduleName, final SemVer semVer) {
        return new SemVerSourceIdentifier(moduleName, semVer);
    }

    /**
     * Creates new YANG Schema semVer source identifier.
     *
     * @param moduleName Name of schema
     * @param revision Revision of source in format YYYY-mm-dd
     * @param semVer semantic version of source
     */
    public static @NonNull SemVerSourceIdentifier create(final String moduleName, final Revision revision,
            final SemVer semVer) {
        return new SemVerSourceIdentifier(moduleName, Optional.ofNullable(revision), semVer);
    }

    /**
     * Creates new YANG Schema semVer source identifier.
     *
     * @param moduleName Name of schema
     * @param revision Optional of source revision in format YYYY-mm-dd. If not present, default value will be used.
     * @param semVer semantic version of source
     */
    public static @NonNull SemVerSourceIdentifier create(final String moduleName, final Optional<Revision> revision,
            final SemVer semVer) {
        return new SemVerSourceIdentifier(moduleName, revision, semVer);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(getName());
        result = prime * result + Objects.hashCode(semVer);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SemVerSourceIdentifier)) {
            return false;
        }
        final SemVerSourceIdentifier other = (SemVerSourceIdentifier) obj;
        return Objects.equals(getName(), other.getName()) && Objects.equals(semVer, other.semVer);
    }

    @Override
    public String toString() {
        return "SemVerSourceIdentifier [name=" + getName() + "(" + semVer + ")" + "@" + getRevision() + "]";
    }
}
