/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.util;

import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;

public final class BindingSchemaMapping {
    private BindingSchemaMapping() {

    }

    public static String getGetterMethodName(final DataSchemaNode node) {
        return node instanceof TypedDataSchemaNode ? getGetterMethodName((TypedDataSchemaNode) node)
                : BindingMapping.getGetterMethodName(node.getQName(), false);
    }

    public static String getGetterMethodName(final TypedDataSchemaNode node) {
        // Bug 8903: If it is a derived type of boolean, not an inner type, then the return type
        // of method would be the generated type of typedef not build-in types, so here it should be 'get'.
        final TypeDefinition<?> type = node.getType();
        return BindingMapping.getGetterMethodName(node.getQName(),
            type instanceof BooleanTypeDefinition
            && (type.getPath().equals(node.getPath()) || type.getBaseType() == null));
    }
}
