/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

class ChoiceNodeCodecContext extends DataContainerCodecContext<ChoiceNode> {

    private final YangInstanceIdentifier.PathArgument yangArgument;
    private final ImmutableMap<QName, ChoiceCaseNode> caseChildToCase;

    ChoiceNodeCodecContext(final Class<?> cls, final ChoiceNode nodeSchema, final CodecContextFactory context) {
        super(cls, nodeSchema.getQName().getModule(), nodeSchema, context);
        Map<QName, ChoiceCaseNode> childToCase = new HashMap<>();
        yangArgument = new YangInstanceIdentifier.NodeIdentifier(nodeSchema.getQName());
        for (ChoiceCaseNode caseNode : nodeSchema.getCases()) {
            for (DataSchemaNode caseChild : caseNode.getChildNodes()) {
                childToCase.put(caseChild.getQName(), caseNode);
            }
        }
        caseChildToCase = ImmutableMap.copyOf(childToCase);
    }

    @Override
    protected YangInstanceIdentifier.PathArgument getDomPathArgument() {
        return yangArgument;
    }

    @Override
    protected DataContainerCodecContext<?> loadChild(final Class<?> childClass) {

        ChoiceCaseNode childSchema = factory.getRuntimeContext().getCaseSchemaDefinition(schema, childClass);
        return new CaseNodeCodecContext(childClass, childSchema, factory);
    }

    @Override
    protected NodeCodecContext getYangIdentifierChild(final YangInstanceIdentifier.PathArgument arg) {

        QName childQName = arg.getNodeType();
        ChoiceCaseNode caze = caseChildToCase.get(childQName);
        Preconditions.checkArgument(caze != null, "Argument %s is not valid child of %s", arg, schema);
        ;
        Class<?> cazeClass = factory.getRuntimeContext().getClassForSchema(caze);
        return getStreamChild(cazeClass).getYangIdentifierChild(arg);
    }

}