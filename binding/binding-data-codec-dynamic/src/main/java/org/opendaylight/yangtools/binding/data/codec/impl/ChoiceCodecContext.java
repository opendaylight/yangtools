/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.MultimapBuilder.SetMultimapBuilder;
import com.google.common.collect.Multimaps;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.ChoiceIn;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.data.codec.api.BindingChoiceCodecTreeNode;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.yangtools.binding.runtime.api.CaseRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.ChoiceRuntimeType;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
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
 * <p>Unfortunately this does not quite work with groupings, as their generation has changed: we do not have interfaces
 * that would capture grouping instantiations, hence we do not have a proper addressing point and users need to specify
 * the interfaces generated in the grouping's definition. These can be very much ambiguous, as a {@code grouping} can be
 * used in multiple modules independently within an {@code augment} targeting {@code choice}, as each instantiation is
 * guaranteed to have a unique namespace -- but we do not have the appropriate instantiations of those nodes.
 *
 * <p>To address this issue we have a two-class lookup mechanism, which relies on the interface generated for the
 * {@code case} statement to act as the namespace anchor bridging the nodes inside the grouping to the namespace in
 * which they are instantiated.
 *
 * <p>Furthermore downstream code relies on historical mechanics, which would guess what the instantiation is, silently
 * assuming the ambiguity is theoretical and does not occur in practice.
 *
 * <p>This leads to three classes of addressing, in order descending performance requirements.
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
final class ChoiceCodecContext<T extends ChoiceIn<?>>
        extends DataContainerCodecContext<T, ChoiceRuntimeType, ChoiceCodecPrototype<T>>
        implements BindingChoiceCodecTreeNode<T> {
    private static final Logger LOG = LoggerFactory.getLogger(ChoiceCodecContext.class);

    private final ImmutableListMultimap<Class<?>, CommonDataObjectCodecPrototype<?>> ambiguousByCaseChildClass;
    private final ImmutableMap<Class<?>, CommonDataObjectCodecPrototype<?>> byCaseChildClass;
    private final ImmutableMap<NodeIdentifier, CaseCodecPrototype> byYangCaseChild;
    private final ImmutableMap<Class<?>, CommonDataObjectCodecPrototype<?>> byClass;
    private final Set<Class<?>> ambiguousByCaseChildWarnings;

    ChoiceCodecContext(final Class<T> javaClass, final ChoiceRuntimeType runtimeType,
            final CodecContextFactory contextFactory) {
        this(new ChoiceCodecPrototype<>(contextFactory, runtimeType, javaClass));
    }

    ChoiceCodecContext(final ChoiceCodecPrototype<T> prototype) {
        super(prototype);
        final var byYangCaseChildBuilder = new HashMap<NodeIdentifier, CaseCodecPrototype>();
        final var byClassBuilder = new HashMap<Class<?>, CommonDataObjectCodecPrototype<?>>();
        final var childToCase = SetMultimapBuilder.hashKeys().hashSetValues()
            .<Class<?>, CommonDataObjectCodecPrototype<?>>build();

        // Load case statements valid in this choice and keep track of their names
        final var choiceType = prototype.runtimeType();
        final var factory = prototype.contextFactory();
        final var localCases = new HashSet<JavaTypeName>();
        for (var caseType : choiceType.validCaseChildren()) {
            @SuppressWarnings("unchecked")
            final var caseClass = (Class<? extends DataObject>) loadCase(factory.runtimeContext(), caseType);
            final var caseProto = new CaseCodecPrototype(caseClass, caseType, factory);

            localCases.add(caseType.getIdentifier());
            byClassBuilder.put(caseClass, caseProto);

            // Updates collection of case children
            for (var cazeChild : getChildrenClasses(caseClass)) {
                childToCase.put(cazeChild, caseProto);
            }
            // Updates collection of YANG instance identifier to case
            for (var stmt : caseType.statement().effectiveSubstatements()) {
                if (stmt instanceof DataSchemaNode cazeChild) {
                    byYangCaseChildBuilder.put(NodeIdentifier.create(cazeChild.getQName()), caseProto);
                }
            }
        }
        byYangCaseChild = ImmutableMap.copyOf(byYangCaseChildBuilder);

        // Move unambiguous child->case mappings to byCaseChildClass, removing them from childToCase
        final var ambiguousByCaseBuilder = ImmutableListMultimap.<Class<?>, CommonDataObjectCodecPrototype<?>>builder();
        final var unambiguousByCaseBuilder = ImmutableMap.<Class<?>, CommonDataObjectCodecPrototype<?>>builder();
        for (var entry : Multimaps.asMap(childToCase).entrySet()) {
            final var cases = entry.getValue();
            if (cases.size() != 1) {
                // Sort all possibilities by their FQCN to retain semi-predictable results
                final var list = new ArrayList<>(entry.getValue());
                list.sort(Comparator.comparing(proto -> proto.javaClass().getCanonicalName()));
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
        final var bySubstitutionBuilder = new HashMap<Class<?>, CommonDataObjectCodecPrototype<?>>();
        final var context = factory.runtimeContext();
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

    private static Class<?> loadCase(final BindingRuntimeContext context, final CaseRuntimeType caseType) {
        final var className = caseType.getIdentifier();
        try {
            return context.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new LinkageError("Failed to load class for " + className, e);
        }
    }

    @Override
    @Deprecated(since = "13.0.0", forRemoval = true)
    public WithStatus getSchema() {
        // FIXME: Bad cast, we should be returning an EffectiveStatement perhaps?
        return (WithStatus) prototype().runtimeType().statement();
    }

    @Override
    CommonDataObjectCodecPrototype<?> streamChildPrototype(final Class<?> childClass) {
        return byClass.get(childClass);
    }

    Iterable<Class<?>> getCaseChildrenClasses() {
        return Iterables.concat(byCaseChildClass.keySet(), ambiguousByCaseChildClass.keySet());
    }

    @Override
    public CodecContext yangPathArgumentChild(final PathArgument arg) {
        return ((CaseCodecContext<?>) super.yangPathArgumentChild(arg)).yangPathArgumentChild(arg);
    }

    @Override
    CodecContextSupplier yangChildSupplier(final NodeIdentifier arg) {
        return byYangCaseChild.get(arg);
    }

    @Override
    T deserializeObject(final NormalizedNode normalizedNode) {
        final var casted = checkDataArgument(ChoiceNode.class, normalizedNode);
        final var it = casted.body().iterator();
        if (!it.hasNext()) {
            // FIXME: can this reasonably happen? Empty choice nodes do not have semantics, or do they?
            return null;
        }

        final var childName = it.next().name();
        final var caze = childNonNull(byYangCaseChild.get(childName), childName, "%s is not a valid case child of %s",
            childName, this);
        return (T) caze.getCodecContext().deserializeObject(casted);
    }

    @Override
    public CommonDataObjectCodecContext<?, ?> bindingPathArgumentChild(final DataObjectStep<?> step,
            final List<PathArgument> builder) {
        final var caseType = step.caseType();
        // Prefer non-ambiguous addressing, which should not pose any problems. Otherwise fall back to checking for
        // ambiguities
        final var caze = caseType != null ? getStreamChild(caseType) : getCaseByChildClass(step.type());
        caze.addYangPathArgument(step, builder);
        return caze.bindingPathArgumentChild(step, builder);
    }

    private DataContainerCodecContext<?, ?, ?> getCaseByChildClass(final @NonNull Class<? extends DataObject> type) {
        var result = byCaseChildClass.get(type);
        if (result == null) {
            // We have not found an unambiguous result, try ambiguous ones
            final var inexact = ambiguousByCaseChildClass.get(type);
            if (!inexact.isEmpty()) {
                result = inexact.getFirst();
                // Issue a warning, but only once so as not to flood the logs
                if (ambiguousByCaseChildWarnings.add(type)) {
                    LOG.warn("""
                        Ambiguous reference {} to child of {} resolved to {}, the first case in {} This mapping is \
                        not guaranteed to be stable and is subject to variations based on runtime circumstances. \
                        Please see the stack trace for hints about the source of ambiguity.""",
                        type, getBindingClass(), result.javaClass(),
                        Lists.transform(inexact, CommonDataObjectCodecPrototype::javaClass), new Throwable());
                }
            }
        }

        return childNonNull(result, type, "Class %s is not child of any cases for %s", type, getBindingClass())
            .getCodecContext();
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
            DataContainerAnalysis.getYangModeledReturnType(method, Naming.GETTER_PREFIX)
                .ifPresent(entity -> ret.add((Class<? extends DataObject>) entity));
        }
        return ret;
    }
}
