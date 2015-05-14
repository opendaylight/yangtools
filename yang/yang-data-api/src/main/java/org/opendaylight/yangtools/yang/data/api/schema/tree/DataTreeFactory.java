/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

/**
 * Factory interface for creating data trees.
 */
public interface DataTreeFactory {
    /**
     * Create a new data tree, which may store all data structures
     * modeled by particular YANG schema.
     *
     * The returned data tree behaves same as data tree
     * created by invoking {@link #create(TreeType)} with {@link TreeType#OPERATIONAL}.
     *
     *
     * @return A data tree instance.
     */
    DataTree create();

    /**
     * Create a new data tree with specified Data Tree type
     * which affects behavior of data tree.
     *
     *
     * @return A data tree instance.
     */
    DataTree create(TreeType type);
}
