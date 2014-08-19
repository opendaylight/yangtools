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
import java.util.Map;

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

        for (Class<?> caze : factory().getRuntimeContext().getCases(bindingClass())) {
            DataContainerCodecPrototype<ChoiceCaseNode> cazeDef = loadCase(caze);
            if (cazeDef != null) {
                byClassBuilder.put(cazeDef.getBindingClass(), cazeDef);
                for (Class<? extends DataObject> cazeChild : BindingReflections.getChildrenClasses((Class) caze)) {
                    byCaseChildClassBuilder.put(cazeChild, cazeDef);
                }
                for (DataSchemaNode cazeChild : cazeDef.getSchema().getChildNodes()) {
                    byYangCaseChildBuilder.put(new NodeIdentifier(cazeChild.getQName()), cazeDef);
                }
            }
        }

        byYangCaseChild = ImmutableMap.copyOf(byYangCaseChildBuilder);
        byClass = ImmutableMap.copyOf(byClassBuilder);
        byCaseChildClass = ImmutableMap.copyOf(byCaseChildClassBuilder);
    }

    @Override
    protected DataContainerCodecContext<?> getStreamChild(final Class<?> childClass) {
        return byClass.get(childClass).get();
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