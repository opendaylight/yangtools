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
import org.opendaylight.yangtools.yang.data.impl.schema.SchemaUtils;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * Abstract(base) parser for AugmentationNode, parses elements of type E.
 *
 * @param <E> type of elements to be parsed
 */
public abstract class AugmentationNodeBaseParser<E> extends
        BaseDispatcherParser<E,AugmentationNode, AugmentationSchema> {

    @Override
    protected final DataContainerNodeBuilder<InstanceIdentifier.AugmentationIdentifier, AugmentationNode> getBuilder(AugmentationSchema schema) {
        return Builders.augmentationBuilder(schema);
    }

    @Override
    protected final Set<DataSchemaNode> getRealSchemasForAugment(AugmentationSchema schema, AugmentationSchema augmentSchema) {
        return SchemaUtils.getRealSchemasForAugment(schema, augmentSchema);
    }


    @Override
    protected final DataSchemaNode getSchemaForChild(AugmentationSchema schema, QName childQName) {
        return SchemaUtils.findSchemaForChild(schema, childQName);
    }

    @Override
    protected final Map<QName, ChoiceNode> mapChildElementsFromChoices(AugmentationSchema schema) {
        return SchemaUtils.mapChildElementsFromChoices(schema);
    }

    @Override
    protected final Map<QName, AugmentationSchema> mapChildElementsFromAugments(AugmentationSchema schema) {
        return Collections.emptyMap();
    }
}
