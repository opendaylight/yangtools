/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.impl.legacy;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Abstract superclass for individual list items -- be it {@link ListItemContextNode} or
 * {@link UnkeyedListItemContextNode}.
 */
abstract class AbstractListItemContextNode extends DataContainerContextNode {
    AbstractListItemContextNode(final PathArgument pathArgument, final DataNodeContainer container,
            final DataSchemaNode schema) {
        super(pathArgument, container, schema);
    }

    @Override
    void pushToStack(final SchemaInferenceStack stack) {
        // No-op
    }
}
