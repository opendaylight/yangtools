/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.opendaylight.mdsal.binding.generator.impl.tree.SchemaTreeChild;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Common generator for {@code anydata} and {@code anyxml}.
 */
final class OpaqueObjectGenerator<T extends DataTreeEffectiveStatement<?>> extends AbstractExplicitGenerator<T>
        implements SchemaTreeChild<T, OpaqueObjectGenerator<T>> {
    OpaqueObjectGenerator(final T statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
    }

    @Override
    public OpaqueObjectGenerator<T> generator() {
        return this;
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
