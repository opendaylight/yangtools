/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.impl.model;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

final class LeafListContextNode extends AbstractListLikeContextNode {
    LeafListContextNode(final LeafListSchemaNode schema) {
        super(schema, new LeafListItemContextNode(schema));
    }

    @Override
    public AbstractDataSchemaContextNode childByArg(final PathArgument arg) {
        return requireNonNull(arg) instanceof NodeWithValue ? childByQName(arg.getNodeType()) : null;
    }
}
