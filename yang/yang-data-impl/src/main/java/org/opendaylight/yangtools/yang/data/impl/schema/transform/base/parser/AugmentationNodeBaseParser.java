/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.SchemaUtils;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

public abstract class AugmentationNodeBaseParser<E> extends
        BaseDispatcherParser<E,AugmentationNode, AugmentationSchema> {

    @Override
    protected DataContainerNodeBuilder<InstanceIdentifier.AugmentationIdentifier, AugmentationNode> getBuilder(AugmentationSchema schema) {
        return Builders.augmentationBuilder(schema);
    }

    @Override
    protected Set<DataSchemaNode> getRealSchemasForAugment(AugmentationSchema schema, AugmentationSchema augmentSchema) {
        return SchemaUtils.getRealSchemasForAugment(schema, augmentSchema);
    }


    @Override
    protected DataSchemaNode getSchemaForChild(AugmentationSchema schema, QName childPartialQName) {
        return SchemaUtils.findSchemaForChild(schema, childPartialQName);
    }

    @Override
    protected Map<QName, ChoiceNode> mapChildElementsFromChoices(AugmentationSchema schema) {
        return SchemaUtils.mapChildElementsFromChoices(schema);
    }

    @Override
    protected Map<QName, AugmentationSchema> mapChildElementsFromAugments(AugmentationSchema schema) {
        return Collections.emptyMap();
    }
}
