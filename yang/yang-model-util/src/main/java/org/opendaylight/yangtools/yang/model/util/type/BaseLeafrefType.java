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
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;

final class BaseLeafrefType extends AbstractBaseType<LeafrefTypeDefinition> implements LeafrefTypeDefinition {
    private final RevisionAwareXPath pathStatement;
    private final boolean requireInstance;

    BaseLeafrefType(final SchemaPath path, final RevisionAwareXPath pathStatement, final boolean requireInstance,
            final List<UnknownSchemaNode> unknownSchemaNodes) {
        super(path, unknownSchemaNodes);
        this.pathStatement = Preconditions.checkNotNull(pathStatement);
        this.requireInstance = requireInstance;
    }

    @Override
    public RevisionAwareXPath getPathStatement() {
        return pathStatement;
    }

    @Override
    public boolean requireInstance() {
        return requireInstance;
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
