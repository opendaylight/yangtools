/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

public abstract class AbstractRangedTypeDefinitionBuilder<T extends TypeDefinition<T>> extends AbstractTypeDefinitionBuilder<T> {
    private final Collection<RangeConstraint> rangeConstraints = new ArrayList<>();

    public final AbstractRangedTypeDefinitionBuilder<T> addRangeConstraint(@Nonnull final RangeConstraint constraint) {
        rangeConstraints.add(Preconditions.checkNotNull(constraint));
        return this;
    }

    @Override
    protected T buildNode(final SchemaPath path, final Status status, final String description, final String reference,
            final List<UnknownSchemaNode> unknownSchemaNodes, final T baseType, final String units,
            final Object defaultValue) {
        constraints.validate();
        return buildNode(path, status, description, reference, unknownSchemaNodes, baseType, units, defaultValue,
            )
    }

}
