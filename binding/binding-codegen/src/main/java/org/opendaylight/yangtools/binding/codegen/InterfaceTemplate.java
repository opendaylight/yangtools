/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.Locale;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.binding.model.api.Constant;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.ri.TypeConstants;
import org.opendaylight.yangtools.binding.model.ri.Types;

/**
 * Template for generating JAVA interfaces.
 */
class InterfaceTemplate extends BaseTemplate {
    /**
     * List of constant instances which are generated as JAVA public static final attributes.
     */
    final List<Constant> consts;

    /**
     * List of method signatures which are generated as method declarations.
     */
    final List<MethodSignature> methods;

    /**
     * List of enumeration which are generated as JAVA enum type.
     */
    final List<EnumTypeObjectArchetype> enums;

    /**
     * List of generated types which are enclosed inside the generated type.
     */
    final List<GeneratedType> enclosedGeneratedTypes;

    private @Nullable TypeAnalysis typeAnalysis;

    @NonNullByDefault
    InterfaceTemplate(final GeneratedType type) {
        super(type);
        consts = type.getConstantDefinitions();
        methods = type.getMethodDefinitions();
        enums = type.getEnumerations();
        enclosedGeneratedTypes = type.getEnclosedTypes();
    }

    private @NonNull TypeAnalysis typeAnalysis() {
        final var existing = typeAnalysis;
        return existing != null ? existing : loadTypeAnalysis();
    }

    private @NonNull TypeAnalysis loadTypeAnalysis() {
        final var analysis = TypeAnalysis.of(type());
        typeAnalysis = analysis;
        return analysis;
    }


    @Override
    CharSequence body() {
        //        «type.formatDataForJavaDoc.wrapToDocumentation»
        //        «type.annotations.generateAnnotations»
        //        «generatedAnnotation»
        //        public interface «type.simpleName»
        //            «superInterfaces»
        //        {
        //
        //            «generateInnerClasses»
        //
        //            «generateInnerEnumTypeObjects(enums)»
        //
        //            «generateConstants»
        //
        //            «generateMethods»
        //
        //        }
        //

        final var sc = new StringConcatenation();
        sc.append(wrapToDocumentation(formatDataForJavaDoc(type())));
        sc.newLineIfNotEmpty();
        sc.append(generateAnnotations(type().getAnnotations()));
        sc.newLineIfNotEmpty();
        sc.append(generatedAnnotation());
        sc.newLineIfNotEmpty();
        sc.append("public interface ");
        sc.append(type().simpleName());
        sc.newLineIfNotEmpty();
        sc.append("    ");
        sc.append(superInterfaces(), "    ");
        sc.newLineIfNotEmpty();
        sc.append("{");
        sc.newLine();
        sc.newLine();
        sc.append("    ");
        sc.append(generateInnerClasses(), "    ");
        sc.newLineIfNotEmpty();
        sc.newLine();
        sc.append("    ");
        sc.append(generateInnerEnumTypeObjects(enums), "    ");
        sc.newLineIfNotEmpty();
        sc.newLine();
        sc.append("    ");
        sc.append(generateConstants(), "    ");
        sc.newLineIfNotEmpty();
        sc.newLine();
        sc.append("    ");
        sc.append(generateMethods(), "    ");
        sc.newLineIfNotEmpty();
        sc.newLine();
        sc.append("}");
        sc.newLine();
        sc.newLine();
        return sc;
    }

    /**
     * {@return string with the code for the interface declaration in JAVA format}
     */
    private String superInterfaces() {
        final var ifaces = type().getImplements();
        if (ifaces.isEmpty()) {
            return "";
        }

        final var sb = new StringBuilder()
            .append("extends\n");
        final var it = ifaces.iterator();
        while (true) {
            sb.append(importedName(it.next()));
            if (!it.hasNext()) {
                return sb.toString();
            }
            sb.append(",\n");
        }
    }

    /**
     * {@return string with the source code for inner classes in JAVA format}
     */
    private CharSequence generateInnerClasses() {
        if (enclosedGeneratedTypes.isEmpty()) {
            return "";
        }
        final var innerClasses = enclosedGeneratedTypes.stream()
            .map(this::generateInnerClass)
            .filter(str -> !str.isEmpty())
            .toList();
        if (innerClasses.isEmpty()) {
            return "";
        }

        final var sc = new StringConcatenation();
        final var it = innerClasses.iterator();
        while (true) {
            sc.append(it.next());
            if (!it.hasNext()) {
                return sc;
            }
            sc.newLine();
        }
    }

    /**
     * {@return the code block containing this type's singleton constant declarations}
     */
    String generateConstants() {
        if (consts.isEmpty()) {
            return "";
        }

        final var sb = new StringBuilder();
        for (var constant : consts) {
            // Pattern constants are emitted separately
            if (!constant.getName().startsWith(TypeConstants.PATTERN_CONSTANT_NAME)) {
                sb.append(emitConstant(constant));
            }
        }
        return sb.toString();
    }

    final String generateDefaultImplementedInterface() {
        // Note: we cannot use importedName() or short name due to shadowing explained in MDSAL-365
        final var fqcn = type().canonicalName();

        return '@' + importedName(OVERRIDE) + '\n'
            +  "default " + importedName(CLASS) + '<' + fqcn + "> " + Naming.BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME
                + "() {\n"
            +  "    return " + fqcn + ".class;\n"
            +  "}\n";
    }

    /**
     * {@return string with the declaration of methods source code in JAVA forma}
     */
    CharSequence generateMethods() {
        if (methods.isEmpty()) {
            return "";
        }

        //        «FOR m : methods SEPARATOR "\n"»
        //            «IF m.isDefault»
        //                «generateDefaultMethod(m)»
        //            «ELSEIF m.isStatic»
        //                «generateStaticMethod(m)»
        //            «ELSEIF m.parameters.empty && m.name.isGetterMethodName»
        //                «generateAccessorMethod(m)»
        //            «ELSEIF m.parameters.empty && m.name.isNonnullMethodName»
        //                «generateNonnullAccessorMethod(m)»
        //            «ELSE»
        //                «generateMethod(m)»
        //            «ENDIF»
        //        «ENDFOR»

        var sc = new StringConcatenation();
        boolean hasElements = false;
        for (var method : methods) {
            if (!hasElements) {
                hasElements = true;
            } else {
                sc.appendImmediate("\n", "");
            }
            if (method.isDefault()) {
                sc.append(generateDefaultMethod(method));
            } else if (method.isStatic()) {
                sc.append(generateStaticMethod(method));
            } else if (method.getParameters().isEmpty() && Naming.isGetterMethodName(method.getName())) {
                sc.append(generateAccessorMethod(method));
            } else if (method.getParameters().isEmpty() && Naming.isNonnullMethodName(method.getName())) {
                sc.append(generateNonnullAccessorMethod(method));
            } else {
                sc.append(generateMethod(method));
            }
            sc.newLineIfNotEmpty();
        }
        return sc;
    }

    private CharSequence generateMethod(final MethodSignature method) {
        //        «method.comment.asJavadoc»
        //        «method.annotations.generateAnnotations»
        //        «method.returnType.importedName» «method.name»(«method.parameters.generateParameters»);

        final var sc = new StringConcatenation();
        sc.append(asJavadoc(method.getComment()));
        sc.newLineIfNotEmpty();
        sc.append(generateAnnotations(method.getAnnotations()));
        sc.newLineIfNotEmpty();
        sc.append(importedName(method.getReturnType()));
        sc.append(" ");
        sc.append(method.getName());
        sc.append("(");
        sc.append(generateParameters(method.getParameters()));
        sc.append(");");
        sc.newLineIfNotEmpty();
        return sc;
    }

    private CharSequence generateAnnotations(final @NonNull List<AnnotationType> annotations) {
        if (annotations.isEmpty()) {
            return "";
        }

        final var sc = new StringConcatenation();
        for (var annotation : annotations) {
            sc.append(generateAnnotation(annotation));
            sc.newLineIfNotEmpty();
        }
        return sc;
    }

    private CharSequence generateDefaultMethod(final MethodSignature method) {
        final var methodName = method.getName();
        if (Naming.isNonnullMethodName(methodName)) {
            return generateNonnullMethod(method);
        }
        if (Naming.isRequireMethodName(methodName)) {
            return generateRequireMethod(method);
        }
        return switch (methodName) {
            case Naming.BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME -> generateDefaultImplementedInterface();
            default ->
                JavaFileTemplate.VOID.equals(method.getReturnType().name()) ? generateNoopVoidInterfaceMethod(method)
                    : "";
        };
    }

    private CharSequence generateNonnullMethod(final MethodSignature method) {
        //        «val ret = method.returnType»
        //        «val name = method.name»
        //        «accessorJavadoc(method, ", or an empty list if it is not present.")»
        //        «method.annotations.generateAnnotations»
        //        default «ret.importedNonNull» «name»() {
        //            return «CODEHELPERS.importedName».nonnull(«name.getGetterMethodForNonnull»());
        //        }

        final var sc = new StringConcatenation();
        final var ret = method.getReturnType();
        sc.newLineIfNotEmpty();
        final var name = method.getName();
        sc.newLineIfNotEmpty();
        sc.append(accessorJavadoc(method, ", or an empty list if it is not present."));
        sc.newLineIfNotEmpty();
        sc.append(generateAnnotations(method.getAnnotations()));
        sc.newLineIfNotEmpty();
        sc.append("default ");
        sc.append(importedNonNull(ret));
        sc.append(" ");
        sc.append(name);
        sc.append("() {\n");
        sc.append("    return ");
        sc.append(importedName(JavaFileTemplate.CODEHELPERS), "    ");
        sc.append(".nonnull(");
        sc.append(Naming.getGetterMethodForNonnull(name), "    ");
        sc.append("());\n");
        sc.append("}\n");
        return sc;
    }

    private CharSequence generateNoopVoidInterfaceMethod(final MethodSignature method) {
        //        «method.comment.asJavadoc»
        //        «method.annotations.generateAnnotations»
        //        default «VOID.importedName» «method.name»(«method.parameters.generateParameters») {
        //            // No-op
        //        }

        final var sc = new StringConcatenation();
        sc.append(asJavadoc(method.getComment()));
        sc.newLineIfNotEmpty();
        sc.append(generateAnnotations(method.getAnnotations()));
        sc.newLineIfNotEmpty();
        sc.append("default ");
        sc.append(importedName(VOID));
        sc.append(" ");
        sc.append(method.getName());
        sc.append("(");
        sc.append(generateParameters(method.getParameters()));
        sc.append(") {\n");
        sc.append("    // No-op\n");
        sc.append("}\n");
        return sc;
    }

    private CharSequence generateRequireMethod(final MethodSignature method) {
        //        «val ret = method.returnType»
        //        «val name = method.name»
        //        «val fieldName = name.toLowerCase(Locale.ROOT).replace(REQUIRE_PREFIX, "")»
        //        «accessorJavadoc(method, ", guaranteed to be non-null.", NSEE)»
        //        default «ret.importedNonNull» «name»() {
        //            return «CODEHELPERS.importedName».require(«getGetterMethodForRequire(name)»(), "«fieldName»");
        //        }

        final var ret = method.getReturnType();
        final var name = method.getName();
        final var sc = new StringConcatenation();
        sc.append(accessorJavadoc(method, ", guaranteed to be non-null.", NSEE));
        sc.newLineIfNotEmpty();
        sc.append("default ");
        sc.append(importedNonNull(ret));
        sc.append(" ");
        sc.append(name);
        sc.append("() {\n");
        sc.append("    return ");
        sc.append(importedName(CODEHELPERS), "    ");
        sc.append(".require(");
        sc.append(Naming.getGetterMethodForRequire(name), "    ");
        sc.append("(), \"");
        sc.append(name.toLowerCase(Locale.ROOT).replace(Naming.REQUIRE_PREFIX, ""), "    ");
        sc.append("\");\n");
        sc.append("}\n");
        return sc;
    }

    private String generateAccessorMethod(final MethodSignature method) {
        //        «accessorJavadoc(method, ", or {@code null} if it is not present.")»
        //        «method.generateAccessorAnnotations»
        //        «method.returnType.nullableType» «method.name»();

        final var sc = new StringConcatenation();
        sc.append(accessorJavadoc(method, ", or {@code null} if it is not present."));
        sc.newLineIfNotEmpty();
        sc.append(generateAccessorAnnotations(method));
        sc.newLineIfNotEmpty();
        sc.append(nullableType(method.getReturnType()));
        sc.append(" ");
        sc.append(method.getName());
        sc.append("();\n");
        return sc.toString();
    }

    private CharSequence generateAccessorAnnotations(final MethodSignature method) {
        final var annotations = method.getAnnotations();
        if (annotations.isEmpty()) {
            return "";
        }

        final var sc = new StringConcatenation();
        sc.newLineIfNotEmpty();
        for (var annotation : annotations) {
            if (!Types.BOOLEAN.equals(method.getReturnType()) || !OVERRIDE.equals(annotation.name())) {
                sc.append(generateAnnotation(annotation));
                sc.newLineIfNotEmpty();
            }
        }
        return sc;
    }

    private String generateNonnullAccessorMethod(final MethodSignature method) {
        //        «accessorJavadoc(method, ", or an empty instance if it is not present.")»
        //        «method.annotations.generateAnnotations»
        //        «method.returnType.importedNonNull» «method.name»();

        final var sc = new StringConcatenation();
        sc.append(accessorJavadoc(method, ", or an empty instance if it is not present."));
        sc.newLineIfNotEmpty();
        sc.append(generateAnnotations(method.getAnnotations()));
        sc.newLineIfNotEmpty();
        sc.append(importedNonNull(method.getReturnType()));
        sc.append(" ");
        sc.append(method.getName());
        sc.append("();\n");
        return sc.toString();
    }

    private CharSequence generateStaticMethod(final MethodSignature method) {
        return switch (method.getName()) {
            case Naming.BINDING_EQUALS_NAME -> generateBindingEquals();
            case Naming.BINDING_HASHCODE_NAME -> generateBindingHashCode();
            case Naming.BINDING_TO_STRING_NAME -> generateBindingToString();
            default -> "";
        };
    }

    @VisibleForTesting
    final CharSequence generateBindingHashCode() {
        final var analysis = typeAnalysis();
        final boolean augmentable = analysis.augmentType() != null;
        final var props = analysis.properties();
        if (!augmentable && props.isEmpty()) {
            return "";
        }

        final var sc = new StringConcatenation();
        sc.append("/**\n");
        sc.append(" * Default implementation of {@link ");
        sc.append(importedName(OBJECT), " ");
        sc.append("#hashCode()} contract for this interface.\n");
        sc.append(
            " * Implementations of this interface are encouraged to defer to this method to get consistent hashing\n");
        sc.append(" * results across all implementations.\n");
        sc.append(" *\n");
        sc.append(" * @param obj Object for which to generate hashCode() result.\n");
        sc.append(" * @return Hash code value of data modeled by this interface.\n");
        sc.append(" * @throws ");
        sc.append(importedName(NPE), " ");
        sc.append(" if {@code obj} is {@code null}\n");
        sc.append(" */\n");
        sc.append("static int ");
        sc.append(Naming.BINDING_HASHCODE_NAME);
        sc.append("(final ");
        sc.append(fullyQualifiedNonNull(type()));
        sc.append(" obj) {\n");
        sc.append("    int result = 1;");
        sc.newLine();
        if (!props.isEmpty()) {
            sc.append("    final int prime = 31;\n");
            for (var property : props) {
                sc.append("    result = prime * result + ");
                sc.append(importedUtilClass(property), "    ");
                sc.append(".hashCode(obj.");
                sc.append(getterMethodName(property), "    ");
                sc.append("());\n");
            }
        }
        if (augmentable) {
            sc.append("    for (var augmentation : obj.augmentations().values()) {\n");
            sc.append("        result += augmentation.hashCode();\n");
            sc.append("    }\n");
        }
        sc.append("    return result;\n");
        sc.append("}\n");
        return sc;
    }

    private CharSequence generateBindingEquals() {
        final var analysis = typeAnalysis();
        final var augmentable = analysis.augmentType() != null;
        final var props = analysis.properties();
        if (!augmentable && props.isEmpty()) {
            return "";
        }

        final var sc = new StringConcatenation();
        sc.newLineIfNotEmpty();
        sc.append("/**\n");
        sc.append(" * Default implementation of {@link ");
        sc.append(importedName(OBJECT), " ");
        sc.append("#equals(");
        sc.append(importedName(OBJECT), " ");
        sc.append(")} contract for this interface.\n");
        sc.append(
            " * Implementations of this interface are encouraged to defer to this method to get consistent equality\n");
        sc.append(" * results across all implementations.\n");
        sc.append(" *\n");
        sc.append(" * @param thisObj Object acting as the receiver of equals invocation\n");
        sc.append(" * @param obj Object acting as argument to equals invocation\n");
        sc.append(" * @return True if thisObj and obj are considered equal\n");
        sc.append(" * @throws ");
        sc.append(importedName(NPE), " ");
        sc.append(" if {@code thisObj} is {@code null}\n");
        sc.append(" */\n");
        sc.append("static boolean ");
        sc.append(Naming.BINDING_EQUALS_NAME);
        sc.append("(final ");
        sc.append(fullyQualifiedNonNull(type()));
        sc.append(" thisObj, final ");
        sc.append(importedName(Types.objectType()));
        sc.append(" obj) {\n");
        sc.append("    if (thisObj == obj) {\n");
        sc.append("        return true;\n");
        sc.append("    }\n");
        sc.append("    final var other = ");
        sc.append(importedName(CODEHELPERS), "    ");
        sc.append(".checkCast(");
        sc.append(type().canonicalName(), "    ");
        sc.append(".class, obj);\n");
        sc.append("    return other != null\n");
        for (var property : ByTypeMemberComparator.sort(props)) {
            sc.append("        && ");
            sc.append(importedUtilClass(property), "        ");
            sc.append(".equals(thisObj.");
            sc.append(property.getGetterName(), "        ");
            sc.append("(), other.");
            sc.append(property.getGetterName(), "        ");
            sc.append("())\n");
        }
        sc.append("        ");
        if (augmentable) {
            sc.append("&& thisObj.augmentations().equals(other.augmentations())");
        }
        sc.append(";\n");
        sc.append("}\n");
        return sc;
    }

    @VisibleForTesting
    final CharSequence generateBindingToString() {
        final var analysis = typeAnalysis();

        final var sc = new StringConcatenation();
        sc.newLineIfNotEmpty();
        sc.append("/**\n");
        sc.append(" * Default implementation of {@link ");
        sc.append(importedName(OBJECT), " ");
        sc.append("#toString()} contract for this interface.\n");
        sc.append(
            " * Implementations of this interface are encouraged to defer to this method to get consistent string\n");
        sc.append(" * representations across all implementations.\n");
        sc.append(" *\n");
        sc.append(" * @param obj Object for which to generate toString() result.\n");
        sc.append(" * @return {@link ");
        sc.append(importedName(Types.STRING), " ");
        sc.append("} value of data modeled by this interface.\n");
        sc.append(" * @throws ");
        sc.append(importedName(NPE), " ");
        sc.append(" if {@code obj} is {@code null}\n");
        sc.append(" */\n");
        sc.append("static ");
        sc.append(importedName(Types.STRING));
        sc.append(" ");
        sc.append(Naming.BINDING_TO_STRING_NAME);
        sc.append("(final ");
        sc.append(fullyQualifiedNonNull(type()));
        sc.append(" obj) {\n");
        sc.append("    final var helper = ");
        sc.append(importedName(MOREOBJECTS), "    ");
        sc.append(".toStringHelper(\"");
        sc.append(type().simpleName(), "    ");
        sc.append("\");\n");
        for (var property : analysis.properties()) {
            sc.append("    ");
            sc.append(importedName(CODEHELPERS), "    ");
            sc.append(".appendValue(helper, \"");
            sc.append(property.getName(), "    ");
            sc.append("\", obj.");
            sc.append(property.getGetterName(), "    ");
            sc.append("());\n");
        }
        if (analysis.augmentType() != null) {
            sc.append("    ");
            sc.append(importedName(CODEHELPERS), "    ");
            sc.append(".appendAugmentations(helper, \"");
            sc.append(Naming.AUGMENTATION_FIELD, "    ");
            sc.append("\", obj);\n");
        }
        sc.append("    return helper.toString();\n");
        sc.append("}\n");
        return sc;
    }

    private String accessorJavadoc(final MethodSignature method, final String orString) {
        return accessorJavadoc(method, orString, null);
    }

    private String accessorJavadoc(final MethodSignature method, final String orString,
            final @Nullable JavaTypeName exception) {
        final var propName = propertyNameFromGetter(method);
        final var propReturn = propName + orString;

        //        return wrapToDocumentation('''
        //            Return «propReturn»
        //
        //            «method.comment?.referenceDescription.formatReference»
        //            @return {@code «method.returnType.importedName»} «propReturn»
        //            «IF exception !== null»
        //                @throws «exception.importedName» if «propName» is not present
        //            «ENDIF»
        //        ''')

        final var sc = new StringConcatenation();
        sc.append("Return ");
        sc.append(propReturn);
        sc.newLineIfNotEmpty();
        sc.newLine();
        final var comment = method.getComment();
        sc.append(formatReference(comment == null ? null : comment.referenceDescription()));
        sc.newLineIfNotEmpty();
        sc.append("@return {@code ");
        sc.append(importedName(method.getReturnType()));
        sc.append("} ");
        sc.append(propReturn);
        sc.newLineIfNotEmpty();
        if (exception != null) {
            sc.append("@throws ");
            sc.append(importedName(exception));
            sc.append(" if ");
            sc.append(propName);
            sc.append(" is not present\n");
        }
        return wrapToDocumentation(sc.toString());
    }

    @NonNullByDefault
    private String nullableType(final Type type) {
        if (isObject(type) && type instanceof ParameterizedType param
            && (Types.isMapType(param) || Types.isListType(param) || Types.isSetType(param))) {
            return importedNullable(type);
        }
        return importedName(type);
    }

    // The return type has a package, so it's not a primitive type
    private static boolean isObject(final Type type) {
        return !type.packageName().isEmpty();
    }
}
