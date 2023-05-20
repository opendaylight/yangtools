/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.impl.legacy;

import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

final class LeafListEntryContextNode extends AbstractLeafNodeContext {
    LeafListEntryContextNode(final LeafListSchemaNode schema) {
        // FIXME: YANGTOOLS-1413: Empty() here is NOT NICE -- it assumes the list is of such entries...
        super(new NodeWithValue<>(schema.getQName(), Empty.value()), schema);
    }

    @Override
    public boolean isKeyedEntry() {
        return true;
    }

    @Override
    void pushToStack(final SchemaInferenceStack stack) {
        // No-op
    }
}
