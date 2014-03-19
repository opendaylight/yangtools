/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.operations;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.SchemaUtils;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;

class AugmentationNodeModification extends AbstractContainerNodeModification<AugmentationSchema, AugmentationNode> {

    @Override
    protected QName getQName(AugmentationSchema schema) {
        // FIXME null qname for AUGMENT
        return null;
    }

    @Override
    protected Object findSchemaForChild(AugmentationSchema schema, QName nodeType) {
        return SchemaUtils.findSchemaForChild(schema, nodeType);
    }

    @Override
    protected Object findSchemaForAugment(AugmentationSchema schema, InstanceIdentifier.AugmentationIdentifier childToProcessId) {
        throw new UnsupportedOperationException("Augmentation cannot be augmented directly, " + schema);
    }

    @Override
    protected DataContainerNodeBuilder<?, AugmentationNode> getBuilder(AugmentationSchema schema) {
        return Builders.augmentationBuilder(schema);
    }
}
