/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.Optional;

/**
 * YANG statement <code>typedef</code> contains also substatements
 * <ul>
 * <li><code>default</code> - default value which is compatible with
 * <code>type</code>,</li>
 * <li><code>type</code> - base type from which is <code>typedef</code> derived,
 * </li>
 * <li><code>units</code> - textual information about units associated with this
 * type.</li>
 * </ul>
 * This interface contains the methods for getting the values of the arguments of substatements mentioned above.
 * Furthermore {@link LeafSchemaNode} and {@link LeafListSchemaNode} interfaces contribute to their internal type
 * definitions.
 *
 * @param <T>
 *            type of the base type (YANG <code>type</code> substatement) which
 *            is included in the instance of this type
 */
// FIXME: YANGTOOLS-1528: this construct is mostly used in yang-data-api/codec view of the world. Introduce a dead
//                         ringer interface, yang.data.api.type.NormalizedType and use it in yang-data-* components.
public interface TypeDefinition<T extends TypeDefinition<?>> extends SchemaNode {
    /**
     * Returns the base type from which this type is derived. If this is yang built-in type, returns null.
     *
     * @return value of <code>&lt;T&gt;</code> type which represents the base
     *         type of instance of the <code>TypeDefinition</code> type or null,
     *         if this is yang built-in type
     */
    T getBaseType();

    /**
     * Returns the unit which represents the value of the argument of the <code>units</code> substatement of the YANG
     * <code>typedef</code>, <code>leaf</code> or <code>leaf-list</code> statements.
     *
     * @return string with units in which is type measured
     */
    Optional<String> getUnits();

    /**
     * Returns the default value which represents the value of the argument of the <code>default</code> substatement
     * of the YANG <code>typedef</code> or <code>leaf</code> statement.
     *
     * @return instance of <code>Object</code> type which contains default value for <code>typedef</code>
     */
    Optional<? extends Object> getDefaultValue();
}
