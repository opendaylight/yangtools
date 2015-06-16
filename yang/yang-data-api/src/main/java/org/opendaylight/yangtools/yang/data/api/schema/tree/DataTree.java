/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Interface representing a data tree which can be modified in an MVCC fashion.
 */
public interface DataTree extends DataTreeTip {
    /**
     * Take a read-only point-in-time snapshot of the tree.
     *
     * @return Data tree snapshot.
     */
    DataTreeSnapshot takeSnapshot();

    /**
     * Make the data tree use a new schema context. The context will be used
     * only by subsequent operations.
     *
     * @param newSchemaContext new SchemaContext
     * @throws IllegalArgumentException if the new context is incompatible
     */
    void setSchemaContext(SchemaContext newSchemaContext);

    /**
     * Commit a data tree candidate.
     *
     * @param candidate data tree candidate
     */
    void commit(DataTreeCandidate candidate);

    /**
     * Get the root path of this data tree.
     *
     * @return The tree's root path.
     */
    YangInstanceIdentifier getRootPath();
}
