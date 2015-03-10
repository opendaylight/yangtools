/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.operations;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.SchemaUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;

final class ChoiceNodeModification extends AbstractContainerNodeModification<ChoiceSchemaNode, ChoiceNode> {

    @Override
    protected QName getQName(final ChoiceSchemaNode schema) {
        return schema.getQName();
    }

    @Override
    protected Object findSchemaForChild(final ChoiceSchemaNode schema, final QName nodeType) {
        return SchemaUtils.findSchemaForChild(schema, nodeType);
    }

    @Override
    protected Set<YangInstanceIdentifier.PathArgument> getChildrenToProcess(final ChoiceSchemaNode schema,
            final Optional<ChoiceNode> actual,
            final Optional<ChoiceNode> modification)
            throws DataModificationException {
        Set<YangInstanceIdentifier.PathArgument> childrenToProcess = super.getChildrenToProcess(schema, actual,
                modification);

        if (!modification.isPresent()) {
            return childrenToProcess;
        }

        // Detect case node from modification
        ChoiceCaseNode detectedCase = null;
        for (DataContainerChild<? extends YangInstanceIdentifier.PathArgument, ?> child : modification.get().getValue()) {
            Optional<ChoiceCaseNode> detectedCaseForChild = SchemaUtils.detectCase(schema, child);

            if(!detectedCaseForChild.isPresent()) {
                DataModificationException.IllegalChoiceValuesException.throwUnknownChild(schema.getQName(),
                        child.getNodeType());
            }

            if (detectedCase != null && (!detectedCase.equals(detectedCaseForChild.get()))) {
                DataModificationException.IllegalChoiceValuesException.throwMultipleCasesReferenced(schema.getQName(),
                        modification.get(), detectedCase.getQName(), detectedCaseForChild.get().getQName());
            }
            detectedCase = detectedCaseForChild.get();
        }

        if (detectedCase == null) {
            return childrenToProcess;
        }

        // Filter out child nodes that do not belong to detected case =
        // Nodes from other cases present in actual
        Set<YangInstanceIdentifier.PathArgument> childrenToProcessFiltered = Sets.newLinkedHashSet();
        for (YangInstanceIdentifier.PathArgument childToProcess : childrenToProcess) {
            // child from other cases, skip
            if (childToProcess instanceof YangInstanceIdentifier.AugmentationIdentifier
                    && (!SchemaUtils.belongsToCaseAugment(detectedCase,
                            (YangInstanceIdentifier.AugmentationIdentifier) childToProcess))) {
                continue;
            } else if (!belongsToCase(detectedCase, childToProcess)) {
                continue;
            }

            childrenToProcessFiltered.add(childToProcess);
        }

        return childrenToProcessFiltered;
    }

    private boolean belongsToCase(final ChoiceCaseNode detectedCase, final YangInstanceIdentifier.PathArgument childToProcess) {
        return detectedCase.getDataChildByName(childToProcess.getNodeType()) != null;
    }

    @Override
    protected Object findSchemaForAugment(final ChoiceSchemaNode schema, final YangInstanceIdentifier.AugmentationIdentifier childToProcessId) {
        return SchemaUtils.findSchemaForAugment(schema, childToProcessId.getPossibleChildNames());
    }

    @Override
    protected DataContainerNodeBuilder<?, ChoiceNode> getBuilder(
            final ChoiceSchemaNode schema) {
        return Builders.choiceBuilder(schema);
    }
}
