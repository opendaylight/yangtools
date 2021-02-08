/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.type;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;

final class BaseLeafrefType extends AbstractBaseType<LeafrefTypeDefinition> implements LeafrefTypeDefinition {
    private final PathExpression pathStatement;
    private final boolean requireInstance;

    BaseLeafrefType(final QName qname, final PathExpression pathStatement, final boolean requireInstance,
            final Collection<? extends UnknownSchemaNode> unknownSchemaNodes) {
        super(qname, unknownSchemaNodes);
        this.pathStatement = requireNonNull(pathStatement);
        this.requireInstance = requireInstance;
    }

    private BaseLeafrefType(final BaseLeafrefType original, final QName qname) {
        super(original, qname);
        this.pathStatement = original.pathStatement;
        this.requireInstance = original.requireInstance;
    }

    @Override
    BaseLeafrefType bindTo(final QName newQName) {
        return new BaseLeafrefType(this, newQName);
    }

    @Override
    public PathExpression getPathStatement() {
        return pathStatement;
    }

    @Override
    public boolean requireInstance() {
        return requireInstance;
    }

    @Override
    public int hashCode() {
        return LeafrefTypeDefinition.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return LeafrefTypeDefinition.equals(this, obj);
    }

    @Override
    public String toString() {
        return LeafrefTypeDefinition.toString(this);
    }
}
