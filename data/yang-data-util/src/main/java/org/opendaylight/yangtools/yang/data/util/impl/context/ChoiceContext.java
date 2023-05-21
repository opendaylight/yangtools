/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.impl.context;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

final class ChoiceContext extends AbstractPathMixinContext {
    private final ImmutableMap<NodeIdentifier, AbstractContext> byArg;
    private final ImmutableMap<QName, AbstractContext> byQName;
    private final ImmutableMap<AbstractContext, QName> childToCase;

    ChoiceContext(final ChoiceSchemaNode schema) {
        super(schema);
        final var childToCaseBuilder = ImmutableMap.<AbstractContext, QName>builder();
        final var byQNameBuilder = ImmutableMap.<QName, AbstractContext>builder();
        final var byArgBuilder = ImmutableMap.<NodeIdentifier, AbstractContext>builder();

        for (var caze : schema.getCases()) {
            for (var cazeChild : caze.getChildNodes()) {
                final var childOp = AbstractContext.of(cazeChild);
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
    public AbstractContext childByArg(final PathArgument arg) {
        return byArg.get(requireNonNull(arg));
    }

    @Override
    public AbstractContext childByQName(final QName child) {
        return byQName.get(requireNonNull(child));
    }

    @Override
    ImmutableSet<QName> qnameIdentifiers() {
        return byQName.keySet();
    }

    @Override
    public AbstractContext enterChild(final SchemaInferenceStack stack, final QName qname) {
        return pushToStack(stack, childByQName(qname));
    }

    @Override
    public AbstractContext enterChild(final SchemaInferenceStack stack, final PathArgument arg) {
        return pushToStack(stack, childByArg(arg));
    }

    @Override
    void pushToStack(final SchemaInferenceStack stack) {
        stack.enterChoice(dataSchemaNode.getQName());
    }

    private AbstractContext pushToStack(final SchemaInferenceStack stack, final AbstractContext child) {
        requireNonNull(stack);
        if (child != null) {
            final var caseName = verifyNotNull(childToCase.get(child), "No case statement for %s in %s", child, this);
            stack.enterSchemaTree(caseName);
            child.pushToStack(stack);
        }
        return child;
    }
}