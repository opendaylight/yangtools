/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;

import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.yangtools.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.odlext.model.api.ContextReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.TypeAware;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Common base class for {@link LeafGenerator} and {@link LeafListGenerator}.
 */
abstract class AbstractTypeAwareGenerator<T extends DataTreeEffectiveStatement<?>, R extends RuntimeType,
        G extends AbstractTypeAwareGenerator<T, R, G>>
        extends AbstractTypeObjectGenerator<T, R> {
    private IdentityGenerator contextType;

    AbstractTypeAwareGenerator(final T statement, final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent);
        verify(statement instanceof TypeAware, "Unexpected statement %s", statement);
    }

    @Override
    final void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterDataTree(statement().argument());
    }

    @Override
    final void bindDerivedGenerators(final TypeReference reference) {
        // No-op
    }

    @Override
    final void bindTypeDefinition(final GeneratorContext context) {
        super.bindTypeDefinition(context);
        contextType = statement().findFirstEffectiveSubstatementArgument(ContextReferenceEffectiveStatement.class)
            .map(context::resolveIdentity)
            .orElse(null);
    }

    @Override
    final TypeDefinition<?> extractTypeDefinition() {
        final var stmt = statement();
        verify(stmt instanceof TypedDataSchemaNode, "Unexpected statement %s", stmt);
        return ((TypedDataSchemaNode) stmt).getType();
    }

    @Override
    final JavaTypeName createTypeName() {
        // FIXME: we should be be assigning a non-conflict name here
        return getParent().typeName().createEnclosed(assignedName(), "$");
    }

    @Override
    final GeneratedTransferObject createDerivedType(final TypeBuilderFactory builderFactory,
            final GeneratedTransferObject baseType) {
        throw new UnsupportedOperationException();
    }

    @Override
    final MethodSignatureBuilder constructGetter(final GeneratedTypeBuilderBase<?> builder, final Type returnType) {
        final MethodSignatureBuilder ret = super.constructGetter(builder, returnType);

        if (contextType != null) {
            ret.addAnnotation(BindingTypes.ROUTING_CONTEXT)
                .addParameter("value", contextType.typeName().toString() + ".class");
        }

        return ret;
    }

    @Override
    final void constructRequire(final GeneratedTypeBuilderBase<?> builder, final Type returnType) {
        constructRequireImpl(builder, returnType);
    }
}
