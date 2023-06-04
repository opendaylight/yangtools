/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.MultimapBuilder.SetMultimapBuilder;
import com.google.common.collect.Multimaps;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeCachingCodec;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.runtime.api.CaseRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ChoiceRuntimeType;
import org.opendaylight.yangtools.yang.binding.BindingObject;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.binding.contract.Naming;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
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
final class ChoiceNodeCodecContext<D extends DataObject> extends DataContainerCodecContext<D, ChoiceRuntimeType>
        implements BindingDataObjectCodecTreeNode<D> {
    private static final Logger LOG = LoggerFactory.getLogger(ChoiceNodeCodecContext.class);

    private final ImmutableMap<NodeIdentifier, DataContainerCodecPrototype<?>> byYangCaseChild;
    private final ImmutableListMultimap<Class<?>, DataContainerCodecPrototype<?>> ambiguousByCaseChildClass;
    private final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> byCaseChildClass;
    private final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> byClass;
    private final Set<Class<?>> ambiguousByCaseChildWarnings;

    ChoiceNodeCodecContext(final DataContainerCodecPrototype<ChoiceRuntimeType> prototype) {
        super(prototype);
        final var byYangCaseChildBuilder = new HashMap<NodeIdentifier, DataContainerCodecPrototype<?>>();
        final var byClassBuilder = new HashMap<Class<?>, DataContainerCodecPrototype<?>>();
        final var childToCase = SetMultimapBuilder.hashKeys().hashSetValues()
            .<Class<?>, DataContainerCodecPrototype<?>>build();

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
            final var cazeCls = (Class<? extends DataObject>) cazeDef.getBindingClass();
            for (var cazeChild : getChildrenClasses(cazeCls)) {
                childToCase.put(cazeChild, cazeDef);
            }
            // Updates collection of YANG instance identifier to case
            for (var stmt : cazeDef.getType().statement().effectiveSubstatements()) {
                if (stmt instanceof DataSchemaNode cazeChild) {
                    byYangCaseChildBuilder.put(NodeIdentifier.create(cazeChild.getQName()), cazeDef);
                }
            }
        }
        byYangCaseChild = ImmutableMap.copyOf(byYangCaseChildBuilder);

        // Move unambiguous child->case mappings to byCaseChildClass, removing them from childToCase
        final var ambiguousByCaseBuilder = ImmutableListMultimap.<Class<?>, DataContainerCodecPrototype<?>>builder();
        final var unambiguousByCaseBuilder = ImmutableMap.<Class<?>, DataContainerCodecPrototype<?>>builder();
        for (var entry : Multimaps.asMap(childToCase).entrySet()) {
            final var cases = entry.getValue();
            if (cases.size() != 1) {
                // Sort all possibilities by their FQCN to retain semi-predictable results
                final var list = new ArrayList<>(entry.getValue());
                list.sort(Comparator.comparing(proto -> proto.getBindingClass().getCanonicalName()));
                ambiguousByCaseBuilder.putAll(entry.getKey(), list);
            } else {
                unambiguousByCaseBuilder.put(entry.getKey(), cases.iterator().next());
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
        final var bySubstitutionBuilder = new HashMap<Class<?>, DataContainerCodecPrototype<?>>();
        final var context = factory.getRuntimeContext();
        for (var caseType : context.getTypes().allCaseChildren(choiceType)) {
            final var caseName = caseType.getIdentifier();
            if (!localCases.contains(caseName)) {
                // FIXME: do not rely on class loading here, the check we are performing should be possible on
                //        GeneratedType only -- or it can be provided by BindingRuntimeTypes -- i.e. rather than
                //        'allCaseChildren()' it would calculate additional mappings we can use off-the-bat.
                final var substitution = loadCase(context, caseType);

                search: for (var real : byClassBuilder.entrySet()) {
                    if (isSubstitutionFor(substitution, real.getKey())) {
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
        final var child = byClass.get(childClass);
        return (DataContainerCodecContext<C, ?>) childNonNull(child, childClass,
            "Supplied class %s is not valid case in %s", childClass, bindingArg()).get();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C extends DataObject> Optional<DataContainerCodecContext<C, ?>> possibleStreamChild(
            final Class<C> childClass) {
        final var child = byClass.get(childClass);
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
        if (arg instanceof NodeIdentifierWithPredicates) {
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
        final var casted = checkDataArgument(ChoiceNode.class, data);
        final var first = Iterables.getFirst(casted.body(), null);

        if (first == null) {
            // FIXME: this needs to be sorted out
            return null;
        }
        final var caze = byYangCaseChild.get(first.name());
        return (D) caze.getDataObject().deserialize(data);
    }

    @Override
    public NormalizedNode serialize(final D data) {
        return serializeImpl(data);
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

    @Override
    public BindingNormalizedNodeCachingCodec<D> createCachingCodec(
            final ImmutableCollection<Class<? extends BindingObject>> cacheSpecifier) {
        return createCachingCodec(this, cacheSpecifier);
    }

    DataContainerCodecContext<?, ?> getCaseByChildClass(final @NonNull Class<? extends DataObject> type) {
        var result = byCaseChildClass.get(type);
        if (result == null) {
            // We have not found an unambiguous result, try ambiguous ones
            final var inexact = ambiguousByCaseChildClass.get(type);
            if (!inexact.isEmpty()) {
                result = inexact.get(0);
                // Issue a warning, but only once so as not to flood the logs
                if (ambiguousByCaseChildWarnings.add(type)) {
                    LOG.warn("""
                        Ambiguous reference {} to child of {} resolved to {}, the first case in {} This mapping is \
                        not guaranteed to be stable and is subject to variations based on runtime circumstances. \
                        Please see the stack trace for hints about the source of ambiguity.""",
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
        final var ret = new LinkedList<Class<? extends DataObject>>();
        for (var method : type.getMethods()) {
            final var entity = getYangModeledReturnType(method, Naming.GETTER_PREFIX);
            if (entity.isPresent()) {
                ret.add((Class<? extends DataObject>) entity.orElseThrow());
            }
        }
        return ret;
    }
}
