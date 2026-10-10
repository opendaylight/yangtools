/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.context;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree.Step;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

final class ChoiceContext extends AbstractPathMixinContext {
    @NonNullByDefault
    private final Map<NodeIdentifier, Step.@Nullable InCase> byArg;
    @NonNullByDefault
    private final Map<QName, Step.@Nullable InCase> byQName;

    ChoiceContext(final ChoiceSchemaNode schema) {
        super(schema);

        @NonNullByDefault
        final var argList = new ArrayList<Map.Entry<NodeIdentifier, Step.InCase>>();
        @NonNullByDefault
        final var qnameList = new ArrayList<Map.Entry<QName, Step.InCase>>();
        for (var caze : schema.getCases()) {
            for (var cazeChild : caze.getChildNodes()) {
                final var child = AbstractContext.of(cazeChild);
                final var step = new Step.InCase(child, caze.asEffectiveStatement());

                // 1..1 NodeIdentifier -> Step mapping
                argList.add(entryOf(step, child.getPathStep()));

                // 0..N QName -> Step mappings
                final var qnames = child.qnameIdentifiers();
                final var toAdd = qnames.size();
                switch (toAdd) {
                    case 0 -> {
                        // no-op
                    }
                    case 1 -> qnameList.add(entryOf(step,
                        qnames instanceof SequencedCollection<QName> sc ? sc.getFirst() : qnames.iterator().next()));
                    default -> {
                        qnameList.ensureCapacity(qnameList.size() + toAdd);
                        for (var qname : qnames) {
                            qnameList.add(entryOf(step, qname));
                        }
                    }
                }
            }
        }

        byArg = mapOf(argList);
        byQName = mapOf(qnameList);
    }

    @NonNullByDefault
    @SuppressWarnings("null")
    private static <K> Map.Entry<K, Step.InCase> entryOf(final Step.InCase value, final K key) {
        return Map.entry(key, value);
    }

    @NonNullByDefault
    @SuppressWarnings("null")
    private static <K, V> Map<K, @Nullable V> mapOf(final ArrayList<Map.Entry<K, V>> list) {
        return switch (list.size()) {
            case 0 -> Map.of();
            case 1 -> mapOf1(list.getFirst());
            default -> mapOfN(list);
        };
    }

    private static <K, V> Map<K, @Nullable V> mapOf1(final Map.@NonNull Entry<K, V> entry) {
        return Map.of(entry.getKey(), entry.getValue());
    }

    @SuppressWarnings("unchecked")
    private static <K, V> Map<K, @Nullable V> mapOfN(final @NonNull ArrayList<Map.@NonNull Entry<K, V>> list) {
        return Map.ofEntries(list.toArray(Map.Entry[]::new));
    }

    @Override
    public AbstractContext childByArg(final PathArgument arg) {
        return child(byArg.get(arg));
    }

    @Override
    public AbstractContext childByQName(final QName child) {
        return child(byQName.get(child));
    }

    private static <K> @Nullable AbstractContext child(final Step.@Nullable InCase step) {
        return step == null ? null : (AbstractContext) step.child();
    }

    @Override
    public Step.InCase stepTo(final QName child) {
        return byQName.get(requireNonNull(child));
    }

    @Override
    Set<@NonNull QName> qnameIdentifiers() {
        return byQName.keySet();
    }

    @Override
    public AbstractContext enterChild(final SchemaInferenceStack stack, final PathArgument child) {
        return enterChild(stack, byArg.get(child));
    }

    @Override
    public AbstractContext enterChild(final SchemaInferenceStack stack, final QName child) {
        return enterChild(stack, byQName.get(child));
    }

    // split out to enforce order of argument checks
    private static <K> @Nullable AbstractContext enterChild(final SchemaInferenceStack stack,
            final Step.@Nullable InCase step) {
        requireNonNull(stack);
        if (step == null) {
            return null;
        }

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
