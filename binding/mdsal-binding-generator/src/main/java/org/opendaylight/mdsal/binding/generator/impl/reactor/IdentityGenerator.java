/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;
import static org.opendaylight.mdsal.binding.model.ri.BindingTypes.BASE_IDENTITY;

import java.util.List;
import java.util.stream.Collectors;
import org.opendaylight.mdsal.binding.generator.impl.rt.DefaultIdentityRuntimeType;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.runtime.api.IdentityRuntimeType;
import org.opendaylight.yangtools.yang.binding.BaseIdentity;
import org.opendaylight.yangtools.yang.binding.contract.Naming;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code identity} statement.
 */
public final class IdentityGenerator
        extends AbstractDependentGenerator<IdentityEffectiveStatement, IdentityRuntimeType> {
    private List<IdentityGenerator> baseIdentities = null;

    IdentityGenerator(final IdentityEffectiveStatement statement, final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    StatementNamespace namespace() {
        return StatementNamespace.IDENTITY;
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        throw new UnsupportedOperationException("Cannot push " + statement() + " to data tree");
    }

    @Override
    void linkDependencies(final GeneratorContext context) {
        baseIdentities = statement().streamEffectiveSubstatements(BaseEffectiveStatement.class)
            .map(BaseEffectiveStatement::argument)
            .map(context::resolveIdentity)
            .collect(Collectors.toUnmodifiableList());
    }

    @Override
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final var builder = builderFactory.newGeneratedTypeBuilder(typeName());
        if (!baseIdentities.isEmpty()) {
            for (var baseIdentity : baseIdentities) {
                builder.addImplementsType(baseIdentity.getGeneratedType(builderFactory));
            }
        } else {
            builder.addImplementsType(BASE_IDENTITY);
        }

        annotateDeprecatedIfNecessary(statement(), builder);

        narrowImplementedInterface(builder);

        final ModuleGenerator module = currentModule();
        module.addQNameConstant(builder, localName());

        // Constant implementation
        builder.addConstant(Type.of(builder), Naming.VALUE_STATIC_FIELD_NAME, BaseIdentity.class);

        builderFactory.addCodegenInformation(module, statement(), builder);
        builder.setModuleName(module.statement().argument().getLocalName());
//        builder.setSchemaPath(identity.getPath());

        return builder.build();
    }

    @Override
    IdentityRuntimeType createExternalRuntimeType(final Type type) {
        verify(type instanceof GeneratedType, "Unexpected type %s", type);
        return new DefaultIdentityRuntimeType((GeneratedType) type, statement());
    }

    @Override
    IdentityRuntimeType createInternalRuntimeType(final AugmentResolver resolver,
            final IdentityEffectiveStatement statement, final Type type) {
        // 'identity' statements are not part of schema tree and hence should never an internal reference
        throw new UnsupportedOperationException("Should never be called");
    }

    @Override
    void addAsGetterMethod(final GeneratedTypeBuilderBase<?> builder, final TypeBuilderFactory builderFactory) {
        // identities are a separate concept
    }
}
