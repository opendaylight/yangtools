/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.MultimapBuilder.SetMultimapBuilder;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ChoiceNodeCodecContext<D extends DataObject> extends DataContainerCodecContext<D, ChoiceSchemaNode> {
    private static final Logger LOG = LoggerFactory.getLogger(ChoiceNodeCodecContext.class);
    private final ImmutableMap<YangInstanceIdentifier.PathArgument, DataContainerCodecPrototype<?>> byYangCaseChild;

    /*
     * This is a bit tricky. DataObject addressing does not take into account choice/case statements, and hence
     * given:
     *
     * container foo {
     *     choice bar {
     *         leaf baz;
     *     }
     * }
     *
     * we will see {@code Baz extends ChildOf<Foo>}, which is how the users would address it in InstanceIdentifier
     * terms. The implicit assumption being made is that {@code Baz} identifies a particular instantiation and hence
     * provides unambiguous reference to an effective schema statement.
     *
     * <p>
     * Unfortunately this does not quite work with groupings, as their generation has changed: we do not have
     * interfaces that would capture grouping instantiations, hence we do not have a proper addressing point and
     * users need to specify the interfaces generated in the grouping's definition. These can be very much
     * ambiguous, as a {@code grouping} can be used in multiple modules independently within an {@code augment}
     * targeting {@code choice}, as each instantiation is guaranteed to have a unique namespace -- but we do not
     * have the appropriate instantiations of those nodes.
     *
     * <p>
     * To address this issue we have a two-class lookup mechanism, which relies on the interface generated for
     * the {@code case} statement to act as the namespace anchor bridging the nodes inside the grouping to the
     * namespace in which they are instantiated.
     *
     * <p>
     * Furthermore downstream code relies on historical mechanics, which would guess what the instantiation is,
     * silently assuming the ambiguity is theoretical and does not occur in practice.
     *
     * <p>
     * This leads to three classes of addressing, in order descending performance requirements.
     * <ul>
     *   <li>Direct DataObject, where we name an exact child</li>
     *   <li>Case DataObject + Grouping DataObject</li>
     *   <li>Grouping DataObject, which is ambiguous</li>
     * </ul>
     *
     * {@code byCaseChildClass} supports direct DataObject mapping and contains only unambiguous children, while
     * {@code byClass} supports indirect mapping and contains {@code case} sub-statements.
     *
     * ambiguousByCaseChildClass contains ambiguous mappings, for which we end up issuing warnings. We track each
     * ambiguous reference and issue warnings when they are encountered.
     */
    private final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> byClass;
    private final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> byCaseChildClass;
    private final ImmutableListMultimap<Class<?>, DataContainerCodecPrototype<?>> ambiguousByCaseChildClass;
    private final Set<Class<?>> ambiguousByCaseChildWarnings;

    ChoiceNodeCodecContext(final DataContainerCodecPrototype<ChoiceSchemaNode> prototype) {
        super(prototype);
        final Map<YangInstanceIdentifier.PathArgument, DataContainerCodecPrototype<?>> byYangCaseChildBuilder =
                new HashMap<>();
        final Map<Class<?>, DataContainerCodecPrototype<?>> byClassBuilder = new HashMap<>();
        final SetMultimap<Class<?>, DataContainerCodecPrototype<?>> childToCase = SetMultimapBuilder.hashKeys()
                .hashSetValues().build();
        final Set<Class<?>> potentialSubstitutions = new HashSet<>();
        // Walks all cases for supplied choice in current runtime context
        for (final Class<?> caze : factory().getRuntimeContext().getCases(getBindingClass())) {
            // We try to load case using exact match thus name
            // and original schema must equals
            final DataContainerCodecPrototype<CaseSchemaNode> cazeDef = loadCase(caze);
            // If we have case definition, this case is instantiated
            // at current location and thus is valid in context of parent choice
            if (cazeDef != null) {
                byClassBuilder.put(cazeDef.getBindingClass(), cazeDef);
                // Updates collection of case children
                @SuppressWarnings("unchecked")
                final Class<? extends DataObject> cazeCls = (Class<? extends DataObject>) caze;
                for (final Class<? extends DataObject> cazeChild : BindingReflections.getChildrenClasses(cazeCls)) {
                    childToCase.put(cazeChild, cazeDef);
                }
                // Updates collection of YANG instance identifier to case
                for (final DataSchemaNode cazeChild : cazeDef.getSchema().getChildNodes()) {
                    if (cazeChild.isAugmenting()) {
                        final AugmentationSchemaNode augment = SchemaUtils.findCorrespondingAugment(cazeDef.getSchema(),
                            cazeChild);
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
        byYangCaseChild = ImmutableMap.copyOf(byYangCaseChildBuilder);

        // Move unambiguous child->case mappings to byCaseChildClass, removing them from childToCase
        final ImmutableListMultimap.Builder<Class<?>, DataContainerCodecPrototype<?>> ambiguousByCaseBuilder =
                ImmutableListMultimap.builder();
        final Builder<Class<?>, DataContainerCodecPrototype<?>> unambiguousByCaseBuilder = ImmutableMap.builder();
        for (Entry<Class<?>, Set<DataContainerCodecPrototype<?>>> e : Multimaps.asMap(childToCase).entrySet()) {
            final Set<DataContainerCodecPrototype<?>> cases = e.getValue();
            if (cases.size() != 1) {
                // Sort all possibilities by their FQCN to retain semi-predictable results
                final List<DataContainerCodecPrototype<?>> list = new ArrayList<>(e.getValue());
                list.sort(Comparator.comparing(proto -> proto.getBindingClass().getCanonicalName()));
                ambiguousByCaseBuilder.putAll(e.getKey(), list);
            } else {
                unambiguousByCaseBuilder.put(e.getKey(), cases.iterator().next());
            }
        }
        byCaseChildClass = unambiguousByCaseBuilder.build();

        // Setup ambiguous tracking, if needed
        ambiguousByCaseChildClass = ambiguousByCaseBuilder.build();
        ambiguousByCaseChildWarnings = ambiguousByCaseChildClass.isEmpty() ? ImmutableSet.of()
                : ConcurrentHashMap.newKeySet();

        final Map<Class<?>, DataContainerCodecPrototype<?>> bySubstitutionBuilder = new HashMap<>();
        /*
         * Walks all cases which are not directly instantiated and
         * tries to match them to instantiated cases - represent same data as instantiated case,
         * only case name or schema path is different. This is required due property of
         * binding specification, that if choice is in grouping schema path location is lost,
         * and users may use incorrect case class using copy builders.
         */
        for (final Class<?> substitution : potentialSubstitutions) {
            search: for (final Entry<Class<?>, DataContainerCodecPrototype<?>> real : byClassBuilder.entrySet()) {
                if (BindingReflections.isSubstitutionFor(substitution, real.getKey())) {
                    bySubstitutionBuilder.put(substitution, real.getValue());
                    break search;
                }
            }
        }
        byClassBuilder.putAll(bySubstitutionBuilder);
        byClass = ImmutableMap.copyOf(byClassBuilder);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C extends DataObject> DataContainerCodecContext<C, ?> streamChild(final Class<C> childClass) {
        final DataContainerCodecPrototype<?> child = byClass.get(childClass);
        return (DataContainerCodecContext<C, ?>) childNonNull(child, childClass,
            "Supplied class %s is not valid case in %s", childClass, bindingArg()).get();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C extends DataObject> Optional<DataContainerCodecContext<C, ?>> possibleStreamChild(
            final Class<C> childClass) {
        final DataContainerCodecPrototype<?> child = byClass.get(childClass);
        if (child != null) {
            return Optional.of((DataContainerCodecContext<C, ?>) child.get());
        }
        return Optional.absent();
    }

    Iterable<Class<?>> getCaseChildrenClasses() {
        return Iterables.concat(byCaseChildClass.keySet(), ambiguousByCaseChildClass.keySet());
    }

    protected DataContainerCodecPrototype<CaseSchemaNode> loadCase(final Class<?> childClass) {
        final Optional<CaseSchemaNode> childSchema = factory().getRuntimeContext().getCaseSchemaDefinition(getSchema(),
            childClass);
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
        final NormalizedNodeContainer<?, ?, NormalizedNode<?, ?>> casted =
                (NormalizedNodeContainer<?, ?, NormalizedNode<?, ?>>) data;
        final NormalizedNode<?, ?> first = Iterables.getFirst(casted.getValue(), null);

        if (first == null) {
            return null;
        }
        final DataContainerCodecPrototype<?> caze = byYangCaseChild.get(first.getIdentifier());
        return (D) caze.get().deserialize(data);
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

    DataContainerCodecContext<?, ?> getCaseByChildClass(final @Nonnull Class<? extends DataObject> type) {
        DataContainerCodecPrototype<?> result = byCaseChildClass.get(type);
        if (result == null) {
            // We have not found an unambiguous result, try ambiguous ones
            final List<DataContainerCodecPrototype<?>> inexact = ambiguousByCaseChildClass.get(type);
            if (!inexact.isEmpty()) {
                result = inexact.get(0);
                // Issue a warning, but only once so as not to flood the logs
                if (ambiguousByCaseChildWarnings.add(type)) {
                    LOG.warn("Ambiguous reference {} to child of {} resolved to {}, the first case in {} This mapping "
                            + "is not guaranteed to be stable and is subject to variations based on runtime "
                            + "circumstances. Please see the stack trace for hints about the source of ambiguity.",
                            type, bindingArg(), result.getBindingClass(),
                            Lists.transform(inexact, DataContainerCodecPrototype::getBindingClass), new Throwable());
                }
            }
        }

        return childNonNull(result, type, "Class %s is not child of any cases for %s", type, bindingArg()).get();
    }
}
