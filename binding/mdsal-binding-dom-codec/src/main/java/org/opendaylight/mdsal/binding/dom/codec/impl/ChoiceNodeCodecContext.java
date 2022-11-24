/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.MultimapBuilder.SetMultimapBuilder;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.runtime.api.CaseRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ChoiceRuntimeType;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextNode;
import org.opendaylight.yangtools.yang.data.util.NormalizedNodeSchemaUtils;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a bit tricky. DataObject addressing does not take into account choice/case statements, and hence given:
 *
 * <pre>
 *   <code>
 *     container foo {
 *       choice bar {
 *         leaf baz;
 *       }
 *     }
 *   </code>
 * </pre>
 * we will see {@code Baz extends ChildOf<Foo>}, which is how the users would address it in InstanceIdentifier terms.
 * The implicit assumption being made is that {@code Baz} identifies a particular instantiation and hence provides
 * unambiguous reference to an effective schema statement.
 *
 * <p>
 * Unfortunately this does not quite work with groupings, as their generation has changed: we do not have interfaces
 * that would capture grouping instantiations, hence we do not have a proper addressing point and users need to specify
 * the interfaces generated in the grouping's definition. These can be very much ambiguous, as a {@code grouping} can be
 * used in multiple modules independently within an {@code augment} targeting {@code choice}, as each instantiation is
 * guaranteed to have a unique namespace -- but we do not have the appropriate instantiations of those nodes.
 *
 * <p>
 * To address this issue we have a two-class lookup mechanism, which relies on the interface generated for the
 * {@code case} statement to act as the namespace anchor bridging the nodes inside the grouping to the namespace in
 * which they are instantiated.
 *
 * <p>
 * Furthermore downstream code relies on historical mechanics, which would guess what the instantiation is, silently
 * assuming the ambiguity is theoretical and does not occur in practice.
 *
 * <p>
 * This leads to three classes of addressing, in order descending performance requirements.
 * <ul>
 *   <li>Direct DataObject, where we name an exact child</li>
 *   <li>Case DataObject + Grouping DataObject</li>
 *   <li>Grouping DataObject, which is ambiguous</li>
 * </ul>
 *
 * {@link #byCaseChildClass} supports direct DataObject mapping and contains only unambiguous children, while
 * {@link #byClass} supports indirect mapping and contains {@code case} sub-statements.
 *
 * {@link #ambiguousByCaseChildClass} contains ambiguous mappings, for which we end up issuing warnings. We track each
 * ambiguous reference and issue warn once when they are encountered -- tracking warning information in
 * {@link #ambiguousByCaseChildWarnings}.
 */
final class ChoiceNodeCodecContext<D extends DataObject> extends DataContainerCodecContext<D, ChoiceRuntimeType> {
    private static final Logger LOG = LoggerFactory.getLogger(ChoiceNodeCodecContext.class);

    private final ImmutableMap<YangInstanceIdentifier.PathArgument, DataContainerCodecPrototype<?>> byYangCaseChild;
    private final ImmutableListMultimap<Class<?>, DataContainerCodecPrototype<?>> ambiguousByCaseChildClass;
    private final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> byCaseChildClass;
    private final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> byClass;
    private final Set<Class<?>> ambiguousByCaseChildWarnings;

    ChoiceNodeCodecContext(final DataContainerCodecPrototype<ChoiceRuntimeType> prototype) {
        super(prototype);
        final Map<YangInstanceIdentifier.PathArgument, DataContainerCodecPrototype<?>> byYangCaseChildBuilder =
            new HashMap<>();
        final Map<Class<?>, DataContainerCodecPrototype<?>> byClassBuilder = new HashMap<>();
        final SetMultimap<Class<?>, DataContainerCodecPrototype<?>> childToCase =
            SetMultimapBuilder.hashKeys().hashSetValues().build();

        // Load case statements valid in this choice and keep track of their names
        final var choiceType = prototype.getType();
        final var factory = prototype.getFactory();
        final var localCases = new HashSet<JavaTypeName>();
        for (var caseType : choiceType.validCaseChildren()) {
            final var cazeDef = loadCase(factory, caseType);
            localCases.add(caseType.getIdentifier());
            byClassBuilder.put(cazeDef.getBindingClass(), cazeDef);

            // Updates collection of case children
            @SuppressWarnings("unchecked")
            final Class<? extends DataObject> cazeCls = (Class<? extends DataObject>) cazeDef.getBindingClass();
            for (final Class<? extends DataObject> cazeChild : getChildrenClasses(cazeCls)) {
                childToCase.put(cazeChild, cazeDef);
            }
            // Updates collection of YANG instance identifier to case
            for (var stmt : cazeDef.getType().statement().effectiveSubstatements()) {
                if (stmt instanceof DataSchemaNode cazeChild) {
                    if (cazeChild.isAugmenting()) {
                        final AugmentationSchemaNode augment = NormalizedNodeSchemaUtils.findCorrespondingAugment(
                            // FIXME: bad cast
                            (DataSchemaNode) cazeDef.getType().statement(), cazeChild);
                        if (augment != null) {
                            byYangCaseChildBuilder.put(DataSchemaContextNode.augmentationIdentifierFrom(augment),
                                cazeDef);
                            continue;
                        }
                    }
                    byYangCaseChildBuilder.put(NodeIdentifier.create(cazeChild.getQName()), cazeDef);
                }
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

        /*
         * Choice/Case mapping across groupings is compile-time unsafe and we therefore need to also track any
         * CaseRuntimeTypes added to the choice in other contexts. This is necessary to discover when a case represents
         * equivalent data in a different instantiation context.
         *
         * This is required due property of binding specification, that if choice is in grouping schema path location is
         * lost, and users may use incorrect case class using copy builders.
         */
        final Map<Class<?>, DataContainerCodecPrototype<?>> bySubstitutionBuilder = new HashMap<>();
        final var context = factory.getRuntimeContext();
        for (var caseType : context.getTypes().allCaseChildren(choiceType)) {
            final var caseName = caseType.getIdentifier();
            if (!localCases.contains(caseName)) {
                // FIXME: do not rely on class loading here, the check we are performing should be possible on
                //        GeneratedType only -- or it can be provided by BindingRuntimeTypes -- i.e. rather than
                //        'allCaseChildren()' it would calculate additional mappings we can use off-the-bat.
                final Class<?> substitution = loadCase(context, caseType);

                search: for (final Entry<Class<?>, DataContainerCodecPrototype<?>> real : byClassBuilder.entrySet()) {
                    if (BindingReflections.isSubstitutionFor(substitution, real.getKey())) {
                        bySubstitutionBuilder.put(substitution, real.getValue());
                        break search;
                    }
                }
            }
        }

        byClassBuilder.putAll(bySubstitutionBuilder);
        byClass = ImmutableMap.copyOf(byClassBuilder);
    }

    private static DataContainerCodecPrototype<CaseRuntimeType> loadCase(final CodecContextFactory factory,
            final CaseRuntimeType caseType) {
        return DataContainerCodecPrototype.from(loadCase(factory.getRuntimeContext(), caseType), caseType, factory);
    }

    private static Class<?> loadCase(final BindingRuntimeContext context, final CaseRuntimeType caseType) {
        final var className = caseType.getIdentifier();
        try {
            return context.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new LinkageError("Failed to load class for " + className, e);
        }
    }

    @Override
    public WithStatus getSchema() {
        // FIXME: Bad cast, we should be returning an EffectiveStatement perhaps?
        return (WithStatus) getType().statement();
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
        return Optional.empty();
    }

    Iterable<Class<?>> getCaseChildrenClasses() {
        return Iterables.concat(byCaseChildClass.keySet(), ambiguousByCaseChildClass.keySet());
    }

    @Override
    public NodeCodecContext yangPathArgumentChild(final YangInstanceIdentifier.PathArgument arg) {
        final DataContainerCodecPrototype<?> cazeProto;
        if (arg instanceof YangInstanceIdentifier.NodeIdentifierWithPredicates) {
            cazeProto = byYangCaseChild.get(new NodeIdentifier(arg.getNodeType()));
        } else {
            cazeProto = byYangCaseChild.get(arg);
        }

        return childNonNull(cazeProto, arg, "Argument %s is not valid child of %s", arg, getSchema()).get()
                .yangPathArgumentChild(arg);
    }

    @Override
    @SuppressWarnings("unchecked")
    @SuppressFBWarnings(value = "NP_NONNULL_RETURN_VIOLATION", justification = "See FIXME below")
    public D deserialize(final NormalizedNode data) {
        final ChoiceNode casted = checkDataArgument(ChoiceNode.class, data);
        final NormalizedNode first = Iterables.getFirst(casted.body(), null);

        if (first == null) {
            // FIXME: this needs to be sorted out
            return null;
        }
        final DataContainerCodecPrototype<?> caze = byYangCaseChild.get(first.getIdentifier());
        return (D) caze.get().deserialize(data);
    }

    @Override
    protected Object deserializeObject(final NormalizedNode normalizedNode) {
        return deserialize(normalizedNode);
    }

    @Override
    public PathArgument deserializePathArgument(final YangInstanceIdentifier.PathArgument arg) {
        checkArgument(getDomPathArgument().equals(arg));
        return null;
    }

    @Override
    public YangInstanceIdentifier.PathArgument serializePathArgument(final PathArgument arg) {
        // FIXME: check for null, since binding container is null.
        return getDomPathArgument();
    }

    DataContainerCodecContext<?, ?> getCaseByChildClass(final @NonNull Class<? extends DataObject> type) {
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

    /**
     * Scans supplied class and returns an iterable of all data children classes.
     *
     * @param type
     *            YANG Modeled Entity derived from DataContainer
     * @return Iterable of all data children, which have YANG modeled entity
     */
    // FIXME: MDSAL-780: replace use of this method
    @SuppressWarnings("unchecked")
    private static Iterable<Class<? extends DataObject>> getChildrenClasses(final Class<? extends DataContainer> type) {
        checkArgument(type != null, "Target type must not be null");
        checkArgument(DataContainer.class.isAssignableFrom(type), "Supplied type must be derived from DataContainer");
        List<Class<? extends DataObject>> ret = new LinkedList<>();
        for (Method method : type.getMethods()) {
            Optional<Class<? extends DataContainer>> entity = getYangModeledReturnType(method,
                BindingMapping.GETTER_PREFIX);
            if (entity.isPresent()) {
                ret.add((Class<? extends DataObject>) entity.get());
            }
        }
        return ret;
    }
}
