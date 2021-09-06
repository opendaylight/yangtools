/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownEffectiveStatement;

/**
 * Contains the methods for getting the details about the unknown node.
 */
public interface UnknownSchemaNode extends SchemaNode, CopyableNode,
        EffectiveStatementEquivalent<UnknownEffectiveStatement<?, ?>> {
    /**
     * Returns QName instance with the name of the unknown node. This corresponds to
     * {@link ExtensionDefinition#getQName()} of the {@code extension} statement which defined it.
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
     * Get extension definition which identifies this node.
     *
     * @return extension definition if exists, null otherwise
     * @deprecated This method is can cause cyclic dependencies in the object graph. Use
     *             {@link SchemaContext#getExtensions()} with {@link #getNodeType()} instead.
     */
    // FIXME: YANGTOOLS-1317: remove this method
    @Deprecated(forRemoval = true, since = "7.0.8")
    ExtensionDefinition getExtensionDefinition();
}
