/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Abstract superclass for individual list items -- be it {@link ListItemContextNode} or
 * {@link UnkeyedListItemContextNode}.
 */
abstract class AbstractListItemContextNode<T extends PathArgument> extends DataContainerContextNode<T> {
    AbstractListItemContextNode(final T identifier, final DataNodeContainer container, final DataSchemaNode schema) {
        super(identifier, container, schema);
    }

    @Override
    protected void pushToStack(final SchemaInferenceStack stack) {
        // No-op
    }
}
