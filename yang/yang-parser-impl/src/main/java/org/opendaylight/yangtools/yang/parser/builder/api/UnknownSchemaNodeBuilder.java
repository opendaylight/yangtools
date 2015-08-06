/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.api;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

public interface UnknownSchemaNodeBuilder extends SchemaNodeBuilder, DocumentedNodeBuilder {

    @Override
    SchemaPath getPath();

    /**
     * Returns true if node was added via uses statement
     *
     * @return true if node was added via uses statement
     */
    boolean isAddedByUses();

    /**
     * Sets flag if node was introduced by uses statement
     *
     * @param addedByUses true if node was introduced by uses, false otherwise
     */
    void setAddedByUses(boolean addedByUses);

    /**
     *
     * Returns QName associated with this unknown node.
     *
     * @return QName associated with this unknown node.
     */
    QName getNodeType();

    /**
     * Returns string representation of argument associated with this unknown node.
     *
     * @return string representation of argument associated with this unknown node.
     */
    String getNodeParameter();

    /**
     * Sets string representation of argument associated with this unknown node.
     * @param nodeParameter string representation of argument associated with this unknown node.
     */
    void setNodeParameter(String nodeParameter);

    /**
     * Returns extension definition, which declares this unknown node
     * @return extension definition, which declares this unknown node
     */
    ExtensionDefinition getExtensionDefinition();

    /**
     * Sets extension definition, which declares this unknown node
     * @param extensionDefinition extension definition, which declares this unknown node
     */
    void setExtensionDefinition(ExtensionDefinition extensionDefinition);

    /**
     * Returns builder for extension, which declares this unknown node.
     *
     * @return builder for extension, which declares this unknown node.
     */
    ExtensionBuilder getExtensionBuilder();

    /**
     * Sets extension builder, which declares this unknown node
     *
     * @param extension
     *            extension builder, which declares this unknown node
     */
    void setExtensionBuilder(ExtensionBuilder extension);

    @Override
    UnknownSchemaNode build();

    /**
     * Sets node type associated with this unknown schema node
     *
     * @param qName node type associated with this unknown schema node
     */
    void setNodeType(QName qName);

}