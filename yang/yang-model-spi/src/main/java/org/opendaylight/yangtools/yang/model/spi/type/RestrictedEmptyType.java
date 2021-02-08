/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.type;

import java.util.Collection;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;

final class RestrictedEmptyType extends AbstractRestrictedType<EmptyTypeDefinition> implements EmptyTypeDefinition {
    RestrictedEmptyType(final EmptyTypeDefinition baseType, final QName qname,
            final Collection<? extends UnknownSchemaNode> unknownSchemaNodes) {
        super(baseType, qname, unknownSchemaNodes);
    }

    private RestrictedEmptyType(final RestrictedEmptyType original, final QName qname) {
        super(original, qname);
    }

    @Override
    RestrictedEmptyType bindTo(final QName newQName) {
        return new RestrictedEmptyType(this, newQName);
    }

    @Override
    public int hashCode() {
        return EmptyTypeDefinition.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return EmptyTypeDefinition.equals(this, obj);
    }

    @Override
    public String toString() {
        return EmptyTypeDefinition.toString(this);
    }
}
