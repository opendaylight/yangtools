/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;

/**
 * AugmentationSchema represents augment definition. The "augment" statement allows a module or submodule to add
 * to the schema tree defined in an external module, or the current module and its submodules, and to add to the nodes
 * from a grouping in a "uses" statement.
 */
public interface AugmentationSchemaNode extends DataNodeContainer, NotificationNodeContainer, ActionNodeContainer,
        WhenConditionAware, WithStatus, EffectiveStatementEquivalent<AugmentEffectiveStatement> {
    /**
     * Returns augmentation target path.
     *
     * @return SchemaNodeIdentifier that identifies a node in the schema tree. This node is called the augment's target
     *         node. The target node MUST be either a container, list, choice, case, input, output, or anotification
     *         node. It is augmented with the nodes defined as child nodes of this AugmentationSchema.
     */
    default SchemaNodeIdentifier getTargetPath() {
        return asEffectiveStatement().argument();
    }
}
