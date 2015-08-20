/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.api;

import java.util.List;
import org.opendaylight.yangtools.concepts.Mutable;

/**
 * Parent interface for all builder interfaces.
 */
public interface Builder extends Mutable {

    /**
     * Returns name of module in which node created by this builder
     * was declared.
     *
     * @return module name
     */
    String getModuleName();

    /**
     * Set name of module in which this node is declared.
     *
     * @param moduleName name of module
     * @deprecated Module name should be set during creation of builder.
     */
    @Deprecated
    void setModuleName(String moduleName);

    /**
     * Get current line in yang file, on which statement
     * associated with this builder was declared.
     *
     * @return current line in yang file
     */
    int getLine();

    /**
     * Returns parent node builder of this node.
     *
     * @return parent node builder or null if this is top level node
     */
    Builder getParent();

    /**
     * Set parent of this node.
     *
     * @param parent
     *            parent node builder
     */
    void setParent(Builder parent);

    /**
     * Adds an unknown node builder to this builder.
     *
     * When product (child) is builded by the {@link #build()}
     * method, this builder is also built and unknown node is added
     * as child to the product of this builder.
     *
     * @param unknownNode an unknown node builder
     */
    void addUnknownNodeBuilder(UnknownSchemaNodeBuilder unknownNode);

    /**
     * Get builders of unknown nodes defined in this node.
     *
     * @return collection of UnknownSchemaNodeBuilder objects
     */
    List<UnknownSchemaNodeBuilder> getUnknownNodes();

    /**
     * Build YANG data model node.
     *
     * This method should create an instance of YANG data model node. After
     * creating an instance, this instance should be returned for each call
     * without repeating build process.
     *
     * @return YANG data model node
     */
    Object build();

}
