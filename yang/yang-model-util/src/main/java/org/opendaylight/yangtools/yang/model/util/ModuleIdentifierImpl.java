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
import java.util.Date;
import java.util.Optional;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;

/**
 * ModuleIdentifier that can be used for indexing/searching by name.
 * Name is only non-null attribute.
 * Equality check on namespace and revision is only triggered if they are non-null
 *
 * @deprecated This class will be removed with {@link ModuleIdentifier}
 */
@Deprecated
@Beta
public final class ModuleIdentifierImpl implements ModuleIdentifier {
    private final Date revision;
    private final String name;

    private ModuleIdentifierImpl(final String name, final Optional<Date> revision) {
        this.name = checkNotNull(name);
        this.revision = revision.orElse(null);
    }

    public static ModuleIdentifier create(final String name, final Optional<Date> revision) {
        return new ModuleIdentifierImpl(name, revision);
    }

    @Override
    public Optional<Date> getRevision() {
        return Optional.ofNullable(revision);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "ModuleIdentifierImpl{"
            + "name='" + name + '\''
            + ", revision=" + revision
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

        // only fail if this revision is non-null
        if (getRevision() != null && !getRevision().equals(other.getRevision())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
