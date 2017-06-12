/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;

final class BaseEnumerationType extends AbstractBaseType<EnumTypeDefinition> implements EnumTypeDefinition {
    private final List<EnumPair> values;

    BaseEnumerationType(final SchemaPath path, final List<UnknownSchemaNode> unknownSchemaNodes,
            final Collection<EnumPair> values) {
        super(path, unknownSchemaNodes);
        this.values = ImmutableList.copyOf(values);
    }

    @Nonnull
    @Override
    public List<EnumPair> getValues() {
        return values;
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
