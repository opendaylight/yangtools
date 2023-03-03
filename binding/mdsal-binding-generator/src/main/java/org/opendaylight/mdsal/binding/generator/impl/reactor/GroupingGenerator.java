/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import com.google.common.base.VerifyException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.opendaylight.mdsal.binding.generator.impl.rt.DefaultGroupingRuntimeType;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.GroupingRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.binding.contract.StatementNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code grouping} statement.
 */
final class GroupingGenerator extends AbstractCompositeGenerator<GroupingEffectiveStatement, GroupingRuntimeType> {
    // Linkage towards concrete data tree instantiations of this grouping. This can contain two different kinds of
    // generators:
    // - GroupingGenerators which provide next step in the linkage
    // - other composite generators, which are the actual instantiations
    private List<AbstractCompositeGenerator<?, ?>> users;

    GroupingGenerator(final GroupingEffectiveStatement statement, final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent);
    }

    void addUser(final AbstractCompositeGenerator<?, ?> user) {
        if (users == null) {
            // We are adding the first user: allocate a small set and notify the groupings we use that we are a user
            users = new ArrayList<>();
            for (var grouping : groupings()) {
                grouping.addUser(this);
            }
        }
        users.add(user);
    }

    boolean hasUser() {
        return users != null;
    }

    void freezeUsers() {
        users = users == null ? List.of() : users.stream().distinct().collect(Collectors.toUnmodifiableList());
    }

    @Override
    StatementNamespace namespace() {
        return StatementNamespace.GROUPING;
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterGrouping(statement().argument());
    }

    @Override
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final var builder = builderFactory.newGeneratedTypeBuilder(typeName());
        builder.addImplementsType(BindingTypes.DATA_OBJECT);
        narrowImplementedInterface(builder);
        addUsesInterfaces(builder, builderFactory);
        addGetterMethods(builder, builderFactory);

        final var module = currentModule();
        module.addQNameConstant(builder, statement().argument());

        annotateDeprecatedIfNecessary(builder);
        builderFactory.addCodegenInformation(module, statement(), builder);

        return builder.build();
    }

    @Override
    void addAsGetterMethod(final GeneratedTypeBuilderBase<?> builder, final TypeBuilderFactory builderFactory) {
        // groupings are a separate concept
    }

    @Override
    CompositeRuntimeTypeBuilder<GroupingEffectiveStatement, GroupingRuntimeType> createBuilder(
            final GroupingEffectiveStatement statement) {
        final var local = users;
        if (local == null) {
            throw new VerifyException(this + " has unresolved users");
        }

        final var vectors = local.stream()
            .map(AbstractCompositeGenerator::getRuntimeType)
            .distinct()
            .collect(Collectors.toUnmodifiableList());

        return new CompositeRuntimeTypeBuilder<>(statement) {
            @Override
            GroupingRuntimeType build(final GeneratedType type, final GroupingEffectiveStatement statement,
                    final List<RuntimeType> children, final List<AugmentRuntimeType> augments) {
                // Groupings cannot be targeted by 'augment'
                if (augments.isEmpty()) {
                    return new DefaultGroupingRuntimeType(type, statement, children, vectors);
                }
                throw new VerifyException("Unexpected augments " + augments);
            }
        };
    }
}
