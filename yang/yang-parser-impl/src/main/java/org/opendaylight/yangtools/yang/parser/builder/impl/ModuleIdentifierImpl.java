/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Optional;
import java.net.URI;
import java.util.Date;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;

/**
 * ModuleIdentifier that can be used for indexing/searching by name.
 * Name is only non-null attribute.
 * Equality check on namespace and revision is only triggered if they are non-null
 */
public class ModuleIdentifierImpl implements ModuleIdentifier {
    private final QNameModule qnameModule;
    private final String name;

    public ModuleIdentifierImpl(final String name, final Optional<URI> namespace, final Optional<Date> revision) {
        this.name = checkNotNull(name);
        this.qnameModule = QNameModule.create(namespace.orNull(), revision.orNull());
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
    public String getName() {
        return name;
    }

    @Override
    public URI getNamespace() {
        return qnameModule.getNamespace();
    }

    @Override
    public String toString() {
        return "ModuleIdentifierImpl{" +
                "name='" + name + '\'' +
                ", namespace=" + getNamespace() +
                ", revision=" + getRevision() +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || (!(o instanceof ModuleIdentifier))) {
            return false;
        }

        ModuleIdentifier that = (ModuleIdentifier) o;

        if (!name.equals(that.getName())) {
            return false;
        }

        // only fail if this namespace is non-null
        if (getNamespace() != null && !getNamespace().equals(that.getNamespace())) {
            return false;
        }
        // only fail if this revision is non-null
        if (getRevision() != null && !getRevision().equals(that.getRevision())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
