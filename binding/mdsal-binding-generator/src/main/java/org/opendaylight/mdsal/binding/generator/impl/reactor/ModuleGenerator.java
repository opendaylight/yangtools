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

import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.generator.impl.reactor.CollisionDomain.Member;
import org.opendaylight.mdsal.binding.generator.impl.rt.DefaultModuleRuntimeType;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ModuleRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.binding.contract.Naming;
import org.opendaylight.yangtools.yang.common.AbstractQName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code module} statement. These generators are roots for generating types for a
 * particular {@link QNameModule} as mapped into the root package.
 */
public final class ModuleGenerator extends AbstractCompositeGenerator<ModuleEffectiveStatement, ModuleRuntimeType> {
    private final @NonNull JavaTypeName yangModuleInfo;

    /**
     * Note that for sake of simplicity of lookup and child mapping, this instance serves as the root for all child
     * generators, but mapping to {@link CollisionDomain}s and their {@link Member}s is rather weird. This generator
     * actually produces one of <em>secondary</em> members, more precisely the {@link Naming#DATA_ROOT_SUFFIX} one.
     * Counter-intuitively the other secondary members live as children of this generator. To support this we also have
     * this field, which is actually the <em>primary</em> derived from the module's name.
     */
    private final Member prefixMember;

    ModuleGenerator(final ModuleEffectiveStatement statement) {
        super(statement);
        yangModuleInfo = JavaTypeName.create(javaPackage(), Naming.MODULE_INFO_CLASS_NAME);
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
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final GeneratedTypeBuilder builder = builderFactory.newGeneratedTypeBuilder(typeName());
        builder.setModuleName(statement().argument().getLocalName());
        builder.addImplementsType(BindingTypes.DATA_ROOT);

        final int usesCount = addUsesInterfaces(builder, builderFactory);
        // if we have more than 2 top level uses statements we need to define getImplementedInterface() on the top-level
        // DataRoot object
        if (usesCount > 1) {
            narrowImplementedInterface(builder);
        }

        addGetterMethods(builder, builderFactory);
        builderFactory.addCodegenInformation(statement(), builder);

        return builder.build();
    }

    @NonNull Member getPrefixMember() {
        return verifyNotNull(prefixMember);
    }

    void addQNameConstant(final GeneratedTypeBuilderBase<?> builder, final AbstractQName localName) {
        builder.addConstant(BindingTypes.QNAME, Naming.QNAME_STATIC_FIELD_NAME,
            Map.entry(yangModuleInfo, localName.getLocalName()));
    }

    // FIXME: use YangDataName
    void addNameConstant(final GeneratedTypeBuilderBase<?> builder, final String templateName) {
        builder.addConstant(BindingTypes.YANG_DATA_NAME, Naming.NAME_STATIC_FIELD_NAME,
            Map.entry(yangModuleInfo, templateName));
    }

    @Override
    CompositeRuntimeTypeBuilder<ModuleEffectiveStatement, ModuleRuntimeType> createBuilder(
            final ModuleEffectiveStatement statement) {
        return new CompositeRuntimeTypeBuilder<>(statement) {
            @Override
            ModuleRuntimeType build(final GeneratedType type, final ModuleEffectiveStatement statement,
                    final List<RuntimeType> children, final List<AugmentRuntimeType> augments) {
                verify(augments.isEmpty(), "Unexpected augments %s", augments);
                return new DefaultModuleRuntimeType(type, statement, children);
            }
        };
    }
}
