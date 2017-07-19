/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.base.serializer;

import java.util.Set;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.impl.schema.SchemaUtils;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * Abstract(base) serializer for AugmentationNode, serializes elements of type E.
 *
 * @param <E> type of serialized elements
 * @deprecated Use yang-data-codec-xml instead.
 */
@Deprecated
public abstract class AugmentationNodeBaseSerializer<E> extends
        BaseDispatcherSerializer<E, AugmentationNode, AugmentationSchema> {

    @Override
    protected Set<DataSchemaNode> getRealSchemasForAugment(final AugmentationSchema schema, final AugmentationSchema augmentationSchema) {
        return SchemaUtils.getRealSchemasForAugment(schema, augmentationSchema);
    }

    @Override
    protected DataSchemaNode getSchemaForChild(final AugmentationSchema schema,
                                               final DataContainerChild<? extends YangInstanceIdentifier.PathArgument, ?> childNode) {
        return SchemaUtils.findSchemaForChild(schema, childNode.getNodeType());
    }

    @Override
    protected AugmentationSchema getAugmentedCase(final AugmentationSchema schema, final AugmentationNode augmentationNode) {
        throw new UnsupportedOperationException("");
    }

}
