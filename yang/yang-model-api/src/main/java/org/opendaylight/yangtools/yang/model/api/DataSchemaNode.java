/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

/**
 *
 * Data Schema Node represents abstract supertype from which all data tree
 * definitions are derived.
 *
 * Contains the method which are used for getting metadata from the schema nodes
 * which contains data.
 *
 * @see ContainerSchemaNode
 * @see ListSchemaNode
 * @see LeafListSchemaNode
 * @see ChoiceSchemaNode
 * @see ChoiceCaseNode
 * @see LeafSchemaNode
 * @see AnyXmlSchemaNode
 * @see AnyDataSchemaNode
 *
 *
 */
public interface DataSchemaNode extends SchemaNode {

    /**
     * Returns <code>true</code> if the data node was added by augmentation,
     * otherwise returns <code>false</code>
     *
     * @return <code>true</code> if the data node was added by augmentation,
     *         otherwise returns <code>false</code>
     */
    boolean isAugmenting();

    /**
     * Returns <code>true</code> if the data node was added by uses statement,
     * otherwise returns <code>false</code>
     *
     * @return <code>true</code> if the data node was added by uses statement,
     *         otherwise returns <code>false</code>
     */
    boolean isAddedByUses();

    /**
     * Returns <code>true</code> if the data represents configuration data,
     * otherwise returns <code>false</code>
     *
     * @return <code>true</code> if the data represents configuration data,
     *         otherwise returns <code>false</code>
     */
    boolean isConfiguration();

    /**
     * Returns the constraints associated with Data Schema Node
     *
     * @return the constraints associated with Data Schema Node
     */
    ConstraintDefinition getConstraints();
}
