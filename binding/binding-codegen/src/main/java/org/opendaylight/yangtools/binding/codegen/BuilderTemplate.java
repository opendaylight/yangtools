/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static com.google.common.base.Verify.verify;

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
import org.opendaylight.yangtools.binding.contract.Naming;
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
        bb.newLineIfNotEmpty();
        bb.append(generateDeprecatedAnnotation(targetType.getAnnotations()));
        bb.newLineIfNotEmpty();
        bb.append(generatedAnnotation());
        bb.newLineIfNotEmpty();
        bb
            .str("public class ").str(type().simpleName()).str(" {").nl()
            // FIXME: remove this newline
            .nl()
            .indented(builderFields());
        bb.nl().indented(constantsDeclarations());
        bb.newLine();
        if (augmentType != null) {
            bb.append("    ");
            final var augmentTypeRef = importedName(augmentType);
            final var mapTypeRef = importedName(JU_MAP);

            bb.append(mapTypeRef);
            bb.append("<");
            bb.append(importedName(CLASS));
            bb.append("<? extends ");
            bb.append(augmentTypeRef);
            bb.append(">, ");
            bb.append(augmentTypeRef);
            bb.append("> ");
            bb.append(Naming.AUGMENTATION_FIELD);
            bb.append(" = ");
            bb.append(mapTypeRef);
            bb.append(".of();\n");
        }

        final var targetTypeName = importedName(targetType);
        bb.nl().append(
                  "    /**\n");
        bb.append("     * Construct an empty builder.\n");
        bb.append("     */\n");
        bb.append("    public ");
        bb.append(type().simpleName());
        bb.append("() {\n");
        bb.append("        // No-op\n");
        bb.append("    }\n");
        bb.indented(generateConstructorsFromIfcs())
            .nl().append(
                  "    /**\n");
        bb.append("     * Construct a builder initialized with state from specified {@link ");
        bb.append(targetTypeName);
        bb.append("}.\n");
        bb.append("     *\n");
        bb.append("     * @param base ");
        bb.append(targetTypeName);
        bb.append(" from which the builder should be initialized\n");
        bb.append("     */\n");
        bb.append("    public ");
        bb.append(generateCopyConstructor(targetType, type().getEnclosedTypes().getFirst()), "    ");
        bb.newLineIfNotEmpty();
        bb
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
            .str("    /**").nl()
            .str("     * A new {@link ").str(targetTypeName).str("} instance.").nl()
            .str("     *").nl()
            .str("     * @return A new {@link ").str(targetTypeName).str("} instance.").nl()
            .str("     */").nl()
            .str("    public ").str(importedNonNull(targetType)).str(" build() {").nl()
            .str("        return new ").str(importedName(type().getEnclosedTypes().getFirst())).str("(this);").nl()
            .str("    }").nl()
            .nl()
            .indented(implTemplate.body())
            .str("}").nl();
    }

    private @Nullable BlockBuilder builderFields() {
        // FIXME: this just begs for specialization
        final var key = keyType;
        if (key != null) {
            verify(!properties.isEmpty(), "empty properties with key %s", key);
            return propertyFields()
                .str("private ").str(importedName(key)).str(" key;").nl();
        }
        return properties == null || properties.isEmpty() ? null : propertyFields();
    }

    @NonNullByDefault
    private BlockBuilder propertyFields() {
        final var bb = new BlockBuilder();
        for (var prop : properties) {
            bb.str("private ").str(importedReturnType(prop)).str(" ").str(fieldName(prop)).append(";\n");
        }
        return bb;
    }

    @Override
    CharSequence generateDeprecatedAnnotation(final AnnotationType ann) {
        final var forRemoval = ann.getParameter("forRemoval");
        return forRemoval != null ? "@" + importedName(DEPRECATED) + "(forRemoval = " +  forRemoval.getValue()  + ")"
            :  "@" + importedName(SUPPRESS_WARNINGS) + "(\"deprecation\")";
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
                    bb.appendImmediate("\n", "");
                }
                bb.append(generateConstructorFromIfc(genType));
                bb.newLineIfNotEmpty();
            }
        }
        return bb;
    }

    /**
     * Generate constructor with argument of given type.
     */
    private @NonNull BlockBuilder generateConstructorFromIfc(final GeneratedType genType) {
        //        «IF impl.hasNonDefaultMethods»
        //            «val typeName = impl.importedName»
        //            /**
        //             * Construct a new builder initialized from specified {@link «typeName»}.
        //             *
        //             * @param arg «typeName» from which the builder should be initialized
        //             */
        //            public «type.simpleName»(«typeName» arg) {
        //                «printConstructorPropertySetter(impl)»
        //            }
        //
        //        «ENDIF»
        //        «FOR implTypeImplement : impl.implements»
        //            «generateConstructorFromIfc(implTypeImplement)»
        //        «ENDFOR»

        final var bb = new BlockBuilder();
        if (hasNonDefaultMethods(genType)) {
            final var typeName = importedName(genType);
            bb.append("/**\n");
            bb.append(" * Construct a new builder initialized from specified {@link ");
            bb.append(typeName);
            bb.append("}.\n");
            bb.append(" *\n");
            bb.append(" * @param arg ");
            bb.append(typeName);
            bb.append(" from which the builder should be initialized\n");
            bb.append(" */\n");
            bb.append("public ");
            bb.append(type().simpleName());
            bb.append("(");
            bb.append(typeName);
            bb.append(" arg) {\n");
            bb.indented(printConstructorPropertySetter(genType)).append("}\n");
            bb.newLine();
        }
        for (var implTypeImplement : genType.getImplements()) {
            if (implTypeImplement instanceof GeneratedType implType) {
                bb.append(generateConstructorFromIfc(implType));
                bb.newLineIfNotEmpty();
            }
        }
        return bb;
    }

    private @Nullable BlockBuilder printConstructorPropertySetter(final Type implementedIfc) {
        if (!(implementedIfc instanceof GeneratedType ifc) || ifc instanceof GeneratedTransferObject) {
            return null;
        }

        //        «FOR getter : ifc.nonDefaultMethods»
        //            «IF Naming.isGetterMethodName(getter.name)»
        //                «val propertyName = getter.propertyNameFromGetter»
        //                «printPropertySetter(getter, '''arg.«getter.name»()''', propertyName)»;
        //            «ENDIF»
        //        «ENDFOR»
        final var bb = new BlockBuilder();
        for (var getter : nonDefaultMethods(ifc)) {
            if (Naming.isGetterMethodName(getter.getName())) {
                bb.append(printPropertySetter(getter, "arg." + getter.getName() + "()",
                    propertyNameFromGetter(getter)));
                bb.append(";\n");
            }
        }

        //        «FOR impl : ifc.implements»
        //            «printConstructorPropertySetter(impl, getSpecifiedGetters(ifc))»
        //        «ENDFOR»
        for (var impl : ifc.getImplements()) {
            bb.append(printConstructorPropertySetter(impl, getSpecifiedGetters(ifc)));
            bb.newLineIfNotEmpty();
        }
        return bb;
    }

    private CharSequence printConstructorPropertySetter(final Type implementedIfc,
            final Set<MethodSignature> alreadySetProperties) {
        if (!(implementedIfc instanceof GeneratedType ifc) || ifc instanceof GeneratedTransferObject) {
            return "";
        }

        final var bb = new BlockBuilder();
        //    «FOR getter : ifc.nonDefaultMethods»
        //        «IF Naming.isGetterMethodName(getter.name) && getterByName(alreadySetProperties, getter.name) ===
        // null»
        //            «val propertyName = getter.propertyNameFromGetter»
        //            «printPropertySetter(getter, '''arg.«getter.name»()''', propertyName)»;
        //        «ENDIF»
        //    «ENDFOR»
        for (var getter : nonDefaultMethods(ifc)) {
            if (Naming.isGetterMethodName(getter.getName())
                && getterByName(alreadySetProperties, getter.getName()) == null) {
                bb.append(printPropertySetter(getter, "arg." + getter.getName() + "()",
                    propertyNameFromGetter(getter)));
                bb.append(";\n");
            }
        }

        //    «FOR descendant : ifc.implements»
        //        «printConstructorPropertySetter(descendant, Sets.union(alreadySetProperties,
        // getSpecifiedGetters(ifc)))»
        //    «ENDFOR»
        for (var descendant : ifc.getImplements()) {
            bb.append(printConstructorPropertySetter(descendant,
                Sets.union(alreadySetProperties, getSpecifiedGetters(ifc))));
            bb.newLineIfNotEmpty();
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
        bb.newLineIfNotEmpty();
        bb.append("public void fieldsFrom(final ");
        bb.append(importedName(BindingTypes.GROUPING));
        bb.append(" arg) {\n");
        bb.append("    boolean isValidArg = false;\n");
        for (var impl : getAllIfcs(targetType)) {
            bb.indented(generateIfCheck(impl, done));
        }
        bb.str("    ").str(importedName(CODEHELPERS)).append(".validValue(isValidArg, arg, \"");
        bb.append(toListOfNames(getAllIfcs(targetType)), "    ");
        bb.append("\");\n");
        bb.append("}\n");
        return bb;
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
            .str("private static final class LazyEmpty {").nl()
            .str("    static final ").str(nonnullTarget).str(" INSTANCE = new ").str(type().simpleName())
                .strLn("().build();")
            .nl()
            .str("    private LazyEmpty() {").nl()
            .str("        // Hidden on purpose").nl()
            .str("    }").nl()
            .str("}").nl()
            .nl()
            .str("/**").nl()
            .str(" * Get empty instance of ").str(targetName).strLn(".")
            .str(" *").nl()
            .str(" * @return An empty {@link ").str(targetName).str("}").nl()
            .str(" */").nl()
            .str("public static ").str(nonnullTarget).str(" empty() {").nl()
            .str("    return LazyEmpty.INSTANCE;").nl()
            .str("}").nl();
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

        final var bb = new StringBuilder();
        bb.append("/**\n");
        bb.append(
            " * Set fields from given grouping argument. Valid argument is instance of one of following types:\n");
        bb.append(" * <ul>\n");
        for (var impl : getAllIfcs(type)) {
            bb.append(" *   <li>{@link ");
            bb.append(importedName(impl));
            bb.append("}</li>\n");
        }
        bb.append(" * </ul>\n");
        bb.append(" *\n");
        bb.append(" * @param arg grouping object\n");
        bb.append(" * @throws ");
        bb.append(importedName(IAE));
        bb.append(" if given argument is none of valid types or has property with incompatible value\n");
        bb.append(" */\n");
        return bb;
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
            .str("if (arg instanceof ").str(importedName(implType)).str(" castArg) {").nl()
                .indented(printPropertySetter(implType))
            .str("    isValidArg = true;").nl()
            .str("}").nl();
    }

    private @Nullable BlockBuilder printPropertySetter(final Type implementedIfc) {
        if (!(implementedIfc instanceof GeneratedType ifc) || ifc instanceof GeneratedTransferObject) {
            return null;
        }

        final var bb = new BlockBuilder();
        for (var getter : nonDefaultMethods(ifc)) {
            if (Naming.isGetterMethodName(getter.getName()) && !hasOverrideAnnotation(getter)) {
                bb.append(printPropertySetter(getter, "castArg." + getter.getName() + "()",
                    propertyNameFromGetter(getter)));
                bb.append(";\n");
            }
        }
        return bb;
    }

    private String printPropertySetter(final MethodSignature getter, final String retrieveProperty,
            final String propertyName) {
        final var ownGetter = implTemplate.findGetter(getter.getName());
        final var ownGetterType = ownGetter.getReturnType();
        if (strictTypeEquals(getter.getReturnType(), ownGetterType)) {
            return "this._" + propertyName + " = " + retrieveProperty;
        }
        if (ownGetterType instanceof ParameterizedType parameterized) {
            final var itemType = parameterized.getActualTypeArguments().getFirst();
            if (Types.isListType(parameterized)) {
                return printPropertySetter(retrieveProperty, propertyName, "checkListFieldCast",
                    importedName(itemType));
            }
            if (Types.isSetType(parameterized)) {
                return printPropertySetter(retrieveProperty, propertyName, "checkSetFieldCast", importedName(itemType));
            }
        }
        return printPropertySetter(retrieveProperty, propertyName, "checkFieldCast", importedName(ownGetterType));
    }

    private String printPropertySetter(final String retrieveProperty, final String propertyName,
            final String checkerName, final String className) {
        return "this._" + propertyName + " = " + importedName(CODEHELPERS) + '.' + checkerName + '('
            + className + ".class, \"" + propertyName + "\", " + retrieveProperty + ')';
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

    private List<String> toListOfNames(final Collection<Type> types) {
        return types.stream().map(this::importedName).collect(Collectors.toUnmodifiableList());
    }

    @NonNullByDefault
    private BlockBuilder constantsDeclarations() {
        final var bb = new BlockBuilder();
        for (var def : type().getConstantDefinitions()) {
            if (!def.getName().startsWith(TypeConstants.PATTERN_CONSTANT_NAME)) {
                bb.append(emitConstant(def));
                bb.newLineIfNotEmpty();
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

            bb.append("private static final ");
            bb.append(jurPatternRef);
            bb.append("[] ");
            bb.append(Constants.MEMBER_PATTERN_LIST);
            bb.append(fieldSuffix);
            bb.append(" = ");
            bb.append(importedName(CODEHELPERS));
            bb.append(".compilePatterns(");
            bb.append(importedName(JU_LIST));
            bb.append(".of(\n");
            {
                boolean first = true;
                for (var v : xsdToPattern.keySet()) {
                    if (first) {
                        first = false;
                    } else {
                        bb.appendImmediate(", ", "");
                    }
                    bb.append("\"");
                    bb.append(StringEscapeUtils.escapeJava(v));
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
                        bb.appendImmediate(", ", "");
                    }
                    bb.append("\"");
                    bb.append(StringEscapeUtils.escapeJava(v));
                    bb.append("\"");
                }
            }
            bb.append(" };\n");
        }
        return bb;
    }

    private CharSequence generateSetter(final BuilderGeneratedProperty field) {
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
        final var restrictions = restrictionsForSetter(actualType);

        final var bb = new BlockBuilder();
        if (restrictions != null) {
            bb.append(generateCheckers(field, restrictions, actualType));
            bb.newLineIfNotEmpty();
        }

        //
        //    /**
        //     * Set the property corresponding to {@link «targetType.importedName»#«field.getterName»()} to the
        // specified
        //     * value.
        //     *
        //     * @param values desired value
        //     * @return this builder
        //     */
        //    public «type.simpleName» set«field.getName.toFirstUpper»(final «field.returnType.importedName» values) {
        bb.nl().append(
                  "/**\n");
        bb.append(" * Set the property corresponding to {@link ");
        bb.append(importedName(targetType));
        bb.append("#");
        bb.append(field.getGetterName());
        bb.append("()} to the specified\n");
        bb.append(" * value.\n");
        bb.append(" *\n");
        bb.append(" * @param values desired value\n");
        bb.append(" * @return this builder\n");
        bb.append(" */\n");
        bb.append("public ");
        bb.append(type().simpleName());
        bb.append(" set");
        bb.append(Naming.toFirstUpper(field.getName()));
        bb.append("(final ");
        bb.append(importedReturnType(field));
        bb.append(" values) {\n");

        //        «IF restrictions !== null»
        //            if (values != null) {
        //               for («actualType.importedName» value : values) {
        //                   «checkArgument(field, restrictions, actualType, "value")»
        //               }
        //            }
        //        «ENDIF»
        if (restrictions != null) {
            bb.append("if (values != null) {\n");
            bb.append("   for (");
            bb.append(importedName(actualType), "   ");
            bb.append(" value : values) {\n");
            bb.append("       ");
            bb.append(checkArgument(field, restrictions, actualType, "value"), "       ");
            bb.newLineIfNotEmpty();
            bb.append("   }\n");
            bb.append("}\n");
        }

        //            this.«field.fieldName» = values;
        //            return this;
        //        }
        //
        bb.append("    this.");
        bb.append(fieldName(field));
        bb.append(" = values;\n");
        bb.append("    return this;\n");
        bb.append("}\n");
        return bb.nl();
    }

    private @NonNull BlockBuilder generateMapSetter(final BuilderGeneratedProperty field, final Type actualType) {
        final var bb = new BlockBuilder();
        final var restrictions = JavaFileTemplate.restrictionsForSetter(actualType);
        if (restrictions != null) {
            bb.append(generateCheckers(field, restrictions, actualType));
            bb.newLineIfNotEmpty();
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

        bb.nl().append(
                  "/**\n");
        bb.append(" * Set the property corresponding to {@link ");
        bb.append(importedName(targetType));
        bb.append("#");
        bb.append(field.getGetterName());
        bb.append("()} to the specified\n");
        bb.append(" * value.\n");
        bb.append(" *\n");
        bb.append(" * @param values desired value\n");
        bb.append(" * @return this builder\n");
        bb.append(" */\n");
        bb.append("public ");
        bb.append(type().simpleName());
        bb.append(" set");
        bb.append(Naming.toFirstUpper(field.getName()));
        bb.append("(final ");
        bb.append(importedReturnType(field));
        bb.append(" values) {\n");

        //        «IF restrictions !== null»
        //            if (values != null) {
        //               for («actualType.importedName» value : values.values()) {
        //                   «checkArgument(field, restrictions, actualType, "value")»
        //               }
        //            }
        //        «ENDIF»
        if (restrictions != null) {
            bb.append("if (values != null) {\n");
            bb.append("   for (");
            bb.append(importedName(actualType), "   ");
            bb.append(" value : values.values()) {\n");
            bb.append("       ");
            bb.append(checkArgument(field, restrictions, actualType, "value"), "       ");
            bb.newLineIfNotEmpty();
            bb.append("   }\n");
            // FIXME: no nl() here ?
            bb.nl().append("}\n");
        }

        //            this.«field.fieldName» = values;
        //            return this;
        //        }
        bb.append("    this.");
        bb.append(fieldName(field));
        bb.append(" = values;\n");
        bb.append("    return this;\n");
        bb.append("}\n");
        return bb;
    }

    private @NonNull BlockBuilder generateSimpleSetter(final BuilderGeneratedProperty field, final Type actualType) {
        final var bb = new BlockBuilder();
        final var restrictions = restrictionsForSetter(actualType);
        if (restrictions != null) {
            bb.nl().append(generateCheckers(field, restrictions, actualType));
            bb.newLineIfNotEmpty();
        }
        bb.nl().append(
                  "/**\n");
        bb.append(" * Set the property corresponding to {@link ");
        bb.append(importedName(targetType), " ");
        bb.append("#");
        bb.append(field.getGetterName());
        bb.append("()} to the specified\n");
        bb.append(" * value.\n");
        bb.append(" *\n");
        bb.append(" * @param value desired value\n");
        bb.append(" * @return this builder\n");
        bb.append(" */\n");
        bb.append("public ");
        bb.append(type().simpleName());
        bb.append(" set");
        bb.append(Naming.toFirstUpper(field.getName()));
        bb.append("(final ");
        bb.append(importedReturnType(field));
        bb.append(" value) {\n");
        if (restrictions != null) {
            bb.append("    if (value != null) {\n");
            bb.append("        ");
            bb.append(checkArgument(field, restrictions, actualType, "value"), "        ");
            bb.newLineIfNotEmpty();
            bb.append("    }\n");
        }
        bb.append("    this.");
        bb.append(fieldName(field));
        bb.append(" = value;\n");
        bb.append("    return this;\n");
        bb.append("}\n");
        return bb;
    }

    /**
     * {@return string with the setter methods}
     */
    private @NonNull BlockBuilder generateSetters() {
        final var bb = new BlockBuilder();
        if (keyType != null) {
            bb.append("/**\n");
            bb.append(" * Set the key value corresponding to {@link ");
            bb.append(importedName(targetType));
            bb.append("#");
            bb.append(Naming.KEY_AWARE_KEY_NAME, " ");
            bb.append("()} to the specified\n");
            bb.append(" * value.\n");
            bb.append(" *\n");
            bb.append(" * @param key desired value\n");
            bb.append(" * @return this builder\n");
            bb.append(" */\n");
            bb.append("public ");
            bb.append(type().simpleName());
            bb.append(" withKey(final ");
            bb.append(importedName(keyType));
            bb.append(" key) {\n");
            bb.append("    this.key = key;\n");
            bb.append("    return this;\n");
            bb.append("}\n");
        }
        for (var property : properties) {
            bb.append(generateSetter(property));
            bb.newLineIfNotEmpty();
        }
        bb.newLine();
        if (augmentType != null) {
            final var augmentTypeRef = importedName(augmentType);
            final var hashMapRef = importedName(JU_HASHMAP);
            bb.append("/**\n");
            bb.append(" * Add an augmentation to this builder\'s product.\n");
            bb.append(" *\n");
            bb.append(" * @param augmentation augmentation to be added\n");
            bb.append(" * @return this builder\n");
            bb.append(" * @throws ");
            bb.append(importedName(NPE));
            bb.append(" if {@code augmentation} is null\n");
            bb.append(" */\n");
            bb.append("public ");
            bb.append(type().simpleName());
            bb.append(" addAugmentation(");
            bb.append(augmentTypeRef);
            bb.append(" augmentation) {\n");
            bb.append("    if (!(this.");
            bb.append(Naming.AUGMENTATION_FIELD);
            bb.append(" instanceof ");
            bb.append(hashMapRef);
            bb.append(")) {\n");
            bb.append("        ");
            bb.append("this.");
            bb.append(Naming.AUGMENTATION_FIELD);
            bb.append(" = new ");
            bb.append(hashMapRef);
            bb.append("<>();\n");
            bb.append("    }\n");
            bb.nl().append("    this.");
            bb.append(Naming.AUGMENTATION_FIELD);
            bb.append(".put(augmentation.");
            bb.append(Naming.BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME);
            bb.append("(), augmentation);\n");
            bb.append("    return this;\n");
            bb.append("}\n");
            // FIXME: use a text block here
            bb.nl().append(
                     "/**\n");
            bb.append(" * Remove an augmentation from this builder\'s product. If this builder does not track such an");
            bb.append(" augmentation\n");
            bb.append(" * type, this method does nothing.\n");
            bb.append(" *\n");
            bb.append(" * @param augmentationType augmentation type to be removed\n");
            bb.append(" * @return this builder\n");
            bb.append(" */\n");
            bb.append("public ");
            bb.append(type().simpleName());
            bb.append(" removeAugmentation(");
            bb.append(importedName(CLASS));
            bb.append("<? extends ");
            bb.append(augmentTypeRef);
            bb.append("> augmentationType) {\n");
            bb.append("    if (this.");
            bb.append(Naming.AUGMENTATION_FIELD, "    ");
            bb.append(" instanceof ");
            bb.append(hashMapRef, "    ");
            bb.append(") {\n");
            bb.append("        this.");
            bb.append(Naming.AUGMENTATION_FIELD);
            bb.append(".remove(augmentationType);\n");
            bb.append("    }\n");
            bb.append("    return this;\n");
            bb.append("}\n");
        }
        return bb;
    }

    // FIXME: remove this suppression
    @SuppressWarnings("checkstyle:lineLength")
    private @NonNull BlockBuilder createDescription(final GeneratedType targetType) {
        //        val target = targetType.importedName
        //        return '''
        //        Class that builds {@link «target»} instances. Overall design of the class is that of a
        //        <a href="https://en.wikipedia.org/wiki/Fluent_interface">fluent interface</a>, where method chaining is used.
        //
        //        <p>
        //        In general, this class is supposed to be used like this template:
        //        <pre>
        //          <code>
        //            «target» create«target»(int fooXyzzy, int barBaz) {
        //                return new «target»Builder()
        //                    .setFoo(new FooBuilder().setXyzzy(fooXyzzy).build())
        //                    .setBar(new BarBuilder().setBaz(barBaz).build())
        //                    .build();
        //            }
        //          </code>
        //        </pre>
        //
        //        <p>
        //        This pattern is supported by the immutable nature of «target», as instances can be freely passed around without
        //        worrying about synchronization issues.
        //
        //        <p>
        //        As a side note: method chaining results in:
        //        <ul>
        //          <li>very efficient Java bytecode, as the method invocation result, in this case the Builder reference, is
        //              on the stack, so further method invocations just need to fill method arguments for the next method
        //              invocation, which is terminated by {@link #build()}, which is then returned from the method</li>
        //          <li>better understanding by humans, as the scope of mutable state (the builder) is kept to a minimum and is
        //              very localized</li>
        //          <li>better optimization opportunities, as the object scope is minimized in terms of invocation (rather than
        //              method) stack, making <a href="https://en.wikipedia.org/wiki/Escape_analysis">escape analysis</a> a lot
        //              easier. Given enough compiler (JIT/AOT) prowess, the cost of th builder object can be completely
        //              eliminated</li>
        //        </ul>
        //
        //        @see «target»

        final var target = importedName(targetType);
        final var bb = new BlockBuilder();
        // FIXME: use Java text blocks as much as possible
        bb.append("Class that builds {@link ");
        bb.append(target);
        bb.append("} instances. Overall design of the class is that of a\n");
        bb.append("<a href=\"https://en.wikipedia.org/wiki/Fluent_interface\">fluent interface</a>, where method chaining is used.\n");
        bb.nl().append(
                  "<p>\n");
        bb.append("In general, this class is supposed to be used like this template:\n");
        bb.append("<pre>\n");
        bb.append("  <code>\n");
        bb.append("    ");
        bb.append(target);
        bb.append(" create");
        bb.append(target);
        bb.append("(int fooXyzzy, int barBaz) {\n");
        bb.append("        return new ");
        bb.append(target);
        bb.append("Builder()\n");
        bb.append("            .setFoo(new FooBuilder().setXyzzy(fooXyzzy).build())\n");
        bb.append("            .setBar(new BarBuilder().setBaz(barBaz).build())\n");
        bb.append("            .build();\n");
        bb.append("    }\n");
        bb.append("  </code>\n");
        bb.append("</pre>\n");
        bb.nl().append(
                  "<p>\n");
        bb.append("This pattern is supported by the immutable nature of ");
        bb.append(target);
        bb.append(", as instances can be freely passed around without");
        bb.newLineIfNotEmpty();
        bb.append("worrying about synchronization issues.\n");
        bb.nl().append("<p>\n");
        bb.append("As a side note: method chaining results in:");
        bb.nl().append("<ul>");
        bb.nl().append("  <li>very efficient Java bytecode, as the method invocation result, in this case the Builder reference, is\n");
        bb.append("      on the stack, so further method invocations just need to fill method arguments for the next method\n");
        bb.append("      invocation, which is terminated by {@link #build()}, which is then returned from the method</li>\n");
        bb.append("  <li>better understanding by humans, as the scope of mutable state (the builder) is kept to a minimum and is\n");
        bb.append("      very localized</li>\n");
        bb.append("  <li>better optimization opportunities, as the object scope is minimized in terms of invocation (rather than\n");
        bb.append("      method) stack, making <a href=\"https://en.wikipedia.org/wiki/Escape_analysis\">escape analysis</a> a lot\n");
        bb.append("      easier. Given enough compiler (JIT/AOT) prowess, the cost of th builder object can be completely\n");
        bb.append("      eliminated</li>\n");
        bb.append("</ul>\n");
        bb.nl().append("@see ");
        bb.append(target);
        bb.newLineIfNotEmpty();
        return bb;
    }

    @Override
    String formatDataForJavaDoc(final GeneratedType type) {
        final var typeDescription = createDescription(type);
        if (typeDescription == null || typeDescription.isEmpty()) {
            return "";
        }

        final var bb = new BlockBuilder();
        bb.append(typeDescription);
        bb.newLineIfNotEmpty();
        return bb.toRawString();
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
                .append(Naming.AUGMENTABLE_AUGMENTATION_NAME).append("(").append(importedName(CLASS))
                .append("<E$$> augmentationType) {\n")
            .append("    return (E$$) ").append(Naming.AUGMENTATION_FIELD).append(".get(")
                .append(importedName(JU_OBJECTS)).append(".requireNonNull(augmentationType));\n")
            .append("}\n");
    }

    @Override
    void appendCopyKeys(final StringBuilder sb, final List<GeneratedProperty> keyProps) {
        sb.append("    this.key = base.").append(Naming.KEY_AWARE_KEY_NAME).append("();\n");
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
            .append("        this.").append(Naming.AUGMENTATION_FIELD).append(" = new ")
                .append(importedName(JU_HASHMAP)).append("<>(aug);\n")
            .append("    }\n");
    }
}
