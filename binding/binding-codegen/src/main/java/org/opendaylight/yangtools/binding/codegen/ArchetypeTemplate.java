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
import static org.opendaylight.yangtools.binding.model.ri.Types.PRIMITIVE_BOOLEAN;
import static org.opendaylight.yangtools.binding.model.ri.Types.STRING;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.binding.model.ri.DocUtils;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.meta.DataSchemaCompat;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;

/**
 * A template backed by an {@link Archetype} defined in some module manifested as a {@link DataRootArchetype}.
 */
@NonNullByDefault
abstract sealed class ArchetypeTemplate<T extends Archetype> extends BaseTemplate
        permits BitsTypeObjectTemplate, ChoiceInTemplate, EnumTypeObjectTemplate, FeatureTemplate, IdentityTemplate,
                InterfaceTemplate, KeyTemplate, OpaqueObjectTemplate, RpcTemplate, ScalarTypeObjectTemplate,
                UnionTypeObjectTemplate {
    private static final String GENERATED_ANNOTATION =
        "@javax.annotation.processing.Generated(\"mdsal-binding-generator\")";
    /**
     * {@code java.lang.Boolean} as a JavaTypeName.
     */
    private static final JavaTypeName BOOLEAN = JavaTypeName.create(Boolean.class);

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

    private @Nullable BlockBuilder javadocBlock(final ModuleEffectiveStatement module,
            final EffectiveStatement<?, ?> stmt, final DocumentedNode node) {
        final var sb = new StringBuilder();
        final var comment = DocUtils.typeCommentOf(node);
        if (comment != null) {
            sb.append(comment.getJavadoc());
        }
        appendSnippet(sb, archetype(), module, stmt, node);

        final var str = sb.toString();
        if (str.isBlank()) {
            return null;
        }

        final var bb = Block.builder();
        appendAsJavadoc(bb, str.stripTrailing() + '\n');
        return bb;
    }

    private @Nullable BlockBuilder deprecatedAnnotation(final DocumentedNode.@NonNull WithStatus node) {
        return switch (node.getStatus()) {
            case CURRENT -> null;
            case DEPRECATED -> newBlockBuilder().at().eol(importedName(DEPRECATED));
            case OBSOLETE -> newBlockBuilder().at().str(importedName(DEPRECATED)).eol("(forRemoval = true)");
        };
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

    final BlockBuilder generateHashCode(final List<GeneratedProperty> props) {
        return newBlockBuilder()
            .at().eol(importedName(OVERRIDE))
            .str("public int hashCode()").jBlock(bb -> {
                if (props.size() == 1) {
                    bb.str("return ");
                    final var prop = props.getFirst();
                    if (PRIMITIVE_BOOLEAN.equals(prop.getReturnType())) {
                        bb.str(importedName(BOOLEAN)).str(".hashCode(");
                    } else {
                        bb.str(importedName(CODEHELPERS)).str(".wrapperHashCode(");
                    }
                    bb.str(fieldName(prop)).eol(");");
                } else {
                    bb
                        .eol("final int prime = 31;")
                        .eol("int result = 1;");
                    for (var property : props) {
                        final var type = property.getReturnType();
                        final var receiver = type.equals(PRIMITIVE_BOOLEAN)
                            // FIXME: unified perhaps?
                            ? importedName(BOOLEAN) : importedUtilClass(type);

                        bb.str("result = prime * result + ").str(receiver).str(".hashCode(").str(fieldName(property))
                            .eol(");");
                    }
                    bb.eol("return result;");
                }
            }).nl();
    }

    final BlockBuilder generateEquals(final List<GeneratedProperty> props) {
        return newBlockBuilder()
            .at().eol(importedName(OVERRIDE))
            .str("public final boolean equals(").str(importedName(OBJECT)).str(" obj)").jBlock(bb -> {
                bb.str("return this == obj || obj instanceof ").str(type().simpleName()).str(" other");
                for (var prop : props) {
                    bb.nl().str("    && ");

                    final var fieldName = fieldName(prop);
                    final var type = prop.getReturnType();
                    if (type.equals(PRIMITIVE_BOOLEAN)) {
                        bb.str(fieldName).str(" == other.").str(fieldName);
                    } else {
                        bb.str(importedUtilClass(type)).str(".equals(").str(fieldName).str(", other.").str(fieldName)
                            .str(")");
                    }
                }
                bb.eS();
            }).nl();
    }

    final BlockBuilder generateToString(final List<GeneratedProperty> props) {
        return newBlockBuilder()
            .at().eol(importedName(OVERRIDE))
            .str("public ").str(importedName(STRING)).str(" toString()").jBlock(bb -> {
                // FIXME: use selfRef
                final var selfRef = importedName(type());

                bb.str("return ").str(importedName(CODEHELPERS));
                switch (props.size()) {
                    case 0 -> bb.str(".jcTS0(").str(selfRef).eol(".class);");
                    case 1 -> appendTS1(bb, selfRef, props.iterator().next());
                    default -> appendTSN(bb, selfRef, props);
                }
            }).nl();
    }

    private static void appendTS1(final BlockBuilder bb, final String selfRef, final GeneratedProperty prop) {
        final var name = prop.getName();
        // FIXME: this should be specialized in BitsTypeObjectTemplate
        if (isBit(prop)) {
            bb.str(".jcTSB(").str(selfRef).eol(".class).bit(").jStr(prop.getName()).str(", ").str(fieldName(prop))
                .eol(").build();");
            return;
        }

        if (name.equals("value")) {
            // Special case equivalent to ScalarTypeObject.toString()
            bb.str(".stoTS(").str(selfRef).str(".class, ");
        } else {
            bb.str(".jcTS1(").str(selfRef).str(".class, ").jStr(prop.getName()).str(", ");
        }
        bb.str(fieldName(prop)).eol(");");
    }

    private static void appendTSN(final BlockBuilder bb, final String selfRef, final List<GeneratedProperty> props) {
        bb.str(".jcTSB(").str(selfRef).eol(".class)");
        for (var prop : props) {
            // FIXME: this should be specialized in BitsTypeObjectTemplate
            bb.ind(isBit(prop) ? ".bit(" : ".prop(").jStr(prop.getName()).str(", ").str(fieldName(prop)).eol(")");
        }
        bb.ind(".build();").newLine();
    }

    // FIXME: this gates BitsTypeObject specializations
    private static boolean isBit(final GeneratedProperty prop) {
        return PRIMITIVE_BOOLEAN.equals(prop.getReturnType());
    }
}
