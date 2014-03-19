/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.operations;

import org.opendaylight.yangtools.yang.data.api.ModifyAction;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

import com.google.common.base.Optional;

/**
 * Edit config operations utilities.
 */
public final class DataOperations {

    private DataOperations() {}

    public static Optional<ContainerNode> modify(ContainerSchemaNode schema, ContainerNode stored,
            ContainerNode modified) throws DataModificationException {
        return modify(schema, stored, modified, ModifyAction.MERGE);
    }

    public static Optional<MapNode> modify(ListSchemaNode schema, MapNode stored, MapNode modified)
            throws DataModificationException {
        return modify(schema, stored, modified, ModifyAction.MERGE);
    }

    public static Optional<ContainerNode> modify(ContainerSchemaNode schema, ContainerNode stored,
            ContainerNode modified, ModifyAction defaultOperation) throws DataModificationException {

        OperationStack operations = new OperationStack(defaultOperation);

        return new ContainerNodeModification().modify(schema, Optional.fromNullable(stored),
                Optional.fromNullable(modified), operations);
    }

    public static Optional<MapNode> modify(ListSchemaNode schema, MapNode stored, MapNode modified,
            ModifyAction defaultOperation) throws DataModificationException {

        OperationStack operations = new OperationStack(defaultOperation);

        return new MapNodeModification().modify(schema, Optional.fromNullable(stored), Optional.fromNullable(modified),
                operations);
    }
}
