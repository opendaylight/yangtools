/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.contract.StatementNamespace;
import org.opendaylight.yangtools.binding.generator.impl.reactor.CollisionDomain.Member;
import org.opendaylight.yangtools.binding.generator.impl.rt.DefaultModuleRuntimeType;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.DataRootArchetypeBuilder;
import org.opendaylight.yangtools.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.ModuleRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.YangDataRuntimeType;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code module} statement. These generators are roots for generating types for a
 * particular {@link QNameModule} as mapped into the root package.
 */
public final class ModuleGenerator extends AbstractCompositeGenerator<ModuleEffectiveStatement, ModuleRuntimeType> {
    /**
     * Note that for sake of simplicity of lookup and child mapping, this instance serves as the root for all child
     * generators, but mapping to {@link CollisionDomain}s and their {@link Member}s is rather weird. This generator
     * actually produces one of <em>secondary</em> members, more precisely the {@link Naming#DATA_ROOT_SUFFIX} one.
     * Counter-intuitively the other secondary members live as children of this generator. To support this we also have
     * this field, which is actually the <em>primary</em> derived from the module's name.
     */
    private final @NonNull Member prefixMember;

    ModuleGenerator(final ModuleEffectiveStatement statement) {
        super(statement);
        prefixMember = domain().addPrefix(this, new ModuleNamingStrategy(statement.argument()));
    }

    @Override
    StatementNamespace namespace() {
        return StatementNamespace.DATA_ROOT;
    }

    @Override
    ModuleGenerator currentModule() {
        return this;
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        // No-op
    }

    @Override
    String createJavaPackage() {
        return Naming.getRootPackageName(statement().localQNameModule());
    }

    @Override
    AbstractCompositeGenerator<?, ?> getPackageParent() {
        return this;
    }

    @Override
    CollisionDomain parentDomain() {
        return domain();
    }

    @Override
    Member createMember(final CollisionDomain domain) {
        return domain.addSecondary(this, prefixMember, Naming.DATA_ROOT_SUFFIX);
    }

    @Override
    DataRootArchetype createTypeImpl(final TypeBuilderFactory builderFactory) {
        final var builder = new DataRootArchetypeBuilder(typeName(), statement());
        addUsesInterfaces(builder, builderFactory);
        defaultImplementedInterace(builder);
        addGetterMethods(builder, builderFactory);
        return builder.build();
    }

    @NonNull Member getPrefixMember() {
        return prefixMember;
    }

    @Override
    CompositeRuntimeTypeBuilder<ModuleEffectiveStatement, ModuleRuntimeType> createBuilder(
            final ModuleEffectiveStatement statement) {
        return new CompositeRuntimeTypeBuilder<>(statement) {
            @Override
            ModuleRuntimeType build(final Archetype type, final ModuleEffectiveStatement statement,
                    final List<RuntimeType> children, final List<AugmentRuntimeType> augments) {
                verify(augments.isEmpty(), "Unexpected augments %s", augments);

                final var yangDataChildren = new ArrayList<YangDataRuntimeType>();
                for (var child : ModuleGenerator.this) {
                    if (child instanceof YangDataGenerator yangDataGen) {
                        yangDataChildren.add(yangDataGen.getRuntimeType());
                    }
                }

                return new DefaultModuleRuntimeType(type, statement, children, yangDataChildren);
            }
        };
    }
}
