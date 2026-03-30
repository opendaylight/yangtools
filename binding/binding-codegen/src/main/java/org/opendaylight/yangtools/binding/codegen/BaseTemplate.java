/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.opendaylight.yangtools.binding.generator.BindingGeneratorUtil.encodeAngleBrackets;
import static org.opendaylight.yangtools.binding.generator.BindingGeneratorUtil.replaceAllIllegalChars;

import com.google.common.base.CharMatcher;
import com.google.common.base.VerifyException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.generator.BindingGeneratorUtil;
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
import org.opendaylight.yangtools.binding.model.api.TypeMemberComment;
import org.opendaylight.yangtools.binding.model.api.UnionTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.YangSourceDefinition.Multiple;
import org.opendaylight.yangtools.binding.model.api.YangSourceDefinition.Single;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.binding.model.ri.TypeConstants;
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
    private static final CharMatcher WS_MATCHER = CharMatcher.anyOf("\n\t");
    private static final Pattern SPACES_PATTERN = Pattern.compile(" +");
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
    final @NonNull String generateParameters(final @NonNull List<MethodSignature.Parameter> parameters) {
        final var it = parameters.iterator();
        if (!it.hasNext()) {
            return "";
        }

        final var sb = new StringBuilder();
        while (true) {
            final var parameter = it.next();
            sb.append(importedName(parameter.type())).append(' ').append(parameter.name());
            if (!it.hasNext()) {
                return sb.toString();
            }
            sb.append(", ");
        }
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
            case Naming.NAME_STATIC_FIELD_NAME -> {
                @SuppressWarnings("unchecked")
                final var entry = (Entry<JavaTypeName, YangDataName>) constant.getValue();
                yield emitNameConstant(name, type, entry.getKey(), entry.getValue().name());
            }
            case Naming.QNAME_STATIC_FIELD_NAME -> {
                @SuppressWarnings("unchecked")
                final var entry = (Entry<JavaTypeName, String>) constant.getValue();
                yield emitQNameConstant(name, type, entry.getKey(), entry.getValue());
            }
            case Naming.VALUE_STATIC_FIELD_NAME -> emitValueConstant(name, type);
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
            public static final\s""" + importedNonNull(type) + ' ' + name + " = " + importedName(yangModuleInfo) + '.'
                + Naming.MODULE_INFO_YANGDATANAMEOF_METHOD_NAME + "(\"" + yangDataName + "\");\n";
    }

    @NonNullByDefault
    final String emitQNameConstant(final String name, final Type type, final JavaTypeName yangModuleInfo,
            final String localName) {
        return """
            /**
             * YANG identifier of the statement represented by this class.
             */
            public static final\s""" + importedNonNull(type) + ' ' + name + " = " + importedName(yangModuleInfo) + '.'
                + Naming.MODULE_INFO_QNAMEOF_METHOD_NAME + "(\"" + localName + "\");\n";
    }

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
            +  "    public " + importedName(CLASS) + '<' + typeName + "> "
                + Naming.BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME + "() {\n"
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
            +  "            && " + typeName + ".class.equals(other."
                + Naming.BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME + "());\n"
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
    @NonNullByDefault
    StringBuilder asGetterMethod(final GeneratedProperty field) {
        // derive state
        final var fieldName = fieldName(field);
        final var methodName = getterMethodName(field);
        final var returnType = field.getReturnType();
        final var importedName = importedName(returnType);
        // any Java array type needs to be duplicated to prevent modification
        final var codeHelpers = returnType.simpleName().endsWith("[]") ? importedName(CODEHELPERS) : null;

        // emit separately
        final var sb = new StringBuilder()
            .append("public ").append(importedName).append(' ').append(methodName).append("() {\n")
            .append("    return ");
        if (codeHelpers != null) {
            sb.append(codeHelpers).append(".copyArray(").append(fieldName).append(')');
        } else {
            sb.append(fieldName);
        }
        return sb.append(";\n}\n");
    }

    /**
     * Template method which generates the setter method for {@code field}.
     *
     * @param field generated property with data about field which is generated as the setter method
     * @return string with the setter method source code in JAVA format
     */
    @NonNullByDefault
    final CharSequence asSetterMethod(final GeneratedProperty field) {
        final var fieldName = fieldName(field);
        final var fieldType = importedReturnType(field);
        final var suffix = Naming.toFirstUpper(field.getName());
        final var typeName = type().simpleName();

        return new StringBuilder()
            .append("public ").append(typeName).append(" set").append(suffix).append('(').append(fieldType)
                .append(" value) {\n")
            .append("    this.").append(fieldName).append(" = value;\n")
            .append("    return this;\n")
            .append("}\n")
            .toString();
    }

    /**
     * Template method which generates JAVA comments.
     *
     * @param comment string with the comment for whole JAVA class
     * @return string with comment in JAVA format
     */
    static final @NonNull String asJavadoc(final @Nullable TypeMemberComment comment) {
        if (comment == null) {
            return "";
        }

        final var sb = new StringBuilder();
        final var contract = comment.contractDescription();
        if (contract != null) {
            sb.append(contract).append("\n\n");
        }
        final var reference = comment.referenceDescription();
        if (reference != null) {
            sb.append(formatReference(reference));
        }
        final var signature = comment.typeSignature();
        if (signature != null) {
            sb.append(signature).append('\n');
        }
        return wrapToDocumentation(sb.toString());
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
                        || schema instanceof NotificationDefinition && !BindingTypes.isNotificationBody(genType)) {
                        final String builderName = genType.simpleName() + Naming.BUILDER_SUFFIX;

                        sb.append("\n<p>To create instances of this class use {@link ").append(builderName)
                        .append("}.\n")
                        .append("@see ").append(builderName).append('\n');
                        if (node instanceof ListSchemaNode list) {
                            final var keyDef = list.getKeyDefinition();
                            if (!keyDef.isEmpty()) {
                                sb.append("@see ").append(genType.simpleName()).append(Naming.KEY_SUFFIX);
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
                final var augmentType = BindingTypes.extractAugmentationTarget(parameterized);
                if (augmentType != null) {
                    return augmentType;
                }
            }
        }
        return null;
    }

    static final @NonNull String formatReference(final @Nullable String reference) {
        if (reference == null) {
            return "";
        }

        final var sb = new StringBuilder().append("""
            <pre>
                <code>
            """);

        // FIXME: use a {@code} block which will render some of this encoding superfluous, but it requires paying
        //        attention to '}' pairing in input
        var formattedText = BindingGeneratorUtil.encodeAngleBrackets(reference);
        formattedText = WS_MATCHER.replaceFrom(JavaFileTemplate.encodeJavadocSymbols(formattedText), ' ');
        formattedText = SPACES_PATTERN.matcher(formattedText).replaceAll(" ");

        // FIXME: can we NOT use StringTokenizer here?
        var lineBuilder = new StringBuilder();
        var isFirstElementOnNewLineEmptyChar = false;
        final var tokenizer = new StringTokenizer(formattedText, " ", true);
        while (tokenizer.hasMoreTokens()) {
            final var nextElement = tokenizer.nextToken();
            final var lbLength = lineBuilder.length();

            if (lbLength != 0 && lbLength + nextElement.length() > 80) {
                final var limit = lbLength - 1;
                if (lineBuilder.charAt(limit) == ' ') {
                    lineBuilder.setLength(limit);
                }
                // FIXME: use append(CharSequence, int, int) instead
                if (!lineBuilder.isEmpty() && lineBuilder.charAt(0) == ' ') {
                    lineBuilder.deleteCharAt(0);
                }
                sb.append("        ").append(lineBuilder).append('\n');
                lineBuilder.setLength(0);

                if (" ".equals(nextElement)) {
                    isFirstElementOnNewLineEmptyChar = !isFirstElementOnNewLineEmptyChar;
                }
            }
            if (isFirstElementOnNewLineEmptyChar) {
                isFirstElementOnNewLineEmptyChar = !isFirstElementOnNewLineEmptyChar;
            } else {
                lineBuilder.append(nextElement);
            }
        }
        if (!lineBuilder.isEmpty()) {
            sb.append("        ").append(lineBuilder).append('\n');
        }

        return sb.append("""
                </code>
            </pre>

            """)
            .toString();
    }

    @NonNullByDefault
    final String generateAnnotation(final AnnotationType annotation) {
        final var sb = new StringBuilder()
            .append('@').append(importedName(annotation));

        final var params = annotation.getParameters();
        if (params != null && !params.isEmpty()) {
            sb.append("(\n");

            final var it = params.iterator();
            while (true) {
                final var param = it.next();
                sb.append("    ").append(param.getName()).append('=').append(param.getValue());
                if (!it.hasNext()) {
                    break;
                }
                sb.append(",\n");
            }

            sb.append("\n)");
        }

        return sb.append('\n').toString();
    }

    @NonNullByDefault
    final String generateCheckers(final GeneratedProperty field, final Restrictions restrictions,
            final Type actualType) {
        final var sb = new StringBuilder();
        restrictions.getRangeConstraint().ifPresent(
            range -> sb
                .append(AbstractRangeGenerator.forType(actualType)
                    .generateRangeChecker(Naming.toFirstUpper(field.getName()), range, this))
        );
        // FIXME: this call looks unlike the range checker call: it should be refactored to acquire a generator,
        //        so that we can suppress checker when not needed -- just like ranges do above
        restrictions.getLengthConstraint().ifPresent(
            length -> sb.append(LengthGenerator.generateLengthChecker(fieldName(field), actualType, length, this))

        );
        return sb.toString();
    }

    static final @NonNull String getterMethodName(final @NonNull String propName) {
        return Naming.GETTER_PREFIX + Naming.toFirstUpper(propName);
    }

    static final @NonNull String getterMethodName(final GeneratedProperty field) {
        return getterMethodName(field.getName());
    }

    /**
     * Return properties participating in the construction of a key type. Returned list is guaranteed to be ordered to
     * match order the type constructor expects.
     *
     * @param keyType key type
     * @return properties participating in the construction of a key type, in constructor order
     */
    static final @NonNull List<GeneratedProperty> keyConstructorArgs(final GeneratedTransferObject keyType) {
        return keyType.getProperties().stream()
            .sorted(Comparator.comparing(GeneratedProperty::getName))
            .collect(Collectors.toList());
    }

    @NonNullByDefault
    static final String wrapToDocumentation(final String text) {
        // TODO: isBlank()?
        if (text.isEmpty()) {
            return "";
        }

        final var bb = new BlockBuilder();
        appendAsJavadoc(bb, "", text);
        return bb.toRawString();
    }

    @NonNullByDefault
    static final void appendAsJavadoc(final BlockBuilder bb, final String indent, final String text) {
        final var sb = new StringBuilder().append(indent).append("/**\n");

        final int length = text.length();
        int begin = 0;
        while (begin < length) {
            sb.append(indent).append(" *");

            final int nl = text.indexOf('\n', begin);
            final int end = nl != -1 ? nl : length;
            appendLine(sb, text, begin, end);
            sb.append('\n');
            begin = end + 1;
        }

        bb.append(sb.append(indent).append(" */").toString());
    }

    @NonNullByDefault
    private static void appendLine(final StringBuilder sb, final String str, final int start, final int limit) {
        // do not emit obvious trailing whitespace
        int end = limit;
        while (true) {
            if (end == start) {
                return;
            }

            final int prev = end - 1;
            final char ch = str.charAt(prev);
            if (ch != ' ' && ch != '\t') {
                break;
            }

            end = prev;
        }

        sb.append(' ').append(str, start, end);
    }

    @NonNullByDefault
    final StringBuilder checkArgument(final GeneratedProperty property, final Restrictions restrictions,
            final Type actualType, final String value) {
        final var valueRef = actualType instanceof ConcreteType ? value : value + ".getValue()";

        final var sb = new StringBuilder();
        if (restrictions.getRangeConstraint().isPresent()) {
            AbstractRangeGenerator.forType(actualType)
                .appendCheckerCall(sb, Naming.toFirstUpper(property.getName()), valueRef);
        }

        final var fieldName = fieldName(property);
        if (restrictions.getLengthConstraint().isPresent()) {
            LengthGenerator.appendCheckerCall(sb, fieldName, valueRef);
        }

        final var fieldUpperCase = fieldName.toUpperCase(Locale.ROOT);

        for (var currentConstant : type().getConstantDefinitions()) {
            final var currentName = currentConstant.getName();

            if (currentName.startsWith(TypeConstants.PATTERN_CONSTANT_NAME)
                && fieldUpperCase.equals(currentName.substring(TypeConstants.PATTERN_CONSTANT_NAME.length()))) {
                sb.append(importedName(CODEHELPERS)).append(".checkPattern(value, ")
                    .append(Constants.MEMBER_PATTERN_LIST).append(fieldName).append(", ")
                    .append(Constants.MEMBER_REGEX_LIST).append(fieldName).append(");\n");
            }
        }

        return sb;
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
            bb.append(it.next());
            if (!it.hasNext()) {
                break;
            }
            bb.append("\n");
        }
        return bb;
    }
}
