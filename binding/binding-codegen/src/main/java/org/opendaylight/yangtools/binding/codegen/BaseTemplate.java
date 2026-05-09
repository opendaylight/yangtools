/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static com.google.common.base.Verify.verify;
import static org.opendaylight.yangtools.binding.codegen.Constants.MEMBER_PATTERN_LIST;
import static org.opendaylight.yangtools.binding.codegen.Constants.MEMBER_REGEX_LIST;
import static org.opendaylight.yangtools.binding.codegen.YangModuleInfoTemplate.QNAMEOF_METHOD_NAME;
import static org.opendaylight.yangtools.binding.codegen.YangModuleInfoTemplate.YANGDATANAMEOF_METHOD_NAME;
import static org.opendaylight.yangtools.binding.codegen.YangModuleInfoTemplate.nameInModuleOf;
import static org.opendaylight.yangtools.binding.codegen.YangModuleInfoTemplate.yangModuleInfoOf;
import static org.opendaylight.yangtools.binding.contract.Naming.BUILDER_SUFFIX;
import static org.opendaylight.yangtools.binding.contract.Naming.GETTER_PREFIX;
import static org.opendaylight.yangtools.binding.contract.Naming.KEY_SUFFIX;
import static org.opendaylight.yangtools.binding.contract.Naming.NAME_STATIC_FIELD_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.QNAME_STATIC_FIELD_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.toFirstUpper;
import static org.opendaylight.yangtools.binding.model.ri.BindingTypes.extractAugmentationTarget;
import static org.opendaylight.yangtools.binding.model.ri.BindingTypes.isNotificationBody;
import static org.opendaylight.yangtools.binding.model.ri.TypeConstants.PATTERN_CONSTANT_NAME;
import static org.opendaylight.yangtools.binding.model.ri.Types.PRIMITIVE_BOOLEAN;
import static org.opendaylight.yangtools.binding.model.ri.Types.STRING;

import com.google.common.base.VerifyException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.Constant;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Restrictions;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.ri.DocUtils;
import org.opendaylight.yangtools.yang.common.YangDataName;
import org.opendaylight.yangtools.yang.model.api.ContainerLikeCompat;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementEquivalent;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContactStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrganizationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.export.DeclaredStatementFormatter;

abstract sealed class BaseTemplate extends JavaFileTemplate
        permits AbstractBuilderTemplate, ArchetypeTemplate, ClassTemplate, EnumTypeObjectTemplate, InterfaceTemplate {
    static final Comparator<GeneratedProperty> PROP_COMPARATOR = Comparator.comparing(GeneratedProperty::getName);

    private static final DeclaredStatementFormatter YANG_FORMATTER = DeclaredStatementFormatter.builder()
        .addIgnoredStatement(ContactStatement.DEF)
        .addIgnoredStatement(DescriptionStatement.DEF)
        .addIgnoredStatement(OrganizationStatement.DEF)
        .addIgnoredStatement(ReferenceStatement.DEF)
        .build();

    /**
     * {@code java.lang.Boolean} as a JavaTypeName.
     */
    private static final @NonNull JavaTypeName BOOLEAN = JavaTypeName.create(Boolean.class);

    @NonNullByDefault
    BaseTemplate(final GeneratedClass javaType, final GeneratedType type) {
        super(javaType, type);
    }

    @Override
    final void generateTo(final Appendable out) throws IOException {
        // FIXME: of this code should live in GeneratedClass.of() or thereabout
        final var javaType = javaType();
        if (!(javaType instanceof GeneratedClass.TopLevel topLevel)) {
            throw new VerifyException("Unexpected type " + javaType);
        }

        // note: this has a side-effect of populating imports
        final var body = body().build();

        // package declaration
        out.append("package ").append(type().packageName()).append(";\n\n");

        // import block
        final var importedNames = topLevel.imports().toArray(JavaTypeName[]::new);
        for (var importedName : importedNames) {
            out.append("import ").append(importedName.canonicalName()).append(";\n");
        }
        if (importedNames.length != 0) {
            out.append('\n');
        }

        // body
        body.appendTo(out);
    }

    /**
     * {@return Body of this Java file}
     */
    abstract @NonNull BlockBuilder body();

    // Helper patterns
    static final @NonNull String fieldName(final GeneratedProperty property) {
        return "_" + property.getName();
    }

    /**
     * Template method which generates method parameters with their types from {@code parameters}.
     *
     * @param parameters list of parameter instances which are transformed to the method parameters
     * @return string with the list of the method parameters with their types in JAVA format
     */
    final @Nullable StringBuilder generateParameters(final @NonNull List<MethodSignature.Parameter> parameters) {
        final var it = parameters.iterator();
        if (!it.hasNext()) {
            return null;
        }

        final var sb = new StringBuilder();
        while (true) {
            final var parameter = it.next();
            sb.append(importedName(parameter.type())).append(' ').append(parameter.name());
            if (!it.hasNext()) {
                break;
            }
            sb.append(", ");
        }
        return sb;
    }

    /**
     * {@return string with the list of the parameter names of the {@code parameters}, separated by {@code ", "}}
     * @param parameters non-empty group of generated property instances which are transformed to the sequence
     *                   of parameter names, must not be empty
     */
    static final @NonNull String asArguments(final @NonNull List<GeneratedProperty> parameters) {
        final var sb = new StringBuilder();
        final var it = parameters.iterator();
        while (true) {
            sb.append(fieldName(it.next()));
            if (!it.hasNext()) {
                return sb.toString();
            }
            sb.append(", ");
        }
    }

    /**
     * Template method which generates method parameters with their types from {@code parameters}.
     *
     * @param parameters group of generated property instances which are transformed to the method parameters
     * @return string with the list of the method parameters with their types in Java format
     */
    final @NonNull String asArgumentsDeclaration(final @NonNull Iterable<GeneratedProperty> parameters) {
        final var it = parameters.iterator();
        if (!it.hasNext()) {
            return "";
        }

        final var sb = new StringBuilder();
        while (true) {
            final var parameter = it.next();
            sb.append(importedReturnType(parameter)).append(' ').append(fieldName(parameter));
            if (!it.hasNext()) {
                return sb.toString();
            }
            sb.append(", ");
        }
    }

    /**
     * Template method which generates method parameters with their types from <code>parameters</code>, annotating them
     * with {@link NonNull}.
     *
     * @param parameters group of generated property instances which are transformed to the method parameters
     * @return string with the list of the method parameters with their types in JAVA format
     */
    final @NonNull String asNonNullArgumentsDeclaration(final @NonNull List<GeneratedProperty> parameters) {
        final var it = parameters.iterator();
        if (!it.hasNext()) {
            return "";
        }

        final var sb = new StringBuilder();
        while (true) {
            final var parameter = it.next();
            sb.append(importedNonNull(parameter.getReturnType())).append(' ').append(fieldName(parameter));
            if (!it.hasNext()) {
                return sb.toString();
            }
            sb.append(", ");
        }
    }

    @NonNullByDefault
    final String emitConstant(final Constant constant) {
        final var name = constant.getName();
        final var type = constant.getType();

        return switch (name) {
            case NAME_STATIC_FIELD_NAME -> emitNameConstant(name, type, (YangDataName) constant.getValue());
            case QNAME_STATIC_FIELD_NAME -> emitQNameConstant(name, type, (String) constant.getValue());
            default -> "public static final " + importedName(type) + ' ' + name + " = " + constant.getValue() + ";\n";
        };
    }

    @NonNullByDefault
    private String emitNameConstant(final String name, final Type type, final YangDataName yangDataName) {
        final var yangModuleInfo = yangModuleInfoOf(yangDataName.module());
        return """
            /**
             * Yang Data template name of the statement represented by this class.
             */
            public static final\s""" + importedNonNull(type) + ' ' + name + " = " + importedName(yangModuleInfo)
                + '.' + YANGDATANAMEOF_METHOD_NAME + "(\"" + yangDataName.name() + "\");\n";
    }

    @NonNullByDefault
    final String emitQNameConstant(final String name, final Type type, final String localName) {
        final var yangModuleInfo = nameInModuleOf(type());
        return """
            /**
             * YANG identifier of the statement represented by this class.
             */
            public static final\s""" + importedNonNull(type) + ' ' + name + " = " + importedName(yangModuleInfo)
                + '.' + QNAMEOF_METHOD_NAME + "(\"" + localName + "\");\n";
    }

    /**
     * Template method which generates the getter method for {@code field}.
     *
     * @param field generated property with data about field which is generated as the getter method
     * @return string with the getter method source code in JAVA format
     */
    // FIXME: return a Block when we can do efficient copies
    @NonNullByDefault
    BlockBuilder asGetterMethod(final GeneratedProperty field) {
        return newBlockBuilder()
            .str("public ").str(importedReturnType(field)).sp().str(getterMethodName(field)).str("()").jBlock(bb -> {
                final var fieldName = fieldName(field);
                bb.str("return ");
                // any Java array type needs to be duplicated to prevent modification
                if (field.getReturnType().isArray()) {
                    bb.str(importedName(CODEHELPERS)).str(".copyArray(").str(fieldName).eol(");");
                } else {
                    bb.str(fieldName).eS();
                }
            }).nl();
    }

    /**
     * Template method which generates the setter method for {@code field}.
     *
     * @param field generated property with data about field which is generated as the setter method
     * @return string with the setter method source code in JAVA format
     */
    @NonNullByDefault
    final BlockBuilder asSetterMethod(final GeneratedProperty field) {
        final var fieldType = importedReturnType(field);
        final var suffix = toFirstUpper(field.getName());
        final var typeName = type().simpleName();

        return newBlockBuilder()
            .str("public ").str(typeName).str(" set").str(suffix).str("(").str(fieldType).str(" value)").jBlock(bb -> {
                bb
                    .str("this.").str(fieldName(field)).eol(" = value;")
                    .str("return this;").newLine();
            }).nl();
    }

    @NonNullByDefault
    String formatDataForJavaDoc(final GeneratedType type) {
        final var sb = new StringBuilder();
        final var comment = type.getComment();
        if (comment != null) {
            sb.append(comment.getJavadoc());
        }
        final var def = type.yangSourceDefinition();
        if (def != null) {
            appendSnippet(sb, type, def.getModule(), def.getNode());
        }

        final var str = sb.toString();
        return str.isBlank() ? "" : str.stripTrailing() + '\n';
    }

    final @Nullable BlockBuilder javadocBlock(final @NonNull ModuleEffectiveStatement module,
            final @NonNull DocumentedNode node) {
        final var sb = new StringBuilder();
        final var comment = DocUtils.typeCommentOf(node);
        if (comment != null) {
            sb.append(comment.getJavadoc());
        }
        appendSnippet(sb, type(), module, node);

        final var str = sb.toString();
        if (str.isBlank()) {
            return null;
        }

        final var bb = Block.builder();
        appendAsJavadoc(bb, str.stripTrailing() + '\n');
        return bb;
    }

    @NonNullByDefault
    private void appendSnippet(final StringBuilder sb, final GeneratedType type, final ModuleEffectiveStatement module,
            final DocumentedNode node) {
        appendYangSnippet(sb
            .append('\n')
            .append("<p>\n")
            .append("This class represents the following YANG schema fragment defined in module <b>")
            .append(module.argument().getLocalName()).append("</b>\n")
            .append("<pre>\n"),
            module, requireEffective(node).requireDeclared())
            .append("</pre>");

        if (node instanceof SchemaNode schema) {
            // sb.append("The schema path to identify an instance is\n");
            // appendPath(sb.append("<i>"), def.getModule(), schema.getPath().getPathFromRoot());
            // sb.append("</i>\n");

            if (schema instanceof ContainerSchemaNode || schema instanceof ListSchemaNode
                || schema instanceof NotificationDefinition && !isNotificationBody(type)) {
                final var simpleName = type.simpleName();
                final var builderName = simpleName + BUILDER_SUFFIX;

                sb
                    .append("\n<p>To create instances of this class use {@link ").append(builderName)
                    .append("}.\n")
                    .append("@see ").append(builderName).append('\n');
                if (node instanceof ListSchemaNode list) {
                    final var keyDef = list.getKeyDefinition();
                    if (!keyDef.isEmpty()) {
                        sb.append("@see ").append(simpleName).append(KEY_SUFFIX);
                    }
                    sb.append('\n');
                }
            }
        } else if (node instanceof AugmentEffectiveStatement) {
            // Find target Augmentation<Foo> and reference Foo
            final var augType = findAugmentationArgument(type);
            if (augType != null) {
                sb
                    .append("\n\n")
                    .append("@see ").append(importedName(augType));
            }
        }
        if (node instanceof TypedefEffectiveStatement && type instanceof GeneratedTransferObject genTO) {
            final var augType = genTO.getSuperType();
            if (augType != null) {
                sb
                    .append("\n\n")
                    .append("@see ").append(augType.simpleName());
            }
        }
    }

    // TODO: can we ditch this method?
    @NonNullByDefault
    private static EffectiveStatement<?, ?> requireEffective(final DocumentedNode node) {
        return switch (node) {
            case EffectiveStatementEquivalent<?> equivalent -> equivalent.asEffectiveStatement();
            case EffectiveStatement<?, ?> effective -> effective;
            case ContainerLikeCompat compat -> requireEffective(compat.delegate());
            default -> throw new VerifyException("Unsupported node " + node);
        };
    }

    // FIXME: return BlockFragment
    @NonNullByDefault
    private static StringBuilder appendYangSnippet(final StringBuilder sb, final ModuleEffectiveStatement module,
            final DeclaredStatement<?> stmt) {
        for (var str : YANG_FORMATTER.toYangTextSnippet(module, stmt)) {
            sb.append(DocUtils.replaceAllIllegalChars(DocUtils.encodeAngleBrackets(encodeJavadocSymbols(str))));
        }
        return sb;
    }

    private static @Nullable Type findAugmentationArgument(final GeneratedType genType) {
        for (var implType : genType.getImplements()) {
            if (implType instanceof ParameterizedType parameterized) {
                final var augmentType = extractAugmentationTarget(parameterized);
                if (augmentType != null) {
                    return augmentType;
                }
            }
        }
        return null;
    }

    @NonNullByDefault
    final BlockBuilder generateAnnotation(final AnnotationType annotation) {
        final var bb = newBlockBuilder()
            .at().str(importedName(annotation));

        final var params = annotation.getParameters();
        if (params != null && !params.isEmpty()) {
            bb.eol("(");

            final var it = params.iterator();
            while (true) {
                final var param = it.next();
                bb.str("    ").str(param.getName()).str("=").str(param.getValue());
                if (!it.hasNext()) {
                    break;
                }
                bb.eol(",");
            }

            bb.nl().str(")");
        }

        return bb.nl();
    }

    @NonNullByDefault
    final BlockBuilder generateCheckers(final GeneratedProperty field, final Restrictions restrictions,
            final Type actualType) {
        verify(!restrictions.isEmpty());

        final var bb = newBlockBuilder();
        restrictions.getRangeConstraint().ifPresent(range ->
            bb.blk(AbstractRangeGenerator.forType(actualType).generateRangeChecker(
                    toFirstUpper(field.getName()), range, javaType())));
        // FIXME: this call looks unlike the range checker call: it should be refactored to acquire a generator,
        //        so that we can suppress checker when not needed -- just like ranges do above
        restrictions.getLengthConstraint().ifPresent(length ->
            bb.blk(LengthGenerator.generateLengthChecker(fieldName(field), actualType, length, javaType()))
        );
        return bb;
    }

    // FIXME: remove this concatenation
    static final @NonNull String getterMethodName(final @NonNull String propName) {
        return GETTER_PREFIX + toFirstUpper(propName);
    }

    static final @NonNull String getterMethodName(final GeneratedProperty field) {
        return getterMethodName(field.getName());
    }

    static final @Nullable BlockBuilder wrapToDocumentation(final @NonNull String text) {
        // TODO: isBlank()?
        if (text.isEmpty()) {
            return null;
        }

        final var bb = Block.builder();
        appendAsJavadoc(bb, text);
        return bb;
    }

    @NonNullByDefault
    static final void appendAsJavadoc(final BlockBuilder bb, final String text) {
        bb.eol("/**");

        final int length = text.length();
        int begin = 0;
        while (begin < length) {
            bb.str(" *");

            final int nl = text.indexOf('\n', begin);
            final int end = nl != -1 ? nl : length;
            appendLine(bb, text, begin, end);
            begin = end + 1;
        }

        bb.eol(" */");
    }

    @NonNullByDefault
    private static void appendLine(final BlockBuilder bb, final String str, final int start, final int limit) {
        // do not emit obvious trailing whitespace
        int end = limit;
        while (true) {
            if (end == start) {
                bb.newLine();
                return;
            }

            final int prev = end - 1;
            final char ch = str.charAt(prev);
            if (ch != ' ' && ch != '\t') {
                break;
            }

            end = prev;
        }

        bb.sp().eol(str, start, end);
    }

    // FIXME: return a Block
    @NonNullByDefault
    final BlockBuilder checkArgument(final GeneratedProperty property, final Restrictions restrictions,
            final Type actualType, final String value) {
        verify(!restrictions.isEmpty());

        final var valueRef = actualType instanceof ConcreteType ? value : value + ".getValue()";

        final var bb = newBlockBuilder();
        if (restrictions.getRangeConstraint().isPresent()) {
            AbstractRangeGenerator.forType(actualType)
                .appendCheckerCall(bb, toFirstUpper(property.getName()), valueRef);
        }

        final var fieldName = fieldName(property);
        if (restrictions.getLengthConstraint().isPresent()) {
            LengthGenerator.appendCheckerCall(bb, fieldName, valueRef);
        }

        final var fieldUpperCase = fieldName.toUpperCase(Locale.ROOT);

        for (var currentConstant : type().getConstantDefinitions()) {
            final var currentName = currentConstant.getName();

            if (currentName.startsWith(PATTERN_CONSTANT_NAME)
                && fieldUpperCase.equals(currentName.substring(PATTERN_CONSTANT_NAME.length()))) {
                bb.str(importedName(CODEHELPERS)).str(".checkPattern(value, " + MEMBER_PATTERN_LIST)
                    .str(fieldName).str(", " + MEMBER_REGEX_LIST).str(fieldName).eol(");");
            }
        }

        return bb;
    }

    /**
     * {@return a string containing generated code for specified archetypes}
     * @param archetypes the {@link EnumTypeObjectArchetype}s to generate
     */
    final @Nullable BlockBuilder generateInnerEnumTypeObjects(
            final @NonNull List<EnumTypeObjectArchetype> archetypes) {
        if (archetypes.isEmpty()) {
            return null;
        }

        final var it = archetypes.iterator();
        final var bb = newBlockBuilder();
        while (true) {
            final var archetype = it.next();
            EnumTypeObjectTemplate.generateAsInner(javaType().getNestedClass(archetype), archetype, bb);
            if (!it.hasNext()) {
                return bb;
            }
            bb.newLine();
        }
    }

    final @Nullable BlockBuilder generateInnerClasses(final List<GeneratedType> innerTypes) {
        final var innerClasses = new ArrayList<BlockBuilder>();
        for (var innerType : innerTypes) {
            if (innerType instanceof GeneratedTransferObject gto) {
                final var innerJavaType = javaType().getNestedClass(gto);
                innerClasses.add(ClassTemplate.generateAsInner(innerJavaType, gto));
            }
        }
        if (innerClasses.isEmpty()) {
            return null;
        }

        final var bb = newBlockBuilder();
        final var it = innerClasses.iterator();
        while (true) {
            bb.blk(it.next());
            if (!it.hasNext()) {
                break;
            }
            bb.newLine();
        }
        return bb;
    }

    final @Nullable BlockBuilder annotationDeclaration() {
        final var annotations = type().getAnnotations();
        if (annotations.isEmpty()) {
            return null;
        }

        final var bb = newBlockBuilder();
        for (var annotation : annotations) {
            bb.at().eol(annotation.simpleName());
        }
        return bb;
    }

    final @Nullable BlockBuilder deprecatedAnnotation(final DocumentedNode.@NonNull WithStatus node) {
        return switch (node.getStatus()) {
            case CURRENT -> null;
            case DEPRECATED -> newBlockBuilder().at().eol(importedName(DEPRECATED));
            case OBSOLETE -> newBlockBuilder().at().str(importedName(DEPRECATED)).eol("(forRemoval = true)");
        };
    }

    final @NonNull BlockBuilder generateHashCode(final List<GeneratedProperty> props) {
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

    final @NonNull BlockBuilder generateEquals(final List<GeneratedProperty> props) {
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

    final @NonNull BlockBuilder generateToString(final List<GeneratedProperty> props) {
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

    @NonNullByDefault
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

    @NonNullByDefault
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
