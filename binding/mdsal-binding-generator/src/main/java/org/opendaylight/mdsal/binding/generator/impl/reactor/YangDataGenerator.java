/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.generator.impl.reactor.CollisionDomain.Member;
import org.opendaylight.mdsal.binding.generator.impl.rt.DefaultYangDataRuntimeType;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.YangDataRuntimeType;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;
import org.opendaylight.yangtools.yang.common.UnresolvedQName;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code rc:yang-data} statement.
 */
abstract sealed class YangDataGenerator
        extends AbstractCompositeGenerator<YangDataEffectiveStatement, YangDataRuntimeType> {
    private static final class WithIdentifier extends YangDataGenerator {
        private final @NonNull Unqualified identifier;

        WithIdentifier(final YangDataEffectiveStatement statement, final ModuleGenerator parent,
                final Unqualified identifier) {
            super(statement, parent);
            this.identifier = requireNonNull(identifier);
        }

        @Override
        CamelCaseNamingStrategy createNamingStrategy() {
            return new CamelCaseNamingStrategy(namespace(), identifier);
        }
    }

    private static final class WithString extends YangDataGenerator {
        WithString(final YangDataEffectiveStatement statement, final ModuleGenerator parent) {
            super(statement, parent);
        }

        @Override
        YangDataNamingStrategy createNamingStrategy() {
            return new YangDataNamingStrategy(statement().argument());
        }
    }

    private YangDataGenerator(final YangDataEffectiveStatement statement, final ModuleGenerator parent) {
        super(statement, parent);
    }

    static @NonNull YangDataGenerator of(final YangDataEffectiveStatement statement, final ModuleGenerator parent) {
        // yang-data's argument is not guaranteed to comply with YANG 'identifier', but it usually does. If it does, we
        // use the usual mechanics, but if it does not, we have to deal with any old string, similar to what we do for
        // bit names. Here we decide which path to take.
        final String templateName = statement.argument().name();
        final var identifier = UnresolvedQName.tryLocalName(templateName);
        return identifier != null ? new WithIdentifier(statement, parent, identifier)
            : new WithString(statement, parent);
    }

    @Override
    final void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterYangData(statement().argument());
    }

    @Override
    final ClassPlacement classPlacement() {
        return ClassPlacement.TOP_LEVEL;
    }

    @Override
    final Member createMember(final CollisionDomain domain) {
        return domain.addPrimary(this, createNamingStrategy());
    }

    abstract @NonNull ClassNamingStrategy createNamingStrategy();

    @Override
    final StatementNamespace namespace() {
        return StatementNamespace.YANG_DATA;
    }

    @Override
    final GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final GeneratedTypeBuilder builder = builderFactory.newGeneratedTypeBuilder(typeName());

        builder.addImplementsType(BindingTypes.yangData(builder));
        addUsesInterfaces(builder, builderFactory);
        addConcreteInterfaceMethods(builder);

        addGetterMethods(builder, builderFactory);

        final var module = currentModule();
        module.addNameConstant(builder, statement().argument());

        builder.setModuleName(module.statement().argument().getLocalName());
        builderFactory.addCodegenInformation(module, statement(), builder);

        return builder.build();
    }

    @Override
    final CompositeRuntimeTypeBuilder<YangDataEffectiveStatement, YangDataRuntimeType> createBuilder(
            final YangDataEffectiveStatement statement) {
        return new CompositeRuntimeTypeBuilder<>(statement) {
            @Override
            YangDataRuntimeType build(final GeneratedType type, final YangDataEffectiveStatement statement,
                    final List<RuntimeType> children, final List<AugmentRuntimeType> augments) {
                return new DefaultYangDataRuntimeType(type, statement, children);
            }
        };
    }

    @Override
    final void addAsGetterMethod(final GeneratedTypeBuilderBase<?> builder, final TypeBuilderFactory builderFactory) {
        // is not a part of any structure
    }
}
