/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.impl.model;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

final class MapContextNode extends AbstractListLikeContextNode {
    MapContextNode(final ListSchemaNode list) {
        super(list, new ListItemContextNode(null, list));
    }

    @Override
    public AbstractDataSchemaContextNode childByArg(final PathArgument arg) {
        return requireNonNull(arg) instanceof NodeIdentifierWithPredicates ? childByQName(arg.getNodeType()) : null;
    }
}
