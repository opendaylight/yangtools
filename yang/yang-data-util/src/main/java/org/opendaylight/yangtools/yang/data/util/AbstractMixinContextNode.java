/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

abstract class AbstractMixinContextNode<T extends PathArgument> extends AbstractInteriorContextNode<T> {
    AbstractMixinContextNode(final T identifier, final DataSchemaNode schema) {
        super(identifier, schema);
    }

    @Override
    public final boolean isMixin() {
        return true;
    }
}