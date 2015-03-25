/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

/**
 *
 * Node which can have documentation assigned.
 *
 */
public interface DocumentedNode {

    /**
     * Returns description of the instance of the type <code>SchemaNode</code>
     *
     * @return string with textual description the node which represents the
     *         argument of the YANG <code>description</code> substatement
     */
    String getDescription();

    /**
     * Returns reference of the instance of the type <code>SchemaNode</code>
     *
     * The reference refers to external document that provides additional
     * information relevant for the instance of this type.
     *
     * @return string with the reference to some external document which
     *         represents the argument of the YANG <code>reference</code>
     *         substatement
     */
    String getReference();

    /**
     * Returns status of the instance of the type <code>SchemaNode</code>
     *
     * @return status of this node which represents the argument of the YANG
     *         <code>status</code> substatement
     */
    Status getStatus();
}
