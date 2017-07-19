/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser;

import com.google.common.collect.Sets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.SchemaUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * Abstract(base) parser for ChoiceNodes, parses elements of type E.
 *
 * @param <E> type of elements to be parsed
 * @deprecated Use yang-data-codec-xml instead.
 */
@Deprecated
public abstract class ChoiceNodeBaseParser<E> extends BaseDispatcherParser<E, YangInstanceIdentifier.NodeIdentifier, ChoiceNode, ChoiceSchemaNode> {

    protected ChoiceNodeBaseParser() {}

    protected ChoiceNodeBaseParser(final BuildingStrategy<YangInstanceIdentifier.NodeIdentifier, ChoiceNode> buildingStrategy) {
        super(buildingStrategy);
    }

    @Override
    protected final DataContainerNodeBuilder<YangInstanceIdentifier.NodeIdentifier, ChoiceNode> getBuilder(
            final ChoiceSchemaNode schema) {
        return Builders.choiceBuilder(schema);
    }

    @Override
    protected final Set<DataSchemaNode> getRealSchemasForAugment(final ChoiceSchemaNode schema, final AugmentationSchema augmentSchema) {
        Set<DataSchemaNode> fromAllCases = Sets.newHashSet();

        fromAllCases.addAll(SchemaUtils.getRealSchemasForAugment(schema, augmentSchema));

        for (ChoiceCaseNode choiceCaseNode : schema.getCases()) {
            fromAllCases.addAll(SchemaUtils.getRealSchemasForAugment((AugmentationTarget) choiceCaseNode, augmentSchema));
        }

        return fromAllCases;
    }

    @Override
    protected final DataSchemaNode getSchemaForChild(final ChoiceSchemaNode schema,
            final QName childQName) {
        return SchemaUtils.findSchemaForChild(schema, childQName);
    }

    @Override
    protected final Map<QName, ChoiceSchemaNode> mapChildElementsFromChoices(final ChoiceSchemaNode schema) {
        Map<QName, ChoiceSchemaNode> mappedChoices = new LinkedHashMap<>();

        for (ChoiceCaseNode choiceCaseNode : schema.getCases()) {
            mappedChoices.putAll(SchemaUtils.mapChildElementsFromChoices(choiceCaseNode));
        }

        return mappedChoices;
    }

    @Override
    protected final Map<QName, AugmentationSchema> mapChildElementsFromAugments(final ChoiceSchemaNode schema) {
        Map<QName, AugmentationSchema> mappedAugments = new LinkedHashMap<>();

        for (ChoiceCaseNode choiceCaseNode : schema.getCases()) {
            mappedAugments.putAll(SchemaUtils.mapChildElementsFromAugments(choiceCaseNode));
        }
        return mappedAugments;
    }

}
