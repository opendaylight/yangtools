/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.CaseRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;

abstract class CompositeRuntimeTypeBuilder<S extends EffectiveStatement<?, ?>, R extends CompositeRuntimeType> {
    private final List<AugmentRuntimeType> augmentTypes = new ArrayList<>();
    private final List<RuntimeType> childTypes = new ArrayList<>();
    private final @NonNull S statement;

    CompositeRuntimeTypeBuilder(final S statement) {
        this.statement = requireNonNull(statement);
    }

    final CompositeRuntimeTypeBuilder<S, R> populate(final AugmentResolver resolver,
            final AbstractCompositeGenerator<S, R> generator) {
        resolver.enter(generator);
        try {
            processGenerator(resolver, generator);
        } finally {
            resolver.exit();
        }
        return this;
    }

    final @NonNull R build(final @NonNull GeneratedType generatedType) {
        return build(generatedType, statement, childTypes, augmentTypes);
    }

    abstract @NonNull R build(GeneratedType type, S statement, List<RuntimeType> children,
        List<AugmentRuntimeType> augments);

    final @NonNull List<CaseRuntimeType> getCaseChilden() {
        return childTypes.stream()
            .map(child -> {
                verify(child instanceof CaseRuntimeType, "Unexpected child %s in %s", child, statement);
                return (CaseRuntimeType) child;
            })
            .collect(Collectors.toUnmodifiableList());
    }

    final @NonNull S statement() {
        return statement;
    }

    boolean isAugmentedChild(final QName qname) {
        // Note we are dealing with two different kinds of augments and they behave differently with respect
        // to namespaces. Top-level augments do not make an adjustment, while uses-augments do.
        for (var augment : augmentTypes) {
            if (augment.schemaTreeChild(qname) != null) {
                return true;
            }
        }
        return false;
    }

    void processAugment(final AugmentResolver resolver, final AbstractAugmentGenerator augment) {
        augmentTypes.add(augment.runtimeTypeIn(resolver, statement));
    }

    private void processGenerator(final AugmentResolver resolver, final AbstractCompositeGenerator<S, R> generator) {
        // Figure out which augments are valid in target statement and record their RuntimeTypes.
        // We will pass the latter to create method. We will use the former to perform replacement lookups instead
        // of 'this.augments'. That is necessary because 'this.augments' holds all augments targeting the GeneratedType,
        // hence equivalent augmentations from differing places would match our lookup and the reverse search would be
        // lead astray.
        //
        // Note we should not do this for 'module' and 'uses' statements, as those are not valid augment targets. Of
        // those two we only generate things for 'module'.
        if (!(statement instanceof ModuleEffectiveStatement)) {
            for (var stmt : statement.effectiveSubstatements()) {
                if (stmt instanceof AugmentEffectiveStatement augment) {
                    processAugment(resolver, resolver.getAugment(augment));
                }
            }
        }

        // Now construct RuntimeTypes for each schema tree child of stmt
        for (var stmt : statement.effectiveSubstatements()) {
            if (stmt instanceof SchemaTreeEffectiveStatement<?> child) {
                // Try valid augments first: they should be empty most of the time and filter all the cases where we
                // would not find the streamChild among our local and grouping statements. Note that unlike all others,
                // such matches are not considered to be children in Binding DataObject tree, they are only considered
                // such in the schema tree.
                //
                // That is in general -- 'choice' statements are doing their own thing separately.
                if (!isAugmentedChild(child.argument())) {
                    final var childGen = verifyNotNull(findChildGenerator(generator, child.argument().getLocalName()),
                        "Cannot find child for %s in %s", child, generator);
                    final var childRuntimeType = childGen.createInternalRuntimeType(resolver, child);
                    if (childRuntimeType != null) {
                        childTypes.add(childRuntimeType);
                    }
                }
            }
        }
    }

    // When we reach here we have dealt with all known augments in this scope, hence the only option is that the
    // statement is either local or added via 'uses' -- in either case it has namespace equal to whatever the local
    // namespace is and there can be no conflicts on QName.getLocalName(). That simplifies things a ton.
    private static <S extends SchemaTreeEffectiveStatement<?>> AbstractExplicitGenerator<S, ?> findChildGenerator(
            final AbstractCompositeGenerator<?, ?> parent, final String localName) {
        // Search direct children first ...
        for (var child : parent) {
            if (child instanceof AbstractExplicitGenerator) {
                @SuppressWarnings("unchecked")
                final AbstractExplicitGenerator<S, ?> gen = (AbstractExplicitGenerator<S, ?>) child;
                final EffectiveStatement<?, ?> genStmt = gen.statement();
                if (genStmt instanceof SchemaTreeEffectiveStatement<?> schemaStmt
                    && localName.equals(schemaStmt.argument().getLocalName())) {
                    return gen;
                }
            }
        }

        // ... and groupings recursively last
        for (var grouping : parent.groupings()) {
            final AbstractExplicitGenerator<S, ?> found = findChildGenerator(grouping, localName);
            if (found != null) {
                return found;
            }
        }

        return null;
    }
}
