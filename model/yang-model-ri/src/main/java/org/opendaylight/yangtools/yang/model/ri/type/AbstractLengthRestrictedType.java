/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.LengthRestrictedTypeDefinition;

abstract class AbstractLengthRestrictedType<T extends LengthRestrictedTypeDefinition<T>>
        extends AbstractRestrictedType<T> implements LengthRestrictedTypeDefinition<T> {
    private final @Nullable LengthConstraint lengthConstraint;

    AbstractLengthRestrictedType(final T baseType, final QName qname,
            final Collection<? extends UnknownSchemaNode> unknownSchemaNodes,
            final @Nullable LengthConstraint lengthConstraint) {
        super(baseType, qname, unknownSchemaNodes);
        this.lengthConstraint = lengthConstraint;
    }

    @Override
    public final Optional<LengthConstraint> getLengthConstraint() {
        return Optional.ofNullable(lengthConstraint);
    }
}
