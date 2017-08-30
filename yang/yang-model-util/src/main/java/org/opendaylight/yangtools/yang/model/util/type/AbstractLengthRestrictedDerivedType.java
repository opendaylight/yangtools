/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.LengthRestrictedTypeDefinition;

abstract class AbstractLengthRestrictedDerivedType<T extends LengthRestrictedTypeDefinition<T>>
        extends AbstractDerivedType<T> implements LengthRestrictedTypeDefinition<T> {

    AbstractLengthRestrictedDerivedType(final T baseType, final SchemaPath path,
            final Object defaultValue, final String description, final String reference, final Status status,
            final String units, final Collection<UnknownSchemaNode> unknownSchemaNodes) {
        super(baseType, path, defaultValue, description, reference, status, units, unknownSchemaNodes);
    }

    @Nonnull
    @Override
    public final List<LengthConstraint> getLengthConstraints() {
        return baseType().getLengthConstraints();
    }
}
