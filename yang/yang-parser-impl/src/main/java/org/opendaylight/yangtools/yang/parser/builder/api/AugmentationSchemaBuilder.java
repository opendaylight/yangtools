/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.api;

import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Builder for {@link AugmentationSchema}, which represents 'augment' statement.
 */
public interface AugmentationSchemaBuilder extends DataNodeContainerBuilder,DocumentedNodeBuilder {

    /**
     * Returns when condition
     *
     * If when condition is present node defined by the parent data definition
     * statement is only valid when the returned XPath expression conceptually
     * evaluates to "true" for a particular instance, then the node defined by
     * the parent data definition statement is valid; otherwise, it is not.
     *
     * @return when condition as string
     */
    String getWhenCondition();

    /**
     * Adds string representation of when condition.
     *
     * If when condition is present node defined by the parent data definition
     * statement is only valid when the returned XPath
     * expression conceptually evaluates to "true"
     * for a particular instance, then the node defined by the parent data
     * definition statement is valid; otherwise, it is not.
     *
     * @param whenCondition string representation of when condition
     */
    void addWhenCondition(String whenCondition);

    /**
     * Returns target path representation as was present in schema source.
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

    @Override
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
     * @param resolved information about augmentation process
     */
    void setResolved(boolean resolved);

    /**
     *
     * Returns position of defining <code>augment</code> statement
     * as was present in schema source.
     *
     * @return Position of definiing augment statement in source code.
     */
    int getOrder();

    /**
     *  Set true if target of augment is unsupported (e.g. node in body of extension).
     *  In such case, augmentation is skipped and AugmentationSchema is not built.
     *
     *  @param unsupportedTarget information about target of augment statement
     */
    void setUnsupportedTarget(boolean unsupportedTarget);

    /**
     *  Return true if target of augment is unsupported (e.g. node in body of extension).
     *  In such case, augmentation is skipped and AugmentationSchema is not built.
     *
     *  @return information about target of augment statement
     */
    boolean isUnsupportedTarget();
}
