/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.impl.legacy;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

abstract sealed class AbstractLeafContextNode extends AbstractDataSchemaContextNode
        permits LeafContextNode, LeafListEntryContextNode, OpaqueContextNode {
    AbstractLeafContextNode(final NodeIdentifier pathStep, final DataSchemaNode schema) {
        super(pathStep, schema);
    }

    @Override
    public final DataSchemaContextNode getChild(final PathArgument child) {
        return null;
    }

    @Override
    public final DataSchemaContextNode getChild(final QName child) {
        return null;
    }

    @Override
    protected final DataSchemaContextNode enterChild(final QName child, final SchemaInferenceStack stack) {
        return null;
    }

    @Override
    protected final DataSchemaContextNode enterChild(final PathArgument child, final SchemaInferenceStack stack) {
        return null;
    }
}
