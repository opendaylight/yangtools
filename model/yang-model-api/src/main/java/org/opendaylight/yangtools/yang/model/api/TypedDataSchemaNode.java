/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

/**
 * A {@link DataSchemaNode} which holds values of the same type. This can be either a single value, like
 * in a {@link LeafSchemaNode} or multiple values, like a {@link LeafListSchemaNode}.
 *
 * @author Robert Varga
 */
public sealed interface TypedDataSchemaNode extends DataSchemaNode, TypeAware, EffectiveStatementEquivalent
        permits LeafSchemaNode, LeafListSchemaNode {
    /**
     * Returns type of the instance which implements <code>DataSchemaNode</code>.
     *
     * @return type definition of leaf or leaf-list schema node which represents the
     *         value of the argument of the YANG <code>type</code> substatement
     *         of the <code>leaf</code> or <code>leaf-list</code> statement
     */
    @Override
    TypeDefinition<? extends TypeDefinition<?>> getType();
}
