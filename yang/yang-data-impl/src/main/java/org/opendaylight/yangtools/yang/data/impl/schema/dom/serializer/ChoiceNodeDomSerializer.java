/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.dom.serializer;

import com.google.common.collect.Sets;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.impl.schema.dom.DomUtils;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

import java.util.Set;

public class ChoiceNodeDomSerializer
        extends
        AbstractDispatcherSerializer<InstanceIdentifier.NodeIdentifier, ChoiceNode, org.opendaylight.yangtools.yang.model.api.ChoiceNode> {

    @Override
    protected DataSchemaNode getSchemaForChild(org.opendaylight.yangtools.yang.model.api.ChoiceNode schema,
            DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> choiceChild) {
        return DomUtils.findSchemaForChild(schema, choiceChild.getNodeType());
    }

    @Override
    protected AugmentationSchema getAugmentedCase(org.opendaylight.yangtools.yang.model.api.ChoiceNode schema,
            AugmentationNode choiceChild) {
        return DomUtils.findSchemaForAugment(schema, choiceChild.getIdentifier().getPossibleChildNames());
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
}
