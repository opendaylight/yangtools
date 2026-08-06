/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.meta.DataSchemaCompat;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;

/**
 * A template backed by an {@link Archetype} defined in some module manifested as a {@link DataRootArchetype}.
 */
@NonNullByDefault
abstract sealed class ArchetypeTemplate<T extends Archetype> extends BaseTemplate
        permits ActionTemplate, BitsTypeObjectTemplate, ChoiceInTemplate, EnumTypeObjectTemplate, FeatureTemplate,
                IdentityTemplate, InterfaceTemplate, KeyTemplate, KeyedListActionTemplate, OpaqueObjectTemplate,
                RpcTemplate, ScalarTypeObjectTemplate, UnionTypeObjectTemplate {
    /**
     * An {@link ArchetypeTemplate} which comes with a {@link BuilderTemplate}.
     */
    sealed interface WithBuilder permits AugmentationTemplate, CaseObjectTemplate, ContainerObjectTemplate,
            EntryObjectTemplate, InstanceNotificationTemplate, ItemObjectTemplate, KeyedListNotificationTemplate,
            NotificationTemplate, RpcInputTemplate, RpcOutputTemplate, YangDataTemplate {
        /**
         * {@return a new BuilderTemplate}
         */
        BuilderTemplate newBuilderTemplate();
    }

    private static final String GENERATED_ANNOTATION =
        "@javax.annotation.processing.Generated(\"mdsal-binding-generator\")";

    final DataRootArchetype root;
    final T archetype;

    ArchetypeTemplate(final GeneratedClass javaType, final T archetype, final DataRootArchetype root) {
        super(javaType);
        this.archetype = requireNonNull(archetype);
        this.root = requireNonNull(root);
    }

    ArchetypeTemplate(final DataRootArchetype root, final T archetype) {
        this(GeneratedClass.of(archetype), archetype, root);
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
            .frg(DeprecatedAnnotation.of(javaType(), node));
        return generatedAnnotation ? bb.eol(GENERATED_ANNOTATION) : bb;
    }

    private @Nullable BlockBuilder javadocBlock(final ModuleEffectiveStatement module,
            final EffectiveStatement<?, ?> stmt, final DocumentedNode node) {
        final var sb = new StringBuilder();
        final var comment = DocUtils.typeCommentOf(node);
        if (comment != null) {
            sb.append(comment.getJavadoc());
        }
        appendSnippet(sb, archetype, module, stmt, node);

        final var str = sb.toString();
        if (str.isBlank()) {
            return null;
        }

        final var bb = Block.builder();
        appendAsJavadoc(bb, str.stripTrailing() + '\n');
        return bb;
    }
}
