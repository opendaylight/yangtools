/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Data Schema Node represents abstract supertype from which all data tree definitions are derived. Unlike what
 * the name would suggest, this interface corresponds more to RFC7950 {@code data definition statement} than to
 * {@code data node}, yet it notably does not include {@link UsesNode} and {@link AugmentationSchemaNode}, which are
 * resolved separately.
 *
 * <p>
 * Common interface is composed of {@link #isConfiguration()}, governing validity in config/operation data stores
 * and {@link WhenConditionAware} mixin, which governs validity based on other document data.
 *
 * @see ContainerSchemaNode
 * @see ListSchemaNode
 * @see LeafListSchemaNode
 * @see ChoiceSchemaNode
 * @see CaseSchemaNode
 * @see LeafSchemaNode
 * @see AnyxmlSchemaNode
 * @see AnydataSchemaNode
 */
public interface DataSchemaNode extends SchemaNode, CopyableNode, WhenConditionAware {
    public enum ConfigValue {
        TRUE,
        FALSE,
        UNDEFINED,
    }

    /**
     * Returns indication of whether this node represents configuration, operational state or undefined.
     *
     * @return Indication of whether this node represents configuration, operational state or undefined.
     */
    @NonNull ConfigValue isConfiguration();
}
