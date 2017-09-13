/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import org.opendaylight.yangtools.yang.common.QNameModule;

/**
 * Interface describing YANG <code>leaf</code> statement. The interface contains
 * the methods for getting the following data (substatements of
 * <code>leaf</code> statement)
 * <ul>
 * <li><code>type</code></li>
 * <li><code>default</code></li>
 * <li><code>units</code></li>
 * </ul>
 * <p>
 * The 'leaf' statement is used to define a leaf node in the schema tree.
 * </p>
 */
public interface LeafSchemaNode extends TypedSchemaNode {
    /**
     * Returns the default value of YANG <code>leaf</code>.
     *
     * @return string with the value of the argument of YANG
     *         <code>default</code> substatement of the <code>leaf</code>
     *         statement
     */
    String getDefault();

    /**
     * Returns the module where default value is declared if node type is
     * identityref. Used to resolve default value in original module context.
     *
     * @return module where <code>default</code> substatement is declared
     */
    QNameModule getDefaultValueModule();

    /**
     * Returns the units in which are the values of the <code>leaf</code>
     * presented.
     *
     * @return string with the value of the argument of YANG <code>units</code>
     *         substatement of the <code>leaf</code> statement
     */
    String getUnits();

}
