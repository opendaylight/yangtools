/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;

final class RestrictedEnumerationType extends AbstractRestrictedType<EnumTypeDefinition> implements EnumTypeDefinition {
    private final @NonNull ImmutableList<EnumPair> values;

    RestrictedEnumerationType(final EnumTypeDefinition baseType, final SchemaPath path,
            final Collection<UnknownSchemaNode> unknownSchemaNodes, final Collection<EnumPair> values) {
        super(baseType, path, unknownSchemaNodes);
        this.values = ImmutableList.copyOf(Preconditions.checkNotNull(values));
    }

    @Override
    public List<EnumPair> getValues() {
        return values;
    }

    @Override
    public int hashCode() {
        return EnumTypeDefinition.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return EnumTypeDefinition.equals(this, obj);
    }

    @Override
    public String toString() {
        return EnumTypeDefinition.toString(this);
    }
}
