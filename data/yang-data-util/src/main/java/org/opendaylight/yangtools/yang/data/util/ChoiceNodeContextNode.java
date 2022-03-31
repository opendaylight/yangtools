/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.collect.ImmutableMap;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

final class ChoiceNodeContextNode extends AbstractMixinContextNode<NodeIdentifier> {
    private final ImmutableMap<PathArgument, DataSchemaContextNode<?>> byArg;
    private final ImmutableMap<QName, DataSchemaContextNode<?>> byQName;
    private final ImmutableMap<DataSchemaContextNode<?>, QName> childToCase;

    ChoiceNodeContextNode(final ChoiceSchemaNode schema) {
        super(NodeIdentifier.create(schema.getQName()), schema);
        ImmutableMap.Builder<DataSchemaContextNode<?>, QName> childToCaseBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<QName, DataSchemaContextNode<?>> byQNameBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<PathArgument, DataSchemaContextNode<?>> byArgBuilder = ImmutableMap.builder();

        for (CaseSchemaNode caze : schema.getCases()) {
            for (DataSchemaNode cazeChild : caze.getChildNodes()) {
                final DataSchemaContextNode<?> childOp = cazeChild.isAugmenting()
                    ? DataSchemaContextNode.ofAugmenting(caze, caze, cazeChild)
                        : DataSchemaContextNode.of(cazeChild);
                byArgBuilder.put(childOp.getIdentifier(), childOp);
                childToCaseBuilder.put(childOp, caze.getQName());
                for (QName qname : childOp.getQNameIdentifiers()) {
                    byQNameBuilder.put(qname, childOp);
                }
            }
        }

        childToCase = childToCaseBuilder.build();
        byQName = byQNameBuilder.build();
        byArg = byArgBuilder.build();
    }

    @Override
    public DataSchemaContextNode<?> getChild(final PathArgument child) {
        return byArg.get(child);
    }

    @Override
    public DataSchemaContextNode<?> getChild(final QName child) {
        return byQName.get(child);
    }

    @Override
    protected Set<QName> getQNameIdentifiers() {
        return byQName.keySet();
    }

    @Override
    protected DataSchemaContextNode<?> enterChild(final QName child, final SchemaInferenceStack stack) {
        return pushToStack(getChild(child), stack);
    }

    @Override
    protected DataSchemaContextNode<?> enterChild(final PathArgument child, final SchemaInferenceStack stack) {
        return pushToStack(getChild(child), stack);
    }

    @Override
    protected void pushToStack(final @NonNull SchemaInferenceStack stack) {
        stack.enterChoice(getIdentifier().getNodeType());
    }

    private @Nullable DataSchemaContextNode<?> pushToStack(final @Nullable DataSchemaContextNode<?> child,
            final @NonNull SchemaInferenceStack stack) {
        if (child != null) {
            final var caseName = verifyNotNull(childToCase.get(child), "No case statement for %s in %s", child, this);
            stack.enterSchemaTree(caseName);
            child.pushToStack(stack);
        }
        return child;
    }
}