/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import org.opendaylight.yangtools.yang.common.QName;

/**
 * Contains the methods for getting the details about the unknown node.
 */
public interface UnknownSchemaNode extends SchemaNode, AddedByUsesAware {
    /**
     * Returns QName instance with the name of the unknown node.
     *
     * @return QName with name the name of the unknown node.
     */
    QName getNodeType();

    /**
     * Returns name of the unknown node.
     *
     * @return string with the name of unknown node.
     */
    String getNodeParameter();

    /**
     * Describes whether the node was added through <code>augment</code> YANG
     * statement.
     *
     * @return boolean value which is <code>true</code> if the node is added by
     *         <code>augment</code> YANG statement
     *
     * @deprecated This method exposes mechanism of how this node was instantiated. This runs contrary to the idea
     *             that a SchemaNode is part of the effective model of the world. Examining a node's DeclaredStatement
     *             world should be sufficient to ascertain its origin.
     */
    @Deprecated
    // FIXME: 6.0.0: rename this to isAugmenting(), unifying this interface with CopyableNode
    boolean isAddedByAugmentation();

    /**
     * Get extension definition which identifies this node.
     *
     * @return extension definition if exists, null otherwise
     */
    ExtensionDefinition getExtensionDefinition();
}
