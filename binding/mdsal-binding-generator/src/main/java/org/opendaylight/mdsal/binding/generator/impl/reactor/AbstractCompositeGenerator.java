/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
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
import org.opendaylight.yangtools.yang.common.QNameModule;
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
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
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
 */
abstract class AbstractCompositeGenerator<T extends EffectiveStatement<?, ?>> extends AbstractExplicitGenerator<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractCompositeGenerator.class);

    private final @NonNull CollisionDomain domain = new CollisionDomain(this);
    private final List<Generator> children;

    private List<AbstractAugmentGenerator> augments = List.of();
    private List<GroupingGenerator> groupings;

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
        // We are resolving 'uses' statements to their corresponding 'grouping' definitions
        final List<GroupingGenerator> tmp = new ArrayList<>();
        for (EffectiveStatement<?, ?> stmt : statement().effectiveSubstatements()) {
            if (stmt instanceof UsesEffectiveStatement) {
                tmp.add(context.resolveTreeScoped(GroupingGenerator.class, ((UsesEffectiveStatement) stmt).argument()));
            }
        }
        groupings = List.copyOf(tmp);
    }

    final void addAugment(final AbstractAugmentGenerator augment) {
        if (augments.isEmpty()) {
            augments = new ArrayList<>(2);
        }
        augments.add(requireNonNull(augment));
    }

    @Override
    final AbstractCompositeGenerator<?> getOriginal() {
        return (AbstractCompositeGenerator<?>) super.getOriginal();
    }

    final @NonNull AbstractExplicitGenerator<?> getOriginalChild(final QName childQName) {
        // First try groupings/augments ...
        final AbstractExplicitGenerator<?> found = findInferredGenerator(childQName);
        if (found != null) {
            return found;
        }

        // ... no luck, we really need to start looking at our origin
        final AbstractExplicitGenerator<?> prev = verifyNotNull(previous(),
            "Failed to find %s in scope of %s", childQName, this);

        final QName prevQName = childQName.bindTo(prev.getQName().getModule());
        return verifyNotNull(prev.findSchemaTreeGenerator(prevQName),
            "Failed to find child %s (proxy for %s) in %s", prevQName, childQName, prev).getOriginal();
    }

    @Override
    final AbstractExplicitGenerator<?> findSchemaTreeGenerator(final QName qname) {
        final AbstractExplicitGenerator<?> found = super.findSchemaTreeGenerator(qname);
        return found != null ? found : findInferredGenerator(qname);
    }

    private @Nullable AbstractExplicitGenerator<?> findInferredGenerator(final QName qname) {
        // First search our local groupings ...
        for (GroupingGenerator grouping : groupings) {
            final AbstractExplicitGenerator<?> gen = grouping.findSchemaTreeGenerator(
                qname.bindTo(grouping.statement().argument().getModule()));
            if (gen != null) {
                return gen;
            }
        }
        // ... next try local augments, which may have groupings themselves
        for (AbstractAugmentGenerator augment : augments) {
            final AbstractExplicitGenerator<?> gen = augment.findSchemaTreeGenerator(qname);
            if (gen != null) {
                return gen;
            }
        }
        return null;
    }

    final @NonNull AbstractExplicitGenerator<?> resolveSchemaNode(final SchemaNodeIdentifier path) {
        // This is not quite straightforward. 'path' works on top of schema tree, which is instantiated view. Since we
        // do not generate duplicate instantiations along 'uses' path, findSchemaTreeGenerator() would satisfy our
        // request by returning a child of the source 'grouping'.
        //
        // When that happens, our subsequent lookups need to adjust the namespace being looked up to the grouping's
        // namespace... except for the case when the step is actually an augmentation, in which case we must not make
        // that adjustment.
        //
        // Hence we deal with this lookup recursively, dropping namespace hints when we cross into groupings.
        return resolveSchemaNode(path.getNodeIdentifiers().iterator(), null);
    }

    private @NonNull AbstractExplicitGenerator<?> resolveSchemaNode(final Iterator<QName> qnames,
            final @Nullable QNameModule localNamespace) {
        final QName qname = qnames.next();

        // First try local augments, as those are guaranteed to match namespace exactly
        for (AbstractAugmentGenerator augment : augments) {
            final AbstractExplicitGenerator<?> gen = augment.findSchemaTreeGenerator(qname);
            if (gen != null) {
                return resolveNext(gen, qnames, null);
            }
        }

        // Second try local groupings, as those perform their own adjustment
        for (GroupingGenerator grouping : groupings) {
            final QNameModule ns = grouping.statement().argument().getModule();
            final AbstractExplicitGenerator<?> gen = grouping.findSchemaTreeGenerator(qname.bindTo(ns));
            if (gen != null) {
                return resolveNext(gen, qnames, ns);
            }
        }

        // Lastly try local statements adjusted with namespace, if applicable
        final QName lookup = localNamespace == null ? qname : qname.bindTo(localNamespace);
        final AbstractExplicitGenerator<?> gen = verifyNotNull(super.findSchemaTreeGenerator(lookup),
            "Failed to find %s as %s in %s", qname, lookup, this);
        return resolveNext(gen, qnames, localNamespace);
    }

    private static @NonNull AbstractExplicitGenerator<?> resolveNext(final @NonNull AbstractExplicitGenerator<?> gen,
            final Iterator<QName> qnames, final QNameModule localNamespace) {
        if (qnames.hasNext()) {
            verify(gen instanceof AbstractCompositeGenerator, "Unexpected generator %s", gen);
            return ((AbstractCompositeGenerator<?>) gen).resolveSchemaNode(qnames, localNamespace);
        }
        return gen;
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
                        tmpAug.add(new UsesAugmentGenerator((AugmentEffectiveStatement) usesSub, this));
                    }
                }
            } else {
                LOG.trace("Ignoring statement {}", stmt);
                continue;
            }
        }

        // Sort augments and add them last. This ensures child iteration order always reflects potential
        // interdependencies, hence we do not need to worry about them.
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
