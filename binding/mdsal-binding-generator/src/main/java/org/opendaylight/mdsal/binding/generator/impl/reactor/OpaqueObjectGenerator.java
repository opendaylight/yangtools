/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.opendaylight.mdsal.binding.generator.impl.rt.DefaultAnydataRuntimeType;
import org.opendaylight.mdsal.binding.generator.impl.rt.DefaultAnyxmlRuntimeType;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.mdsal.binding.runtime.api.AnydataRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.AnyxmlRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.OpaqueRuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Common generator for {@code anydata} and {@code anyxml}.
 */
abstract class OpaqueObjectGenerator<S extends DataTreeEffectiveStatement<?>, R extends OpaqueRuntimeType>
        extends AbstractExplicitGenerator<S, R> {
    static final class Anydata extends OpaqueObjectGenerator<AnydataEffectiveStatement, AnydataRuntimeType> {
        Anydata(final AnydataEffectiveStatement statement, final AbstractCompositeGenerator<?, ?> parent) {
            super(statement, parent);
        }

        @Override
        AnydataRuntimeType createRuntimeType() {
            return generatedType().map(type -> new DefaultAnydataRuntimeType(type, statement())).orElse(null);
        }

        @Override
        AnydataRuntimeType rebaseRuntimeType(final AnydataRuntimeType type, final AnydataEffectiveStatement statement) {
            return new DefaultAnydataRuntimeType(type.javaType(), statement);
        }
    }

    static final class Anyxml extends OpaqueObjectGenerator<AnyxmlEffectiveStatement, AnyxmlRuntimeType> {
        Anyxml(final AnyxmlEffectiveStatement statement, final AbstractCompositeGenerator<?, ?> parent) {
            super(statement, parent);
        }

        @Override
        AnyxmlRuntimeType createRuntimeType() {
            return generatedType().map(type -> new DefaultAnyxmlRuntimeType(type, statement())).orElse(null);
        }

        @Override
        AnyxmlRuntimeType rebaseRuntimeType(final AnyxmlRuntimeType type, final AnyxmlEffectiveStatement statement) {
            return new DefaultAnyxmlRuntimeType(type.javaType(), statement);
        }
    }

    OpaqueObjectGenerator(final S statement, final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterDataTree(statement().getIdentifier());
    }

    @Override
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final GeneratedTypeBuilder builder = builderFactory.newGeneratedTypeBuilder(typeName());
        builder.addImplementsType(BindingTypes.opaqueObject(builder));
        addImplementsChildOf(builder);
        defaultImplementedInterace(builder);
        annotateDeprecatedIfNecessary(builder);

        final ModuleGenerator module = currentModule();
        module.addQNameConstant(builder, localName());

        builderFactory.addCodegenInformation(module, statement(), builder);
        builder.setModuleName(module.statement().argument().getLocalName());
//        newType.setSchemaPath(schemaNode.getPath());

        return builder.build();
    }

    @Override
    void constructRequire(final GeneratedTypeBuilderBase<?> builder, final Type returnType) {
        constructRequireImpl(builder, returnType);
    }
}
