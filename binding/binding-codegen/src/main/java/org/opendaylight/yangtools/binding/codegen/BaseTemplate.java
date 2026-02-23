/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import com.google.common.base.CharMatcher;
import com.google.common.base.VerifyException;
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
import org.eclipse.xtext.xbase.lib.StringExtensions;
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
import org.opendaylight.yangtools.binding.model.api.Restrictions;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.TypeMemberComment;
import org.opendaylight.yangtools.binding.model.ri.TypeConstants;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.common.YangDataName;

abstract class BaseTemplate extends JavaFileTemplate {
    private static final CharMatcher WS_MATCHER = CharMatcher.anyOf("\n\t");
    private static final Pattern SPACES_PATTERN = Pattern.compile(" +");

    BaseTemplate(final @NonNull GeneratedType type) {
        super(type);
    }

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
     * Generate the body of this Java file, i.e. the entire class declaration.
     *
     * @return Body of this Java file
     */
    abstract @NonNull CharSequence body();

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
     * Template method which generates sequence of the names of the class attributes from {@code parameters}.
     *
     * @param parameters group of generated property instances which are transformed to the sequence of parameter names
     * @return string with the list of the parameter names of the {@code parameters}
     */
    static final @NonNull String asArguments(final @NonNull List<GeneratedProperty> parameters) {
        final var it = parameters.iterator();
        if (!it.hasNext()) {
            return "";
        }

        final var sb = new StringBuilder();
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
            sb.append(importedName(parameter.getReturnType())).append(' ').append(fieldName(parameter));
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
    final CharSequence emitConstant(final Constant constant) {
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
    final CharSequence emitQNameConstant(final String name, final Type type, final JavaTypeName yangModuleInfo,
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
    CharSequence asGetterMethod(final GeneratedProperty field) {
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
        return sb.append(";\n}\n").toString();
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
        final var fieldType = importedName(field.getReturnType());
        final var suffix = StringExtensions.toFirstUpper(field.getName());
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
                    .generateRangeChecker(StringExtensions.toFirstUpper(field.getName()), range, this))
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

        final var sb = new StringBuilder();
        appendAsJavadoc(sb, "", text);
        return sb.toString();
    }

    @NonNullByDefault
    static final void appendAsJavadoc(final StringBuilder sb, final String indent, final String text) {
        sb.append(indent).append("/**\n");

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

        sb.append(indent).append(" */");
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
    final String checkArgument(final GeneratedProperty property, final Restrictions restrictions,
            final Type actualType, final String value) {
        final var valueRef = actualType instanceof ConcreteType ? value : value + ".getValue()";

        final var sb = new StringBuilder();
        if (restrictions.getRangeConstraint().isPresent()) {
            sb.append(AbstractRangeGenerator.forType(actualType)
                .generateRangeCheckerCall(StringExtensions.toFirstUpper(property.getName()), valueRef));
        }

        final var fieldName = fieldName(property);
        if (restrictions.getLengthConstraint().isPresent()) {
            sb.append(LengthGenerator.generateLengthCheckerCall(fieldName, valueRef));
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

        return sb.toString();
    }

    /**
     * {@return a string containing generated code for specified archetypes}
     * @param archetypes the {@link EnumTypeObjectArchetype}s to generate
     */
    @NonNullByDefault
    final String generateInnerEnumTypeObjects(final List<EnumTypeObjectArchetype> archetypes) {
        if (archetypes.isEmpty()) {
            return "";
        }

        final var it = archetypes.iterator();
        final var sb = new StringBuilder();
        while (true) {
            final var archetype = it.next();
            EnumTypeObjectTemplate.generateAsInner(javaType().getEnclosedType(archetype.name()), archetype, sb);
            if (!it.hasNext()) {
                return sb.toString();
            }
            sb.append('\n');
        }
    }
}
