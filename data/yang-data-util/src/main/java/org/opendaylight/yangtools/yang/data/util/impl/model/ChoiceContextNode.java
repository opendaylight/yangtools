/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.impl.model;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

final class ChoiceContextNode extends AbstractMixinContextNode {
    private final ImmutableMap<NodeIdentifier, AbstractDataSchemaContextNode> byArg;
    private final ImmutableMap<QName, AbstractDataSchemaContextNode> byQName;
    private final ImmutableMap<DataSchemaContextNode, QName> childToCase;

    ChoiceContextNode(final ChoiceSchemaNode schema) {
        super(schema);
        final var childToCaseBuilder = ImmutableMap.<DataSchemaContextNode, QName>builder();
        final var byQNameBuilder = ImmutableMap.<QName, AbstractDataSchemaContextNode>builder();
        final var byArgBuilder = ImmutableMap.<NodeIdentifier, AbstractDataSchemaContextNode>builder();

        for (var caze : schema.getCases()) {
            for (var cazeChild : caze.getChildNodes()) {
                final var childOp = AbstractDataSchemaContextNode.of(cazeChild);
                byArgBuilder.put(childOp.getPathStep(), childOp);
                childToCaseBuilder.put(childOp, caze.getQName());
                for (QName qname : childOp.qnameIdentifiers()) {
                    byQNameBuilder.put(qname, childOp);
                }
            }
        }

        childToCase = childToCaseBuilder.build();
        byQName = byQNameBuilder.build();
        byArg = byArgBuilder.build();
    }

    @Override
    public AbstractDataSchemaContextNode childByArg(final PathArgument arg) {
        return byArg.get(requireNonNull(arg));
    }

    @Override
    public AbstractDataSchemaContextNode childByQName(final QName child) {
        return byQName.get(requireNonNull(child));
    }

    @Override
    Set<QName> qnameIdentifiers() {
        return byQName.keySet();
    }

    @Override
    public DataSchemaContextNode enterChild(final SchemaInferenceStack stack, final QName qname) {
        return pushToStack(stack, childByQName(qname));
    }

    @Override
    public DataSchemaContextNode enterChild(final SchemaInferenceStack stack, final PathArgument arg) {
        return pushToStack(stack, childByArg(arg));
    }

    @Override
    void pushToStack(final @NonNull SchemaInferenceStack stack) {
        stack.enterChoice(dataSchemaNode.getQName());
    }

    private @Nullable DataSchemaContextNode pushToStack(final SchemaInferenceStack stack,
            final @Nullable AbstractDataSchemaContextNode child) {
        requireNonNull(stack);
        if (child != null) {
            final var caseName = verifyNotNull(childToCase.get(child), "No case statement for %s in %s", child, this);
            stack.enterSchemaTree(caseName);
            child.pushToStack(stack);
        }
        return child;
    }
}