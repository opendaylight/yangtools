/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AddedByUsesAware;
import org.opendaylight.yangtools.yang.model.api.CopyableNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesEffectiveStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A composite generator. Composite generators may contain additional children, which end up being mapped into
 * the naming hierarchy 'under' the composite generator. To support this use case, each composite has a Java package
 * name assigned.
 *
 * <p>State tracking for resolution of children to their original declaration, i.e. back along the 'uses' and 'augment'
 * axis. This is quite convoluted because we are traversing the generator tree recursively in the iteration order of
 * children, but actual dependencies may require resolution in a different order, for example in the case of:
 * <pre>
 *   container foo {
 *     uses bar {             // A
 *       augment bar {        // B
 *         container xyzzy;   // C
 *       }
 *     }
 *
 *     grouping bar {
 *       container bar {      // D
 *         uses baz;          // E
 *       }
 *     }
 *
 *     grouping baz {
 *       leaf baz {           // F
 *         type string;
 *       }
 *     }
 *   }
 *
 *   augment /foo/bar/xyzzy { // G
 *     leaf xyzzy {           // H
 *       type string;
 *     }
 *   }
 * </pre>
 *
 * <p>In this case we have three manifestations of 'leaf baz' -- marked A, E and F in the child iteration order. In
 * order to perform a resolution, we first have to determine that F is the original definition, then establish that E
 * is using the definition made by F and finally establish that A is using the definition made by F.
 *
 * <p>Dealing with augmentations is harder still, because we need to attach them to the original definition, hence for
 * the /foo/bar container at A, we need to understand that its original definition is at D and we need to attach the
 * augment at B to D. Futhermore we also need to establish that the augmentation at G attaches to container defined in
 * C, so that the 'leaf xyzzy' existing as /foo/bar/xyzzy/xyzzy under C has its original definition at H.
 *
 * <p>Finally realize that the augment at G can actually exist in a different module and is shown in this example only
 * the simplified form. That also means we could encounter G well before 'container foo' as well as we can have multiple
 * such augments sprinkled across multiple modules having the same dependency rules as between C and G -- but they still
 * have to form a directed acyclic graph and we partially deal with those complexities by having modules sorted by their
 * dependencies.
 *
 * <p>For further details see {@link #linkOriginalGenerator()} and {@link #linkOriginalGeneratorRecursive()}, which deal
 * with linking original instances in the tree iteration order. The part dealing with augment attachment lives mostly
 * in {@link AugmentRequirement}.
 */
abstract sealed class CompositeGenerator<S extends EffectiveStatement<?, ?>, R extends CompositeRuntimeType>
        extends AbstractExplicitGenerator<S, R>
        permits DataContainerGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(CompositeGenerator.class);

    // FIXME: we want to allocate this lazily to lower memory footprint
    private final @NonNull CollisionDomain domain = new CollisionDomain(this);
    private final @NonNull List<Generator> childGenerators;

    /**
     * List of composite children which have not been recursively processed. This may become a mutable list when we
     * have some children which have not completed linking. Once we have completed linking of all children, including
     * {@link #unlinkedChildren}, this will be set to {@code null}.
     */
    private List<DataContainerGenerator<?, ?>> unlinkedComposites = List.of();
    /**
     * List of children which have not had their original linked. This list starts of as null. When we first attempt
     * linkage, it becomes non-null.
     */
    private List<Generator> unlinkedChildren;

    @NonNullByDefault
    CompositeGenerator(final S statement) {
        super(statement);
        childGenerators = createChildren(statement);
    }

    @NonNullByDefault
    CompositeGenerator(final S statement, final CompositeGenerator<?, ?> parent) {
        super(statement, parent);
        childGenerators = createChildren(statement);
    }

    final @NonNull CollisionDomain domain() {
        return domain;
    }

    @Override
    public final Iterator<Generator> iterator() {
        return childGenerators.iterator();
    }

    @Override
    final boolean isEmpty() {
        return childGenerators.isEmpty();
    }

    @Override
    final CompositeGenerator<S, R> getOriginal() {
        return (CompositeGenerator<S, R>) super.getOriginal();
    }

    @Override
    final CompositeGenerator<S, R> tryOriginal() {
        return (CompositeGenerator<S, R>) super.tryOriginal();
    }

    final <X extends EffectiveStatement<?, ?>, Y extends RuntimeType> @Nullable OriginalLink<X, Y> originalChild(
            final QName childQName) {
        // First try groupings/augments ...
        final var inferred = findInferredGenerator(childQName);
        if (inferred != null) {
            return (OriginalLink<X, Y>) OriginalLink.partial(inferred);
        }

        // ... no luck, we really need to start looking at our origin
        final var prev = previous();
        if (prev != null) {
            final var prevQName = childQName.bindTo(prev.getQName().getModule());
            final var found = prev.findSchemaTreeGenerator(prevQName);
            if (found != null) {
                return (OriginalLink<X, Y>) found.originalLink();
            }
        }

        return null;
    }

    @Override
    final AbstractExplicitGenerator<?, ?> findSchemaTreeGenerator(final QName qname) {
        final var found = super.findSchemaTreeGenerator(qname);
        return found != null ? found : findInferredGenerator(qname);
    }

    /**
     * Attempt to search for a child in inferred statements, like augments and groupings, for the purposes of
     * {@link #originalChild(QName)}.
     *
     * @param qname the QName of the child being sought after
     * @return the child, or {@code null not found}
     */
    abstract @Nullable AbstractExplicitGenerator<?, ?> findInferredGenerator(@NonNull QName qname);


    // FIXME: These four methods are specific to augment handling and should be exposed as aninterface towards callers.
    //        As per RFC7950:
    //           This node is called the augment's target node.  The target node MUST be either
    //           a container, list, choice, case, input, output, or notification node.
    //        So things like OperationGenerator are explicitly excluded, yet we have it here.

    @NonNullByDefault
    abstract void addAugment(AugmentGenerator augment);

    /**
     * Attempt to find an {@link AugmentGenerator} matching specified statement.
     *
     * @param statement the statement
     * @return matching generator, or {@code null} if not found
     */
    abstract @Nullable AugmentGenerator findAugmentByStatement(AugmentEffectiveStatement statement);

    /**
     * Attempt to find a child in a previously {@link #addAugment(AugmentGenerator) added} augment generator.
     *
     * @param qname the QName of the child being sought after
     * @return the {@link AugmentGenerator} containing the child, or {@code null} if no such generator exists
     */
    abstract @Nullable AugmentGenerator findAugmentForGenerator(QName qname);

    /**
     * Attempt to find a child in a {@code grouping} this generator uses.
     *
     * @param qname the QName of the child being sought after
     * @return the {@link GroupingGenerator} containing the child, or {@code null} if no such generator exists
     */
    abstract @Nullable GroupingGenerator findGroupingForGenerator(QName qname);

    // end of above FIXME

    /**
     * Attempt to link the generator corresponding to the original definition for this generator's statements as well as
     * to all child generators.
     *
     * @return Progress indication
     */
    final @NonNull LinkageProgress linkOriginalGeneratorRecursive() {
        if (unlinkedComposites == null) {
            // We have unset this list (see below), and there is nothing left to do
            return LinkageProgress.DONE;
        }

        if (unlinkedChildren == null) {
            unlinkedChildren = childGenerators.stream()
                .filter(AbstractExplicitGenerator.class::isInstance)
                .map(child -> (AbstractExplicitGenerator<?, ?>) child)
                .collect(Collectors.toList());
        }

        var progress = LinkageProgress.NONE;
        if (!unlinkedChildren.isEmpty()) {
            // Attempt to make progress on child linkage
            final var it = unlinkedChildren.iterator();
            while (it.hasNext()) {
                if (it.next() instanceof AbstractExplicitGenerator<?, ?> explicit && explicit.linkOriginalGenerator()) {
                    progress = LinkageProgress.SOME;
                    it.remove();

                    // If this is a composite generator we need to process is further
                    if (explicit instanceof DataContainerGenerator<?, ?> composite) {
                        if (unlinkedComposites.isEmpty()) {
                            unlinkedComposites = new ArrayList<>();
                        }
                        unlinkedComposites.add(composite);
                    }
                }
            }

            if (unlinkedChildren.isEmpty()) {
                // Nothing left to do, make sure any previously-allocated list can be scavenged
                unlinkedChildren = List.of();
            }
        }

        // Process children of any composite children we have.
        final var it = unlinkedComposites.iterator();
        while (it.hasNext()) {
            final var tmp = it.next().linkOriginalGeneratorRecursive();
            if (tmp != LinkageProgress.NONE) {
                progress = LinkageProgress.SOME;
            }
            if (tmp == LinkageProgress.DONE) {
                it.remove();
            }
        }

        if (unlinkedChildren.isEmpty() && unlinkedComposites.isEmpty()) {
            // All done, set the list to null to indicate there is nothing left to do in this generator or any of our
            // children.
            unlinkedComposites = null;
            return LinkageProgress.DONE;
        }

        return progress;
    }

    // FIXME: This method is *really* not nice: it contains a bunch of checks for 'this' class type.
    //        We really want to split it out into an interface which will receive 'this' and the statement and decide
    //        what to do and have each subclass supply its own version of it
    private @NonNull List<Generator> createChildren(final EffectiveStatement<?, ?> statement) {
        final var tmp = new ArrayList<Generator>();
        final var tmpAug = new ArrayList<AugmentGenerator>();

        for (var stmt : statement.effectiveSubstatements()) {
            switch (stmt) {
                case ActionEffectiveStatement action -> {
                    if (!isAugmenting(action)) {
                        tmp.add(this instanceof EntryObjectGenerator entry
                            ? new KeyedListActionGenerator(action, entry) : new ActionGenerator(action, this));
                    }
                }
                case AnydataEffectiveStatement anydata -> {
                    if (!isAugmenting(anydata)) {
                        tmp.add(new OpaqueObjectGenerator.Anydata(anydata, this));
                    }
                }
                case AnyxmlEffectiveStatement anyxml -> {
                    if (!isAugmenting(anyxml)) {
                        tmp.add(new OpaqueObjectGenerator.Anyxml(anyxml, this));
                    }
                }
                case CaseEffectiveStatement cast -> tmp.add(new CaseGenerator(cast, this));
                case ChoiceEffectiveStatement choice -> {
                    // FIXME: use isOriginalDeclaration() ?
                    if (!isAddedByUses(choice)) {
                        tmp.add(new ChoiceGenerator(choice, this));
                    }
                }
                case ContainerEffectiveStatement container -> {
                    if (isOriginalDeclaration(container)) {
                        tmp.add(new ContainerGenerator(container, this));
                    }
                }
                case FeatureEffectiveStatement feature -> {
                    if (this instanceof ModuleGenerator parent) {
                        tmp.add(new FeatureGenerator(feature, parent));
                    }
                }
                case GroupingEffectiveStatement grouping -> tmp.add(new GroupingGenerator(grouping, this));
                case IdentityEffectiveStatement identity -> tmp.add(new IdentityGenerator(identity, this));
                case InputEffectiveStatement input -> tmp.add(new InputGenerator(input, this));
                case LeafEffectiveStatement leaf -> {
                    if (!isAugmenting(leaf)) {
                        tmp.add(new LeafGenerator(leaf, this));
                    }
                }
                case LeafListEffectiveStatement leafList -> {
                    if (!isAugmenting(leafList)) {
                        tmp.add(new LeafListGenerator(leafList, this));
                    }
                }
                case ListEffectiveStatement list -> {
                    if (isOriginalDeclaration(list)) {
                        final var key = list.keyStatement();
                        if (key != null) {
                            final var listGen = new EntryObjectGenerator(list, key, this);
                            tmp.add(listGen);
                            tmp.add(listGen.keyGenerator());
                        } else {
                            tmp.add(new ItemObjectGenerator(list, this));
                        }
                    }
                }
                case NotificationEffectiveStatement notification -> {
                    if (!isAugmenting(notification)) {
                        switch (this) {
                            case EntryObjectGenerator entry ->
                                tmp.add(new KeyedListNotificationGenerator(notification, entry));
                            case GroupingGenerator grouping -> {
                                if (!isAddedByUses(notification)) {
                                    tmp.add(new NotificationBodyGenerator(notification, grouping));
                                }
                            }
                            case ModuleGenerator module ->
                                tmp.add(new NotificationGenerator(notification, module));
                            default ->
                                tmp.add(new InstanceNotificationGenerator(notification, this));
                        }
                    }
                }
                case OutputEffectiveStatement output -> tmp.add(new OutputGenerator(output, this));
                case RpcEffectiveStatement rpc -> {
                    if (this instanceof ModuleGenerator module) {
                        tmp.add(new RpcGenerator(rpc, module));
                    }
                }
                case TypedefEffectiveStatement typedef -> tmp.add(new TypedefGenerator(typedef, this));
                case AugmentEffectiveStatement augment -> {
                    // FIXME: MDSAL-695: So here we are ignoring any augment which is not in a module, while the 'uses'
                    //                   processing takes care of the rest. There are two problems here:
                    //
                    //                   1) this could be an augment introduced through uses -- in this case we are
                    //                      picking confusing it with this being its declaration site, we should
                    //                      probably be ignoring it, but then
                    //
                    //                   2) we are losing track of AugmentEffectiveStatement for which we do not
                    //                      generate interfaces -- and recover it at runtime through explicit walk along
                    //                      the corresponding AugmentationSchemaNode.getOriginalDefinition() pointer
                    //
                    //                   So here is where we should decide how to handle this augment, and make sure we
                    //                   retain information about this being an alias. That will serve as the base for
                    //                   keys in the augment -> original map we provide to BindingRuntimeTypes.
                    if (this instanceof ModuleGenerator module) {
                        tmpAug.add(new ModuleAugmentGenerator(augment, module));
                    }
                }
                case UsesEffectiveStatement uses -> {
                    for (var usesSub : uses.effectiveSubstatements()) {
                        if (usesSub instanceof AugmentEffectiveStatement usesAug) {
                            tmpAug.add(new UsesAugmentGenerator(usesAug, uses, this));
                        }
                    }
                }
                case YangDataEffectiveStatement yangData -> {
                    if (this instanceof ModuleGenerator moduleGen) {
                        tmp.add(YangDataGenerator.of(yangData, moduleGen));
                    }
                }
                default -> LOG.trace("Ignoring statement {}", stmt);
            }
        }

        // Sort augments and add them last. This ensures child iteration order always reflects potential
        // interdependencies, hence we do not need to worry about them. This is extremely important, as there are a
        // number of places where we would have to either move the logic to parent statement and explicitly filter/sort
        // substatements to establish this order.
        tmpAug.sort(AugmentGenerator.COMPARATOR);
        tmp.addAll(tmpAug);
        return List.copyOf(tmp);
    }

    // Utility equivalent of (!isAddedByUses(stmt) && !isAugmenting(stmt)). Takes advantage of relationship between
    // CopyableNode and AddedByUsesAware
    private static boolean isOriginalDeclaration(final EffectiveStatement<?, ?> stmt) {
        if (stmt instanceof AddedByUsesAware aware
            && (aware.isAddedByUses() || aware instanceof CopyableNode copyable && copyable.isAugmenting())) {
            return false;
        }
        return true;
    }

    private static boolean isAddedByUses(final EffectiveStatement<?, ?> stmt) {
        return stmt instanceof AddedByUsesAware aware && aware.isAddedByUses();
    }

    private static boolean isAugmenting(final EffectiveStatement<?, ?> stmt) {
        return stmt instanceof CopyableNode copyable && copyable.isAugmenting();
    }
}
