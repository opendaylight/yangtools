/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Factory interface for creating data trees.
 */
public interface DataTreeFactory {
    /**
     * Create a new data tree based on specified configuration, with a best-guess root. Use this method only if you
     * do not have a corresponding SchemaContext handy. Mandatory nodes whose enforcement point is the root node will
     * not be enforced even if some are present in the SchemaContext and validation is requested in configuration.
     *
     * <p>
     * Correctness note: this method may not accurately initialize the root node in certain non-root scenarios due to
     * the impossibility to accurately derive root type from plain YangInstanceIdentifier. Using
     * {@link #create(DataTreeConfiguration, SchemaContext)} is recommended, as it does not suffer from this
     * shortcoming.
     *
     * @param treeConfig
     *          Tree configuration.
     * @return A data tree instance.
     * @throws NullPointerException if treeConfig is null
     */
    DataTree create(DataTreeConfiguration treeConfig);

    /**
     * Create a new data tree based on specified configuration, with a root node derived from the schema context lookup
     * of the configuration. Mandatory nodes whose enforcement point is the root node will not be enforced even if some
     * are present in the SchemaContext and validation is requested in configuration.
     *
     * @param treeConfig
     *          Tree configuration.
     * @return A data tree instance.
     * @throws NullPointerException if any of the arguments are null
     * @throws IllegalArgumentException if tree configuration does not match the SchemaContext, for example by root path
     *                                  referring to a node which does not exist in the SchemaContext
     */
    DataTree create(DataTreeConfiguration treeConfig, SchemaContext initialSchemaContext);

    /**
     * Create a new data tree based on specified configuration, with the specified node. Use {@link #absentRoot()}
     * if the node is not present, but may materialize later.
     *
     * @param treeConfig
     *          Tree configuration.
     * @return A data tree instance.
     * @throws DataValidationFailedException if initial root is not valid according to the schema context
     * @throws NullPointerException if any of the arguments are null
     * @throws IllegalArgumentException if a mismatch between the arguments is detected
     */
    DataTree create(DataTreeConfiguration treeConfig, SchemaContext initialSchemaContext,
            NormalizedNodeContainer<?, ?, ?> initialRoot) throws DataValidationFailedException;

    /**
     * Return an singleton marker for absent root. Returned instance should only be used in conjunction with
     * {@link #create(DataTreeConfiguration, SchemaContext, NormalizedNodeContainer)} to instantiate a DataTree which
     * is logically absent.
     *
     * @return A singleton absent root marker.
     */
    @Beta
    static NormalizedNodeContainer<?, ?, ?> absentRoot() {
        return AbsentRoot.instance();
    }
}
