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
    private final List<Constant> consts;
    /**
     * List of method signatures which are generated as method declarations.
     */
    private final List<MethodSignature> methods;
    /**
     * List of enumeration which are generated as JAVA enum type.
     */
    private final List<EnumTypeObjectArchetype> enums;
    /**
     * List of generated types which are enclosed inside the generated type.
     */
    private final List<GeneratedType> enclosedGeneratedTypes;

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

        final var bb = new BlockBuilder();
        bb.append(wrapToDocumentation(formatDataForJavaDoc(type())));
        bb.newLineIfNotEmpty();
        bb.append(generateAnnotations(type().getAnnotations()));
        bb.newLineIfNotEmpty();
        bb.append(generatedAnnotation());
        bb.newLineIfNotEmpty();
        bb.append("public interface ");
        bb.append(type().simpleName());
        bb.newLineIfNotEmpty();
        bb.append("    ");
        bb.append(superInterfaces(), "    ");
        bb.newLineIfNotEmpty();
        bb.append("{\n");
        bb.nl().append("    ");
        bb.append(generateInnerClasses(enclosedGeneratedTypes), "    ");
        bb.newLineIfNotEmpty();
        bb.nl().append("    ");
        bb.append(generateInnerEnumTypeObjects(enums), "    ");
        bb.newLineIfNotEmpty();
        bb.nl().append("    ");
        bb.append(generateConstants(), "    ");
        bb.newLineIfNotEmpty();
        bb.nl().append("    ");
        bb.append(generateMethods(), "    ");
        bb.newLineIfNotEmpty();
        bb.nl().append("}");
        return bb
            .nl()
            .nl();
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

        var bb = new BlockBuilder();
        boolean hasElements = false;
        for (var method : methods) {
            if (!hasElements) {
                hasElements = true;
            } else {
                bb.appendImmediate("\n", "");
            }
            if (method.isDefault()) {
                bb.append(generateDefaultMethod(method));
            } else if (method.isStatic()) {
                bb.append(generateStaticMethod(method));
            } else if (method.getParameters().isEmpty() && Naming.isGetterMethodName(method.getName())) {
                bb.append(generateAccessorMethod(method));
            } else if (method.getParameters().isEmpty() && Naming.isNonnullMethodName(method.getName())) {
                bb.append(generateNonnullAccessorMethod(method));
            } else {
                bb.append(generateMethod(method));
            }
            bb.newLineIfNotEmpty();
        }
        return bb;
    }

    private @NonNull BlockBuilder generateMethod(final MethodSignature method) {
        //        «method.comment.asJavadoc»
        //        «method.annotations.generateAnnotations»
        //        «method.returnType.importedName» «method.name»(«method.parameters.generateParameters»);

        final var bb = new BlockBuilder();
        bb.append(asJavadoc(method.getComment()));
        bb.newLineIfNotEmpty();
        bb.append(generateAnnotations(method.getAnnotations()));
        bb.newLineIfNotEmpty();
        bb.append(importedReturnType(method));
        bb.append(" ");
        bb.append(method.getName());
        bb.append("(");
        bb.append(generateParameters(method.getParameters()));
        bb.append(");");
        bb.newLineIfNotEmpty();
        return bb;
    }

    private CharSequence generateAnnotations(final @NonNull List<AnnotationType> annotations) {
        if (annotations.isEmpty()) {
            return "";
        }

        final var bb = new BlockBuilder();
        for (var annotation : annotations) {
            bb.append(generateAnnotation(annotation));
            bb.newLineIfNotEmpty();
        }
        return bb;
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

    private BlockBuilder generateNonnullMethod(final MethodSignature method) {
        //        «val ret = method.returnType»
        //        «val name = method.name»
        //        «accessorJavadoc(method, ", or an empty list if it is not present.")»
        //        «method.annotations.generateAnnotations»
        //        default «ret.importedNonNull» «name»() {
        //            return «CODEHELPERS.importedName».nonnull(«name.getGetterMethodForNonnull»());
        //        }

        final var bb = new BlockBuilder();
        final var ret = method.getReturnType();
        bb.newLineIfNotEmpty();
        final var name = method.getName();
        bb.newLineIfNotEmpty();
        bb.append(accessorJavadoc(method, ", or an empty list if it is not present."));
        bb.newLineIfNotEmpty();
        bb.append(generateAnnotations(method.getAnnotations()));
        bb.newLineIfNotEmpty();
        bb.append("default ");
        bb.append(importedNonNull(ret));
        bb.append(" ");
        bb.append(name);
        bb.append("() {\n");
        bb.append("    return ");
        bb.append(importedName(JavaFileTemplate.CODEHELPERS), "    ");
        bb.append(".nonnull(");
        bb.append(Naming.getGetterMethodForNonnull(name), "    ");
        bb.append("());\n");
        bb.append("}\n");
        return bb;
    }

    private CharSequence generateNoopVoidInterfaceMethod(final MethodSignature method) {
        //        «method.comment.asJavadoc»
        //        «method.annotations.generateAnnotations»
        //        default «VOID.importedName» «method.name»(«method.parameters.generateParameters») {
        //            // No-op
        //        }

        final var bb = new BlockBuilder();
        bb.append(asJavadoc(method.getComment()));
        bb.newLineIfNotEmpty();
        bb.append(generateAnnotations(method.getAnnotations()));
        bb.newLineIfNotEmpty();
        bb.append("default ");
        bb.append(importedName(VOID));
        bb.append(" ");
        bb.append(method.getName());
        bb.append("(");
        bb.append(generateParameters(method.getParameters()));
        bb.append(") {\n");
        bb.append("    // No-op\n");
        bb.append("}\n");
        return bb;
    }

    private BlockBuilder generateRequireMethod(final MethodSignature method) {
        //        «val ret = method.returnType»
        //        «val name = method.name»
        //        «val fieldName = name.toLowerCase(Locale.ROOT).replace(REQUIRE_PREFIX, "")»
        //        «accessorJavadoc(method, ", guaranteed to be non-null.", NSEE)»
        //        default «ret.importedNonNull» «name»() {
        //            return «CODEHELPERS.importedName».require(«getGetterMethodForRequire(name)»(), "«fieldName»");
        //        }

        final var ret = method.getReturnType();
        final var name = method.getName();
        final var bb = new BlockBuilder();
        bb.append(accessorJavadoc(method, ", guaranteed to be non-null.", NSEE));
        bb.newLineIfNotEmpty();
        bb.append("default ");
        bb.append(importedNonNull(ret));
        bb.append(" ");
        bb.append(name);
        bb.append("() {\n");
        bb.append("    return ");
        bb.append(importedName(CODEHELPERS), "    ");
        bb.append(".require(");
        bb.append(Naming.getGetterMethodForRequire(name), "    ");
        bb.append("(), \"");
        bb.append(name.toLowerCase(Locale.ROOT).replace(Naming.REQUIRE_PREFIX, ""), "    ");
        bb.append("\");\n");
        bb.append("}\n");
        return bb;
    }

    private BlockBuilder generateAccessorMethod(final MethodSignature method) {
        //        «accessorJavadoc(method, ", or {@code null} if it is not present.")»
        //        «method.generateAccessorAnnotations»
        //        «method.returnType.nullableType» «method.name»();

        final var bb = new BlockBuilder();
        bb.append(accessorJavadoc(method, ", or {@code null} if it is not present."));
        bb.newLineIfNotEmpty();
        bb.append(generateAccessorAnnotations(method));
        bb.newLineIfNotEmpty();
        bb.append(nullableType(method.getReturnType()));
        bb.append(" ");
        bb.append(method.getName());
        bb.append("();\n");
        return bb;
    }

    private CharSequence generateAccessorAnnotations(final MethodSignature method) {
        final var annotations = method.getAnnotations();
        if (annotations.isEmpty()) {
            return "";
        }

        final var bb = new BlockBuilder();
        bb.newLineIfNotEmpty();
        for (var annotation : annotations) {
            if (!Types.BOOLEAN.equals(method.getReturnType()) || !OVERRIDE.equals(annotation.name())) {
                bb.append(generateAnnotation(annotation));
                bb.newLineIfNotEmpty();
            }
        }
        return bb;
    }

    private BlockBuilder generateNonnullAccessorMethod(final MethodSignature method) {
        //        «accessorJavadoc(method, ", or an empty instance if it is not present.")»
        //        «method.annotations.generateAnnotations»
        //        «method.returnType.importedNonNull» «method.name»();

        final var bb = new BlockBuilder();
        bb.append(accessorJavadoc(method, ", or an empty instance if it is not present."));
        bb.newLineIfNotEmpty();
        bb.append(generateAnnotations(method.getAnnotations()));
        bb.newLineIfNotEmpty();
        bb.append(importedNonNull(method.getReturnType()));
        bb.append(" ");
        bb.append(method.getName());
        bb.append("();\n");
        return bb;
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

        final var bb = new BlockBuilder();
        bb.append("/**\n");
        bb.append(" * Default implementation of {@link ");
        bb.append(importedName(OBJECT), " ");
        bb.append("#hashCode()} contract for this interface.\n");
        bb.append(
            " * Implementations of this interface are encouraged to defer to this method to get consistent hashing\n");
        bb.append(" * results across all implementations.\n");
        bb.append(" *\n");
        bb.append(" * @param obj Object for which to generate hashCode() result.\n");
        bb.append(" * @return Hash code value of data modeled by this interface.\n");
        bb.append(" * @throws ");
        bb.append(importedName(NPE), " ");
        bb.append(" if {@code obj} is {@code null}\n");
        bb.append(" */\n");
        bb.append("static int ");
        bb.append(Naming.BINDING_HASHCODE_NAME);
        bb.append("(final ");
        bb.append(fullyQualifiedNonNull(type()));
        bb.append(" obj) {\n");
        bb.append("    int result = 1;\n");
        if (!props.isEmpty()) {
            bb.append("    final int prime = 31;\n");
            for (var property : props) {
                bb.append("    result = prime * result + ");
                bb.append(importedUtilClass(property), "    ");
                bb.append(".hashCode(obj.");
                bb.append(getterMethodName(property), "    ");
                bb.append("());\n");
            }
        }
        if (augmentable) {
            bb.append("    for (var augmentation : obj.augmentations().values()) {\n");
            bb.append("        result += augmentation.hashCode();\n");
            bb.append("    }\n");
        }
        bb.append("    return result;\n");
        bb.append("}\n");
        return bb;
    }

    private CharSequence generateBindingEquals() {
        final var analysis = typeAnalysis();
        final var augmentable = analysis.augmentType() != null;
        final var props = analysis.properties();
        if (!augmentable && props.isEmpty()) {
            return "";
        }

        final var bb = new BlockBuilder();
        bb.newLineIfNotEmpty();
        bb.append("/**\n");
        bb.append(" * Default implementation of {@link ");
        bb.append(importedName(OBJECT), " ");
        bb.append("#equals(");
        bb.append(importedName(OBJECT), " ");
        bb.append(")} contract for this interface.\n");
        bb.append(
            " * Implementations of this interface are encouraged to defer to this method to get consistent equality\n");
        bb.append(" * results across all implementations.\n");
        bb.append(" *\n");
        bb.append(" * @param thisObj Object acting as the receiver of equals invocation\n");
        bb.append(" * @param obj Object acting as argument to equals invocation\n");
        bb.append(" * @return True if thisObj and obj are considered equal\n");
        bb.append(" * @throws ");
        bb.append(importedName(NPE), " ");
        bb.append(" if {@code thisObj} is {@code null}\n");
        bb.append(" */\n");
        bb.append("static boolean ");
        bb.append(Naming.BINDING_EQUALS_NAME);
        bb.append("(final ");
        bb.append(fullyQualifiedNonNull(type()));
        bb.append(" thisObj, final ");
        bb.append(importedName(Types.objectType()));
        bb.append(" obj) {\n");
        bb.append("    if (thisObj == obj) {\n");
        bb.append("        return true;\n");
        bb.append("    }\n");
        bb.append("    final var other = ");
        bb.append(importedName(CODEHELPERS), "    ");
        bb.append(".checkCast(");
        bb.append(type().canonicalName(), "    ");
        bb.append(".class, obj);\n");
        bb.append("    return other != null\n");
        for (var property : ByTypeMemberComparator.sort(props)) {
            bb.append("        && ");
            bb.append(importedUtilClass(property), "        ");
            bb.append(".equals(thisObj.");
            bb.append(property.getGetterName(), "        ");
            bb.append("(), other.");
            bb.append(property.getGetterName(), "        ");
            bb.append("())\n");
        }
        bb.append("        ");
        if (augmentable) {
            bb.append("&& thisObj.augmentations().equals(other.augmentations())");
        }
        bb.append(";\n");
        bb.append("}\n");
        return bb;
    }

    @VisibleForTesting
    final BlockBuilder generateBindingToString() {
        final var analysis = typeAnalysis();

        final var bb = new BlockBuilder();
        bb.newLineIfNotEmpty();
        bb.append("/**\n");
        bb.append(" * Default implementation of {@link ");
        bb.append(importedName(OBJECT), " ");
        bb.append("#toString()} contract for this interface.\n");
        bb.append(
            " * Implementations of this interface are encouraged to defer to this method to get consistent string\n");
        bb.append(" * representations across all implementations.\n");
        bb.append(" *\n");
        bb.append(" * @param obj Object for which to generate toString() result.\n");
        bb.append(" * @return {@link ");
        bb.append(importedName(Types.STRING), " ");
        bb.append("} value of data modeled by this interface.\n");
        bb.append(" * @throws ");
        bb.append(importedName(NPE), " ");
        bb.append(" if {@code obj} is {@code null}\n");
        bb.append(" */\n");
        bb.append("static ");
        bb.append(importedName(Types.STRING));
        bb.append(" ");
        bb.append(Naming.BINDING_TO_STRING_NAME);
        bb.append("(final ");
        bb.append(fullyQualifiedNonNull(type()));
        bb.append(" obj) {\n");
        bb.append("    final var helper = ");
        bb.append(importedName(MOREOBJECTS), "    ");
        bb.append(".toStringHelper(\"");
        bb.append(type().simpleName(), "    ");
        bb.append("\");\n");
        for (var property : analysis.properties()) {
            bb.append("    ");
            bb.append(importedName(CODEHELPERS), "    ");
            bb.append(".appendValue(helper, \"");
            bb.append(property.getName(), "    ");
            bb.append("\", obj.");
            bb.append(property.getGetterName(), "    ");
            bb.append("());\n");
        }
        if (analysis.augmentType() != null) {
            bb.append("    ");
            bb.append(importedName(CODEHELPERS), "    ");
            bb.append(".appendAugmentations(helper, \"");
            bb.append(Naming.AUGMENTATION_FIELD, "    ");
            bb.append("\", obj);\n");
        }
        bb.append("    return helper.toString();\n");
        bb.append("}\n");
        return bb;
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

        final var bb = new BlockBuilder();
        bb.str("Return ").append(propReturn);
        bb.newLineIfNotEmpty();
        final var comment = method.getComment();
        bb.nl().append(formatReference(comment == null ? null : comment.referenceDescription()));
        bb.newLineIfNotEmpty();
        bb.str("@return {@code ").str(importedReturnType(method)).str("} ").append(propReturn);
        bb.newLineIfNotEmpty();
        if (exception != null) {
            bb.str("@throws ").append(importedName(exception));
            bb.str(" if ").append(propName);
            bb.append(" is not present\n");
        }
        return bb.toJavadocBlock();
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
