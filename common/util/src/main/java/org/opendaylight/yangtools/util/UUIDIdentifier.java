/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.util.UUID;
import org.opendaylight.yangtools.concepts.Identifier;

@Beta
public final class UUIDIdentifier implements Identifier, Comparable<UUIDIdentifier> {
    private static final long serialVersionUID = 1L;
    private final UUID uuid;

    public UUIDIdentifier(final UUID uuid) {
        this.uuid = Preconditions.checkNotNull(uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || (o instanceof UUIDIdentifier && uuid.equals(((UUIDIdentifier)o).uuid));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(UUIDIdentifier.class).add("uuid", uuid).toString();
    }

    @Override
    public int compareTo(final UUIDIdentifier o) {
        return uuid.compareTo(o.uuid);
    }
}
