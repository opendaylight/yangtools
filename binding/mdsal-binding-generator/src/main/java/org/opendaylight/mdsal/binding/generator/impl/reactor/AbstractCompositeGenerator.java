/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.Enumeration;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
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
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.type.TypeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A composite generator. Composite generators may contain additional children, which end up being mapped into
 * the naming hierarchy 'under' the composite generator. To support this use case, each composite has a Java package
 * name assigned.
 *
 * <p>
 * State tracking for resolution of children to their original declaration, i.e. back along the 'uses' and 'augment'
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
 * <p>
 * In this case we have three manifestations of 'leaf baz' -- marked A, E and F in the child iteration order. In order
 * to perform a resolution, we first have to determine that F is the original definition, then establish that E is using
 * the definition made by F and finally establish that A is using the definition made by F.
 *
 * <p>
 * Dealing with augmentations is harder still, because we need to attach them to the original definition, hence for the
 * /foo/bar container at A, we need to understand that its original definition is at D and we need to attach the augment
 * at B to D. Futhermore we also need to establish that the augmentation at G attaches to container defined in C, so
 * that the 'leaf xyzzy' existing as /foo/bar/xyzzy/xyzzy under C has its original definition at H.
 *
 * <p>
 * Finally realize that the augment at G can actually exist in a different module and is shown in this example only
 * the simplified form. That also means we could encounter G well before 'container foo' as well as we can have multiple
 * such augments sprinkled across multiple modules having the same dependency rules as between C and G -- but they still
 * have to form a directed acyclic graph and we partially deal with those complexities by having modules sorted by their
 * dependencies.
 *
 * <p>
 * For further details see {@link #linkOriginalGenerator()} and {@link #linkOriginalGeneratorRecursive()}, which deal
 * with linking original instances in the tree iteration order. The part dealing with augment attachment lives mostly
 * in {@link AugmentRequirement}.
 */
abstract class AbstractCompositeGenerator<T extends EffectiveStatement<?, ?>> extends AbstractExplicitGenerator<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractCompositeGenerator.class);

    // FIXME: we want to allocate this lazily to lower memory footprint
    private final @NonNull CollisionDomain domain = new CollisionDomain(this);
    private final List<Generator> children;

    /**
     * List of {@code augment} statements targeting this generator. This list is maintained only for the primary
     * incarnation. This list is an evolving entity until after we have finished linkage of original statements. It is
     * expected to be stable at the start of {@code step 2} in {@link GeneratorReactor#execute(TypeBuilderFactory)}.
     */
    private List<AbstractAugmentGenerator> augments = List.of();

    /**
     * List of {@code grouping} statements this statement references. This field is set once by
     * {@link #linkUsesDependencies(GeneratorContext)}.
     */
    private List<GroupingGenerator> groupings;

    /**
     * List of composite children which have not been recursively processed. This may become a mutable list when we
     * have some children which have not completed linking. Once we have completed linking of all children, including
     * {@link #unlinkedChildren}, this will be set to {@code null}.
     */
    private List<AbstractCompositeGenerator<?>> unlinkedComposites = List.of();
    /**
     * List of children which have not had their original linked. This list starts of as null. When we first attempt
     * linkage, it becomes non-null.
     */
    private List<Generator> unlinkedChildren;

    AbstractCompositeGenerator(final T statement) {
        super(statement);
        children = createChildren(statement);
    }

    AbstractCompositeGenerator(final T statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
        children = createChildren(statement);
    }

    @Override
    public final Iterator<Generator> iterator() {
        return children.iterator();
    }

    @Override
    final boolean isEmpty() {
        return children.isEmpty();
    }

    final @Nullable AbstractExplicitGenerator<?> findGenerator(final List<EffectiveStatement<?, ?>> stmtPath) {
        return findGenerator(MatchStrategy.identity(), stmtPath, 0);
    }

    final @Nullable AbstractExplicitGenerator<?> findGenerator(final MatchStrategy childStrategy,
            // TODO: Wouldn't this method be nicer with Deque<EffectiveStatement<?, ?>> ?
            final List<EffectiveStatement<?, ?>> stmtPath, final int offset) {
        final EffectiveStatement<?, ?> stmt = stmtPath.get(offset);

        // Try direct children first, which is simple
        AbstractExplicitGenerator<?> ret = childStrategy.findGenerator(stmt, children);
        if (ret != null) {
            final int next = offset + 1;
            if (stmtPath.size() == next) {
                // Final step, return child
                return ret;
            }
            if (ret instanceof AbstractCompositeGenerator) {
                // We know how to descend down
                return ((AbstractCompositeGenerator<?>) ret).findGenerator(childStrategy, stmtPath, next);
            }
            // Yeah, don't know how to continue here
            return null;
        }

        // At this point we are about to fork for augments or groupings. In either case only schema tree statements can
        // be found this way. The fun part is that if we find a match and need to continue, we will use the same
        // strategy for children as well. We now know that this (and subsequent) statements need to have a QName
        // argument.
        if (stmt instanceof SchemaTreeEffectiveStatement) {
            // grouping -> uses instantiation changes the namespace to the local namespace of the uses site. We are
            // going the opposite direction, hence we are changing namespace from local to the grouping's namespace.
            for (GroupingGenerator gen : groupings) {
                final MatchStrategy strat = MatchStrategy.grouping(gen);
                ret = gen.findGenerator(strat, stmtPath, offset);
                if (ret != null) {
                    return ret;
                }
            }

            // All augments are dead simple: they need to match on argument (which we expect to be a QName)
            final MatchStrategy strat = MatchStrategy.augment();
            for (AbstractAugmentGenerator gen : augments) {
                ret = gen.findGenerator(strat, stmtPath, offset);
                if (ret != null) {
                    return ret;
                }
            }
        }
        return null;
    }

    final @NonNull CollisionDomain domain() {
        return domain;
    }

    final void linkUsesDependencies(final GeneratorContext context) {
        // We are establishing two linkages here:
        // - we are resolving 'uses' statements to their corresponding 'grouping' definitions
        // - we propagate those groupings as anchors to any augment statements, which takes out some amount of guesswork
        //   from augment+uses resolution case, as groupings know about their immediate augments as soon as uses linkage
        //   is resolved
        final List<GroupingGenerator> tmp = new ArrayList<>();
        for (EffectiveStatement<?, ?> stmt : statement().effectiveSubstatements()) {
            if (stmt instanceof UsesEffectiveStatement) {
                final UsesEffectiveStatement uses = (UsesEffectiveStatement) stmt;
                final GroupingGenerator grouping = context.resolveTreeScoped(GroupingGenerator.class, uses.argument());
                tmp.add(grouping);

                // Trigger resolution of uses/augment statements. This looks like guesswork, but there may be multiple
                // 'augment' statements in a 'uses' statement and keeping a ListMultimap here seems wasteful.
                for (Generator gen : this) {
                    if (gen instanceof UsesAugmentGenerator) {
                        ((UsesAugmentGenerator) gen).resolveGrouping(uses, grouping);
                    }
                }
            }
        }
        groupings = List.copyOf(tmp);
    }

    final void startUsesAugmentLinkage(final List<AugmentRequirement> requirements) {
        for (Generator child : children) {
            if (child instanceof UsesAugmentGenerator) {
                requirements.add(((UsesAugmentGenerator) child).startLinkage());
            }
            if (child instanceof AbstractCompositeGenerator) {
                ((AbstractCompositeGenerator<?>) child).startUsesAugmentLinkage(requirements);
            }
        }
    }

    final void addAugment(final AbstractAugmentGenerator augment) {
        if (augments.isEmpty()) {
            augments = new ArrayList<>(2);
        }
        augments.add(requireNonNull(augment));
    }

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
            unlinkedChildren = children.stream()
                .filter(AbstractExplicitGenerator.class::isInstance)
                .map(child -> (AbstractExplicitGenerator<?>) child)
                .collect(Collectors.toList());
        }

        var progress = LinkageProgress.NONE;
        if (!unlinkedChildren.isEmpty()) {
            // Attempt to make progress on child linkage
            final var it = unlinkedChildren.iterator();
            while (it.hasNext()) {
                final var child = it.next();
                if (child instanceof AbstractExplicitGenerator) {
                    if (((AbstractExplicitGenerator<?>) child).linkOriginalGenerator()) {
                        progress = LinkageProgress.SOME;
                        it.remove();

                        // If this is a composite generator we need to process is further
                        if (child instanceof AbstractCompositeGenerator) {
                            if (unlinkedComposites.isEmpty()) {
                                unlinkedComposites = new ArrayList<>();
                            }
                            unlinkedComposites.add((AbstractCompositeGenerator<?>) child);
                        }
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

    @Override
    final AbstractCompositeGenerator<T> getOriginal() {
        return (AbstractCompositeGenerator<T>) super.getOriginal();
    }

    @Override
    final AbstractCompositeGenerator<T> tryOriginal() {
        return (AbstractCompositeGenerator<T>) super.tryOriginal();
    }

    final <S extends EffectiveStatement<?, ?>> @Nullable OriginalLink<S> originalChild(final QName childQName) {
        // First try groupings/augments ...
        var found = findInferredGenerator(childQName);
        if (found != null) {
            return (OriginalLink<S>) OriginalLink.partial(found);
        }

        // ... no luck, we really need to start looking at our origin
        final var prev = previous();
        if (prev != null) {
            final QName prevQName = childQName.bindTo(prev.getQName().getModule());
            found = prev.findSchemaTreeGenerator(prevQName);
            if (found != null) {
                return (OriginalLink<S>) found.originalLink();
            }
        }

        return null;
    }

    @Override
    final AbstractExplicitGenerator<?> findSchemaTreeGenerator(final QName qname) {
        final AbstractExplicitGenerator<?> found = super.findSchemaTreeGenerator(qname);
        return found != null ? found : findInferredGenerator(qname);
    }

    final @Nullable AbstractAugmentGenerator findAugmentForGenerator(final QName qname) {
        for (var augment : augments) {
            final var gen = augment.findSchemaTreeGenerator(qname);
            if (gen != null) {
                return augment;
            }
        }
        return null;
    }

    final @Nullable GroupingGenerator findGroupingForGenerator(final QName qname) {
        for (GroupingGenerator grouping : groupings) {
            final var gen = grouping.findSchemaTreeGenerator(qname.bindTo(grouping.statement().argument().getModule()));
            if (gen != null) {
                return grouping;
            }
        }
        return null;
    }

    private @Nullable AbstractExplicitGenerator<?> findInferredGenerator(final QName qname) {
        // First search our local groupings ...
        for (var grouping : groupings) {
            final var gen = grouping.findSchemaTreeGenerator(qname.bindTo(grouping.statement().argument().getModule()));
            if (gen != null) {
                return gen;
            }
        }
        // ... next try local augments, which may have groupings themselves
        for (var augment : augments) {
            final var gen = augment.findSchemaTreeGenerator(qname);
            if (gen != null) {
                return gen;
            }
        }
        return null;
    }

    /**
     * Update the specified builder to implement interfaces generated for the {@code grouping} statements this generator
     * is using.
     *
     * @param builder Target builder
     * @param builderFactory factory for creating {@link TypeBuilder}s
     * @return The number of groupings this type uses.
     */
    final int addUsesInterfaces(final GeneratedTypeBuilder builder, final TypeBuilderFactory builderFactory) {
        for (GroupingGenerator grp : groupings) {
            builder.addImplementsType(grp.getGeneratedType(builderFactory));
        }
        return groupings.size();
    }

    static final void addAugmentable(final GeneratedTypeBuilder builder) {
        builder.addImplementsType(BindingTypes.augmentable(builder));
    }

    final void addGetterMethods(final GeneratedTypeBuilder builder, final TypeBuilderFactory builderFactory) {
        for (Generator child : this) {
            // Only process explicit generators here
            if (child instanceof AbstractExplicitGenerator) {
                ((AbstractExplicitGenerator<?>) child).addAsGetterMethod(builder, builderFactory);
            }

            final GeneratedType enclosedType = child.enclosedType(builderFactory);
            if (enclosedType instanceof GeneratedTransferObject) {
                builder.addEnclosingTransferObject((GeneratedTransferObject) enclosedType);
            } else if (enclosedType instanceof Enumeration) {
                builder.addEnumeration((Enumeration) enclosedType);
            } else {
                verify(enclosedType == null, "Unhandled enclosed type %s in %s", enclosedType, child);
            }
        }
    }

    private List<Generator> createChildren(final EffectiveStatement<?, ?> statement) {
        final List<Generator> tmp = new ArrayList<>();
        final List<AbstractAugmentGenerator> tmpAug = new ArrayList<>();

        for (EffectiveStatement<?, ?> stmt : statement.effectiveSubstatements()) {
            if (stmt instanceof ActionEffectiveStatement) {
                if (!isAugmenting(stmt)) {
                    tmp.add(new ActionGenerator((ActionEffectiveStatement) stmt, this));
                }
            } else if (stmt instanceof AnydataEffectiveStatement) {
                if (!isAugmenting(stmt)) {
                    tmp.add(new OpaqueObjectGenerator<>((AnydataEffectiveStatement) stmt, this));
                }
            } else if (stmt instanceof AnyxmlEffectiveStatement) {
                if (!isAugmenting(stmt)) {
                    tmp.add(new OpaqueObjectGenerator<>((AnyxmlEffectiveStatement) stmt, this));
                }
            } else if (stmt instanceof CaseEffectiveStatement) {
                tmp.add(new CaseGenerator((CaseEffectiveStatement) stmt, this));
            } else if (stmt instanceof ChoiceEffectiveStatement) {
                // FIXME: use isOriginalDeclaration() ?
                if (!isAddedByUses(stmt)) {
                    tmp.add(new ChoiceGenerator((ChoiceEffectiveStatement) stmt, this));
                }
            } else if (stmt instanceof ContainerEffectiveStatement) {
                if (isOriginalDeclaration(stmt)) {
                    tmp.add(new ContainerGenerator((ContainerEffectiveStatement) stmt, this));
                }
            } else if (stmt instanceof GroupingEffectiveStatement) {
                tmp.add(new GroupingGenerator((GroupingEffectiveStatement) stmt, this));
            } else if (stmt instanceof IdentityEffectiveStatement) {
                tmp.add(new IdentityGenerator((IdentityEffectiveStatement) stmt, this));
            } else if (stmt instanceof InputEffectiveStatement) {
                // FIXME: do not generate legacy RPC layout
                tmp.add(this instanceof RpcGenerator ? new RpcContainerGenerator((InputEffectiveStatement) stmt, this)
                    : new OperationContainerGenerator((InputEffectiveStatement) stmt, this));
            } else if (stmt instanceof LeafEffectiveStatement) {
                if (!isAugmenting(stmt)) {
                    tmp.add(new LeafGenerator((LeafEffectiveStatement) stmt, this));
                }
            } else if (stmt instanceof LeafListEffectiveStatement) {
                if (!isAugmenting(stmt)) {
                    tmp.add(new LeafListGenerator((LeafListEffectiveStatement) stmt, this));
                }
            } else if (stmt instanceof ListEffectiveStatement) {
                if (isOriginalDeclaration(stmt)) {
                    final ListGenerator listGen = new ListGenerator((ListEffectiveStatement) stmt, this);
                    tmp.add(listGen);

                    final KeyGenerator keyGen = listGen.keyGenerator();
                    if (keyGen != null) {
                        tmp.add(keyGen);
                    }
                }
            } else if (stmt instanceof NotificationEffectiveStatement) {
                if (!isAugmenting(stmt)) {
                    tmp.add(new NotificationGenerator((NotificationEffectiveStatement) stmt, this));
                }
            } else if (stmt instanceof OutputEffectiveStatement) {
                // FIXME: do not generate legacy RPC layout
                tmp.add(this instanceof RpcGenerator ? new RpcContainerGenerator((OutputEffectiveStatement) stmt, this)
                    : new OperationContainerGenerator((OutputEffectiveStatement) stmt, this));
            } else if (stmt instanceof RpcEffectiveStatement) {
                tmp.add(new RpcGenerator((RpcEffectiveStatement) stmt, this));
            } else if (stmt instanceof TypedefEffectiveStatement) {
                tmp.add(new TypedefGenerator((TypedefEffectiveStatement) stmt, this));
            } else if (stmt instanceof AugmentEffectiveStatement) {
                // FIXME: MDSAL-695: So here we are ignoring any augment which is not in a module, while the 'uses'
                //                   processing takes care of the rest. There are two problems here:
                //
                //                   1) this could be an augment introduced through uses -- in this case we are picking
                //                      confusing it with this being its declaration site, we should probably be
                //                      ignoring it, but then
                //
                //                   2) we are losing track of AugmentEffectiveStatement for which we do not generate
                //                      interfaces -- and recover it at runtime through explicit walk along the
                //                      corresponding AugmentationSchemaNode.getOriginalDefinition() pointer
                //
                //                   So here is where we should decide how to handle this augment, and make sure we
                //                   retain information about this being an alias. That will serve as the base for keys
                //                   in the augment -> original map we provide to BindingRuntimeTypes.
                if (this instanceof ModuleGenerator) {
                    tmpAug.add(new ModuleAugmentGenerator((AugmentEffectiveStatement) stmt, this));
                }
            } else if (stmt instanceof UsesEffectiveStatement) {
                final UsesEffectiveStatement uses = (UsesEffectiveStatement) stmt;
                for (EffectiveStatement<?, ?> usesSub : uses.effectiveSubstatements()) {
                    if (usesSub instanceof AugmentEffectiveStatement) {
                        tmpAug.add(new UsesAugmentGenerator((AugmentEffectiveStatement) usesSub, uses, this));
                    }
                }
            } else {
                LOG.trace("Ignoring statement {}", stmt);
            }
        }

        // Sort augments and add them last. This ensures child iteration order always reflects potential
        // interdependencies, hence we do not need to worry about them. This is extremely important, as there are a
        // number of places where we would have to either move the logic to parent statement and explicitly filter/sort
        // substatements to establish this order.
        tmpAug.sort(AbstractAugmentGenerator.COMPARATOR);
        tmp.addAll(tmpAug);

        // Compatibility FooService and FooListener interfaces, only generated for modules.
        if (this instanceof ModuleGenerator) {
            final ModuleGenerator moduleGen = (ModuleGenerator) this;

            final List<NotificationGenerator> notifs = tmp.stream()
                .filter(NotificationGenerator.class::isInstance)
                .map(NotificationGenerator.class::cast)
                .collect(Collectors.toUnmodifiableList());
            if (!notifs.isEmpty()) {
                tmp.add(new NotificationServiceGenerator(moduleGen, notifs));
            }

            final List<RpcGenerator> rpcs = tmp.stream()
                .filter(RpcGenerator.class::isInstance)
                .map(RpcGenerator.class::cast)
                .collect(Collectors.toUnmodifiableList());
            if (!rpcs.isEmpty()) {
                tmp.add(new RpcServiceGenerator(moduleGen, rpcs));
            }
        }

        return List.copyOf(tmp);
    }

    // Utility equivalent of (!isAddedByUses(stmt) && !isAugmenting(stmt)). Takes advantage of relationship between
    // CopyableNode and AddedByUsesAware
    private static boolean isOriginalDeclaration(final EffectiveStatement<?, ?> stmt) {
        if (stmt instanceof AddedByUsesAware) {
            if (((AddedByUsesAware) stmt).isAddedByUses()
                || stmt instanceof CopyableNode && ((CopyableNode) stmt).isAugmenting()) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAddedByUses(final EffectiveStatement<?, ?> stmt) {
        return stmt instanceof AddedByUsesAware && ((AddedByUsesAware) stmt).isAddedByUses();
    }

    private static boolean isAugmenting(final EffectiveStatement<?, ?> stmt) {
        return stmt instanceof CopyableNode && ((CopyableNode) stmt).isAugmenting();
    }
}
