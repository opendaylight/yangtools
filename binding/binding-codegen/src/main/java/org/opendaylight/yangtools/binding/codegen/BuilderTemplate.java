/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static com.google.common.base.Verify.verify;
import static org.opendaylight.yangtools.binding.contract.Naming.AUGMENTABLE_AUGMENTATION_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.AUGMENTATION_FIELD;
import static org.opendaylight.yangtools.binding.contract.Naming.BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.KEY_AWARE_KEY_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.isGetterMethodName;
import static org.opendaylight.yangtools.binding.contract.Naming.toFirstUpper;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.binding.model.ri.TypeConstants;
import org.opendaylight.yangtools.binding.model.ri.Types;

/**
 * Template for generating JAVA builder classes.
 */
final class BuilderTemplate extends AbstractBuilderTemplate {
    private final BuilderImplTemplate implTemplate;

    BuilderTemplate(final @NonNull GeneratedType type, final @NonNull GeneratedType targetType,
            final GeneratedTransferObject keyType) {
        super(type, targetType, keyType);
        implTemplate = new BuilderImplTemplate(this, type().getEnclosedTypes().getFirst());
    }

    @Override
    boolean isLocalInnerClass(final JavaTypeName name) {
        // Builders do not have inner types
        return false;
    }

    @Override
    BlockBuilder body() {
        final var bb = new BlockBuilder();
        bb.append(wrapToDocumentation(formatDataForJavaDoc(targetType)));
        bb
            .blk(generateDeprecatedAnnotation(targetType.getAnnotations()))
            .eol(generatedAnnotation())
            .str("public class ").str(type().simpleName()).oB()
            // FIXME: remove this newline
            .nl()
            .indented(builderFields())
            .nl()
            .indented(constantsDeclarations())
            .newLine();
        if (augmentType != null) {
            bb.str("    ");
            final var augmentTypeRef = importedName(augmentType);
            final var mapTypeRef = importedName(JU_MAP);

            bb.str(mapTypeRef).str("<").str(importedName(CLASS)).str("<? extends ").str(augmentTypeRef).str(">, ")
                .str(augmentTypeRef).str("> " + AUGMENTATION_FIELD + " = ").str(mapTypeRef).eol(".of();");
        }

        final var targetTypeName = importedName(targetType);
        bb
            .nl()
            .txt("""
                      /**
                       * Construct an empty builder.
                       */
                  """)
            .str("    public ").str(type().simpleName()).str("()").oB()
            .eol("        // No-op")
            .eol("    }")
            .indented(generateConstructorsFromIfcs())
            .nl()
            .eol("    /**")
            .str("     * Construct a builder initialized with state from specified {@link ").str(targetTypeName)
                .eol("}.")
            .eol("     *")
            .str("     * @param base ").str(targetTypeName).eol(" from which the builder should be initialized")
            .eol("     */")
            .indented("public ", generateCopyConstructor(targetType, type().getEnclosedTypes().getFirst()))
            .nl()
            .indented(generateMethodFieldsFrom())
            .nl()
            .indented(generateEmptyInstance())
            .nl()
            .indented(generateGetters(false));
        if (augmentType != null) {
            bb.nl().indented(generateAugmentation());
        }
        return bb
            .nl()
            .indented(generateSetters())
            .nl()
            .eol("    /**")
            .str("     * A new {@link ").str(targetTypeName).eol("} instance.")
            .eol("     *")
            .str("     * @return A new {@link ").str(targetTypeName).eol("} instance.")
            .eol("     */")
            .str("    public ").str(importedNonNull(targetType)).str(" build()").oB()
            .str("        return new ").str(importedName(type().getEnclosedTypes().getFirst())).str("(this)").eS()
            .str("    ").cB()
            .nl()
            .indented(implTemplate.body())
            .cB();
    }

    private @Nullable BlockBuilder builderFields() {
        // FIXME: this just begs for specialization
        final var key = keyType;
        if (key != null) {
            verify(!properties.isEmpty(), "empty properties with key %s", key);
            return propertyFields()
                .str("private ").str(importedName(key)).eol(" key;");
        }
        return properties == null || properties.isEmpty() ? null : propertyFields();
    }

    @NonNullByDefault
    private BlockBuilder propertyFields() {
        final var bb = new BlockBuilder();
        for (var prop : properties) {
            bb.str("private ").str(importedReturnType(prop)).str(" ").str(fieldName(prop)).eS();
        }
        return bb;
    }

    @Override
    BlockBuilder generateDeprecatedAnnotation(final AnnotationType ann) {
        final var bb = new BlockBuilder().at();
        final var forRemoval = ann.getParameter("forRemoval");
        return forRemoval != null
            ? bb.str(importedName(DEPRECATED)).str("(forRemoval = ").str(forRemoval.getValue()).str(")")
            : bb.str(importedName(SUPPRESS_WARNINGS)).str("(\"deprecation\")");
    }

    private @Nullable BlockBuilder generateConstructorsFromIfcs() {
        if (targetType instanceof GeneratedTransferObject) {
            return null;
        }

        final var bb = new BlockBuilder().nl();
        boolean first = true;
        for (var impl : targetType.getImplements()) {
            if (impl instanceof GeneratedType genType) {
                if (first) {
                    first = false;
                } else {
                    bb.newLine();
                }
                bb.blk(generateConstructorFromIfc(genType));
            }
        }
        return bb;
    }

    /**
     * Generate constructor with argument of given type.
     */
    private @NonNull BlockBuilder generateConstructorFromIfc(final GeneratedType genType) {
        final var bb = new BlockBuilder();
        if (hasNonDefaultMethods(genType)) {
            final var typeName = importedName(genType);
            bb
                .eol("/**")
                .str(" * Construct a new builder initialized from specified {@link ").str(typeName).eol("}.")
                .eol(" *")
                .str(" * @param arg ").str(typeName).eol(" from which the builder should be initialized")
                .eol(" */")
                .str("public ").str(type().simpleName()).str("(").str(typeName).str(" arg)").oB()
                .indented(printConstructorPropertySetter(genType))
                .cB()
                .newLine();
        }
        for (var implTypeImplement : genType.getImplements()) {
            if (implTypeImplement instanceof GeneratedType implType) {
                bb.blk(generateConstructorFromIfc(implType));
            }
        }
        return bb;
    }

    private @Nullable BlockBuilder printConstructorPropertySetter(final Type implementedIfc) {
        if (!(implementedIfc instanceof GeneratedType ifc) || ifc instanceof GeneratedTransferObject) {
            return null;
        }

        final var bb = new BlockBuilder();
        for (var getter : nonDefaultMethods(ifc)) {
            if (isGetterMethodName(getter.getName())) {
                bb.append(printPropertySetter(getter, "arg", propertyNameFromGetter(getter)));
            }
        }

        for (var impl : ifc.getImplements()) {
            bb.blk(printConstructorPropertySetter(impl, getSpecifiedGetters(ifc)));
        }
        return bb;
    }

    private @Nullable BlockBuilder printConstructorPropertySetter(final Type implementedIfc,
            final Set<MethodSignature> alreadySetProperties) {
        if (!(implementedIfc instanceof GeneratedType ifc) || ifc instanceof GeneratedTransferObject) {
            return null;
        }

        final var bb = new BlockBuilder();
        for (var getter : nonDefaultMethods(ifc)) {
            if (isGetterMethodName(getter.getName()) && getterByName(alreadySetProperties, getter.getName()) == null) {
                bb.append(printPropertySetter(getter, "arg", propertyNameFromGetter(getter)));
            }
        }

        for (var descendant : ifc.getImplements()) {
            bb.blk(printConstructorPropertySetter(descendant,
                Sets.union(alreadySetProperties, getSpecifiedGetters(ifc))));
        }
        return bb;
    }

    private static Set<MethodSignature> getSpecifiedGetters(final GeneratedType type) {
        return type.getMethodDefinitions().stream()
            .filter(JavaFileTemplate::hasOverrideAnnotation)
            .collect(ImmutableSet.toImmutableSet());
    }

    /**
     * Generate 'fieldsFrom' method to set builder properties based on type of given argument.
     */
    private @Nullable BlockBuilder generateMethodFieldsFrom() {
        if (targetType instanceof GeneratedTransferObject || !hasImplementsFromUses(targetType)) {
            return null;
        }

        final var done = getBaseIfcs(targetType);

        //        «generateMethodFieldsFromComment(targetType)»
        //        public void fieldsFrom(final «GROUPING.importedName» arg) {
        //            boolean isValidArg = false;
        //            «FOR impl : targetType.getAllIfcs»
        //                «generateIfCheck(impl, done)»
        //            «ENDFOR»
        //            «CODEHELPERS.importedName».validValue(isValidArg, arg, "«targetType.getAllIfcs.toListOfNames»");
        //        }
        final var bb = new BlockBuilder();
        bb.append(generateMethodFieldsFromComment(targetType));
        bb
            .str("public void fieldsFrom(final ").str(importedName(BindingTypes.GROUPING)).str(" arg)").oB()
            .eol("    boolean isValidArg = false;");
        for (var impl : getAllIfcs(targetType)) {
            bb.indented(generateIfCheck(impl, done));
        }
        return bb
            .str("    ").str(importedName(CODEHELPERS)).str(".validValue(isValidArg, arg, \"")
                .str(getAllIfcs(targetType).stream()
                    .map(this::importedName)
                    .collect(Collectors.toUnmodifiableList()).toString())
                .eol("\");")
            .cB();
    }

    /**
     * Generate EMPTY instance which is lazily initialized in empty() method.
     */
    private @Nullable BlockBuilder generateEmptyInstance() {
        if (!isNonPresenceContainer(targetType)) {
            return null;
        }

        final var nonnullTarget = importedNonNull(targetType);
        final var targetName = targetType.simpleName();

        return new BlockBuilder()
            .str("private static final class LazyEmpty").oB()
            .str("    static final ").str(nonnullTarget).str(" INSTANCE = new ").str(type().simpleName())
                .eol("().build();")
            .nl()
            .str("    private LazyEmpty()").oB()
            .str("        // Hidden on purpose").nl()
            .str("    }").nl()
            .cB()
            .nl()
            .eol("/**")
            .str(" * Get empty instance of ").str(targetName).eol(".")
            .eol(" *")
            .str(" * @return An empty {@link ").str(targetName).eol("}")
            .eol(" */")
            .str("public static ").str(nonnullTarget).str(" empty()").oB()
            .eol("    return LazyEmpty.INSTANCE;")
            .cB();
    }

    private StringBuilder generateMethodFieldsFromComment(final GeneratedType type) {
        //        /**
        //         * Set fields from given grouping argument. Valid argument is instance of one of following types:
        //         * <ul>
        //         «FOR impl : type.getAllIfcs»
        //         *   <li>{@link «impl.importedName»}</li>
        //         «ENDFOR»
        //         * </ul>
        //         *
        //         * @param arg grouping object
        //         * @throws «IAE.importedName» if given argument is none of valid types or has property with
        // incompatible value
        //         */

        final var sb = new StringBuilder()
            .append("/**\n")
            .append(
                " * Set fields from given grouping argument. Valid argument is instance of one of following types:\n")
            .append(" * <ul>\n");
        for (var impl : getAllIfcs(type)) {
            sb.append(" *   <li>{@link ").append(importedName(impl)).append("}</li>\n");
        }
        return sb
            .append(" * </ul>\n")
            .append(" *\n")
            .append(" * @param arg grouping object\n")
            .append(" * @throws ").append(importedName(IAE))
                .append(" if given argument is none of valid types or has property with incompatible value\n")
            .append(" */\n");
    }

    /**
     * Method is used to find out if given type implements any interface from uses.
     */
    private boolean hasImplementsFromUses(final GeneratedType type) {
        return getAllIfcs(type).stream()
            .anyMatch(impl -> impl instanceof GeneratedType genType && hasNonDefaultMethods(genType));
    }

    private @Nullable BlockBuilder generateIfCheck(final Type impl, final List<Type> done) {
        return !(impl instanceof GeneratedType implType) || !hasNonDefaultMethods(implType) ? null : new BlockBuilder()
            .str("if (arg instanceof ").str(importedName(implType)).str(" castArg)").oB()
                .indented(printPropertySetter(implType))
            .eol("    isValidArg = true;")
            .cB();
    }

    private @Nullable BlockBuilder printPropertySetter(final Type implementedIfc) {
        if (!(implementedIfc instanceof GeneratedType ifc) || ifc instanceof GeneratedTransferObject) {
            return null;
        }

        final var bb = new BlockBuilder();
        for (var getter : nonDefaultMethods(ifc)) {
            if (isGetterMethodName(getter.getName()) && !hasOverrideAnnotation(getter)) {
                bb.append(printPropertySetter(getter, "castArg", propertyNameFromGetter(getter)));
            }
        }
        return bb;
    }

    // FIXME: return BlockBuilder
    @NonNullByDefault
    private String printPropertySetter(final MethodSignature getter, final String receiver, final String propertyName) {
        final var getterName =  getter.getName();

        final var ownGetter = implTemplate.findGetter(getterName);
        final var ownGetterType = ownGetter.getReturnType();
        if (strictTypeEquals(getter.getReturnType(), ownGetterType)) {
            return "this._" + propertyName + " = " + receiver + '.' + getterName + "();\n";
        }
        if (ownGetterType instanceof ParameterizedType parameterized) {
            final var itemType = parameterized.getActualTypeArguments().getFirst();
            if (Types.isListType(parameterized)) {
                return printPropertySetter(getterName, receiver, propertyName, "checkListFieldCast",
                    importedName(itemType));
            }
            if (Types.isSetType(parameterized)) {
                return printPropertySetter(getterName, receiver, propertyName, "checkSetFieldCast",
                    importedName(itemType));
            }
        }
        return printPropertySetter(getterName, receiver, propertyName, "checkFieldCast", importedName(ownGetterType));
    }

    @NonNullByDefault
    private String printPropertySetter(final String getterName, final String receiver, final String propertyName,
            final String checkerName, final String className) {
        return "this._" + propertyName + " = " + importedName(CODEHELPERS) + '.' + checkerName + '('
            + className + ".class, \"" + propertyName + "\", " + receiver + '.' + getterName + "());\n";
    }

    private static boolean strictTypeEquals(final Type type1, final Type type2) {
        if (!type1.equals(type2)) {
            return false;
        }
        if (type1 instanceof ParameterizedType param1) {
            return type2 instanceof ParameterizedType param2
                && param1.getActualTypeArguments().equals(param2.getActualTypeArguments());
        }
        return !(type2 instanceof ParameterizedType);
    }

    private static List<Type> getBaseIfcs(final GeneratedType type) {
        final var baseIfcs = new ArrayList<Type>();
        for (var ifc : type.getImplements()) {
            if (ifc instanceof GeneratedType genType && hasNonDefaultMethods(genType)) {
                baseIfcs.add(genType);
            }
        }
        return baseIfcs;
    }

    private Set<Type> getAllIfcs(final Type type) {
        if (!(type instanceof GeneratedType ifc) || ifc instanceof GeneratedTransferObject) {
            return Set.of();
        }

        final var baseIfcs = new HashSet<Type>();
        for (var impl : ifc.getImplements()) {
            if (impl instanceof GeneratedType genType && hasNonDefaultMethods(genType)) {
                baseIfcs.add(genType);
            }
            baseIfcs.addAll(getAllIfcs(impl));
        }
        return baseIfcs;
    }

    @NonNullByDefault
    private BlockBuilder constantsDeclarations() {
        final var bb = new BlockBuilder();
        for (var def : type().getConstantDefinitions()) {
            if (!def.getName().startsWith(TypeConstants.PATTERN_CONSTANT_NAME)) {
                bb.append(emitConstant(def));
                continue;
            }

            final var xsdToPattern = (Map<String, String>) def.getValue();
            final var fieldSuffix = def.getName().substring(TypeConstants.PATTERN_CONSTANT_NAME.length());
            final var jurPatternRef = importedName(JUR_PATTERN);
            if (xsdToPattern.size() == 1) {
                final var firstEntry = xsdToPattern.entrySet().iterator().next();
                //  private static final «jurPatternRef» «Constants.MEMBER_PATTERN_LIST»«fieldSuffix» = «jurPatternRef»
                //.compile("«firstEntry.key.escapeJava»");
                //  private static final String «Constants.MEMBER_REGEX_LIST»«fieldSuffix» =
                // "«firstEntry.value.escapeJava»";

                bb.append("private static final ");
                bb.append(jurPatternRef);
                bb.append(" ");
                bb.append(Constants.MEMBER_PATTERN_LIST);
                bb.append(fieldSuffix);
                bb.append(" = ");
                bb.append(jurPatternRef);
                bb.append(".compile(\"");
                bb.append(StringEscapeUtils.escapeJava(firstEntry.getKey()));
                bb.append("\");\n");
                bb.append("private static final String ");
                bb.append(Constants.MEMBER_REGEX_LIST);
                bb.append(fieldSuffix);
                bb.append(" = \"");
                bb.append(StringEscapeUtils.escapeJava(firstEntry.getValue()));
                bb.append("\";\n");
                continue;
            }

            bb.str("private static final ").str(jurPatternRef).str("[] " + Constants.MEMBER_PATTERN_LIST)
                .str(fieldSuffix).str(" = ").str(importedName(CODEHELPERS)).str(".compilePatterns(")
                .str(importedName(JU_LIST)).append(".of(\n");
            {
                boolean first = true;
                for (var v : xsdToPattern.keySet()) {
                    if (first) {
                        first = false;
                    } else {
                        bb.append(", ");
                    }
                    bb.str("\"").append(StringEscapeUtils.escapeJava(v));
                    bb.append("\"");
                }
            }
            bb.append("));\n");
            bb.append("private static final String[] ");
            bb.append(Constants.MEMBER_REGEX_LIST);
            bb.append(fieldSuffix);
            bb.append(" = { ");
            {
                boolean first = true;
                for (var v : xsdToPattern.values()) {
                    if (first) {
                        first = false;
                    } else {
                        bb.append(", ");
                    }
                    bb.str("\"").append(StringEscapeUtils.escapeJava(v));
                    bb.append("\"");
                }
            }
            bb.append(" };\n");
        }
        return bb;
    }

    private @NonNull BlockBuilder generateSetter(final BuilderGeneratedProperty field) {
        final var returnType = field.getReturnType();
        if (returnType instanceof ParameterizedType parameterized) {
            if (Types.isListType(parameterized) || Types.isSetType(parameterized)) {
                final var arguments = parameterized.getActualTypeArguments();
                return arguments.isEmpty() ? generateListSetter(field, Types.objectType())
                    : generateListSetter(field, arguments.getFirst());
            }
            if (Types.isMapType(parameterized)) {
                return generateMapSetter(field, parameterized.getActualTypeArguments().get(1));
            }
        }
        return generateSimpleSetter(field, returnType);
    }

    private @NonNull BlockBuilder generateListSetter(final BuilderGeneratedProperty field, final Type actualType) {
        final var bb = new BlockBuilder();
        final BlockBuilder argumentCheck;
        final var restrictions = restrictionsForSetter(actualType);
        if (restrictions != null) {
            bb.append(generateCheckers(field, restrictions, actualType));
            argumentCheck = new BlockBuilder()
                .str("if (values != null)").oB()
                .str("   for (").str(importedName(actualType)).str(" value : values)").oB()
                .indentedTwice(checkArgument(field, restrictions, actualType, "value"))
                .str("   ").cB()
                .cB();
        } else {
            argumentCheck = null;
        }

        return bb
            .nl()
            .eol("/**")
            .str(" * Set the property corresponding to {@link ").str(importedName(targetType)).str("#")
                .str(field.getGetterName()).eol("()} to the specified")
            .eol(" * value.")
            .eol(" *")
            .eol(" * @param values desired value")
            .eol(" * @return this builder")
            .eol(" */")
            .str("public ").str(type().simpleName()).str(" set").str(toFirstUpper(field.getName())).str("(final ")
                .str(importedReturnType(field)).str(" values)").oB()
            // FIXME: indented?
            .blk(argumentCheck)
            .str("    this.").str(fieldName(field)).eol(" = values;")
            .eol("    return this;")
            .cB()
            .nl();
    }

    private @NonNull BlockBuilder generateMapSetter(final BuilderGeneratedProperty field, final Type actualType) {
        final var bb = new BlockBuilder();
        final var restrictions = JavaFileTemplate.restrictionsForSetter(actualType);
        if (restrictions != null) {
            bb.append(generateCheckers(field, restrictions, actualType));
        }

        //
        //        /**
        //         * Set the property corresponding to {@link «targetType.importedName»#«field.getterName»()} to the
        // specified
        //         * value.
        //         *
        //         * @param values desired value
        //         * @return this builder
        //         */
        //        public «type.simpleName» set«field.name.toFirstUpper»(final «field.returnType.importedName» values) {

        bb
            .nl()
            .eol("/**")
            .str(" * Set the property corresponding to {@link ").str(importedName(targetType)).str("#")
                .str(field.getGetterName()).eol("()} to the specified")
            .txt("""
                 * value.
                 *
                 * @param values desired value
                 * @return this builder
                 */
                """)
            .str("public ").str(type().simpleName()).str(" set").str(toFirstUpper(field.getName())).str("(final ")
                .str(importedReturnType(field)).str(" values)").oB();

        //        «IF restrictions !== null»
        //            if (values != null) {
        //               for («actualType.importedName» value : values.values()) {
        //                   «checkArgument(field, restrictions, actualType, "value")»
        //               }
        //            }
        //        «ENDIF»
        if (restrictions != null) {
            bb
                .eol("if (values != null) {")
                .str("   for (").str(importedName(actualType)).str(" value : values.values())").oB()
                .indentedTwice(checkArgument(field, restrictions, actualType, "value"))
                .eol("   }")
                // FIXME: no nl() here ?
                .nl().append("}\n");
        }

        return bb
            .str("    this.").str(fieldName(field)).eol(" = values;")
            .eol("    return this;")
            .eol("}");
    }

    private @NonNull BlockBuilder generateSimpleSetter(final BuilderGeneratedProperty field, final Type actualType) {
        final var bb = new BlockBuilder();
        final var restrictions = restrictionsForSetter(actualType);
        if (restrictions != null) {
            bb.nl().append(generateCheckers(field, restrictions, actualType));
        }
        bb
            .nl()
            .eol("/**")
            .str(" * Set the property corresponding to {@link ").str(importedName(targetType)).str("#")
                .str(field.getGetterName()).eol("()} to the specified")
            .eol(" * value.")
            .eol(" *")
            .eol(" * @param value desired value")
            .eol(" * @return this builder")
            .eol(" */")
            .str("public ").str(type().simpleName()).str(" set").str(toFirstUpper(field.getName())).str("(final ")
                .str(importedReturnType(field)).str(" value)").oB();
        if (restrictions != null) {
            bb
                .str("    if (value != null)").oB()
                .indentedTwice(checkArgument(field, restrictions, actualType, "value"))
                .str("    ").cB();
        }
        return bb
            .str("    this.").str(fieldName(field)).eol(" = value;")
            .eol("    return this;")
            .cB();
    }

    /**
     * {@return string with the setter methods}
     */
    private @NonNull BlockBuilder generateSetters() {
        final var bb = new BlockBuilder();
        if (keyType != null) {
            bb
                .eol("/**")
                .str(" * Set the key value corresponding to {@link ").str(importedName(targetType)).str("#")
                    .str(KEY_AWARE_KEY_NAME).eol("()} to the specified")
                .txt("""
                       * value.
                       *
                       * @param key desired value
                       * @return this builder
                       */
                      """)
                .str("public ").str(type().simpleName()).str(" withKey(final ").str(importedName(keyType))
                    .str(" key)").oB()
                .txt("""
                          this.key = key;
                          return this;
                      }
                      """);
        }
        for (var property : properties) {
            bb.blk(generateSetter(property));
        }
        bb.newLine();
        if (augmentType != null) {
            final var augmentTypeRef = importedName(augmentType);
            final var hashMapRef = importedName(JU_HASHMAP);
            bb
                .txt("""
                      /**
                       * Add an augmentation to this builder's product.
                       *
                       * @param augmentation augmentation to be added
                       * @return this builder
                      """)
                .str(" * @throws ").str(importedName(NPE)).eol(" if {@code augmentation} is null")
                .eol(" */")
                .str("public ").str(type().simpleName()).str(" addAugmentation(").str(augmentTypeRef)
                    .str(" augmentation)").oB()
                .str("    if (!(this." + AUGMENTATION_FIELD + " instanceof ").str(hashMapRef).str("))").oB()
                .str("        this." + AUGMENTATION_FIELD + " = new ").str(hashMapRef).eol("<>();")
                .eol("    }")
                .eol("    this." + AUGMENTATION_FIELD + ".put(augmentation."
                    + BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME + "(), augmentation);")
                .txt("""
                          return this;
                      }

                      /**
                       * Remove an augmentation from this builder's product. If this builder does not track such an \
                      augmentation
                       * type, this method does nothing.
                       *
                       * @param augmentationType augmentation type to be removed
                       * @return this builder
                       */
                      """)
                .str("public ").str(type().simpleName()).str(" removeAugmentation(").str(importedName(CLASS))
                    .str("<? extends ").str(augmentTypeRef).str("> augmentationType)").oB()
                .str("    if (this." + AUGMENTATION_FIELD  + " instanceof ").str(hashMapRef).str(")").oB()
                .eol("        this." + AUGMENTATION_FIELD + ".remove(augmentationType);")
                .eol("    }")
                .eol("    return this;")
                .cB();
        }
        return bb;
    }

    private @NonNull BlockBuilder createDescription(final GeneratedType targetType) {
        final var target = importedName(targetType);
        return new BlockBuilder()
            .str("Class that builds {@link ").str(target).eol("} instances. Overall design of the class is that of a")
            .txt("""
                  <a href="https://en.wikipedia.org/wiki/Fluent_interface">fluent interface</a>, where method chaining \
                  is used.

                  <p>In general, this class is supposed to be used like this template:
                  <pre>
                    <code>
                  """)
            .str("    ").str(target).str(" create").str(target).eol("(int fooXyzzy, int barBaz) {")
            .str("        return new ").str(target).eol("Builder()")
            .txt("""
                              .setFoo(new FooBuilder().setXyzzy(fooXyzzy).build())
                              .setBar(new BarBuilder().setBaz(barBaz).build())
                              .build();
                      }
                    </code>
                  </pre>

                  """)
            .str("<p>This pattern is supported by the immutable nature of ").str(target)
                .eol(", as instances can be freely passed around without")
            .txt("""
                  worrying about synchronization issues.

                  <p>As a side note: method chaining results in:
                  <ul>
                    <li>very efficient Java bytecode, as the method invocation result, in this case the Builder \
                  reference, is
                        on the stack, so further method invocations just need to fill method arguments for the next \
                  method
                        invocation, which is terminated by {@link #build()}, which is then returned from the method</li>
                    <li>better understanding by humans, as the scope of mutable state (the builder) is kept to a \
                  minimum and is
                        very localized</li>
                    <li>better optimization opportunities, as the object scope is minimized in terms of invocation \
                  (rather than
                        method) stack, making <a href="https://en.wikipedia.org/wiki/Escape_analysis">escape \
                  analysis</a> a lot
                        easier. Given enough compiler (JIT/AOT) prowess, the cost of th builder object can be completely
                        eliminated</li>
                  </ul>

                  """)
            .str("@see ").str(target).nl();
    }

    @Override
    String formatDataForJavaDoc(final GeneratedType type) {
        return createDescription(type).toRawString();
    }

    private StringBuilder generateAugmentation() {
        return new StringBuilder()
            .append("""
                     /**
                      * Return the specified augmentation, if it is present in this builder.
                      *
                      * @param <E$$> augmentation type
                      * @param augmentationType augmentation type class
                      * @return Augmentation object from this builder, or {@code null} if not present
                      * @throws\s""").append(importedName(NPE)).append(" if {@code augmentType} is {@code null}\n")
            .append(" */\n")
            .append('@').append(importedName(SUPPRESS_WARNINGS))
                .append("({ \"unchecked\", \"checkstyle:methodTypeParameterName\"})\n")
            .append("public <E$$ extends ").append(importedName(augmentType)).append("> E$$ ")
                .append(AUGMENTABLE_AUGMENTATION_NAME).append("(").append(importedName(CLASS))
                .append("<E$$> augmentationType) {\n")
            .append("    return (E$$) ").append(AUGMENTATION_FIELD).append(".get(")
                .append(importedName(JU_OBJECTS)).append(".requireNonNull(augmentationType));\n")
            .append("}\n");
    }

    @Override
    void appendCopyKeys(final StringBuilder sb, final List<GeneratedProperty> keyProps) {
        sb.append("    this.key = base.").append(KEY_AWARE_KEY_NAME).append("();\n");
        for (var field : keyProps) {
            sb.append("    this.").append(fieldName(field)).append(" = base.").append(getterMethodName(field))
                .append("();\n");
        }
    }

    @Override
    void appendCopyNonKeys(final StringBuilder sb, final Collection<BuilderGeneratedProperty> props) {
        for (var field : props) {
            sb.append("    this.").append(fieldName(field)).append(" = base.").append(field.getGetterName())
                .append("();\n");
        }
    }

    @Override
    void appendCopyAugmentation(final StringBuilder sb) {
        sb
            .append("    final var aug = base.augmentations();\n")
            .append("    if (!aug.isEmpty()) {\n")
            .append("        this.").append(AUGMENTATION_FIELD).append(" = new ").append(importedName(JU_HASHMAP))
                .append("<>(aug);\n")
            .append("    }\n");
    }
}
