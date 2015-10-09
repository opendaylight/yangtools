/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

@Beta
public abstract class AbstractTypeDefinitionBuilder<T extends TypeDefinition<T>> extends AbstractSchemaNodeBuilder<T> {
    private Object defaultValue;
    private String units;
    private T baseType;

    public final AbstractTypeDefinitionBuilder<T> setBaseType(@Nonnull final T baseType) {
        this.baseType = Preconditions.checkNotNull(baseType);
        return this;
    }

    public final AbstractTypeDefinitionBuilder<T> setDefaultValue(@Nonnull final Object defaultValue) {
        this.defaultValue = Preconditions.checkNotNull(defaultValue);
        return this;
    }

    public final AbstractTypeDefinitionBuilder<T> setUnits(final String units) {
        this.units = Preconditions.checkNotNull(units);
        return this;
    }

    @Override
    protected final T buildNode(final SchemaPath path, final Status status, final String description, final String reference,
            final List<UnknownSchemaNode> unknownSchemaNodes) {
        // TODO Auto-generated method stub
        return buildNode(path, status, description, reference, unknownSchemaNodes, baseType, units, defaultValue);
    }

    protected abstract T buildNode(SchemaPath path, Status status, String description, String reference,
            List<UnknownSchemaNode> unknownSchemaNodes, T baseType, String units, Object defaultValue);
}
