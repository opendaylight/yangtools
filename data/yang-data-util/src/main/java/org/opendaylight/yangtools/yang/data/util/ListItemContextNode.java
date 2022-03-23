/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

final class ListItemContextNode extends AbstractListItemContextNode<NodeIdentifierWithPredicates> {
    ListItemContextNode(final ListSchemaNode schema) {
        // FIXME: this is wrong: we have no predicates at all!
        super(NodeIdentifierWithPredicates.of(schema.getQName()), schema, schema);
    }

    @Override
    public boolean isKeyedEntry() {
        return true;
    }
}