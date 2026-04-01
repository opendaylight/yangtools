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
import static org.opendaylight.yangtools.binding.contract.Naming.BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.BUILDER_SUFFIX;
import static org.opendaylight.yangtools.binding.contract.Naming.GETTER_PREFIX;
import static org.opendaylight.yangtools.binding.contract.Naming.KEY_SUFFIX;
import static org.opendaylight.yangtools.binding.contract.Naming.MODULE_INFO_QNAMEOF_METHOD_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.MODULE_INFO_YANGDATANAMEOF_METHOD_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.NAME_STATIC_FIELD_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.QNAME_STATIC_FIELD_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.VALUE_STATIC_FIELD_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.toFirstUpper;
import static org.opendaylight.yangtools.binding.generator.BindingGeneratorUtil.encodeAngleBrackets;
import static org.opendaylight.yangtools.binding.generator.BindingGeneratorUtil.replaceAllIllegalChars;
import static org.opendaylight.yangtools.binding.model.ri.BindingTypes.extractAugmentationTarget;
import static org.opendaylight.yangtools.binding.model.ri.BindingTypes.isNotificationBody;
import static org.opendaylight.yangtools.binding.model.ri.TypeConstants.PATTERN_CONSTANT_NAME;

import com.google.common.base.VerifyException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.stream.Collectors;
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
import org.opendaylight.yangtools.binding.model.api.UnionTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.YangSourceDefinition.Multiple;
import org.opendaylight.yangtools.binding.model.api.YangSourceDefinition.Single;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.common.YangDataName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
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

abstract class BaseTemplate extends JavaFileTemplate {
    private static final DeclaredStatementFormatter YANG_FORMATTER = DeclaredStatementFormatter.builder()
        .addIgnoredStatement(ContactStatement.DEF)
        .addIgnoredStatement(DescriptionStatement.DEF)
        .addIgnoredStatement(OrganizationStatement.DEF)
        .addIgnoredStatement(ReferenceStatement.DEF)
        .build();

    @NonNullByDefault
    BaseTemplate(final GeneratedType type) {
        super(type);
    }

    @NonNullByDefault
    BaseTemplate(final AbstractJavaGeneratedType javaType, final GeneratedType type) {
        super(javaType, type);
    }

    final @NonNull String generate() {
        final var sb = new StringBuilder()
            .append("package ").append(type().packageName()).append(";\n");

        // Has side-effects
        final var body = body();

        final var importBlock = generateImportBlock();
        if (!importBlock.isEmpty()) {
            sb.append(importBlock).append('\n');
        }
        return sb.append(body).toString();
    }

    private String generateImportBlock() {
        final var javaType = javaType();
        if (javaType() instanceof TopLevelJavaGeneratedType topLevel) {
            return topLevel.imports().map(name -> "import " + name + ";\n").collect(Collectors.joining());
        }
        throw new VerifyException("Unexpected type " + javaType);
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
            case NAME_STATIC_FIELD_NAME -> {
                @SuppressWarnings("unchecked")
                final var entry = (Entry<JavaTypeName, YangDataName>) constant.getValue();
                yield emitNameConstant(name, type, entry.getKey(), entry.getValue().name());
            }
            case QNAME_STATIC_FIELD_NAME -> {
                @SuppressWarnings("unchecked")
                final var entry = (Entry<JavaTypeName, String>) constant.getValue();
                yield emitQNameConstant(name, type, entry.getKey(), entry.getValue());
            }
            case VALUE_STATIC_FIELD_NAME -> emitValueConstant(name, type);
            default -> "public static final " + importedName(type) + ' ' + name + " = " + constant.getValue() + ";\n";
        };
    }

    @NonNullByDefault
    final String emitNameConstant(final String name, final Type type, final JavaTypeName yangModuleInfo,
            final String yangDataName) {
        return """
            /**
             * Yang Data template name of the statement represented by this class.
             */
            public static final\s""" + importedNonNull(type) + ' ' + name + " = " + importedName(yangModuleInfo)
                + '.' + MODULE_INFO_YANGDATANAMEOF_METHOD_NAME + "(\"" + yangDataName + "\");\n";
    }

    @NonNullByDefault
    final String emitQNameConstant(final String name, final Type type, final JavaTypeName yangModuleInfo,
            final String localName) {
        return """
            /**
             * YANG identifier of the statement represented by this class.
             */
            public static final\s""" + importedNonNull(type) + ' ' + name + " = " + importedName(yangModuleInfo)
                + '.' + MODULE_INFO_QNAMEOF_METHOD_NAME + "(\"" + localName + "\");\n";
    }

    // FIXME: return a Block
    @NonNullByDefault
    String emitValueConstant(final String name, final Type type) {
        final var typeName = importedName(type);
        final var override = importedName(OVERRIDE);

        return "/**\n"
            +  " * Singleton value representing the {@link " + typeName + "} identity.\n"
            +  " */\n"
            +  "public static final " + importedNonNull(type) + ' ' + name + " = new " + typeName + "() {\n"
            +  "    @java.io.Serial\n"
            +  "    private static final long serialVersionUID = 1L;\n"
            +  '\n'
            +  "    @" + override + '\n'
            +  "    public " + importedName(CLASS) + '<' + typeName + "> " + BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME
                + "() {\n"
            +  "        return " + typeName + ".class;\n"
            +  "    }\n"
            +  '\n'
            +  "    @" + override + '\n'
            +  "    public int hashCode() {\n"
            +  "        return " + typeName + ".class.hashCode();\n"
            +  "    }\n"
            +  '\n'
            +  "    @" + override + '\n'
            +  "    public boolean equals(final " + importedName(Types.objectType()) + " obj) {\n"
            +  "        return obj == this || obj instanceof " + typeName + " other\n"
            +  "            && " + typeName + ".class.equals(other." + BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME
                + "());\n"
            +  "    }\n"
            +  '\n'
            +  "    @" + override + '\n'
            +  "    public " + importedName(Types.STRING) + " toString() {\n"
            +  "        return " + importedName(MOREOBJECTS) + ".toStringHelper(\"" + typeName
                + "\").add(\"qname\", QNAME).toString();\n"
            +  "    }\n"
            +  '\n'
            +  "    @java.io.Serial\n"
            +  "    private Object readResolve() throws java.io.ObjectStreamException {\n"
            +  "        return " + name + ";\n"
            +  "    }\n"
            +  "};\n";
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
        // derive state
        final var fieldName = fieldName(field);
        final var methodName = getterMethodName(field);
        final var returnType = field.getReturnType();
        final var importedName = importedName(returnType);
        // any Java array type needs to be duplicated to prevent modification
        final var codeHelpers = returnType.isArray() ? importedName(CODEHELPERS) : null;

        // emit separately
        final var bb = new BlockBuilder()
            .str("public ").str(importedName).sp().str(methodName).str("()").oB()
            .str("    return ");
        if (codeHelpers != null) {
            bb.str(codeHelpers).str(".copyArray(").str(fieldName).eol(");");
        } else {
            bb.str(fieldName).eS();
        }
        return bb.cB();
    }

    /**
     * Template method which generates the setter method for {@code field}.
     *
     * @param field generated property with data about field which is generated as the setter method
     * @return string with the setter method source code in JAVA format
     */
    @NonNullByDefault
    final BlockBuilder asSetterMethod(final GeneratedProperty field) {
        final var fieldName = fieldName(field);
        final var fieldType = importedReturnType(field);
        final var suffix = toFirstUpper(field.getName());
        final var typeName = type().simpleName();

        return new BlockBuilder()
            .str("public ").str(typeName).str(" set").str(suffix).str("(").str(fieldType).str(" value)").oB()
                .ind("this.").str(fieldName).eol(" = value;")
                .ind("return this;").nl()
            .cB();
    }

    @NonNullByDefault
    String formatDataForJavaDoc(final GeneratedType type) {
        final var sb = new StringBuilder();
        final var comment = type.getComment();
        if (comment != null) {
            sb.append(comment.getJavadoc());
        }
        appendSnippet(sb, type);

        final var str = sb.toString();
        return str.isBlank() ? "" : str.stripTrailing() + '\n';
    }

    @NonNullByDefault
    final String formatDataForJavaDoc(final GeneratedType type, final String additionalComment) {
        final var comment = type.getComment();
        if (comment == null) {
            return additionalComment.isBlank() ? "" : additionalComment.stripTrailing() + '\n';
        }

        final var sb = new StringBuilder().append(comment.getJavadoc());
        appendSnippet(sb, type);
        return additionalComment.isBlank() ? sb.toString()
            : sb.append(additionalComment.stripTrailing()).append("\n\n\n").toString();
    }

    private void appendSnippet(final StringBuilder sb, final GeneratedType genType) {
        genType.getYangSourceDefinition().ifPresent(def -> {
            sb.append('\n');

            if (def instanceof Single single) {
                final var node = single.getNode();

                sb.append("<p>\n")
                    .append("This class represents the following YANG schema fragment defined in module <b>")
                    .append(def.getModule().argument().getLocalName()).append("</b>\n")
                    .append("<pre>\n");
                appendYangSnippet(sb, def.getModule(), ((EffectiveStatement<?, ?>) node).declared());
                sb.append("</pre>");

                if (node instanceof SchemaNode schema) {
//                    sb.append("The schema path to identify an instance is\n");
//                    appendPath(sb.append("<i>"), def.getModule(), schema.getPath().getPathFromRoot());
//                    sb.append("</i>\n");

                    if (schema instanceof ContainerSchemaNode || schema instanceof ListSchemaNode
                        || schema instanceof NotificationDefinition && !isNotificationBody(genType)) {
                        final var builderName = genType.simpleName() + BUILDER_SUFFIX;

                        sb.append("\n<p>To create instances of this class use {@link ").append(builderName)
                        .append("}.\n")
                        .append("@see ").append(builderName).append('\n');
                        if (node instanceof ListSchemaNode list) {
                            final var keyDef = list.getKeyDefinition();
                            if (!keyDef.isEmpty()) {
                                sb.append("@see ").append(genType.simpleName()).append(KEY_SUFFIX);
                            }
                            sb.append('\n');
                        }
                    }
                } else if (node instanceof AugmentEffectiveStatement) {
                    // Find target Augmentation<Foo> and reference Foo
                    final var augType = findAugmentationArgument(genType);
                    if (augType != null) {
                        sb.append("\n\n")
                        .append("@see ").append(importedName(augType));
                    }
                }
                if (node instanceof TypedefEffectiveStatement && genType instanceof GeneratedTransferObject genTO) {
                    final var augType = genTO.getSuperType();
                    if (augType != null) {
                        sb.append("\n\n")
                        .append("@see ").append(augType.simpleName());
                    }
                }
            } else if (def instanceof Multiple multiple) {
                sb.append("<pre>\n");
                for (var node : multiple.getNodes()) {
                    appendYangSnippet(sb, def.getModule(), ((EffectiveStatement<?, ?>) node).declared());
                }
                sb.append("</pre>\n");
            }
        });
    }

    private static void appendYangSnippet(final StringBuilder sb, final ModuleEffectiveStatement module,
            final DeclaredStatement<?> stmt) {
        for (String str : YANG_FORMATTER.toYangTextSnippet(module, stmt)) {
            sb.append(replaceAllIllegalChars(encodeAngleBrackets(encodeJavadocSymbols(str))));
        }
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
        final var bb = new BlockBuilder()
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

        final var bb = new BlockBuilder();
        restrictions.getRangeConstraint().ifPresent(range ->
            bb.blk(AbstractRangeGenerator.forType(actualType).generateRangeChecker(
                    toFirstUpper(field.getName()), range, this)));
        // FIXME: this call looks unlike the range checker call: it should be refactored to acquire a generator,
        //        so that we can suppress checker when not needed -- just like ranges do above
        restrictions.getLengthConstraint().ifPresent(length ->
            bb.blk(LengthGenerator.generateLengthChecker(fieldName(field), actualType, length, this))
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

        final var bb = new BlockBuilder();
        appendAsJavadoc(bb, text);
        return bb;
    }

    @NonNullByDefault
    static final void appendAsJavadoc(final BlockBuilder bb, final String text) {
        appendAsJavadoc(bb, text, false);
    }

    @NonNullByDefault
    static final void appendAsJavadoc(final BlockBuilder bb, final String text, final boolean indent) {
        appendIndent(bb, indent).eol("/**");

        final int length = text.length();
        int begin = 0;
        while (begin < length) {
            appendIndent(bb, indent).str(" *");

            final int nl = text.indexOf('\n', begin);
            final int end = nl != -1 ? nl : length;
            appendLine(bb, text, begin, end);
            begin = end + 1;
        }

        appendIndent(bb, indent).eol(" */");
    }

    @NonNullByDefault
    private static BlockBuilder appendIndent(final BlockBuilder bb, final boolean indent) {
        return indent ? bb.str("    ") : bb;
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

        final var bb = new BlockBuilder();
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
        final var bb = new BlockBuilder();
        while (true) {
            final var archetype = it.next();
            EnumTypeObjectTemplate.generateAsInner(javaType().getEnclosedType(archetype.name()), archetype, bb);
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
                final var innerJavaType = javaType().getEnclosedType(gto.name());
                innerClasses.add(gto instanceof UnionTypeObjectArchetype union
                    ? UnionTypeObjectTemplate.generateAsInner(innerJavaType, union)
                    : new ClassTemplate(innerJavaType, gto).generateAsInnerClass());
            }
        }
        if (innerClasses.isEmpty()) {
            return null;
        }

        final var bb = new BlockBuilder();
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
}
