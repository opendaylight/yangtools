/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.opendaylight.yangtools.binding.contract.Naming.AUGMENTATION_FIELD;
import static org.opendaylight.yangtools.binding.contract.Naming.BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.BINDING_EQUALS_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.BINDING_HASHCODE_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.BINDING_TO_STRING_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.REQUIRE_PREFIX;
import static org.opendaylight.yangtools.binding.contract.Naming.getGetterMethodForNonnull;
import static org.opendaylight.yangtools.binding.contract.Naming.getGetterMethodForRequire;
import static org.opendaylight.yangtools.binding.contract.Naming.isGetterMethodName;
import static org.opendaylight.yangtools.binding.contract.Naming.isNonnullMethodName;
import static org.opendaylight.yangtools.binding.contract.Naming.isRequireMethodName;

import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.Locale;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
    final BlockBuilder body() {
        final var bb = new BlockBuilder();
        bb.append(wrapToDocumentation(formatDataForJavaDoc(type())));
        bb.append(generateAnnotations(type().getAnnotations()));
        return bb
            .eol(generatedAnnotation())
            .str("public interface ").str(type().simpleName()).nl()
            .indented(superInterfaces()).str("{").nl()
            .nl().indented(generateInnerClasses(enclosedGeneratedTypes))
            .nl()
            .indented(generateInnerEnumTypeObjects(enums))
            .nl()
            .indented(generateConstants())
            .nl()
            .indented(generateMethods())
            .nl()
            .eol("}")
            .nl();
    }

    private @Nullable BlockBuilder superInterfaces() {
        final var ifaces = type().getImplements();
        if (ifaces.isEmpty()) {
            return null;
        }

        final var bb = new BlockBuilder().str("extends").nl();
        final var it = ifaces.iterator();
        while (true) {
            bb.append(importedName(it.next()));
            if (!it.hasNext()) {
                break;
            }
            bb.str(",").newLine();
        }
        return bb.str(" ");
    }

    @Nullable StringBuilder generateConstants() {
        if (consts.isEmpty()) {
            return null;
        }

        final var sb = new StringBuilder();
        for (var constant : consts) {
            // Pattern constants are emitted separately
            if (!constant.getName().startsWith(TypeConstants.PATTERN_CONSTANT_NAME)) {
                // FIXME: short circuit to statically-known case
                sb.append(emitConstant(constant));
            }
        }
        return sb;
    }

    final @NonNull BlockBuilder generateDefaultImplementedInterface() {
        // Note: we cannot use importedName() or short name due to shadowing explained in MDSAL-365
        // FIXME: use selfRef()
        final var fqcn = type().canonicalName();

        return new BlockBuilder()
            .at().eol(importedName(OVERRIDE))
            .str("default ").str(importedName(CLASS)).str("<").str(fqcn)
                .str("> " + BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME + "() {").nl()
            .str("    return ").str(fqcn).str(".class;").nl()
            .str("}").nl();
    }

    @Nullable BlockBuilder generateMethods() {
        if (methods.isEmpty()) {
            return null;
        }

        final var bb = new BlockBuilder();
        final var it = methods.iterator();
        while (true) {
            final var method = it.next();
            if (method.isDefault()) {
                bb.append(generateDefaultMethod(method));
            } else if (method.isStatic()) {
                bb.append(generateStaticMethod(method));
            } else if (method.getParameters().isEmpty() && isGetterMethodName(method.getName())) {
                bb.append(generateAccessorMethod(method));
            } else if (method.getParameters().isEmpty() && isNonnullMethodName(method.getName())) {
                bb.append(generateNonnullAccessorMethod(method));
            } else {
                bb.append(generateMethod(method));
            }

            if (!it.hasNext()) {
                break;
            }
            bb.newLine();
        }
        return bb;
    }

    private @NonNull BlockBuilder generateMethod(final MethodSignature method) {
        final var bb = new BlockBuilder();
        bb.append(asJavadoc(method.getComment()));
        bb.append(generateAnnotations(method.getAnnotations()));
        return bb
            .str(importedReturnType(method)).str(" ").str(method.getName()).str("(")
                .str(generateParameters(method.getParameters())).str(");");
    }

    private @Nullable BlockBuilder generateAnnotations(final @NonNull List<AnnotationType> annotations) {
        if (annotations.isEmpty()) {
            return null;
        }

        final var bb = new BlockBuilder();
        for (var annotation : annotations) {
            bb.append(generateAnnotation(annotation));
        }
        return bb;
    }

    private @Nullable BlockBuilder generateDefaultMethod(final MethodSignature method) {
        final var methodName = method.getName();
        if (isNonnullMethodName(methodName)) {
            return generateNonnullMethod(method);
        }
        if (isRequireMethodName(methodName)) {
            return generateRequireMethod(method);
        }
        return switch (methodName) {
            case BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME -> generateDefaultImplementedInterface();
            default ->
                JavaFileTemplate.VOID.equals(method.getReturnType().name())
                    ? generateNoopVoidInterfaceMethod(method)
                    : null;
        };
    }

    private @NonNull BlockBuilder generateNonnullMethod(final MethodSignature method) {
        final var bb = new BlockBuilder();
        final var ret = method.getReturnType();
        final var name = method.getName();
        bb.append(accessorJavadoc(method, ", or an empty list if it is not present."));
        bb.append(generateAnnotations(method.getAnnotations()));
        return bb
            .str("default ").str(importedNonNull(ret)).str(" ").str(name).str("() {").nl()
            .str("    return ").str(importedName(CODEHELPERS)).str(".nonnull(").str(getGetterMethodForNonnull(name))
                .eol("());")
            .str("}").nl();
    }

    private @NonNull BlockBuilder generateNoopVoidInterfaceMethod(final MethodSignature method) {
        final var bb = new BlockBuilder();
        bb.append(asJavadoc(method.getComment()));
        bb.append(generateAnnotations(method.getAnnotations()));
        return bb
            .str("default ").str(importedName(VOID)).str(" ").str(method.getName()).str("(")
                .str(generateParameters(method.getParameters())).str(") {").nl()
            .eol("    // No-op")
            .str("}").nl();
    }

    private BlockBuilder generateRequireMethod(final MethodSignature method) {
        final var ret = method.getReturnType();
        final var name = method.getName();
        final var bb = new BlockBuilder();
        bb.append(accessorJavadoc(method, ", guaranteed to be non-null.", NSEE));
        return bb
            .str("default ").str(importedNonNull(ret)).str(" ").str(name).str("() {").nl()
            .str("    return ").str(importedName(CODEHELPERS)).str(".require(").str(getGetterMethodForRequire(name))
                .str("(), \"").str(name.toLowerCase(Locale.ROOT).replace(REQUIRE_PREFIX, "")).eol("\");")
            .eol("}");
    }

    private BlockBuilder generateAccessorMethod(final MethodSignature method) {
        final var bb = new BlockBuilder();
        bb.append(accessorJavadoc(method, ", or {@code null} if it is not present."));
        bb.append(generateAccessorAnnotations(method));
        return bb
            .str(nullableType(method.getReturnType())).str(" ").str(method.getName())
            .eol("();");
    }

    private @Nullable BlockBuilder generateAccessorAnnotations(final MethodSignature method) {
        final var annotations = method.getAnnotations();
        if (annotations.isEmpty()) {
            return null;
        }

        final var bb = new BlockBuilder();
        for (var annotation : annotations) {
            if (!Types.BOOLEAN.equals(method.getReturnType()) || !OVERRIDE.equals(annotation.name())) {
                bb.append(generateAnnotation(annotation));
            }
        }
        return bb;
    }

    private BlockBuilder generateNonnullAccessorMethod(final MethodSignature method) {
        final var bb = new BlockBuilder();
        bb.append(accessorJavadoc(method, ", or an empty instance if it is not present."));
        bb.append(generateAnnotations(method.getAnnotations()));
        return bb
            .str(importedNonNull(method.getReturnType())).str(" ").str(method.getName()).eol("();");
    }

    private @Nullable BlockBuilder generateStaticMethod(final MethodSignature method) {
        return switch (method.getName()) {
            case BINDING_EQUALS_NAME -> generateBindingEquals();
            case BINDING_HASHCODE_NAME -> generateBindingHashCode();
            case BINDING_TO_STRING_NAME -> generateBindingToString();
            default -> null;
        };
    }

    @VisibleForTesting
    final @Nullable BlockBuilder generateBindingHashCode() {
        final var analysis = typeAnalysis();
        final boolean augmentable = analysis.augmentType() != null;
        final var props = analysis.properties();
        if (!augmentable && props.isEmpty()) {
            return null;
        }

        final var bb = new BlockBuilder()
            .eol("/**")
            .str(" * Default implementation of {@link ").str(importedName(OBJECT))
                .eol("#hashCode()} contract for this interface.")
            .txt("""
                   * Implementations of this interface are encouraged to defer to this method to get consistent hashing
                   * results across all implementations.
                   *
                   * @param obj Object for which to generate hashCode() result.
                   * @return Hash code value of data modeled by this interface.
                  """)
            .str(" * @throws ").str(importedName(NPE)).str(" if {@code obj} is {@code null}").nl()
            .str(" */").nl()
            .str("static int " + BINDING_HASHCODE_NAME + "(final ").str(fullyQualifiedNonNull(type())).str(" obj) {")
                .nl()
            .str("    int result = 1;").nl();
        if (!props.isEmpty()) {
            bb.str("    final int prime = 31;").newLine();
            for (var property : props) {
                bb.str("    result = prime * result + ").str(importedUtilClass(property)).str(".hashCode(obj.")
                    .str(getterMethodName(property)).str("());").newLine();
            }
        }
        if (augmentable) {
            bb
                .str("    for (var augmentation : obj.augmentations().values()) {").nl()
                .eol("        result += augmentation.hashCode();")
                .str("    }").newLine();
        }
        return  bb
            .eol("    return result;")
            .str("}").nl();
    }

    private @Nullable BlockBuilder generateBindingEquals() {
        final var analysis = typeAnalysis();
        final var augmentable = analysis.augmentType() != null;
        final var props = analysis.properties();
        if (!augmentable && props.isEmpty()) {
            return null;
        }

        final var object = importedName(OBJECT);

        final var bb = new BlockBuilder()
            .eol("/**")
            .str(" * Default implementation of {@link ").str(object).str("#equals(").str(object)
                .eol(")} contract for this interface.")
            .txt("""
                   * Implementations of this interface are encouraged to defer to this method to get consistent equality
                   * results across all implementations.
                   *
                   * @param thisObj Object acting as the receiver of equals invocation
                   * @param obj Object acting as argument to equals invocation
                   * @return True if thisObj and obj are considered equal
                  """)
            .str(" * @throws ").str(importedName(NPE)).eol(" if {@code thisObj} is {@code null}")
            .eol(" */")
            .str("static boolean " + BINDING_EQUALS_NAME + "(final ").str(fullyQualifiedNonNull(type()))
                .str(" thisObj, final ").str(importedName(Types.objectType())).str(" obj) {").nl()
            .str("    if (thisObj == obj) {").nl()
            .eol("        return true;")
            .str("    }").nl()
            .str("    final var other = ").str(importedName(CODEHELPERS)).str(".checkCast(")
                .str(type().canonicalName()).eol(".class, obj);")
            .str("    return other != null");

        for (var property : ByTypeMemberComparator.sort(props)) {
            final var getterName = property.getGetterName();
            bb.nl().str("        && ").str(importedUtilClass(property)).str(".equals(thisObj.").str(getterName)
                .str("(), other.").str(getterName).append("())");
        }
        if (augmentable) {
            bb.nl().append("        && thisObj.augmentations().equals(other.augmentations())");
        }
        return bb
            .eol(";")
            .str("}").nl();
    }

    @VisibleForTesting
    final BlockBuilder generateBindingToString() {
        final var analysis = typeAnalysis();

        final var bb = new BlockBuilder()
            .eol("/**")
            .str(" * Default implementation of {@link ").str(importedName(OBJECT))
                .eol("#toString()} contract for this interface.")
            .txt("""
                   * Implementations of this interface are encouraged to defer to this method to get consistent string
                   * representations across all implementations.
                   *
                   * @param obj Object for which to generate toString() result.
                  """)
            .str(" * @return {@link ").str(importedName(Types.STRING)).eol("} value of data modeled by this interface.")
            .str(" * @throws ")       .str(importedName(NPE)).eol(" if {@code obj} is {@code null}")
            .eol(" */")
            .str("static ").str(importedName(Types.STRING)).str(" " + BINDING_TO_STRING_NAME + "(final ")
                .str(fullyQualifiedNonNull(type())).str(" obj) {").nl()
            .str("    final var helper = ").str(importedName(MOREOBJECTS)).str(".toStringHelper(\"")
                .str(type().simpleName()).eol("\");");
        for (var property : analysis.properties()) {
            bb.str("    ").str(importedName(CODEHELPERS)).str(".appendValue(helper, \"").str(property.getName())
                .str("\", obj.").str(property.getGetterName()).str("());").newLine();
        }
        if (analysis.augmentType() != null) {
            bb.str("    ").str(importedName(CODEHELPERS))
                .str(".appendAugmentations(helper, \"" + AUGMENTATION_FIELD + "\", obj);").newLine();
        }
        return bb
            .eol("    return helper.toString();")
            .str("}").nl();
    }

    private String accessorJavadoc(final MethodSignature method, final String orString) {
        return accessorJavadoc(method, orString, null);
    }

    private String accessorJavadoc(final MethodSignature method, final String orString,
            final @Nullable JavaTypeName exception) {
        final var propName = propertyNameFromGetter(method);
        final var propReturn = propName + orString;
        final var comment = method.getComment();

        //        return wrapToDocumentation('''
        //            Return «propReturn»
        //
        //            «method.comment?.referenceDescription.formatReference»
        //            @return {@code «method.returnType.importedName»} «propReturn»
        //            «IF exception !== null»
        //                @throws «exception.importedName» if «propName» is not present
        //            «ENDIF»
        //        ''')

        final var bb = new BlockBuilder()
            .str("Return ").eol(propReturn);
        final var reference = comment == null ? null : comment.referenceDescription();
        if (reference != null) {
            bb.append(formatReference(reference));
        }
        bb
            .nl()
            .str("@return {@code ").str(importedReturnType(method)).str("} ").str(propReturn).newLine();
        if (exception != null) {
            bb.str("@throws ").str(importedName(exception)).str(" if ").str(propName).str(" is not present").newLine();
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
