/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.context;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree.Step;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

final class ChoiceContext extends AbstractPathMixinContext {
    private final ImmutableMap<NodeIdentifier, Step.InCase> byArg;
    private final ImmutableMap<QName, Step.InCase> byQName;

    ChoiceContext(final ChoiceSchemaNode schema) {
        super(schema);
        final var byQNameBuilder = ImmutableMap.<QName, Step.InCase>builder();
        final var byArgBuilder = ImmutableMap.<NodeIdentifier, Step.InCase>builder();

        for (var caze : schema.getCases()) {
            for (var cazeChild : caze.getChildNodes()) {
                final var childContext = AbstractContext.of(cazeChild);
                final var step = new Step.InCase(childContext, caze.asEffectiveStatement());

                byArgBuilder.put(childContext.getPathStep(), step);
                for (var qname : childContext.qnameIdentifiers()) {
                    byQNameBuilder.put(qname, step);
                }
            }
        }

        byQName = byQNameBuilder.build();
        byArg = byArgBuilder.build();
    }

    @Override
    public AbstractContext childByArg(final PathArgument arg) {
        return childIn(byArg, arg);
    }

    @Override
    public AbstractContext childByQName(final QName child) {
        return childIn(byQName, child);
    }

    private static <K> @Nullable AbstractContext childIn(final ImmutableMap<? extends K, Step.InCase> map,
            final K key) {
        final var step = map.get(requireNonNull(key));
        return step == null ? null : (AbstractContext) step.child();
    }

    @Override
    public Step.InCase stepTo(final QName child) {
        return byQName.get(requireNonNull(child));
    }

    @Override
    ImmutableSet<QName> qnameIdentifiers() {
        return byQName.keySet();
    }

    @Override
    public AbstractContext enterChild(final SchemaInferenceStack stack, final PathArgument child) {
        return enterChild(stack, byArg, child);
    }

    @Override
    public AbstractContext enterChild(final SchemaInferenceStack stack, final QName child) {
        return enterChild(stack, byQName, child);
    }

    // split out to enforce order of argument checks
    private static <K> @Nullable AbstractContext enterChild(final SchemaInferenceStack stack,
            final ImmutableMap<? extends K, Step.InCase> map, final K key) {
        requireNonNull(stack);
        final var step = map.get(requireNonNull(key));
        return step == null ? null : enterChild(stack, step);
    }

    // split out happy path
    @NonNullByDefault
    private static AbstractContext enterChild(final SchemaInferenceStack stack, final Step.InCase step) {
        final var child = (AbstractContext) step.child();
        stack.enterSchemaTree(step.inCase().argument());
        child.pushToStack(stack);
        return child;
    }

    @Override
    void pushToStack(final SchemaInferenceStack stack) {
        stack.enterChoice(dataSchemaNode.getQName());
    }
}
