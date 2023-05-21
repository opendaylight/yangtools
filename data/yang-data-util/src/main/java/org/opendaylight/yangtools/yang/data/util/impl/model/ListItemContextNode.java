/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.impl.model;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Individual list items -- be {@link MapEntryNode} or {@link UnkeyedListEntryNode}.
 */
final class ListItemContextNode extends DataContainerContextNode {
    ListItemContextNode(final NodeIdentifier pathStep, final ListSchemaNode schema) {
        super(pathStep, schema, schema);
    }

    @Override
    void pushToStack(final SchemaInferenceStack stack) {
        // No-op
    }
}
