/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import javax.annotation.Nullable;

/**
 *
 * YANG statement <code>typedef</code> contains also substatements
 * <ul>
 * <li><code>default</code> - default value which is compatible with
 * <code>type</code>,</li>
 * <li><code>type</code> - base type from which is <code>typedef</code> derived,
 * </li>
 * <li><code>units</code> - textual information about units associated with this
 * type.</li>
 * </ul>
 * This interface contains the methods for getting the values of the arguments
 * of substatements mentioned above.
 *
 * @param <T>
 *            type of the base type (YANG <code>type</code> substatement) which
 *            is included in the instance of this type
 */
public interface TypeDefinition<T extends TypeDefinition<?>> extends SchemaNode {

    /**
     * Returns the base type from which this type is derived. If this is yang
     * built-in type, returns null.
     *
     * @return value of <code>&lt;T&gt;</code> type which represents the base
     *         type of instance of the <code>TypeDefinition</code> type or null,
     *         if this is yang built-in type
     */
    @Nullable T getBaseType();

    /**
     * Returns the unit which represents the value of the argument of the
     * <code>units</code> substatement of the YANG <code>typedef</code>
     * statement.
     *
     * @return string with units in which is type measured, or null if no units are defined.
     */
    @Nullable String getUnits();

    /**
     * Returns the default value which represents the value of the argument of
     * the <code>default</code> substatement of the YANG <code>typedef</code>
     * statement.
     *
     * @return instance of <code>Object</code> type which contains default value
     *         for <code>typedef</code>, or null if no default value is defined.
     */
    @Nullable Object getDefaultValue();
}
