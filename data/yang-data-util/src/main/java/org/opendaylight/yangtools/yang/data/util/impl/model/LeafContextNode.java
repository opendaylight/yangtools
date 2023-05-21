/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.impl.model;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextNode.SimpleValue;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;

public final class LeafContextNode extends AbstractDataSchemaContextNode implements SimpleValue {
    LeafContextNode(final LeafSchemaNode schema) {
        super(NodeIdentifier.create(schema.getQName()), schema);
    }
}
