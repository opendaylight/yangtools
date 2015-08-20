/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.api;

import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * Interface for all yang data-schema nodes [anyxml, case, container, grouping,
 * list, module, notification].
 */
public interface DataSchemaNodeBuilder extends SchemaNodeBuilder, GroupingMember {

    /**
     * Get original builder definition from grouping, where it is defined.
     *
     * @return original builder definition from grouping if this node is added
     *         by uses, null otherwise
     */
    SchemaNodeBuilder getOriginal();

    /**
     * Set original builder definition from grouping
     *
     * @param original
     *            original builder definition from grouping
     */
    void setOriginal(SchemaNodeBuilder original);

    /**
     *
     * Returns true if product of this builder is added by augmentation.
     *
     * @return true, if this node is added by augmentation, false otherwise
     */
    boolean isAugmenting();

    /**
     * Set if the product of the builder node is introduced by augmentation.
     *
     * @param augmenting information about augmentation
     */
    void setAugmenting(boolean augmenting);

    /**
     * Get value of config statement.
     *
     * @return value of config statement
     */
    boolean isConfiguration();

    /**
     * Set config statement to the product.
     *
     *
     * @param config true if config true was set, false if config false was set.
     */
    void setConfiguration(boolean config);

    /**
     * Get constraints of this builder.
     *
     * @return constraints of this builder
     */
    ConstraintsBuilder getConstraints();

    /**
     * Build DataSchemaNode object from this builder.
     *
     * @return instance of {@link DataSchemaNode} based on the state present in this builder.
     */
    @Override
    DataSchemaNode build();

}
