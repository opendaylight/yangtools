/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import com.google.common.base.VerifyException;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.DataContainerArchetype;
import org.opendaylight.yangtools.binding.model.DataRootArchetype;
import org.opendaylight.yangtools.yang.model.api.ContainerLikeCompat;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementEquivalent;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Base class for code generators based on {@link DataContainerArchetype}.
 */
// TODO: split this class up into reusable components, i.e. use composition instead of inheritance
abstract sealed class InterfaceTemplate<T extends @NonNull DataContainerArchetype> extends ArchetypeTemplate<T>
        permits AugmentationTemplate, CaseObjectTemplate, ContainerObjectTemplate, DataRootTemplate,
                EntryObjectTemplate, GroupingTemplate, InstanceNotificationTemplate, ItemObjectTemplate,
                KeyedListNotificationTemplate, NotificationTemplate, NotificationBodyTemplate, RpcInputTemplate,
                RpcOutputTemplate, YangDataTemplate {
    // TODO: this should be lazily instantiated  and refcounted as it can be quite large and assuming one-time
    //       file generation, we can free this. builders acess this as well and there is no guarantee of order of
    //       rendering ... so this needs further analysis.
    final @NonNull DataContainerGetters getters;

    @NonNullByDefault
    InterfaceTemplate(final DataRootArchetype root, final T archetype) {
        super(root, archetype);
        getters = DataContainerGetters.of(archetype);
    }

    @Override
    final BlockBuilder body() {
        final var bb = newBlockBuilder()
            .blk(wrapToDocumentation(formatDataForJavaDoc()))
            .frg(DeprecatedAnnotation.of(javaType(), archetype.statement()))
            .eol(generatedAnnotation())
            .str("public interface ").str(archetype.simpleName()).frg(extendsKeyword()).oB();

        final var innerClasses = generateInnerClasses(root, archetype.typeObjects());
        if (innerClasses != null) {
            bb.blk(innerClasses).newLine();
        }

        final var constants = constants();
        if (constants != null) {
            bb.frg(constants).newLine();
        }

        final var methods = archetype.getters();
        if (!methods.isEmpty()) {
            bb.frg(new DataContainerGetterMethods(this));
        }

        return contractMethods(bb).cB();
    }

    @NonNullByDefault
    abstract ExtendsKeyword extendsKeyword();

    @NonNullByDefault
    final Stream<TypeReference> extendsPartials() {
        final var partials = archetype.partials();
        return switch (partials.size()) {
            case 0 -> Stream.empty();
            case 1 -> Stream.of(typeRefOf(partials.getFirst().name()));
            default -> partials.stream().map(partial -> typeRefOf(partial.name()));
        };
    }

    BlockFragment constants() {
        return null;
    }

    @NonNullByDefault
    BlockBuilder contractMethods(final BlockBuilder bb) {
        return bb;
    }

    @NonNullByDefault
    private String formatDataForJavaDoc() {
        final var statement = archetype.statement();

        final var sb = new StringBuilder();
        if (statement instanceof DocumentedNode documented) {
            final var comment = DocUtils.typeCommentOf(documented);
            if (comment != null) {
                sb.append(comment.getJavadoc());
            }
        }
        YangSourceDefinition.of(root.statement(), statement).ifPresent(def -> {
            final var node = def.getNode();
            appendSnippet(sb, archetype, def.getModule(), requireEffective(node), node);
        });

        final var str = sb.toString();
        return str.isBlank() ? "" : str.stripTrailing() + '\n';
    }

    @NonNullByDefault
    private static EffectiveStatement<?, ?> requireEffective(final DocumentedNode node) {
        return switch (node) {
            case EffectiveStatementEquivalent<?> equivalent -> equivalent.asEffectiveStatement();
            case EffectiveStatement<?, ?> effective -> effective;
            case ContainerLikeCompat compat -> requireEffective(compat.delegate());
            default -> throw new VerifyException("Unsupported node " + node);
        };
    }
}
