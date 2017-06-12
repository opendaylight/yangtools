/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.base.Preconditions;
import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

abstract class AbstractRestrictedType<T extends TypeDefinition<T>> extends AbstractTypeDefinition<T> {
    private final T baseType;

    AbstractRestrictedType(final T baseType, final SchemaPath path,
            final Collection<UnknownSchemaNode> unknownSchemaNodes) {
        super(path, unknownSchemaNodes);
        this.baseType = Preconditions.checkNotNull(baseType);
    }

    @Override
    public final T getBaseType() {
        return baseType;
    }

    @Override
    public final String getUnits() {
        return baseType.getUnits();
    }

    @Override
    public final Object getDefaultValue() {
        return baseType.getDefaultValue();
    }

    @Override
    public final String getDescription() {
        return baseType.getDescription();
    }

    @Override
    public final String getReference() {
        return baseType.getReference();
    }

    @Nonnull
    @Override
    public final Status getStatus() {
        return baseType.getStatus();
    }
}
