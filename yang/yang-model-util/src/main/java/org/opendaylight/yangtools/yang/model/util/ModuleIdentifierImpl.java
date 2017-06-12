/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.Beta;
import java.net.URI;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;

/**
 * ModuleIdentifier that can be used for indexing/searching by name.
 * Name is only non-null attribute.
 * Equality check on namespace and revision is only triggered if they are non-null
 */
@Beta
public final class ModuleIdentifierImpl implements ModuleIdentifier {
    private final QNameModule qnameModule;
    private final String name;
    private final SemVer semVer;

    private ModuleIdentifierImpl(final String name, final Optional<URI> namespace, final Optional<Date> revision,
            final SemVer semVer) {
        this.name = checkNotNull(name);
        this.qnameModule = QNameModule.create(namespace.orElse(null), revision.orElse(null));
        this.semVer = (semVer == null ? Module.DEFAULT_SEMANTIC_VERSION : semVer);
    }

    public static ModuleIdentifier create(final String name, final Optional<URI> namespace,
            final Optional<Date> revision) {
        return create(name, namespace, revision, Module.DEFAULT_SEMANTIC_VERSION);
    }

    public static ModuleIdentifier create(final String name, final Optional<URI> namespace,
            final Optional<Date> revision, final SemVer semVer) {
        return new ModuleIdentifierImpl(name, namespace, revision, semVer);
    }

    @Override
    public QNameModule getQNameModule() {
        return qnameModule;
    }

    @Override
    public Date getRevision() {
        return qnameModule.getRevision();
    }

    @Override
    public SemVer getSemanticVersion() {
        return semVer;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public URI getNamespace() {
        return qnameModule.getNamespace();
    }

    @Override
    public String toString() {
        return "ModuleIdentifierImpl{"
            + "name='" + name + '\''
            + ", namespace=" + getNamespace()
            + ", revision=" + qnameModule.getFormattedRevision()
            + ", semantic version=" + semVer
            + '}';
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ModuleIdentifier)) {
            return false;
        }

        ModuleIdentifier other = (ModuleIdentifier) obj;

        if (!name.equals(other.getName())) {
            return false;
        }

        // only fail if this namespace is non-null
        if (getNamespace() != null && !getNamespace().equals(other.getNamespace())) {
            return false;
        }
        // only fail if this revision is non-null
        if (getRevision() != null && !getRevision().equals(other.getRevision())) {
            return false;
        }

        if (!Objects.equals(getSemanticVersion(), other.getSemanticVersion())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
