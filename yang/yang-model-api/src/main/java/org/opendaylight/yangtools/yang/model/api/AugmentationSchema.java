/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.List;
import java.util.Optional;

/**
 * AugmentationSchema represents augment definition. The "augment" statement
 * allows a module or submodule to add to the schema tree defined in an external
 * module, or the current module and its submodules, and to add to the nodes
 * from a grouping in a "uses" statement.
 */
public interface AugmentationSchema extends DataNodeContainer, NotificationNodeContainer, ActionNodeContainer {
    /**
     * Returns when statement.
     *
     * <p>
     * If when condition is present node defined by the parent data definition
     * statement is only valid when the returned XPath
     * expression conceptually evaluates to "true"
     * for a particular instance, then the node defined by the parent data
     * definition statement is valid; otherwise, it is not.
     *
     * @return XPath condition
     */
    RevisionAwareXPath getWhenCondition();

    /**
     * Returns description text.
     *
     * @return textual description of this augment.
     */
    String getDescription();

    /**
     * Returns reference text.
     *
     * @return textual cross-reference to an external document that provides
     *         additional information relevant to this node.
     */
    String getReference();

    /**
     * Returns status of this node.
     *
     * @return actual status of this node.
     */
    Status getStatus();

    /**
     * Returns augmentation schema path.
     *
     * @return SchemaPath that identifies a node in the schema tree. This node
     *         is called the augment's target node. The target node MUST be
     *         either a container, list, choice, case, input, output, or
     *         notification node. It is augmented with the nodes defined as
     *         child nodes of this AugmentationSchema.
     */
    SchemaPath getTargetPath();

    /**
     * Returns unknown schema node children.
     *
     * @return collection of all unknown nodes defined in this augmentation
     */
    List<UnknownSchemaNode> getUnknownSchemaNodes();

    /**
     * Returns Augmentation Definition from which this augmentation is derived
     * if augmentation was added transitively via augmented uses.
     *
     * @return Augmentation Definition from which this augmentation is derived
     *         if augmentation was added transitively via augmented uses.
     */
    Optional<AugmentationSchema> getOriginalDefinition();

}
