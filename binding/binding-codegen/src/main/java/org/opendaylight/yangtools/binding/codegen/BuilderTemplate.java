/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

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
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.StringExtensions;
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

    /**
     * Template method which generates JAVA class body for builder class and for IMPL class.
     *
     * @return string with JAVA source code
     */
    @Override
    public CharSequence body() {
        final var sc = new StringConcatenation();
        sc.append(wrapToDocumentation(formatDataForJavaDoc(targetType)));
        sc.newLineIfNotEmpty();
        sc.append(generateDeprecatedAnnotation(targetType.getAnnotations()));
        sc.newLineIfNotEmpty();
        sc.append(generatedAnnotation());
        sc.newLineIfNotEmpty();
        sc.append("public class ");
        sc.append(type().simpleName());
        sc.append(" {\n");
        sc.newLine();
        sc.append("    ");
        sc.append(generateBuilderFields(), "    ");
        sc.newLineIfNotEmpty();
        sc.newLine();
        sc.append("    ");
        sc.append(constantsDeclarations(), "    ");
        sc.newLineIfNotEmpty();
        sc.newLine();
        if (augmentType != null) {
            sc.append("    ");
            final var augmentTypeRef = importedName(augmentType);
            final var mapTypeRef = importedName(JU_MAP);

            sc.append(mapTypeRef);
            sc.append("<");
            sc.append(importedName(CLASS));
            sc.append("<? extends ");
            sc.append(augmentTypeRef);
            sc.append(">, ");
            sc.append(augmentTypeRef);
            sc.append("> ");
            sc.append(Naming.AUGMENTATION_FIELD);
            sc.append(" = ");
            sc.append(mapTypeRef);
            sc.append(".of();\n");
        }

        final var targetTypeName = importedName(targetType);
        sc.newLine();
        sc.append("    /**\n");
        sc.append("     * Construct an empty builder.\n");
        sc.append("     */\n");
        sc.append("    public ");
        sc.append(type().simpleName());
        sc.append("() {\n");
        sc.append("        // No-op\n");
        sc.append("    }\n");
        sc.newLine();
        sc.append("    ");
        sc.append(generateConstructorsFromIfcs(), "    ");
        sc.newLineIfNotEmpty();
        sc.newLine();
        sc.append("    /**\n");
        sc.append("     * Construct a builder initialized with state from specified {@link ");
        sc.append(targetTypeName);
        sc.append("}.\n");
        sc.append("     *\n");
        sc.append("     * @param base ");
        sc.append(targetTypeName);
        sc.append(" from which the builder should be initialized\n");
        sc.append("     */\n");
        sc.append("    public ");
        sc.append(generateCopyConstructor(targetType, type().getEnclosedTypes().getFirst()), "    ");
        sc.newLineIfNotEmpty();
        sc.newLine();
        sc.append("    ");
        sc.append(generateMethodFieldsFrom(), "    ");
        sc.newLineIfNotEmpty();
        sc.newLine();
        if (isNonPresenceContainer(targetType)) {
            sc.append("    ");
            sc.append(generateEmptyInstance(), "    ");
            sc.newLineIfNotEmpty();
        }
        sc.newLine();
        sc.append("    ");
        sc.append(generateGetters(false), "    ");
        sc.newLineIfNotEmpty();
        if (augmentType != null) {
            sc.newLine();
            sc.append("    ");
            sc.append(generateAugmentation(), "    ");
            sc.newLineIfNotEmpty();
        }
        sc.newLine();
        sc.append("    ");
        sc.append(generateSetters(), "    ");
        sc.newLineIfNotEmpty();
        sc.newLine();
        sc.append("    /**\n");
        sc.append("     * A new {@link ");
        sc.append(targetTypeName);
        sc.append("} instance.\n");
        sc.append("     *\n");
        sc.append("     * @return A new {@link ");
        sc.append(targetTypeName);
        sc.append("} instance.\n");
        sc.append("     */\n");
        sc.append("    public ");
        sc.append(importedNonNull(targetType));
        sc.append(" build() {\n");
        sc.append("        return new ");
        sc.append(importedName(type().getEnclosedTypes().getFirst()));
        sc.append("(this);\n");
        sc.append("    }\n");
        sc.newLine();
        sc.append("    ");
        sc.append(implTemplate.body(), "    ");
        sc.newLineIfNotEmpty();
        sc.append("}\n");
        return sc;
    }

    @Override
    CharSequence generateDeprecatedAnnotation(final AnnotationType ann) {
        final var forRemoval = ann.getParameter("forRemoval");
        return forRemoval != null ? "@" + importedName(DEPRECATED) + "(forRemoval = " +  forRemoval.getValue()  + ")"
            :  "@" + importedName(SUPPRESS_WARNINGS) + "(\"deprecation\")";
    }

    /**
     * Generate default constructor and constructor for every implemented interface from uses statements.
     */
    private CharSequence generateConstructorsFromIfcs() {
        if (targetType instanceof GeneratedTransferObject) {
            return "";
        }

        final var sc = new StringConcatenation();
        boolean first = true;
        for (var impl : targetType.getImplements()) {
            if (first) {
                first = false;
            } else {
                sc.appendImmediate("\n", "");
            }
            sc.append(generateConstructorFromIfc(impl));
            sc.newLineIfNotEmpty();
        }
        return sc;
    }

    /**
     * Generate constructor with argument of given type.
     */
    private CharSequence generateConstructorFromIfc(final Type iface) {
        if (!(iface instanceof GeneratedType genType)) {
            return "";
        }

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

        final var sc = new StringConcatenation();
        if (hasNonDefaultMethods(genType)) {
            final var typeName = importedName(genType);
            sc.append("/**\n");
            sc.append(" * Construct a new builder initialized from specified {@link ");
            sc.append(typeName);
            sc.append("}.\n");
            sc.append(" *\n");
            sc.append(" * @param arg ");
            sc.append(typeName);
            sc.append(" from which the builder should be initialized\n");
            sc.append(" */\n");
            sc.append("public ");
            sc.append(type().simpleName());
            sc.append("(");
            sc.append(typeName);
            sc.append(" arg) {\n");
            sc.append("    ");
            sc.append(printConstructorPropertySetter(genType), "    ");
            sc.newLineIfNotEmpty();
            sc.append("}\n");
            sc.newLine();
        }
        for (var implTypeImplement :  genType.getImplements()) {
            sc.append(generateConstructorFromIfc(implTypeImplement));
            sc.newLineIfNotEmpty();
        }
        return sc;
    }

    private CharSequence printConstructorPropertySetter(final Type implementedIfc) {
        if (!(implementedIfc instanceof GeneratedType ifc) || ifc instanceof GeneratedTransferObject) {
            return "";
        }

        //        «FOR getter : ifc.nonDefaultMethods»
        //            «IF Naming.isGetterMethodName(getter.name)»
        //                «val propertyName = getter.propertyNameFromGetter»
        //                «printPropertySetter(getter, '''arg.«getter.name»()''', propertyName)»;
        //            «ENDIF»
        //        «ENDFOR»
        final var sc = new StringConcatenation();
        for (var getter : nonDefaultMethods(ifc)) {
            if (Naming.isGetterMethodName(getter.getName())) {
                sc.append(printPropertySetter(getter, "arg." + getter.getName() + "()",
                    propertyNameFromGetter(getter)));
                sc.append(";\n");
            }
        }

        //        «FOR impl : ifc.implements»
        //            «printConstructorPropertySetter(impl, getSpecifiedGetters(ifc))»
        //        «ENDFOR»
        for (var impl : ifc.getImplements()) {
            sc.append(printConstructorPropertySetter(impl, getSpecifiedGetters(ifc)));
            sc.newLineIfNotEmpty();
        }
        return sc;
    }

    private CharSequence printConstructorPropertySetter(final Type implementedIfc,
            final Set<MethodSignature> alreadySetProperties) {
        if (!(implementedIfc instanceof GeneratedType ifc) || ifc instanceof GeneratedTransferObject) {
            return "";
        }

        final var sc = new StringConcatenation();
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
                sc.append(printPropertySetter(getter, "arg." + getter.getName() + "()",
                    propertyNameFromGetter(getter)));
                sc.append(";\n");
            }
        }

        //    «FOR descendant : ifc.implements»
        //        «printConstructorPropertySetter(descendant, Sets.union(alreadySetProperties,
        // getSpecifiedGetters(ifc)))»
        //    «ENDFOR»
        for (var descendant : ifc.getImplements()) {
            sc.append(printConstructorPropertySetter(descendant,
                Sets.union(alreadySetProperties, getSpecifiedGetters(ifc))));
            sc.newLineIfNotEmpty();
        }
        return sc;
    }

    private static Set<MethodSignature> getSpecifiedGetters(final GeneratedType type) {
        return type.getMethodDefinitions().stream()
            .filter(JavaFileTemplate::hasOverrideAnnotation)
            .collect(ImmutableSet.toImmutableSet());
    }

    /**
     * Generate 'fieldsFrom' method to set builder properties based on type of given argument.
     */
    private CharSequence generateMethodFieldsFrom() {
        if (targetType instanceof GeneratedTransferObject || !hasImplementsFromUses(targetType)) {
            return "";
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
        final var sc = new StringConcatenation();
        sc.append(generateMethodFieldsFromComment(targetType));
        sc.newLineIfNotEmpty();
        sc.append("public void fieldsFrom(final ");
        sc.append(importedName(BindingTypes.GROUPING));
        sc.append(" arg) {\n");
        sc.append("    boolean isValidArg = false;\n");
        for (var impl : getAllIfcs(targetType)) {
            sc.append("    ");
            sc.append(generateIfCheck(impl, done), "    ");
            sc.newLineIfNotEmpty();
        }
        sc.append("    ");
        sc.append(importedName(CODEHELPERS), "    ");
        sc.append(".validValue(isValidArg, arg, \"");
        sc.append(toListOfNames(getAllIfcs(targetType)), "    ");
        sc.append("\");\n");
        sc.append("}\n");
        return sc;
    }

    /**
     * Generate EMPTY instance which is lazily initialized in empty() method.
     */
    private CharSequence generateEmptyInstance() {
        final var nonnullTarget = importedNonNull(targetType);

        final var sc = new StringConcatenation();
        sc.append("private static final class LazyEmpty {\n");
        sc.append("    static final ");
        sc.append(nonnullTarget, "    ");
        sc.append(" INSTANCE = new ");
        sc.append(type().simpleName());
        sc.append("().build();\n");
        sc.newLine();
        sc.append("    private LazyEmpty() {\n");
        sc.append("        // Hidden on purpose\n");
        sc.append("    }\n");
        sc.append("}\n");
        sc.newLine();
        sc.append("/**\n");
        sc.append(" * Get empty instance of ");
        sc.append(targetType.simpleName());
        sc.append(".\n");
        sc.append(" *\n");
        sc.append(" * @return An empty {@link ");
        sc.append(targetType.simpleName());
        sc.append("}\n");
        sc.append(" */\n");
        sc.append("public static ");
        sc.append(nonnullTarget);
        sc.append(" empty() {\n");
        sc.append("    return LazyEmpty.INSTANCE;\n");
        sc.append("}\n");
        return sc;
    }

    private CharSequence generateMethodFieldsFromComment(final GeneratedType type) {
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

        final var sc = new StringConcatenation();
        sc.append("/**\n");
        sc.append(
            " * Set fields from given grouping argument. Valid argument is instance of one of following types:\n");
        sc.append(" * <ul>\n");
        for (var impl : getAllIfcs(type)) {
            sc.append(" *   <li>{@link ");
            sc.append(importedName(impl));
            sc.append("}</li>\n");
        }
        sc.append(" * </ul>\n");
        sc.append(" *\n");
        sc.append(" * @param arg grouping object\n");
        sc.append(" * @throws ");
        sc.append(importedName(IAE));
        sc.append(" if given argument is none of valid types or has property with incompatible value\n");
        sc.append(" */\n");
        return sc;
    }

    /**
     * Method is used to find out if given type implements any interface from uses.
     */
    private boolean hasImplementsFromUses(final GeneratedType type) {
        return getAllIfcs(type).stream()
            .anyMatch(impl -> impl instanceof GeneratedType genType && hasNonDefaultMethods(genType));
    }

    private CharSequence generateIfCheck(final Type impl, final List<Type> done) {
        if (!(impl instanceof GeneratedType implType) || !hasNonDefaultMethods(implType)) {
            return "";
        }

        //        if (arg instanceof «implType.importedName» castArg) {
        //            «printPropertySetter(implType)»
        //            isValidArg = true;
        //        }
        final var sc = new StringConcatenation();
        sc.append("if (arg instanceof ");
        sc.append(importedName(implType));
        sc.append(" castArg) {\n");
        sc.append("    ");
        sc.append(printPropertySetter(implType), "    ");
        sc.newLineIfNotEmpty();
        sc.append("    isValidArg = true;\n");
        sc.append("}\n");
        return sc;
    }

    private CharSequence printPropertySetter(final Type implementedIfc) {
        if (!(implementedIfc instanceof GeneratedType ifc) || ifc instanceof GeneratedTransferObject) {
            return "";
        }

        final var sc = new StringConcatenation();
        for (var getter : nonDefaultMethods(ifc)) {
            if (Naming.isGetterMethodName(getter.getName()) && !hasOverrideAnnotation(getter)) {
                sc.append(printPropertySetter(getter, "castArg." + getter.getName() + "()",
                    propertyNameFromGetter(getter)));
                sc.append(";\n");
            }
        }
        return sc;
    }

    private CharSequence printPropertySetter(final MethodSignature getter, final String retrieveProperty,
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
                return this.printPropertySetter(retrieveProperty, propertyName, "checkSetFieldCast",
                    importedName(itemType));
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

    private CharSequence constantsDeclarations() {
        final var sc = new StringConcatenation();
        for (var c : type().getConstantDefinitions()) {
            if (!c.getName().startsWith(TypeConstants.PATTERN_CONSTANT_NAME)) {
                sc.append(emitConstant(c));
                sc.newLineIfNotEmpty();
                continue;
            }

            final var cValue = (Map<String, String>) c.getValue();
            final var fieldSuffix = c.getName().substring(TypeConstants.PATTERN_CONSTANT_NAME.length());
            final var jurPatternRef = importedName(JUR_PATTERN);
            if (cValue.size() == 1) {
                final var firstEntry = cValue.entrySet().iterator().next();
                //  private static final «jurPatternRef» «Constants.MEMBER_PATTERN_LIST»«fieldSuffix» = «jurPatternRef»
                //.compile("«firstEntry.key.escapeJava»");
                //  private static final String «Constants.MEMBER_REGEX_LIST»«fieldSuffix» =
                // "«firstEntry.value.escapeJava»";

                sc.append("private static final ");
                sc.append(jurPatternRef);
                sc.append(" ");
                sc.append(Constants.MEMBER_PATTERN_LIST);
                sc.append(fieldSuffix);
                sc.append(" = ");
                sc.append(jurPatternRef);
                sc.append(".compile(\"");
                sc.append(StringEscapeUtils.escapeJava(firstEntry.getKey()));
                sc.append("\");\n");
                sc.append("private static final String ");
                sc.append(Constants.MEMBER_REGEX_LIST);
                sc.append(fieldSuffix);
                sc.append(" = \"");
                sc.append(StringEscapeUtils.escapeJava(firstEntry.getValue()));
                sc.append("\";\n");
                continue;
            }

            sc.append("private static final ");
            sc.append(jurPatternRef);
            sc.append("[] ");
            sc.append(Constants.MEMBER_PATTERN_LIST);
            sc.append(fieldSuffix);
            sc.append(" = ");
            sc.append(importedName(CODEHELPERS));
            sc.append(".compilePatterns(");
            sc.append(importedName(JU_LIST));
            sc.append(".of(\n");
            {
                boolean first = true;
                for (var v : cValue.keySet()) {
                    if (first) {
                        first = false;
                    } else {
                        sc.appendImmediate(", ", "");
                    }
                    sc.append("\"");
                    sc.append(StringEscapeUtils.escapeJava(v));
                    sc.append("\"");
                }
            }
            sc.append("));\n");
            sc.append("private static final String[] ");
            sc.append(Constants.MEMBER_REGEX_LIST);
            sc.append(fieldSuffix);
            sc.append(" = { ");
            {
                boolean first = true;
                for (var v : cValue.values()) {
                    if (first) {
                        first = false;
                    } else {
                        sc.appendImmediate(", ", "");
                    }
                    sc.append("\"");
                    sc.append(StringEscapeUtils.escapeJava(v));
                    sc.append("\"");
                }
            }
            sc.append(" };\n");
        }
        return sc;
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

    private CharSequence generateListSetter(final BuilderGeneratedProperty field, final Type actualType) {
        final var restrictions = restrictionsForSetter(actualType);

        final var sc = new StringConcatenation();
        if (restrictions != null) {
            sc.append(generateCheckers(field, restrictions, actualType));
            sc.newLineIfNotEmpty();
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
        sc.newLine();
        sc.append("/**\n");
        sc.append(" * Set the property corresponding to {@link ");
        sc.append(importedName(targetType));
        sc.append("#");
        sc.append(field.getGetterName());
        sc.append("()} to the specified\n");
        sc.append(" * value.\n");
        sc.append(" *\n");
        sc.append(" * @param values desired value\n");
        sc.append(" * @return this builder\n");
        sc.append(" */\n");
        sc.append("public ");
        sc.append(type().simpleName());
        sc.append(" set");
        sc.append(StringExtensions.toFirstUpper(field.getName()));
        sc.append("(final ");
        sc.append(importedReturnType(field));
        sc.append(" values) {\n");

        //        «IF restrictions !== null»
        //            if (values != null) {
        //               for («actualType.importedName» value : values) {
        //                   «checkArgument(field, restrictions, actualType, "value")»
        //               }
        //            }
        //        «ENDIF»
        if (restrictions != null) {
            sc.append("if (values != null) {");
            sc.newLine();
            sc.append("   ");
            sc.append("for (");
            sc.append(importedName(actualType), "   ");
            sc.append(" value : values) {\n");
            sc.append("       ");
            sc.append(checkArgument(field, restrictions, actualType, "value"), "       ");
            sc.newLineIfNotEmpty();
            sc.append("   }\n");
            sc.append("}\n");
        }

        //            this.«field.fieldName» = values;
        //            return this;
        //        }
        //
        sc.append("    this.");
        sc.append(fieldName(field));
        sc.append(" = values;\n");
        sc.append("    return this;\n");
        sc.append("}\n");
        sc.newLine();
        return sc;
    }

    private CharSequence generateMapSetter(final BuilderGeneratedProperty field, final Type actualType) {
        final var sc = new StringConcatenation();
        final var restrictions = JavaFileTemplate.restrictionsForSetter(actualType);
        if (restrictions != null) {
            sc.append(generateCheckers(field, restrictions, actualType));
            sc.newLineIfNotEmpty();
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

        sc.newLine();
        sc.append("/**\n");
        sc.append(" * Set the property corresponding to {@link ");
        sc.append(importedName(targetType));
        sc.append("#");
        sc.append(field.getGetterName());
        sc.append("()} to the specified\n");
        sc.append(" * value.\n");
        sc.append(" *\n");
        sc.append(" * @param values desired value\n");
        sc.append(" * @return this builder\n");
        sc.append(" */\n");
        sc.append("public ");
        sc.append(type().simpleName());
        sc.append(" set");
        sc.append(StringExtensions.toFirstUpper(field.getName()));
        sc.append("(final ");
        sc.append(importedReturnType(field));
        sc.append(" values) {\n");

        //        «IF restrictions !== null»
        //            if (values != null) {
        //               for («actualType.importedName» value : values.values()) {
        //                   «checkArgument(field, restrictions, actualType, "value")»
        //               }
        //            }
        //        «ENDIF»
        if (restrictions != null) {
            sc.append("if (values != null) {\n");
            sc.append("   for (");
            sc.append(importedName(actualType), "   ");
            sc.append(" value : values.values()) {\n");
            sc.append("       ");
            sc.append(checkArgument(field, restrictions, actualType, "value"), "       ");
            sc.newLineIfNotEmpty();
            sc.append("   }\n");
            sc.newLine();
            sc.append("}\n");
        }

        //            this.«field.fieldName» = values;
        //            return this;
        //        }
        sc.append("    this.");
        sc.append(fieldName(field));
        sc.append(" = values;\n");
        sc.append("    return this;\n");
        sc.append("}\n");
        return sc;
    }

    private CharSequence generateSimpleSetter(final BuilderGeneratedProperty field, final Type actualType) {
        final var sc = new StringConcatenation();
        final var restrictions = restrictionsForSetter(actualType);
        if (restrictions != null) {
            sc.newLine();
            sc.append(generateCheckers(field, restrictions, actualType));
            sc.newLineIfNotEmpty();
        }
        sc.newLine();
        sc.append("/**\n");
        sc.append(" * Set the property corresponding to {@link ");
        sc.append(importedName(targetType), " ");
        sc.append("#");
        sc.append(field.getGetterName());
        sc.append("()} to the specified\n");
        sc.append(" * value.\n");
        sc.append(" *\n");
        sc.append(" * @param value desired value\n");
        sc.append(" * @return this builder\n");
        sc.append(" */\n");
        sc.append("public ");
        sc.append(type().simpleName());
        sc.append(" set");
        sc.append(StringExtensions.toFirstUpper(field.getName()));
        sc.append("(final ");
        sc.append(importedReturnType(field));
        sc.append(" value) {\n");
        if (restrictions != null) {
            sc.append("    if (value != null) {\n");
            sc.append("        ");
            sc.append(checkArgument(field, restrictions, actualType, "value"), "        ");
            sc.newLineIfNotEmpty();
            sc.append("    }\n");
        }
        sc.append("    this.");
        sc.append(fieldName(field));
        sc.append(" = value;\n");
        sc.append("    return this;\n");
        sc.append("}\n");
        return sc;
    }

    /**
     * {@return string with the setter methods}
     */
    private CharSequence generateSetters() {
        final var sc = new StringConcatenation();
        if (keyType != null) {
            sc.append("/**\n");
            sc.append(" * Set the key value corresponding to {@link ");
            sc.append(importedName(targetType));
            sc.append("#");
            sc.append(Naming.KEY_AWARE_KEY_NAME, " ");
            sc.append("()} to the specified\n");
            sc.append(" * value.\n");
            sc.append(" *\n");
            sc.append(" * @param key desired value\n");
            sc.append(" * @return this builder\n");
            sc.append(" */\n");
            sc.append("public ");
            sc.append(type().simpleName());
            sc.append(" withKey(final ");
            sc.append(importedName(keyType));
            sc.append(" key) {\n");
            sc.append("    this.key = key;\n");
            sc.append("    return this;\n");
            sc.append("}\n");
        }
        for (var property : properties) {
            sc.append(generateSetter(property));
            sc.newLineIfNotEmpty();
        }
        sc.newLine();
        if (augmentType != null) {
            final var augmentTypeRef = importedName(augmentType);
            final var hashMapRef = importedName(JU_HASHMAP);
            sc.append("/**\n");
            sc.append(" * Add an augmentation to this builder\'s product.\n");
            sc.append(" *\n");
            sc.append(" * @param augmentation augmentation to be added\n");
            sc.append(" * @return this builder\n");
            sc.append(" * @throws ");
            sc.append(importedName(NPE));
            sc.append(" if {@code augmentation} is null\n");
            sc.append(" */\n");
            sc.append("public ");
            sc.append(type().simpleName());
            sc.append(" addAugmentation(");
            sc.append(augmentTypeRef);
            sc.append(" augmentation) {\n");
            sc.append("    if (!(this.");
            sc.append(Naming.AUGMENTATION_FIELD);
            sc.append(" instanceof ");
            sc.append(hashMapRef);
            sc.append(")) {\n");
            sc.append("        ");
            sc.append("this.");
            sc.append(Naming.AUGMENTATION_FIELD);
            sc.append(" = new ");
            sc.append(hashMapRef);
            sc.append("<>();\n");
            sc.append("    }\n");
            sc.newLine();
            sc.append("    this.");
            sc.append(Naming.AUGMENTATION_FIELD);
            sc.append(".put(augmentation.");
            sc.append(Naming.BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME);
            sc.append("(), augmentation);\n");
            sc.append("    return this;\n");
            sc.append("}\n");
            sc.newLine();
            sc.append("/**\n");
            sc.append(" * Remove an augmentation from this builder\'s product. If this builder does not track such an");
            sc.append(" augmentation\n");
            sc.append(" * type, this method does nothing.\n");
            sc.append(" *\n");
            sc.append(" * @param augmentationType augmentation type to be removed\n");
            sc.append(" * @return this builder\n");
            sc.append(" */\n");
            sc.append("public ");
            sc.append(type().simpleName());
            sc.append(" removeAugmentation(");
            sc.append(importedName(CLASS));
            sc.append("<? extends ");
            sc.append(augmentTypeRef);
            sc.append("> augmentationType) {\n");
            sc.append("    if (this.");
            sc.append(Naming.AUGMENTATION_FIELD, "    ");
            sc.append(" instanceof ");
            sc.append(hashMapRef, "    ");
            sc.append(") {\n");
            sc.append("        this.");
            sc.append(Naming.AUGMENTATION_FIELD);
            sc.append(".remove(augmentationType);\n");
            sc.append("    }\n");
            sc.append("    return this;\n");
            sc.append("}\n");
        }
        return sc;
    }

    // FIXME: remove this suppression
    @SuppressWarnings("checkstyle:lineLength")
    private String createDescription(final GeneratedType targetType) {
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
        final var  sc = new StringConcatenation();
        sc.append("Class that builds {@link ");
        sc.append(target);
        sc.append("} instances. Overall design of the class is that of a\n");
        sc.append("<a href=\"https://en.wikipedia.org/wiki/Fluent_interface\">fluent interface</a>, where method chaining is used.\n");
        sc.newLine();
        sc.append("<p>\n");
        sc.append("In general, this class is supposed to be used like this template:\n");
        sc.append("<pre>\n");
        sc.append("  <code>\n");
        sc.append("    ");
        sc.append(target);
        sc.append(" create");
        sc.append(target);
        sc.append("(int fooXyzzy, int barBaz) {\n");
        sc.append("        return new ");
        sc.append(target);
        sc.append("Builder()\n");
        sc.append("            .setFoo(new FooBuilder().setXyzzy(fooXyzzy).build())\n");
        sc.append("            .setBar(new BarBuilder().setBaz(barBaz).build())\n");
        sc.append("            .build();\n");
        sc.append("    }\n");
        sc.append("  </code>\n");
        sc.append("</pre>\n");
        sc.newLine();
        sc.append("<p>\n");
        sc.append("This pattern is supported by the immutable nature of ");
        sc.append(target);
        sc.append(", as instances can be freely passed around without");
        sc.newLineIfNotEmpty();
        sc.append("worrying about synchronization issues.");
        sc.newLine();
        sc.newLine();
        sc.append("<p>");
        sc.newLine();
        sc.append("As a side note: method chaining results in:");
        sc.newLine();
        sc.append("<ul>");
        sc.newLine();
        sc.append("  <li>very efficient Java bytecode, as the method invocation result, in this case the Builder reference, is\n");
        sc.append("      on the stack, so further method invocations just need to fill method arguments for the next method\n");
        sc.append("      invocation, which is terminated by {@link #build()}, which is then returned from the method</li>\n");
        sc.append("  <li>better understanding by humans, as the scope of mutable state (the builder) is kept to a minimum and is\n");
        sc.append("      very localized</li>\n");
        sc.append("  <li>better optimization opportunities, as the object scope is minimized in terms of invocation (rather than\n");
        sc.append("      method) stack, making <a href=\"https://en.wikipedia.org/wiki/Escape_analysis\">escape analysis</a> a lot\n");
        sc.append("      easier. Given enough compiler (JIT/AOT) prowess, the cost of th builder object can be completely\n");
        sc.append("      eliminated</li>\n");
        sc.append("</ul>\n");
        sc.newLine();
        sc.append("@see ");
        sc.append(target);
        sc.newLineIfNotEmpty();
        return sc.toString();
    }

    @Override
    String formatDataForJavaDoc(final GeneratedType type) {
        final var typeDescription = createDescription(type);
        if (StringExtensions.isNullOrEmpty(typeDescription)) {
            return "";
        }

        final var sc = new StringConcatenation();
        sc.append(typeDescription);
        sc.newLineIfNotEmpty();
        return sc.toString();
    }

    private CharSequence generateAugmentation() {
        //        /**
        //         * Return the specified augmentation, if it is present in this builder.
        //         *
        //         * @param <E$$> augmentation type
        //         * @param augmentationType augmentation type class
        //         * @return Augmentation object from this builder, or {@code null} if not present
        //         * @throws «NPE.importedName» if {@code augmentType} is {@code null}
        //         */
        final var sc = new StringConcatenation();
        sc.append("/**\n");
        sc.append(" * Return the specified augmentation, if it is present in this builder.\n");
        sc.append(" *\n");
        sc.append(" * @param <E$$> augmentation type\n");
        sc.append(" * @param augmentationType augmentation type class\n");
        sc.append(" * @return Augmentation object from this builder, or {@code null} if not present\n");
        sc.append(" * @throws ");
        sc.append(importedName(NPE));
        sc.append(" if {@code augmentType} is {@code null}\n");
        sc.append(" */\n");

        //        @«SUPPRESS_WARNINGS.importedName»({ "unchecked", "checkstyle:methodTypeParameterName"})
        //        public <E$$ extends «augmentType.importedName»> E$$ «AUGMENTABLE_AUGMENTATION_NAME»(
        //«CLASS.importedName»<E$$> augmentationType) {
        //            return (E$$) «AUGMENTATION_FIELD».get(«JU_OBJECTS.importedName».requireNonNull(augmentationType));
        //        }
        sc.append("@");
        sc.append(importedName(SUPPRESS_WARNINGS));
        sc.append("({ \"unchecked\", \"checkstyle:methodTypeParameterName\"})\n");
        sc.append("public <E$$ extends ");
        sc.append(importedName(augmentType));
        sc.append("> E$$ ");
        sc.append(Naming.AUGMENTABLE_AUGMENTATION_NAME);
        sc.append("(");
        sc.append(importedName(CLASS));
        sc.append("<E$$> augmentationType) {\n");
        sc.append("    return (E$$) ");
        sc.append(Naming.AUGMENTATION_FIELD);
        sc.append(".get(");
        sc.append(importedName(JU_OBJECTS));
        sc.append(".requireNonNull(augmentationType));\n");
        sc.append("}\n");
        return sc;
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
