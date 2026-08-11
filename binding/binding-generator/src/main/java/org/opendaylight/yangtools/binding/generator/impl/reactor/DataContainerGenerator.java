/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.base.VerifyException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.ChildOf;
import org.opendaylight.yangtools.binding.model.Archetype;
import org.opendaylight.yangtools.binding.model.GetterMethod;
import org.opendaylight.yangtools.binding.model.GroupingArchetype;
import org.opendaylight.yangtools.binding.model.Type;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.TypeObjectArchetype;
import org.opendaylight.yangtools.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesEffectiveStatement;


public abstract sealed class DataContainerGenerator<S extends EffectiveStatement<?, ?>, R extends CompositeRuntimeType>
        extends CompositeGenerator<S, R>
        permits AugmentGenerator, AugmentableGenerator, GroupingGenerator, ModuleGenerator, YangDataGenerator,
                // FIXME: YANGTOOLS-1935: not this one
                NotificationBodyGenerator,
                // FIXME: YANGTOOLS-1934: not these two
                ChoiceGenerator, OperationGenerator {
    /**
     * List of {@code augment} statements targeting this generator. This list is maintained only for the primary
     * incarnation. This list is an evolving entity until after we have finished linkage of original statements. It is
     * expected to be stable at the start of {@code step 2} in {@link GeneratorReactor#execute()}.
     */
    private @NonNull List<AugmentGenerator> augments = List.of();

    /**
     * List of {@code grouping} statements this statement references. This field is set once by
     * {@link #linkUsesDependencies(GeneratorContext)}.
     */
    private List<GroupingGenerator> groupings;

    @NonNullByDefault
    DataContainerGenerator(final S statement) {
        super(statement);
    }

    @NonNullByDefault
    DataContainerGenerator(final S statement, final CompositeGenerator<?, ?> parent) {
        super(statement, parent);
    }

    // FIXME: this should be part AugmentTargetGenerator
    final @NonNull List<AugmentGenerator> augments() {
        return augments;
    }

    final @NonNull List<GroupingGenerator> groupings() {
        final var ret = groupings;
        if (ret != null) {
            return ret;
        }
        throw new VerifyException("Groupings not initialized in " + this);
    }

    @Override
    final R createExternalRuntimeType(final Type type) {
        return createBuilder(statement()).populate(new AugmentResolver(), this).build(verifyGeneratedType(type));
    }

    abstract @NonNull CompositeRuntimeTypeBuilder<S, R> createBuilder(@NonNull S statement);

    // FIXME: this should be reworked with AugmentTargetGenerator in mind
    @Override
    final R createInternalRuntimeType(final AugmentResolver resolver, final S statement, final Type type) {
        return createBuilder(statement).populate(resolver, this).build(verifyGeneratedType(type));
    }

    // FIXME: this should be reworked with AugmentTargetGenerator in mind
    final @Nullable AbstractExplicitGenerator<?, ?> findGenerator(final List<EffectiveStatement<?, ?>> stmtPath) {
        return findGenerator(MatchStrategy.identity(), stmtPath, 0);
    }

    final @Nullable AbstractExplicitGenerator<?, ?> findGenerator(final MatchStrategy childStrategy,
            // TODO: Wouldn't this method be nicer with Deque<EffectiveStatement<?, ?>> ?
            final List<EffectiveStatement<?, ?>> stmtPath, final int offset) {
        final var stmt = stmtPath.get(offset);

        // Try direct children first, which is simple
        var ret = childStrategy.findGenerator(stmt, this);
        if (ret != null) {
            final int next = offset + 1;
            if (stmtPath.size() == next) {
                // Final step, return child
                return ret;
            }
            if (ret instanceof DataContainerGenerator<?, ?> composite) {
                // We know how to descend down
                return composite.findGenerator(childStrategy, stmtPath, next);
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
            for (var grouping : groupings) {
                final var strat = MatchStrategy.grouping(grouping);
                ret = grouping.findGenerator(strat, stmtPath, offset);
                if (ret != null) {
                    return ret;
                }
            }

            // All augments are dead simple: they need to match on argument (which we expect to be a QName)
            final var strat = MatchStrategy.augment();
            for (var augment : augments) {
                ret = augment.findGenerator(strat, stmtPath, offset);
                if (ret != null) {
                    return ret;
                }
            }
        }
        return null;
    }

    final void linkUsesDependencies(final GeneratorContext context) {
        // We are establishing two linkages here:
        // - we are resolving 'uses' statements to their corresponding 'grouping' definitions
        // - we propagate those groupings as anchors to any augment statements, which takes out some amount of guesswork
        //   from augment+uses resolution case, as groupings know about their immediate augments as soon as uses linkage
        //   is resolved
        final var tmp = new ArrayList<GroupingGenerator>();
        for (var stmt : statement().effectiveSubstatements()) {
            if (stmt instanceof UsesEffectiveStatement uses) {
                final var grouping = context.resolveTreeScoped(GroupingGenerator.class, uses.argument());
                tmp.add(grouping);

                // Trigger resolution of uses/augment statements. This looks like guesswork, but there may be multiple
                // 'augment' statements in a 'uses' statement and keeping a ListMultimap here seems wasteful.
                for (var gen : this) {
                    if (gen instanceof UsesAugmentGenerator usesGen) {
                        usesGen.resolveGrouping(uses, grouping);
                    }
                }
            }
        }
        groupings = List.copyOf(tmp);
    }

    // Iterate over a some generators recursively, linking them to the GroupingGenerators they use. GroupingGenerators
    // are skipped and added to unprocessedGroupings for later processing.
    final void linkUsedGroupings(final Set<GroupingGenerator> skippedChildren) {
        // Link to used groupings IFF we have a corresponding generated Java class
        switch (classPlacement()) {
            case NONE, PHANTOM -> {
                // No-op
            }
            default -> {
                for (var grouping : groupings()) {
                    grouping.addUser(this);
                }
            }
        }

        for (var child : this) {
            switch (child) {
                case GroupingGenerator grouping -> skippedChildren.add(grouping);
                case DataContainerGenerator<?, ?> composite -> composite.linkUsedGroupings(skippedChildren);
                default -> {
                    // no-op
                }
            }
        }
    }

    final void startUsesAugmentLinkage(final List<AugmentRequirement> requirements) {
        for (var child : this) {
            if (child instanceof UsesAugmentGenerator uses) {
                requirements.add(uses.startLinkage());
            }
            if (child instanceof DataContainerGenerator<?, ?> composite) {
                composite.startUsesAugmentLinkage(requirements);
            }
        }
    }

    @Override
    final void addAugment(final AugmentGenerator augment) {
        if (augments.isEmpty()) {
            augments = new ArrayList<>(2);
        }
        augments.add(requireNonNull(augment));
    }

    @Override
    final @Nullable AugmentGenerator findAugmentByStatement(final AugmentEffectiveStatement statement) {
        for (var augment : augments) {
            if (augment.matchesInstantiated(statement)) {
                return augment;
            }
        }
        for (var grouping : groupings()) {
            final var found = grouping.findAugmentByStatement(statement);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    @Override
    final @Nullable AugmentGenerator findAugmentForGenerator(final QName qname) {
        for (var augment : augments) {
            final var gen = augment.findSchemaTreeGenerator(qname);
            if (gen != null) {
                return augment;
            }
        }
        return null;
    }

    @Override
    final @Nullable GroupingGenerator findGroupingForGenerator(final QName qname) {
        for (var grouping : groupings) {
            final var gen = grouping.findSchemaTreeGenerator(qname.bindTo(grouping.statement().argument().getModule()));
            if (gen != null) {
                return grouping;
            }
        }
        return null;
    }

    @Override
    final @Nullable AbstractExplicitGenerator<?, ?> findInferredGenerator(final QName qname) {
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

    @Override
    final Archetype createTypeImpl() {
        return createTypeImpl(typeName(), statement(), groupings.isEmpty() ? List.of()
            : groupings.stream().map(GroupingGenerator::getGeneratedType).toList());
    }

    @NonNullByDefault
    abstract Archetype createTypeImpl(TypeName typeName, @NonNull S statement, List<GroupingArchetype> groupings);

    @NonNullByDefault
    final List<GetterMethod> collectGetters() {
        final var list = new ArrayList<GetterMethod>();

        for (var child : this) {
            // Only process explicit generators here
            if (child instanceof AbstractExplicitGenerator<?, ?> explicit) {
                final var getter = explicit.asGetterMethod();
                if (getter != null) {
                    list.add(getter);
                }
            }
        }

        return List.copyOf(list);
    }

    @NonNullByDefault
    final List<TypeObjectArchetype<?>> collectTypeObjects() {
        final var list = new ArrayList<TypeObjectArchetype<?>>();

        for (var child : this) {
            final var enclosedType = child.enclosedType();
            switch (enclosedType) {
                case TypeObjectArchetype<?> typeObject -> list.add(typeObject);
                case null -> {
                    // No-op
                }
                default -> throw new VerifyException("Unhandled enclosed type %s in %s".formatted(enclosedType, child));
            }
        }

        return List.copyOf(list);
    }

    /**
     * {@return the {@link TypeName} to use as the {@code P} parameter of {@link ChildOf}}
     */
    @NonNullByDefault
    final TypeName parentNameForChildOf() {
        var ancestor = getParent();
        while (true) {
            // choice/case hierarchy does not factor into 'ChildOf' hierarchy, hence we need to skip them
            if (ancestor instanceof CaseGenerator || ancestor instanceof ChoiceGenerator) {
                ancestor = ancestor.getParent();
                continue;
            }

            // if we into a choice we need to follow the hierararchy of that choice
            if (ancestor instanceof AugmentGenerator augment
                && augment.targetGenerator() instanceof ChoiceGenerator targetChoice) {
                ancestor = targetChoice;
                continue;
            }

            return ancestor.typeName();
        }
    }
}
