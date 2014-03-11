/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.dom.parser;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.dom.DomUtils;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.w3c.dom.Element;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Maps;

public final class ChoiceNodeDomParser
        extends
        AbstractDispatcherParser<InstanceIdentifier.NodeIdentifier, ChoiceNode, org.opendaylight.yangtools.yang.model.api.ChoiceNode> {

    @Override
    protected DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifier, ChoiceNode> getBuilder(
            org.opendaylight.yangtools.yang.model.api.ChoiceNode schema) {
        return Builders.choiceBuilder(schema);
    }

    @Override
    protected Set<DataSchemaNode> getRealSchemasForAugment(org.opendaylight.yangtools.yang.model.api.ChoiceNode schema, AugmentationSchema augmentSchema) {
        Set<DataSchemaNode> fromAllCases = Sets.newHashSet();

        fromAllCases.addAll(DomUtils.getRealSchemasForAugment(schema, augmentSchema));

        for (ChoiceCaseNode choiceCaseNode : schema.getCases()) {
            fromAllCases.addAll(DomUtils.getRealSchemasForAugment(choiceCaseNode, augmentSchema));
        }

        return fromAllCases;
    }

    @Override
    protected DataSchemaNode getSchemaForChild(org.opendaylight.yangtools.yang.model.api.ChoiceNode schema,
            QName childPartialQName) {
        return DomUtils.findSchemaForChild(schema, childPartialQName);
    }

    @Override
    protected LinkedListMultimap<QName, Element> mapChildElements(List<Element> xml) {
        return DomUtils.mapChildElements(xml);
    }

    @Override
    protected Map<QName, org.opendaylight.yangtools.yang.model.api.ChoiceNode> mapChildElementsFromChoices(
            org.opendaylight.yangtools.yang.model.api.ChoiceNode schema) {
        Map<QName, org.opendaylight.yangtools.yang.model.api.ChoiceNode> mappedChoices = Maps.newLinkedHashMap();

        for (ChoiceCaseNode choiceCaseNode : schema.getCases()) {
            mappedChoices.putAll(DomUtils.mapChildElementsFromChoices(choiceCaseNode));
        }

        return mappedChoices;
    }

    @Override
    protected Map<QName, AugmentationSchema> mapChildElementsFromAugments(
            org.opendaylight.yangtools.yang.model.api.ChoiceNode schema) {
        Map<QName, AugmentationSchema> mappedAugments = Maps.newLinkedHashMap();

//        mappedAugments.putAll(DomUtils.mapChildElementsFromAugments(schema));

        for (ChoiceCaseNode choiceCaseNode : schema.getCases()) {
            mappedAugments.putAll(DomUtils.mapChildElementsFromAugments(choiceCaseNode));
        }
        return mappedAugments;
    }

}
