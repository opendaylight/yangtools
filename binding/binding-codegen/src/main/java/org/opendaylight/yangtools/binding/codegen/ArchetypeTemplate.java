/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.codegen.ModuleSupportTemplate.QNAMEOF_METHOD_NAME;
import static org.opendaylight.yangtools.binding.codegen.ModuleSupportTemplate.yangModuleInfoOf;
import static org.opendaylight.yangtools.binding.contract.Naming.QNAME_STATIC_FIELD_NAME;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.meta.DataSchemaCompat;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * A template backed by an {@link Archetype} defined in some module manifested as a {@link DataRootArchetype}.
 */
@NonNullByDefault
abstract sealed class ArchetypeTemplate<T extends Archetype> extends BaseTemplate
        permits BitsTypeObjectTemplate, ChoiceInTemplate, ClassTemplate, EnumTypeObjectTemplate, FeatureTemplate,
                IdentityTemplate, KeyTemplate, OpaqueObjectTemplate, RpcTemplate {
    private static final String GENERATED_ANNOTATION =
        "@javax.annotation.processing.Generated(\"mdsal-binding-generator\")";

    final DataRootArchetype root;

    ArchetypeTemplate(final GeneratedClass javaType, final T archetype, final DataRootArchetype root) {
        super(javaType, archetype);
        this.root = requireNonNull(root);
    }

    @SuppressWarnings("unchecked")
    final T archetype() {
        return (T) type();
    }

    /**
     * {@return a new BlockBuilder initialized with javadoc block derived from the specified {@link DocumentedNode}
     * followed by an optional {@code Deprecated} annotation, followed by a {@code Generated} annotation}
     * @param stmt a {@link DataSchemaCompat} statement
     */
    final BlockBuilder newBodyBuilder(final DataSchemaCompat<?, ?> stmt) {
        return newBodyBuilder(stmt, stmt.toDataSchemaNode());
    }

    /**
     * {@return a new BlockBuilder initialized with javadoc block derived from the specified {@link DocumentedNode}
     * view of an {@link EffectiveStatement} followed by an optional {@code Deprecated} annotation, followed by
     * a {@code Generated} annotation}
     * @param stmt the {@link EffectiveStatement}
     * @param node the {@link DocumentedNode}
     */
    final BlockBuilder newBodyBuilder(final EffectiveStatement<?, ?> stmt, final DocumentedNode.WithStatus node) {
        return newBodyBuilder(stmt, node, true);
    }

    /**
     * {@return a new BlockBuilder initialized with javadoc block derived from the specified {@link DocumentedNode}
     * view of an {@link EffectiveStatement} followed by an optional {@code Deprecated} annotation, followed by
     * a {@code Generated} annotation} if instructed opted into.
     * @param stmt the {@link EffectiveStatement}
     * @param node the {@link DocumentedNode}
     * @param generatedAnnotation {@code true} if we should also add {@code Generated} annotation
     */
    final BlockBuilder newBodyBuilder(final EffectiveStatement<?, ?> stmt, final DocumentedNode.WithStatus node,
            final boolean generatedAnnotation) {
        final var bb = newBlockBuilder()
            .blk(javadocBlock(root.statement(), stmt, node))
            .blk(deprecatedAnnotation(node));
        return generatedAnnotation ? bb.eol(GENERATED_ANNOTATION) : bb;
    }

    /**
     * Return a BlockFragment appending the {@code QNAME} field initialized via {@code YangModuleInfo.qnameOf(String)}.
     *
     * @param archetype the archetype
     * @return a {@link BlockFragment}
     */
    final BlockFragment qnameConstant(final Archetype.WithQName<?> archetype) {
        final var qname = archetype.qnameConstant();
        final var module = root.statement().localQNameModule();
        verify(module.equals(qname.getModule()));

        return (BlockFragment) bb -> {
            bb
                .str(importedNonNull(BindingTypes.QNAME)).str(" " + QNAME_STATIC_FIELD_NAME + " = ")
                .str(importedName(yangModuleInfoOf(module))).str("." + QNAMEOF_METHOD_NAME + "(")
                .jStr(qname.getLocalName()).eol(");");
        };
    }
}
