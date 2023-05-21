/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.impl.context;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

final class ListContext extends AbstractListLikeContext {
    ListContext(final ListSchemaNode schema) {
        // FIXME: yeah, NodeIdentifier is being used for individual nodes, but it really should not
        //        (they are not addressable)
        super(schema, new ListItemContext(NodeIdentifier.create(schema.getQName()), schema));
    }

    @Override
    public AbstractContext childByArg(final PathArgument arg) {
        return requireNonNull(arg) instanceof NodeIdentifier ? childByQName(arg.getNodeType()) : null;
    }
}
