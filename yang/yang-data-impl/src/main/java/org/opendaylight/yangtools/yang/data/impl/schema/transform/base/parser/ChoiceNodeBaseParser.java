/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser;

import java.util.Map;
import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.SchemaUtils;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Abstract(base) parser for ChoiceNodes, parses elements of type E.
 *
 * @param <E> type of elements to be parsed
 */
public abstract class ChoiceNodeBaseParser<E> extends
        BaseDispatcherParser<E, ChoiceNode, org.opendaylight.yangtools.yang.model.api.ChoiceNode> {

    @Override
    protected final DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifier, ChoiceNode> getBuilder(
            org.opendaylight.yangtools.yang.model.api.ChoiceNode schema) {
        return Builders.choiceBuilder(schema);
    }

    @Override
    protected final Set<DataSchemaNode> getRealSchemasForAugment(org.opendaylight.yangtools.yang.model.api.ChoiceNode schema,
            AugmentationSchema augmentSchema) {
        Set<DataSchemaNode> fromAllCases = Sets.newHashSet();

        fromAllCases.addAll(SchemaUtils.getRealSchemasForAugment(schema, augmentSchema));

        for (ChoiceCaseNode choiceCaseNode : schema.getCases()) {
            fromAllCases.addAll(SchemaUtils.getRealSchemasForAugment((AugmentationTarget) choiceCaseNode, augmentSchema));
        }

        return fromAllCases;
    }

    @Override
    protected final DataSchemaNode getSchemaForChild(org.opendaylight.yangtools.yang.model.api.ChoiceNode schema,
            QName childQName) {
        return SchemaUtils.findSchemaForChild(schema, childQName);
    }

    @Override
    protected final Map<QName, org.opendaylight.yangtools.yang.model.api.ChoiceNode> mapChildElementsFromChoices(
            org.opendaylight.yangtools.yang.model.api.ChoiceNode schema) {
        Map<QName, org.opendaylight.yangtools.yang.model.api.ChoiceNode> mappedChoices = Maps.newLinkedHashMap();

        for (ChoiceCaseNode choiceCaseNode : schema.getCases()) {
            mappedChoices.putAll(SchemaUtils.mapChildElementsFromChoices(choiceCaseNode));
        }

        return mappedChoices;
    }

    @Override
    protected final Map<QName, AugmentationSchema> mapChildElementsFromAugments(
            org.opendaylight.yangtools.yang.model.api.ChoiceNode schema) {
        Map<QName, AugmentationSchema> mappedAugments = Maps.newLinkedHashMap();

        for (ChoiceCaseNode choiceCaseNode : schema.getCases()) {
            mappedAugments.putAll(SchemaUtils.mapChildElementsFromAugments(choiceCaseNode));
        }
        return mappedAugments;
    }

}
