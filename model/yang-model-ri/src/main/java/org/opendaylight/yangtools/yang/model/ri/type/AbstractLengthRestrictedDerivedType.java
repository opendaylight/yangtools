/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import java.util.Collection;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.LengthRestrictedTypeDefinition;

abstract class AbstractLengthRestrictedDerivedType<T extends LengthRestrictedTypeDefinition<T>>
        extends AbstractDerivedType<T> implements LengthRestrictedTypeDefinition<T> {
    AbstractLengthRestrictedDerivedType(final T baseType, final QName qname,
            final Object defaultValue, final String description, final String reference, final Status status,
            final String units, final Collection<? extends UnknownSchemaNode> unknownSchemaNodes) {
        super(baseType, qname, defaultValue, description, reference, status, units, unknownSchemaNodes);
    }

    @Override
    public final Optional<LengthConstraint> getLengthConstraint() {
        return baseType().getLengthConstraint();
    }
}
