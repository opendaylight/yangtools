/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ChoiceNodeCodecContext extends DataContainerCodecContext<ChoiceNode> {
    private static final Logger LOG = LoggerFactory.getLogger(ChoiceNodeCodecContext.class);
    private final ImmutableMap<YangInstanceIdentifier.PathArgument, DataContainerCodecPrototype<?>> byYangCaseChild;
    private final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> byClass;
    private final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> byCaseChildClass;

    public ChoiceNodeCodecContext(final DataContainerCodecPrototype<ChoiceNode> prototype) {
        super(prototype);
        Map<YangInstanceIdentifier.PathArgument, DataContainerCodecPrototype<?>> byYangCaseChildBuilder = new HashMap<>();
        Map<Class<?>, DataContainerCodecPrototype<?>> byClassBuilder = new HashMap<>();
        Map<Class<?>, DataContainerCodecPrototype<?>> byCaseChildClassBuilder = new HashMap<>();
        Set<Class<?>> potentialSubstitutions = new HashSet<>();
        // Walks all cases for supplied choice in current runtime context
        for (Class<?> caze : factory().getRuntimeContext().getCases(bindingClass())) {
            // We try to load case using exact match thus name
            // and original schema must equals
            DataContainerCodecPrototype<ChoiceCaseNode> cazeDef = loadCase(caze);
            // If we have case definition, this case is instantiated
            // at current location and thus is valid in context of parent choice
            if (cazeDef != null) {
                byClassBuilder.put(cazeDef.getBindingClass(), cazeDef);
                // Updates collection of case children
                for (Class<? extends DataObject> cazeChild : BindingReflections.getChildrenClasses((Class) caze)) {
                    byCaseChildClassBuilder.put(cazeChild, cazeDef);
                }
                // Updates collection of YANG instance identifier to case
                for (DataSchemaNode cazeChild : cazeDef.getSchema().getChildNodes()) {
                    byYangCaseChildBuilder.put(new NodeIdentifier(cazeChild.getQName()), cazeDef);
                }
            } else {
                /*
                 * If case definition is not available, we store it for
                 * later check if it could be used as substitution of existing one.
                 */
                potentialSubstitutions.add(caze);
            }
        }

        Map<Class<?>, DataContainerCodecPrototype<?>> bySubstitutionBuilder = new HashMap<>();
        /*
         * Walks all cases which are not directly instantiated and
         * tries to match them to instantiated cases - represent same data as instantiated case,
         * only case name or schema path is different. This is required due property of
         * binding specification, that if choice is in grouping schema path location is lost,
         * and users may use incorrect case class using copy builders.
         */
        for(Class<?> substitution : potentialSubstitutions) {
            search: for(Entry<Class<?>, DataContainerCodecPrototype<?>> real : byClassBuilder.entrySet()) {
                if(BindingReflections.isSubstitutionFor(substitution, real.getKey())) {
                    bySubstitutionBuilder.put(substitution, real.getValue());
                    break search;
                }
            }
        }
        byClassBuilder.putAll(bySubstitutionBuilder);
        byYangCaseChild = ImmutableMap.copyOf(byYangCaseChildBuilder);
        byClass = ImmutableMap.copyOf(byClassBuilder);
        byCaseChildClass = ImmutableMap.copyOf(byCaseChildClassBuilder);
    }

    @Override
    protected DataContainerCodecContext<?> getStreamChild(final Class<?> childClass) {
        DataContainerCodecPrototype<?> child = byClass.get(childClass);
        Preconditions.checkArgument(child != null,"Supplied class is not valid case",childClass);
        return child.get();
    }

    @Override
    protected Optional<DataContainerCodecContext<?>> getPossibleStreamChild(final Class<?> childClass) {
        DataContainerCodecPrototype<?> child = byClass.get(childClass);
        if(child != null) {
            return Optional.<DataContainerCodecContext<?>>of(child.get());
        }
        return Optional.absent();
    }

    Iterable<Class<?>> getCaseChildrenClasses() {
        return byCaseChildClass.keySet();
    }

    protected DataContainerCodecPrototype<ChoiceCaseNode> loadCase(final Class<?> childClass) {
        Optional<ChoiceCaseNode> childSchema = factory().getRuntimeContext().getCaseSchemaDefinition(schema(), childClass);
        if (childSchema.isPresent()) {
            return DataContainerCodecPrototype.from(childClass, childSchema.get(), factory());
        }

        LOG.debug("Supplied class %s is not valid case in schema %s", childClass, schema());
        return null;
    }

    @Override
    protected NodeCodecContext getYangIdentifierChild(final YangInstanceIdentifier.PathArgument arg) {
        DataContainerCodecPrototype<?> cazeProto = byYangCaseChild.get(arg);
        Preconditions.checkArgument(cazeProto != null, "Argument %s is not valid child of %s", arg, schema());
        return cazeProto.get().getYangIdentifierChild(arg);
    }

    @Override
    protected Object dataFromNormalizedNode(final NormalizedNode<?, ?> data) {
        Preconditions
                .checkArgument(data instanceof org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode);
        NormalizedNodeContainer<?, ?, NormalizedNode<?,?>> casted = (NormalizedNodeContainer<?, ?, NormalizedNode<?,?>>) data;
        NormalizedNode<?, ?> first = Iterables.getFirst(casted.getValue(), null);

        if (first == null) {
            return null;
        }
        DataContainerCodecPrototype<?> caze = byYangCaseChild.get(first.getIdentifier());
        return caze.get().dataFromNormalizedNode(data);
    }

    public DataContainerCodecContext<?> getCazeByChildClass(final Class<? extends DataObject> type) {
        return byCaseChildClass.get(type).get();
    }

}