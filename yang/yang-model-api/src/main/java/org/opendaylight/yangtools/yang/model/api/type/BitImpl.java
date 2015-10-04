/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.type;

import com.google.common.base.Preconditions;
import java.util.List;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;

final class BitImpl extends AbstractSchemaNode implements Bit, Immutable {
    private final Long position;

    protected BitImpl(final SchemaPath path, final String description, final String reference, final Status status,
            final List<UnknownSchemaNode> unknownNodes, final Long position) {
        super(path, description, reference, status, unknownNodes);
        this.position = Preconditions.checkNotNull(position);
    }

    @Override
    public Long getPosition() {
        return position;
    }

    @Override
    public String getName() {
        return getQName().getLocalName();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + position.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        BitImpl other = (BitImpl) obj;
        return position.equals(other.position);
    }

    @Override
    public String toString() {
        return BitImpl.class.getSimpleName() + "[name=" + getName() + ", position="
                + position + "]";
    }
}
