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

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.generator.impl.reactor.CollisionDomain.Member;
import org.opendaylight.mdsal.binding.generator.impl.rt.DefaultAugmentRuntimeType;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.YangSourceDefinition;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.CaseRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.odlext.model.api.AugmentIdentifierEffectiveStatement;
import org.opendaylight.yangtools.yang.common.AbstractQName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * A generator corresponding to a {@code augment} statement. This class is further specialized for the two distinct uses
 * an augment is used.
 */
abstract class AbstractAugmentGenerator
        extends AbstractCompositeGenerator<AugmentEffectiveStatement, AugmentRuntimeType> {
    /**
     * Comparator comparing target path length. This is useful for quickly determining order the order in which two
     * (or more) {@link AbstractAugmentGenerator}s need to be evaluated. This is necessary when augments are layered on
     * top of each other:
     *
     * <p>
     * <pre>
     *   <code>
     *     container foo;
     *
     *     augment /foo/bar {
     *       container baz;
     *     }
     *
     *     augment /foo {
     *       container bar;
     *     }
     *   </code>
     * </pre>
     *
     * <p>
     * Evaluating these in the order of increasing argument component count solves this without having to perform a full
     * analysis.
     *
     * <p>
     * Another problem we are solving here is augmentation target stability, as the declared order in YANG text may
     * change, which does not really change the semantics. If we only relied on length of argument, such a move would
     * result in changing the results of {@link #createMember(CollisionDomain)} and make upgrades rather unpredictable.
     * We solve this by using {@link QName#compareTo(QName)} to determine order.
     */
    static final Comparator<? super AbstractAugmentGenerator> COMPARATOR = (o1, o2) -> {
        final Iterator<QName> thisIt = o1.statement().argument().getNodeIdentifiers().iterator();
        final Iterator<QName> otherIt = o2.statement().argument().getNodeIdentifiers().iterator();

        while (thisIt.hasNext()) {
            if (!otherIt.hasNext()) {
                return 1;
            }

            final int comp = thisIt.next().compareTo(otherIt.next());
            if (comp != 0) {
                return comp;
            }
        }

        return otherIt.hasNext() ? -1 : 0;
    };

    private AbstractCompositeGenerator<?, ?> targetGen;

    AbstractAugmentGenerator(final AugmentEffectiveStatement statement, final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    final StatementNamespace namespace() {
        return StatementNamespace.AUGMENT;
    }

    @Override
    final void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterSchemaTree(statement().argument());
    }

    @Override
    final AbstractQName localName() {
        throw new UnsupportedOperationException();
    }

    @Override
    ClassPlacement classPlacement() {
        // if the target is a choice we are NOT creating an explicit augmentation, but we still need a phantom to
        // reserve the appropriate package name
        final AbstractCompositeGenerator<?, ?> target = targetGenerator();
        return target instanceof ChoiceGenerator ? ClassPlacement.PHANTOM : super.classPlacement();
    }

    @Override
    final Member createMember(final CollisionDomain domain) {
        final AbstractQName explicitIdentifier = statement()
            .findFirstEffectiveSubstatementArgument(AugmentIdentifierEffectiveStatement.class).orElse(null);
        if (explicitIdentifier != null) {
            return domain.addPrimary(this, new CamelCaseNamingStrategy(StatementNamespace.AUGMENT, explicitIdentifier));
        }

        final Member target = targetGenerator().getMember();
        int offset = 1;
        for (Generator gen : getParent()) {
            if (gen == this) {
                break;
            }
            if (gen instanceof AbstractAugmentGenerator aug && target.equalRoot(aug.targetGenerator().getMember())) {
                offset++;
            }
        }

        return domain.addSecondary(this, target, String.valueOf(offset), statement().argument());
    }

    @Override
    final GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final GeneratedTypeBuilder builder = builderFactory.newGeneratedTypeBuilder(typeName());

        YangSourceDefinition.of(currentModule().statement(), statement()).ifPresent(builder::setYangSourceDefinition);
        builder.addImplementsType(BindingTypes.augmentation(targetGenerator().getGeneratedType(builderFactory)));
        addUsesInterfaces(builder, builderFactory);
        addConcreteInterfaceMethods(builder);

        addGetterMethods(builder, builderFactory);
        annotateDeprecatedIfNecessary(builder);

        return builder.build();
    }

    boolean matchesInstantiated(final AugmentEffectiveStatement statement) {
        return statement().equals(statement);
    }

    final void fillRuntimeCasesIn(final AugmentResolver resolver, final ChoiceEffectiveStatement stmt,
            final List<CaseRuntimeType> toList) {
        toList.addAll(createBuilder(effectiveIn(stmt)).populate(resolver, this).getCaseChilden());
    }

    final @NonNull AugmentRuntimeType runtimeTypeIn(final AugmentResolver resolver,
            final EffectiveStatement<?, ?> stmt) {
        verify(stmt instanceof SchemaTreeAwareEffectiveStatement, "Unexpected target statement %s", stmt);
        return verifyNotNull(createInternalRuntimeType(resolver,
            effectiveIn((SchemaTreeAwareEffectiveStatement<?, ?>) stmt)));
    }

    abstract @NonNull TargetAugmentEffectiveStatement effectiveIn(SchemaTreeAwareEffectiveStatement<?, ?> target);

    final @NonNull TargetAugmentEffectiveStatement effectiveIn(final SchemaTreeAwareEffectiveStatement<?, ?> target,
            final Function<QName, QName> transform) {
        final var augment = statement();
        final var stmts = augment.effectiveSubstatements();
        final var builder = ImmutableList.<EffectiveStatement<?, ?>>builderWithExpectedSize(stmts.size());
        for (var child : stmts) {
            if (child instanceof SchemaTreeEffectiveStatement<?> schemaTreeChild) {
                final var qname = schemaTreeChild.argument();
                // Note: a match in target may be missing -- for example if it was 'deviate unsupported'
                target.findSchemaTreeNode(transform.apply(qname)).ifPresent(builder::add);
            } else {
                builder.add(child);
            }
        }
        return new TargetAugmentEffectiveStatement(augment, target, builder.build());
    }

    @Override
    final void addAsGetterMethod(final GeneratedTypeBuilderBase<?> builder, final TypeBuilderFactory builderFactory) {
        // Augments are never added as getters, as they are handled via Augmentable mechanics
    }

    @Override
    CompositeRuntimeTypeBuilder<AugmentEffectiveStatement, AugmentRuntimeType> createBuilder(
            final AugmentEffectiveStatement statement) {
        return new CompositeRuntimeTypeBuilder<>(statement) {
            @Override
            AugmentRuntimeType build(final GeneratedType type, final AugmentEffectiveStatement statement,
                    final List<RuntimeType> children, final List<AugmentRuntimeType> augments) {
                // 'augment' cannot be targeted by augment
                verify(augments.isEmpty(), "Unexpected augments %s", augments);
                return new DefaultAugmentRuntimeType(type, statement, children);
            }
        };
    }

    final void setTargetGenerator(final AbstractCompositeGenerator<?, ?> targetGenerator) {
        verify(targetGen == null, "Attempted to relink %s, already have target %s", this, targetGen);
        targetGen = requireNonNull(targetGenerator);
    }

    final @NonNull AbstractCompositeGenerator<?, ?> targetGenerator() {
        return verifyNotNull(targetGen, "No target for %s", this);
    }
}
