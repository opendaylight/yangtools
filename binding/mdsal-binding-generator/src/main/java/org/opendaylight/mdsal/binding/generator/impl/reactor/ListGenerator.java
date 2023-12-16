/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static org.opendaylight.mdsal.binding.model.ri.BindingTypes.keyAware;

import java.util.List;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.generator.impl.rt.DefaultListRuntimeType;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.MethodSignature.ValueMechanics;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.mdsal.binding.model.ri.Types;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.KeyRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ListRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.binding.contract.Naming;
import org.opendaylight.yangtools.yang.binding.contract.StatementNamespace;
import org.opendaylight.yangtools.yang.common.Ordering;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code list} statement.
 */
final class ListGenerator extends CompositeSchemaTreeGenerator<ListEffectiveStatement, ListRuntimeType> {
    private final @Nullable KeyGenerator keyGen;

    ListGenerator(final ListEffectiveStatement statement, final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent);
        keyGen = statement.findFirstEffectiveSubstatement(KeyEffectiveStatement.class)
            .map(key -> new KeyGenerator(key, parent, this))
            .orElse(null);
    }

    @Override
    StatementNamespace namespace() {
        return StatementNamespace.LIST;
    }

    @Nullable KeyGenerator keyGenerator() {
        return keyGen;
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterDataTree(statement().argument());
    }

    @Override
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final var builder = builderFactory.newGeneratedTypeBuilder(typeName());
        addImplementsChildOf(builder);
        addAugmentable(builder);
        addUsesInterfaces(builder, builderFactory);
        addConcreteInterfaceMethods(builder);

        final var module = currentModule();
        module.addQNameConstant(builder, localName());

        final var local = keyGen;
        if (local != null) {
            // Add yang.binding.Identifiable and its key() method
            final var keyType = local.getGeneratedType(builderFactory);
            builder.addImplementsType(keyAware(keyType));
            builder.addMethod(Naming.KEY_AWARE_KEY_NAME)
                .setReturnType(keyType)
                .addAnnotation(OVERRIDE_ANNOTATION);
        }

        addGetterMethods(builder, builderFactory);

        annotateDeprecatedIfNecessary(builder);
        builderFactory.addCodegenInformation(module, statement(), builder);
        builder.setModuleName(module.statement().argument().getLocalName());
        //    builder.setSchemaPath(node.getPath());

        return builder.build();
    }

    private @Nullable KeyRuntimeType keyRuntimeType() {
        final var local = keyGen;
        return local != null ? local.getRuntimeType() : null;
    }

    @Override
    Type methodReturnType(final TypeBuilderFactory builderFactory) {
        final var generatedType = super.methodReturnType(builderFactory);
        // We are wrapping the generated type in either a List or a Map based on presence of the key
        final var local = keyGen;
        if (local != null && statement().ordering() == Ordering.SYSTEM) {
            return Types.mapTypeFor(local.getGeneratedType(builderFactory), generatedType);
        }

        return Types.listTypeFor(generatedType);
    }

    @Override
    MethodSignatureBuilder constructGetter(final GeneratedTypeBuilderBase<?> builder, final Type returnType) {
        final var ret = super.constructGetter(builder, returnType).setMechanics(ValueMechanics.NULLIFY_EMPTY);

        final var nonnull = builder
            .addMethod(Naming.getNonnullMethodName(localName().getLocalName()))
            .setReturnType(returnType)
            .setDefault(true);
        annotateDeprecatedIfNecessary(nonnull);

        return ret;
    }

    @Override
    CompositeRuntimeTypeBuilder<ListEffectiveStatement, ListRuntimeType> createBuilder(
            final ListEffectiveStatement statement) {
        return new CompositeRuntimeTypeBuilder<>(statement) {
            @Override
            ListRuntimeType build(final GeneratedType type, final ListEffectiveStatement statement,
                    final List<RuntimeType> children, final List<AugmentRuntimeType> augments) {
                // FIXME: the key here is not rebased correctly :(
                return new DefaultListRuntimeType(type, statement, children, augments, keyRuntimeType());
            }
        };
    }
}
