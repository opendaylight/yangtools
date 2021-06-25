/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.yang.types;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.Restrictions;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.ri.BaseYangTypes;
import org.opendaylight.mdsal.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

@Deprecated(forRemoval = true)
final class BaseYangTypesProvider implements TypeProvider {
    static final @NonNull BaseYangTypesProvider INSTANCE = new BaseYangTypesProvider();

    private BaseYangTypesProvider() {
        // Hidden on purpose
    }

    /**
     * Searches <code>Type</code> value to which is YANG <code>type</code>
     * mapped.
     *
     * @param type
     *            type definition representation of YANG type
     * @return java <code>Type</code> representation of <code>type</code>.
     *         If <code>type</code> isn't found then <code>null</code> is
     *         returned.
     */
    @Override
    public Type javaTypeForSchemaDefinitionType(final TypeDefinition<?> type, final SchemaNode parentNode,
            final boolean lenientRelativeLeafrefs) {
        return type == null ? null : BaseYangTypes.javaTypeForYangType(type.getQName().getLocalName());
    }

    @Override
    public Type javaTypeForSchemaDefinitionType(final TypeDefinition<?> type, final SchemaNode parentNode,
            final Restrictions restrictions, final boolean lenientRelativeLeafrefs) {
        final String typeName = type.getQName().getLocalName();
        final Type mapped = BaseYangTypes.javaTypeForYangType(typeName);
        return mapped == null || restrictions == null ? mapped : Types.restrictedType(mapped, restrictions);
    }
}