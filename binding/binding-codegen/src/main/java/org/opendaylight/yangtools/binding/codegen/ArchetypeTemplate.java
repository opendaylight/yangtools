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
import static org.opendaylight.yangtools.binding.codegen.YangModuleInfoTemplate.QNAMEOF_METHOD_NAME;
import static org.opendaylight.yangtools.binding.codegen.YangModuleInfoTemplate.yangModuleInfoOf;
import static org.opendaylight.yangtools.binding.contract.Naming.QNAME_STATIC_FIELD_NAME;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;

/**
 * A template backed by an {@link Archetype} defined in some module manifested as a {@link DataRootArchetype}.
 */
@NonNullByDefault
abstract sealed class ArchetypeTemplate<T extends Archetype> extends BaseTemplate permits FeatureTemplate, KeyTemplate {
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
     * followed by a {@code Generated} annotation}
     * @param schemaNode the {@link DocumentedNode}
     */
    final BlockBuilder newBodyBuilder(final DocumentedNode schemaNode) {
        return schemaNode instanceof DocumentedNode.WithStatus withStatus ? newBodyBuilder(withStatus)
            : newBlockBuilder()
                .blk(javadocBlock(root.statement(), schemaNode))
                .eol(GENERATED_ANNOTATION);
    }

    /**
     * {@return a new BlockBuilder initialized with javadoc block derived from the specified {@link DocumentedNode}
     * followed by an optional {@code Deprecated} annotation, followed by a {@code Generated} annotation}
     * @param schemaNode the {@link DocumentedNode}
     */
    final BlockBuilder newBodyBuilder(final DocumentedNode.WithStatus schemaNode) {
        return newBlockBuilder()
            .blk(javadocBlock(root.statement(), schemaNode))
            .blk(deprecatedAnnotation(schemaNode))
            .eol(GENERATED_ANNOTATION);
    }

    /**
     * Append the {@code QNAME} field initialized via {@code YangModuleInfo.qnameOf(localName)}.
     *
     * @param bb the {@link BlockBuilder}
     * @param qname the {@link QName} value
     */
    final void appendQNameField(final BlockBuilder bb, final QName qname) {
        final var module = root.statement().localQNameModule();
        verify(module.equals(qname.getModule()));

        bb
            .str(importedNonNull(BindingTypes.QNAME)).str(" " + QNAME_STATIC_FIELD_NAME + " = ")
            .str(importedName(yangModuleInfoOf(module))).str("." + QNAMEOF_METHOD_NAME + "(")
            .jStr(qname.getLocalName()).eol(");");
    }
}