/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

abstract class AbstractLeafContextNode<T extends PathArgument, S extends DataSchemaNode>
        extends DataSchemaContextNode<T> {
    AbstractLeafContextNode(final T identifier, final S schema) {
        // non-null asserted by subclass constructor's access
        // FIXME: move this down to superclass
        super(identifier, requireNonNull(schema));
    }

    @Override
    public final DataSchemaContextNode<?> getChild(final PathArgument child) {
        return null;
    }

    @Override
    public final DataSchemaContextNode<?> getChild(final QName child) {
        return null;
    }
}
