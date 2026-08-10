/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import com.google.common.base.VerifyException;
import java.util.Iterator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.DataContainerArchetype;
import org.opendaylight.yangtools.binding.model.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.Type;
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
    final boolean augmentable;

    private final @NonNull DataContainerContract contract;

    @NonNullByDefault
    InterfaceTemplate(final DataRootArchetype root, final T archetype, final DataContainerContract contract,
            final boolean augmentable) {
        super(root, archetype);
        this.contract = requireNonNull(contract);
        this.augmentable = augmentable;
        getters = DataContainerGetters.of(archetype);
    }

    @Override
    final BlockBuilder body() {
        final var bb = newBlockBuilder()
            .blk(wrapToDocumentation(formatDataForJavaDoc()))
            .frg(DeprecatedAnnotation.of(javaType(), archetype.statement()))
            .eol(generatedAnnotation())
            .str("public interface ").str(archetype.simpleName());

        // We can have three shapes here to ensure reasonable separation from inner members:
        //
        //   interface Foo {
        //       int VALUE = 42;
        //
        // or
        //
        //   interface Foo extends One {
        //       int VALUE = 42;
        //
        // or
        //
        //   interface Foo
        //       extends One,
        //               Two {
        //       int VALUE = 42;
        //
        // TODO: split this out into a ExtendsKeyword, which is a BlockFragment
        // TODO: there always should be at least one interface
        final var ifaces = extendsTypes();
        if (ifaces.hasNext()) {
            final var first = ifaces.next();
            if (ifaces.hasNext()) {
                bb.nl().ind("extends ");

                // Note: We could try to pack multiple references into a single line, but that would require us to pick
                //       a length limit and peek into importedName to see how long it is.
                //       Perhaps it is worth the added complexity: for now this simple approach just works
                var current = first;
                while (true) {
                    bb.str(importedName(current));
                    if (!ifaces.hasNext()) {
                        break;
                    }
                    // space equivalent of 'extends'
                    bb.eol(",").ind("        ");
                    current = ifaces.next();
                }
            } else {
                bb.str(" extends ").str(importedName(first));
            }
        }

        bb.oB();

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

    // FIXME: This method forces the use of ConcreteType and ParameterizedType. Replace Type with a BlockFragment
    //        subclass (TypeFragment?) does the equivalent of JavaFileTemplate.importedName(Type)
    @NonNullByDefault
    Iterator<? extends Type> extendsTypes() {
        return archetype.partials().iterator();
    }

    BlockFragment constants() {
        return null;
    }

    @NonNullByDefault
    BlockBuilder contractMethods(final BlockBuilder bb) {
        return bb.frg(contract.implementationIn(this));
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
