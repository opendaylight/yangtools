/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.api;

import org.opendaylight.yangtools.yang.model.api.*;

/**
 * Interface for builders of 'augment' statement.
 */
public interface AugmentationSchemaBuilder extends DataNodeContainerBuilder {

    String getWhenCondition();

    void addWhenCondition(String whenCondition);

    String getDescription();

    void setDescription(String description);

    String getReference();

    void setReference(String reference);

    Status getStatus();

    void setStatus(Status status);

    /**
     * Get path to target node as single string.
     *
     * @return path to target node as String
     */
    String getTargetPathAsString();

    /**
     * Get path to target node.
     * <p>
     * Note that individual parts of path contain only prefix relative to
     * current context and name of node.
     * </p>
     *
     * @return path to target node as SchemaPath
     */
    SchemaPath getTargetPath();

    /**
     * Get schema path of target node.
     *
     * @return SchemaPath of target node
     */
    SchemaPath getTargetNodeSchemaPath();

    /**
     * Set schema path of target node.
     *
     * @param path
     *            SchemaPath of target node
     */
    void setTargetNodeSchemaPath(SchemaPath path);

    AugmentationSchema build();

    /**
     * Get information about augmentation process.
     *
     * @return true, if augmentation process was performed already, false
     *         otherwise
     */
    boolean isResolved();

    /**
     * Set information about augmentation process.
     *
     * @param resolved
     */
    void setResolved(boolean resolved);

}
