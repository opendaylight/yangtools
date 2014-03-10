/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.builder.impl;

import com.google.common.base.Optional;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;

import java.net.URI;
import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * ModuleIdentifier that can be used for indexing/searching by name.
 * Name is only non-null attribute.
 * Equality check on namespace and revision is only triggered if they are non-null
 */
public class ModuleIdentifierImpl implements ModuleIdentifier {
    private final String name;
    private final Optional<URI> namespace;
    private final Optional<Date> revision;

    public ModuleIdentifierImpl(String name, Optional<URI> namespace, Optional<Date> revision) {
        this.name = checkNotNull(name);
        this.namespace = checkNotNull(namespace);
        this.revision = checkNotNull(revision);
    }

    @Override
    public Date getRevision() {
        return revision.orNull();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public URI getNamespace() {
        return namespace.orNull();
    }

    @Override
    public String toString() {
        return "ModuleIdentifierImpl{" +
                "name='" + name + '\'' +
                ", namespace=" + namespace +
                ", revision=" + revision +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || (o instanceof ModuleIdentifier == false)) {
            return false;
        }

        ModuleIdentifier that = (ModuleIdentifier) o;

        if (!name.equals(that.getName())) {
            return false;
        }
        // only fail if this namespace is non-null
        if (namespace.isPresent() && namespace.get().equals(that.getNamespace()) == false)  {
            return false;
        }
        // only fail if this revision is non-null
        if (revision.isPresent() && revision.get().equals(that.getRevision()) == false) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
