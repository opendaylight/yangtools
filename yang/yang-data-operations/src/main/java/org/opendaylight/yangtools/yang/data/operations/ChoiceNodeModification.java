/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.operations;

import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.SchemaUtils;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

class ChoiceNodeModification extends
        AbstractContainerNodeModification<ChoiceNode, org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode> {

    @Override
    protected QName getQName(ChoiceNode schema) {
        return schema.getQName();
    }

    @Override
    protected Object findSchemaForChild(ChoiceNode schema, QName nodeType) {
        return SchemaUtils.findSchemaForChild(schema, nodeType);
    }

    @Override
    protected Set<InstanceIdentifier.PathArgument> getChildrenToProcess(ChoiceNode schema,
            Optional<org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode> actual,
            Optional<org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode> modification)
            throws DataModificationException {
        Set<InstanceIdentifier.PathArgument> childrenToProcess = super.getChildrenToProcess(schema, actual,
                modification);

        if (modification.isPresent() == false) {
            return childrenToProcess;
        }

        // Detect case node from modification
        ChoiceCaseNode detectedCase = null;
        for (DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> child : modification.get().getValue()) {
            Optional<ChoiceCaseNode> detectedCaseForChild = SchemaUtils.detectCase(schema, child);

            if(detectedCaseForChild.isPresent() == false) {
                DataModificationException.IllegalChoiceValuesException.throwUnknownChild(schema.getQName(),
                        child.getNodeType());
            }

            if (detectedCase != null && detectedCase.equals(detectedCaseForChild.get()) == false) {
                DataModificationException.IllegalChoiceValuesException.throwMultipleCasesReferenced(schema.getQName(),
                        modification.get(), detectedCase.getQName(), detectedCaseForChild.get().getQName());
            }
            detectedCase = detectedCaseForChild.get();
        }

        if (detectedCase == null)
            return childrenToProcess;

        // Filter out child nodes that do not belong to detected case =
        // Nodes from other cases present in actual
        Set<InstanceIdentifier.PathArgument> childrenToProcessFiltered = Sets.newHashSet();
        for (InstanceIdentifier.PathArgument childToProcess : childrenToProcess) {
            // child from other cases, skip
            if (childToProcess instanceof AugmentationNode
                    && SchemaUtils.belongsToCaseAugment(detectedCase,
                            (InstanceIdentifier.AugmentationIdentifier) childToProcess) == false) {
                continue;
            } else if (belongsToCase(detectedCase, childToProcess) == false) {
                continue;
            }

            childrenToProcessFiltered.add(childToProcess);
        }

        return childrenToProcessFiltered;
    }

    private boolean belongsToCase(ChoiceCaseNode detectedCase, InstanceIdentifier.PathArgument childToProcess) {
        return detectedCase.getDataChildByName(childToProcess.getNodeType()) != null;
    }

    @Override
    protected Object findSchemaForAugment(ChoiceNode schema, InstanceIdentifier.AugmentationIdentifier childToProcessId) {
        return SchemaUtils.findSchemaForAugment(schema, childToProcessId.getPossibleChildNames());
    }

    @Override
    protected DataContainerNodeBuilder<?, org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode> getBuilder(
            ChoiceNode schema) {
        return Builders.choiceBuilder(schema);
    }
}
