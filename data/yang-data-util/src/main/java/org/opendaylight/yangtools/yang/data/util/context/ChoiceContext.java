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
    private final Map<NodeIdentifier, Step.ThroughCase> byArg;
    @NonNullByDefault
    private final Map<QName, Step.ThroughCase> byQName;

    ChoiceContext(final ChoiceSchemaNode schema) {
        super(schema);

        @NonNullByDefault
        final var argList = new ArrayList<Map.Entry<NodeIdentifier, Step.ThroughCase>>();
        @NonNullByDefault
        final var qnameList = new ArrayList<Map.Entry<QName, Step.ThroughCase>>();
        for (var caze : schema.getCases()) {
            for (var cazeChild : caze.getChildNodes()) {
                final var child = AbstractContext.of(cazeChild);
                final var step = new Step.ThroughCase(child, caze.asEffectiveStatement());

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
    private static <K> Map.Entry<K, Step.ThroughCase> entryOf(final Step.ThroughCase value, final K key) {
        return Map.entry(key, value);
    }

    @NonNullByDefault
    private static <K, V> Map<K, V> mapOf(final ArrayList<Map.Entry<K, V>> list) {
        return switch (list.size()) {
            case 0 -> Map.of();
            case 1 -> {
                final var entry = list.getFirst();
                yield Map.of(entry.getKey(), entry.getValue());
            }
            default -> mapOfN(list);
        };
    }

    @NonNullByDefault
    @SuppressWarnings("unchecked")
    private static <K, V> Map<K, V> mapOfN(final ArrayList<Map.Entry<K, V>> list) {
        return Map.ofEntries(list.toArray(Map.Entry[]::new));
    }

    @Override
    public AbstractContext childByArg(final PathArgument arg) {
        return childFrom(byArg.get(arg));
    }

    @Override
    public AbstractContext childByQName(final QName child) {
        return childFrom(byQName.get(child));
    }

    private static <K> @Nullable AbstractContext childFrom(final Step.@Nullable ThroughCase step) {
        return step == null ? null : (AbstractContext) step.child();
    }

    @Override
    public Step.ThroughCase stepTowards(final QName child) {
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

    private static <K> @Nullable AbstractContext enterChild(final SchemaInferenceStack stack,
            final Step.@Nullable ThroughCase step) {
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
