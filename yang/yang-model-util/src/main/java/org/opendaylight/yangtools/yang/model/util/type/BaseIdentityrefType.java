/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;

final class BaseIdentityrefType extends AbstractBaseType<IdentityrefTypeDefinition>
        implements IdentityrefTypeDefinition {
    private final IdentitySchemaNode identity;
    private final Set<IdentitySchemaNode> identities;

    BaseIdentityrefType(final SchemaPath path, final List<UnknownSchemaNode> unknownSchemaNodes,
            final IdentitySchemaNode identity, final Set<IdentitySchemaNode> identities) {
        super(path, unknownSchemaNodes);
        this.identity = Preconditions.checkNotNull(identity);
        this.identities = Preconditions.checkNotNull(identities);
    }

    @Deprecated
    @Override
    public IdentitySchemaNode getIdentity() {
        return identity;
    }

    @Override
    public Set<IdentitySchemaNode> getIdentities() {
        return identities;
    }

    @Override
    public int hashCode() {
        return TypeDefinitions.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return TypeDefinitions.equals(this, obj);
    }

    @Override
    public String toString() {
        return TypeDefinitions.toString(this);
    }
}
