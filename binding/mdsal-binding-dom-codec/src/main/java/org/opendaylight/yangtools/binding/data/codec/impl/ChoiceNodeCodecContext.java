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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.impl.schema.SchemaUtils;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ChoiceNodeCodecContext<D extends DataObject> extends DataContainerCodecContext<D,ChoiceSchemaNode> {
    private static final Logger LOG = LoggerFactory.getLogger(ChoiceNodeCodecContext.class);
    private final ImmutableMap<YangInstanceIdentifier.PathArgument, DataContainerCodecPrototype<?>> byYangCaseChild;
    private final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> byClass;
    private final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> byCaseChildClass;

    public ChoiceNodeCodecContext(final DataContainerCodecPrototype<ChoiceSchemaNode> prototype) {
        super(prototype);
        final Map<YangInstanceIdentifier.PathArgument, DataContainerCodecPrototype<?>> byYangCaseChildBuilder = new HashMap<>();
        final Map<Class<?>, DataContainerCodecPrototype<?>> byClassBuilder = new HashMap<>();
        final Map<Class<?>, DataContainerCodecPrototype<?>> byCaseChildClassBuilder = new HashMap<>();
        final Set<Class<?>> potentialSubstitutions = new HashSet<>();
        // Walks all cases for supplied choice in current runtime context
        for (final Class<?> caze : factory().getRuntimeContext().getCases(getBindingClass())) {
            // We try to load case using exact match thus name
            // and original schema must equals
            final DataContainerCodecPrototype<ChoiceCaseNode> cazeDef = loadCase(caze);
            // If we have case definition, this case is instantiated
            // at current location and thus is valid in context of parent choice
            if (cazeDef != null) {
                byClassBuilder.put(cazeDef.getBindingClass(), cazeDef);
                // Updates collection of case children
                @SuppressWarnings("unchecked")
                final Class<? extends DataObject> cazeCls = (Class<? extends DataObject>) caze;
                for (final Class<? extends DataObject> cazeChild : BindingReflections.getChildrenClasses(cazeCls)) {
                    byCaseChildClassBuilder.put(cazeChild, cazeDef);
                }
                // Updates collection of YANG instance identifier to case
                for (final DataSchemaNode cazeChild : cazeDef.getSchema().getChildNodes()) {
                    if (cazeChild.isAugmenting()) {
                        final AugmentationSchema augment = SchemaUtils.findCorrespondingAugment(cazeDef.getSchema(), cazeChild);
                        if (augment != null) {
                            byYangCaseChildBuilder.put(SchemaUtils.getNodeIdentifierForAugmentation(augment), cazeDef);
                            continue;
                        }
                    }
                    byYangCaseChildBuilder.put(NodeIdentifier.create(cazeChild.getQName()), cazeDef);
                }
            } else {
                /*
                 * If case definition is not available, we store it for
                 * later check if it could be used as substitution of existing one.
                 */
                potentialSubstitutions.add(caze);
            }
        }

        final Map<Class<?>, DataContainerCodecPrototype<?>> bySubstitutionBuilder = new HashMap<>();
        /*
         * Walks all cases which are not directly instantiated and
         * tries to match them to instantiated cases - represent same data as instantiated case,
         * only case name or schema path is different. This is required due property of
         * binding specification, that if choice is in grouping schema path location is lost,
         * and users may use incorrect case class using copy builders.
         */
        for(final Class<?> substitution : potentialSubstitutions) {
            search: for(final Entry<Class<?>, DataContainerCodecPrototype<?>> real : byClassBuilder.entrySet()) {
                if (BindingReflections.isSubstitutionFor(substitution, real.getKey())) {
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

    @SuppressWarnings("unchecked")
    @Override
    public <DV extends DataObject> DataContainerCodecContext<DV, ?> streamChild(final Class<DV> childClass) {
        final DataContainerCodecPrototype<?> child = byClass.get(childClass);
        return (DataContainerCodecContext<DV, ?>) childNonNull(child, childClass, "Supplied class %s is not valid case", childClass).get();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <DV extends DataObject> Optional<DataContainerCodecContext<DV, ?>> possibleStreamChild(
            final Class<DV> childClass) {
        final DataContainerCodecPrototype<?> child = byClass.get(childClass);
        if (child != null) {
            return Optional.<DataContainerCodecContext<DV,?>>of((DataContainerCodecContext<DV, ?>) child.get());
        }
        return Optional.absent();
    }

    Iterable<Class<?>> getCaseChildrenClasses() {
        return byCaseChildClass.keySet();
    }

    protected DataContainerCodecPrototype<ChoiceCaseNode> loadCase(final Class<?> childClass) {
        final Optional<ChoiceCaseNode> childSchema = factory().getRuntimeContext().getCaseSchemaDefinition(getSchema(), childClass);
        if (childSchema.isPresent()) {
            return DataContainerCodecPrototype.from(childClass, childSchema.get(), factory());
        }

        LOG.debug("Supplied class %s is not valid case in schema %s", childClass, getSchema());
        return null;
    }

    @Override
    public NodeCodecContext<?> yangPathArgumentChild(final YangInstanceIdentifier.PathArgument arg) {
        final DataContainerCodecPrototype<?> cazeProto;
        if (arg instanceof YangInstanceIdentifier.NodeIdentifierWithPredicates) {
            cazeProto = byYangCaseChild.get(new NodeIdentifier(arg.getNodeType()));
        } else {
            cazeProto = byYangCaseChild.get(arg);
        }

        return childNonNull(cazeProto, arg,"Argument %s is not valid child of %s", arg, getSchema()).get()
                .yangPathArgumentChild(arg);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public D deserialize(final NormalizedNode<?, ?> data) {
        Preconditions.checkArgument(data instanceof ChoiceNode);
        final NormalizedNodeContainer<?, ?, NormalizedNode<?,?>> casted = (NormalizedNodeContainer<?, ?, NormalizedNode<?,?>>) data;
        final NormalizedNode<?, ?> first = Iterables.getFirst(casted.getValue(), null);

        if (first == null) {
            return null;
        }
        final DataContainerCodecPrototype<?> caze = byYangCaseChild.get(first.getIdentifier());
        return (D) caze.get().deserialize(data);
    }

    DataContainerCodecContext<?, ?> getCazeByChildClass(final @Nonnull Class<? extends DataObject> type) {
        final DataContainerCodecPrototype<?> protoCtx =
                childNonNull(byCaseChildClass.get(type), type, "Class %s is not child of any cases for %s", type,
                        bindingArg());
        return protoCtx.get();
    }

    @Override
    protected Object deserializeObject(final NormalizedNode<?, ?> normalizedNode) {
        return deserialize(normalizedNode);
    }

    @Override
    public PathArgument deserializePathArgument(final YangInstanceIdentifier.PathArgument arg) {
        Preconditions.checkArgument(getDomPathArgument().equals(arg));
        return null;
    }

    @Override
    public YangInstanceIdentifier.PathArgument serializePathArgument(final PathArgument arg) {
        // FIXME: check for null, since binding container is null.
        return getDomPathArgument();
    }
}
