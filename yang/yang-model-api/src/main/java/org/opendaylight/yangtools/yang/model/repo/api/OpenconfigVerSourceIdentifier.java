/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import java.util.Objects;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.model.api.Module;

/**
 * YANG Schema source identifier with specified openconfig version
 *
 * Simple transfer object represents identifier of source for YANG schema
 * (module or submodule), which consists of
 * <ul>
 * <li>YANG schema name {@link #getName()}
 * <li>Openconfig version of yang schema {@link #getOpenconfigVersion()}
 * <li>(Optional) Module revision ({link {@link #getRevision()}
 * </ul>
 *
 * Source identifier is designated to be carry only necessary information to
 * look-up YANG model source and to be used by various SchemaSourceProviders.
 *
 * <b>Note:</b>On source retrieval layer it is impossible to distinguish between
 * YANG module and/or submodule unless source is present.
 *
 * <p>
 * (For further reference see: http://tools.ietf.org/html/rfc6020#section-5.2
 * and http://tools.ietf.org/html/rfc6022#section-3.1 ).
 */
@Beta
public final class OpenconfigVerSourceIdentifier extends SourceIdentifier {
    private static final long serialVersionUID = 1L;
    private final SemVer semVer;

    /**
     * Creates new YANG Schema openconfig-version source identifier.
     *
     * @param name
     *            Name of schema
     * @param formattedRevision
     *            Optional of source revision in format YYYY-mm-dd. If not
     *            present, default value will be used.
     * @param semVer
     *            openconfig version of source
     */
    OpenconfigVerSourceIdentifier(final String name, final Optional<String> formattedRevision, final SemVer semVer) {
        super(name, formattedRevision);
        this.semVer = semVer == null ? Module.DEFAULT_OPENCONFIG_VERSION : semVer;
    }

    /**
     * Creates new YANG Schema openconfig-version source identifier.
     *
     * @param name
     *            Name of schema
     * @param semVer
     *            openconfig version of source
     */
    OpenconfigVerSourceIdentifier(final String name, final SemVer semVer) {
        this(name, Optional.absent(), semVer);
    }

    /**
     * Returns openconfig version of source or
     * {@link Module#DEFAULT_OPENCONFIG_VERSION} if openconfig version was not
     * supplied.
     *
     * @return revision of source or {@link Module#DEFAULT_OPENCONFIG_VERSION} if
     *         revision was not supplied.
     */
    public SemVer getOpenconfigVersion() {
        return semVer;
    }

    /**
     * Creates new YANG Schema openconfig-version source identifier.
     *
     * @param moduleName
     *            Name of schema
     * @param semVer
     *            openconfig version of source
     */
    public static OpenconfigVerSourceIdentifier create(final String moduleName, final SemVer semVer) {
        return new OpenconfigVerSourceIdentifier(moduleName, semVer);
    }

    /**
     * Creates new YANG Schema openconfig-version source identifier.
     *
     * @param moduleName
     *            Name of schema
     * @param revision
     *            Revision of source in format YYYY-mm-dd
     * @param semVer
     *            openconfig version of source
     */
    public static OpenconfigVerSourceIdentifier create(final String moduleName, final String revision,
            final SemVer semVer) {
        return new OpenconfigVerSourceIdentifier(moduleName, Optional.of(revision), semVer);
    }

    /**
     * Creates new YANG Schema openconfig-version source identifier.
     *
     * @param moduleName
     *            Name of schema
     * @param revision
     *            Optional of source revision in format YYYY-mm-dd. If not
     *            present, default value will be used.
     * @param semVer
     *            openconfig version of source
     */
    public static OpenconfigVerSourceIdentifier create(final String moduleName,
            final Optional<String> revision, final SemVer semVer) {
        return new OpenconfigVerSourceIdentifier(moduleName, revision, semVer);
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
        if (!(obj instanceof OpenconfigVerSourceIdentifier)) {
            return false;
        }
        final OpenconfigVerSourceIdentifier other = (OpenconfigVerSourceIdentifier) obj;
        return Objects.equals(getName(), other.getName()) && Objects.equals(semVer, other.semVer);
    }

    @Override
    public String toString() {
        return "OpenconfigVerSourceIdentifier [name=" + getName() + "(" + semVer + ")" + "@" + getRevision() + "]";
    }
}
