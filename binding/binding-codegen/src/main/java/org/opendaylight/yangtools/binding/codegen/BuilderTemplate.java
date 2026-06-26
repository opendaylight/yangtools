/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.
 * Copyright (c) 2026 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.contract.Naming.BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.KEY_AWARE_KEY_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.isGetterMethodName;
import static org.opendaylight.yangtools.binding.contract.Naming.toFirstUpper;
import static org.opendaylight.yangtools.binding.model.ri.BindingTypes.GROUPING;
import static org.opendaylight.yangtools.binding.model.ri.BindingTypes.entryObject;
import static org.opendaylight.yangtools.binding.model.ri.TypeConstants.PATTERN_CONSTANT_NAME;
import static org.opendaylight.yangtools.binding.model.ri.Types.isListType;
import static org.opendaylight.yangtools.binding.model.ri.Types.isMapType;
import static org.opendaylight.yangtools.binding.model.ri.Types.isSetType;
import static org.opendaylight.yangtools.binding.model.ri.Types.objectType;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.KeyArchetype;
import org.opendaylight.yangtools.binding.model.api.LegacyArchetype;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

/**
 * Template for generating JAVA builder classes.
 */
final class BuilderTemplate extends BaseTemplate {
    @NonNullByDefault
    record Builder(LegacyArchetype type) implements Template.Builder {
        Builder {
            requireNonNull(type);
        }

        @Override
        public BuilderTemplate build() {
            final var targetName = type.name();
            final var simpleName = targetName.simpleName();
            final var builderName = targetName.createSibling(simpleName + Naming.BUILDER_SUFFIX);
            final var implName = simpleName + "Impl";
            final var javaType = GeneratedClass.of(builderName, implName, type);
            final var analysis = TypeAnalysis.of(type);

            // FIXME: there are three cases here:
            //        - non-augmentable
            //        - augmentable
            //        - entry object (implies augmentable)
            //        we should have three separate classes instead of @Nullable fields for the latter two cases
            return new BuilderTemplate(javaType, javaType.getNestedClass(implName), type, analysis.properties(),
                analysis.augmentType(), BindingTypes.extractEntryObjectKey(type));
        }
    }

    /**
     * The name of the field holding augmentations.
     */
    static final @NonNull String AUGMENTATION_FIELD = "augmentation";

    /**
     * Generated property is set if among methods is found one with the name GET_AUGMENTATION_METHOD_NAME.
     */
    final ParameterizedType augmentType;

    /**
     * Set of class attributes (fields) which are derived from the getter methods names.
     */
    final @NonNull Set<BuilderGeneratedProperty> properties;

    /**
     * KeyArchetype for key type, {@code null} if this type does not have a key.
     */
    final KeyArchetype keyType;

    // FIXME: better description: 'targetType' in the context of BuilderImplTemplate is type returned
    //        from BindingContract.implementedInterface() -- and is expected to extend JavaContract and provide default
    //        implementations of its methods
    final @NonNull LegacyArchetype targetType;

    private final GeneratedClass.@NonNull Nested implJavaType;

    @NonNullByDefault
    private BuilderTemplate(final GeneratedClass.TopLevel javaType, final GeneratedClass.Nested implJavaType,
            final LegacyArchetype targetType, final Set<BuilderGeneratedProperty> properties,
            final @Nullable ParameterizedType augmentType, final @Nullable KeyArchetype keyType) {
        super(javaType);
        this.implJavaType = requireNonNull(implJavaType);
        this.targetType = requireNonNull(targetType);
        this.properties = requireNonNull(properties);
        this.augmentType = augmentType;
        this.keyType = keyType;
    }

    private @NonNull String simpleName() {
        return typeName().simpleName();
    }

    @Override
    BlockBuilder body() {
        final var simpleName = simpleName();

        final var bb = newBlockBuilder()
            .blk(wrapToDocumentation(createDescription().toRawString()))
            .blk(generateDeprecatedAnnotation(targetType.getAnnotations()))
            .eol(generatedAnnotation())
            .str("public class ").str(simpleName).oB()
            // FIXME: remove this newline
            .nl()
            .blk(builderFields())
            .nl()
            .blk(constantsDeclarations())
            .nl();
        if (augmentType != null) {
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
            .str("public ").str(simpleName).str("()").oB()
                .eol("// No-op")
            .cB()
            .blk(generateConstructorsFromIfcs())
            .nl()
            .eol("/**")
            .str(" * Construct a builder initialized with state from specified {@link ").str(targetTypeName).eol("}.")
            .eol(" *")
            .str(" * @param base ").str(targetTypeName).eol(" from which the builder should be initialized")
            .eol(" */")
            .indented("public ", generateCopyConstructor(targetType))
            .nl()
            .blk(generateMethodFieldsFrom())
            .nl()
            .blk(generateEmptyInstance())
            .nl()
            .blk(generateGetters(false));
        if (augmentType != null) {
            bb.nl().blk(generateAugmentation());
        }

        return bb
            .nl()
            .blk(generateSetters())
            .nl()
            .eol("/**")
            .str(" * {@return A new {@link ").str(targetTypeName).eol("} instance}")
            .eol(" */")
            .str("public ").str(importedNonNull(targetType)).str(" build()").oB()
                .str("return new ").str(importedName(implJavaType.name())).eol("(this);")
            .cB()
            .nl()
            .blk(new BuilderImplTemplate(implJavaType, this).body())
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
        final var bb = newBlockBuilder();
        for (var prop : properties) {
            bb.str("private ").str(importedReturnType(prop)).sp().str(fieldName(prop)).eS();
        }
        return bb;
    }

    private @Nullable BlockBuilder generateDeprecatedAnnotation(final @NonNull List<AnnotationType> annotations) {
        for (var annotation : annotations) {
            if (JavaFileTemplate.DEPRECATED.equals(annotation.name())) {
                final var bb = newBlockBuilder().at();
                final var forRemoval = annotation.getParameter("forRemoval");
                return forRemoval != null
                    ? bb.str(importedName(DEPRECATED)).str("(forRemoval = ").str(forRemoval.getValue()).eol(")")
                    : bb.str(importedName(SUPPRESS_WARNINGS)).eol("(\"deprecation\")");
            }
        }
        return null;
    }

    private @Nullable BlockBuilder generateConstructorsFromIfcs() {
        final var bb = newBlockBuilder().nl();
        boolean first = true;
        for (var impl : targetType.getImplements()) {
            if (impl instanceof LegacyArchetype genType) {
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
    private @NonNull BlockBuilder generateConstructorFromIfc(final @NonNull LegacyArchetype genType) {
        final var bb = newBlockBuilder();
        if (hasNonDefaultMethods(genType)) {
            final var typeName = importedName(genType);
            bb
                .eol("/**")
                .str(" * Construct a new builder initialized from specified {@link ").str(typeName).eol("}.")
                .eol(" *")
                .str(" * @param arg ").str(typeName).eol(" from which the builder should be initialized")
                .eol(" */")
                .str("public ").str(simpleName()).str("(").str(typeName).str(" arg)").oB()
                    .blk(printConstructorPropertySetter(genType))
                .cB()
                .newLine();
        }
        for (var implTypeImplement : genType.getImplements()) {
            if (implTypeImplement instanceof LegacyArchetype implType) {
                bb.blk(generateConstructorFromIfc(implType));
            }
        }
        return bb;
    }

    private @Nullable BlockBuilder printConstructorPropertySetter(final Type implementedIfc) {
        if (!(implementedIfc instanceof LegacyArchetype ifc)) {
            return null;
        }

        final var bb = newBlockBuilder();
        for (var getter : nonDefaultMethods(ifc)) {
            if (isGetterMethodName(getter.getName())) {
                bb.eol(printPropertySetter(getter, "arg", propertyNameFromGetter(getter)));
            }
        }

        for (var impl : ifc.getImplements()) {
            bb.blk(printConstructorPropertySetter(impl, getSpecifiedGetters(ifc)));
        }
        return bb;
    }

    private @Nullable BlockBuilder printConstructorPropertySetter(final Type implementedIfc,
            final Set<MethodSignature> alreadySetProperties) {
        if (!(implementedIfc instanceof LegacyArchetype ifc)) {
            return null;
        }

        final var bb = newBlockBuilder();
        for (var getter : nonDefaultMethods(ifc)) {
            if (isGetterMethodName(getter.getName()) && getterByName(alreadySetProperties, getter.getName()) == null) {
                bb.eol(printPropertySetter(getter, "arg", propertyNameFromGetter(getter)));
            }
        }

        for (var descendant : ifc.getImplements()) {
            bb.blk(printConstructorPropertySetter(descendant,
                Sets.union(alreadySetProperties, getSpecifiedGetters(ifc))));
        }
        return bb;
    }

    private static Set<MethodSignature> getSpecifiedGetters(final LegacyArchetype type) {
        return type.getMethodDefinitions().stream()
            .filter(JavaFileTemplate::hasOverrideAnnotation)
            .collect(ImmutableSet.toImmutableSet());
    }

    /**
     * Generate 'fieldsFrom' method to set builder properties based on type of given argument.
     */
    private @Nullable BlockBuilder generateMethodFieldsFrom() {
        if (!hasImplementsFromUses(targetType)) {
            return null;
        }

        // FIXME: this is not used anywhere: I think this is meant to suppress duplicate checks?
        final var done = getBaseIfcs(targetType);

        return newBlockBuilder()
            .blk(generateMethodFieldsFromComment(targetType))
            .str("public void fieldsFrom(final ").str(importedName(GROUPING)).str(" arg)").jBlock(bb -> {
                bb.eol("boolean isValidArg = false;");
                for (var impl : getAllIfcs(targetType)) {
                    bb.blk(generateIfCheck(impl, done));
                }
                bb.str(importedName(CODEHELPERS)).str(".validValue(isValidArg, arg, ")
                    .jStr(getAllIfcs(targetType).stream().map(this::importedName).toList().toString()).eol(");");
            }).nl();
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

        return newBlockBuilder()
            .str("private static final class LazyEmpty").oB()
                .str("static final ").str(nonnullTarget).str(" INSTANCE = new ").str(simpleName())
                    .eol("().build();")
                 .nl()
                 .str("private LazyEmpty()").oB()
                     .eol("// Hidden on purpose")
                 .cB()
            .cB()
            .nl()
            .eol("/**")
            .str(" * {@return an empty {@link ").str(targetName).eol("}}")
            .eol(" */")
            .str("public static ").str(nonnullTarget).str(" empty()").oB()
                .eol("return LazyEmpty.INSTANCE;")
            .cB();
    }

    @NonNullByDefault
    private BlockBuilder generateCopyConstructor(final Type fromType) {
        return newBlockBuilder()
            .str(simpleName()).str("(final ").str(importedName(fromType)).str(" base)").jBlock(bb -> {
                if (augmentType != null) {
                    bb
                        .eol("final var aug = base.augmentations();")
                        .str("if (!aug.isEmpty())").oB()
                            .str("this." + AUGMENTATION_FIELD + " = new ").str(importedName(JU_HASHMAP)).eol("<>(aug);")
                        .cB();
                }

                if (keyType != null && targetType.getImplements().contains(entryObject(targetType, keyType))) {
                    final var allProps = new ArrayList<>(properties);
                    final var keyProps = keyConstructorArgs(keyType);
                    for (var field : keyProps) {
                        removeProperty(allProps, field.getName());
                    }

                    bb.eol("this.key = base." + KEY_AWARE_KEY_NAME + "();");
                    for (var field : keyProps) {
                        bb.str("this.").str(fieldName(field)).str(" = base.").str(getterMethodName(field)).eol("();");
                    }

                    appendCopyNonKeys(bb, allProps);
                } else {
                    appendCopyNonKeys(bb, properties);
                }
            }).nl();
    }

    @NonNullByDefault
    private BlockBuilder generateMethodFieldsFromComment(final LegacyArchetype type) {
        // FIXME: create a specialized JavadocBuilder to help with this
        final var bb = newBlockBuilder().txt("""
                    /**
                     * Set fields from given grouping argument. Valid argument is instance of one of following types:
                     * <ul>
                    """);
        for (var impl : getAllIfcs(type)) {
            bb.str(" *   <li>{@link ").str(importedName(impl)).eol("}</li>");
        }
        return bb
            .txt("""
                 * </ul>
                 *
                 * @param arg grouping object
                """)
            .str(" * @throws ").str(importedName(IAE))
                .eol(" if given argument is none of valid types or has property with incompatible value")
            .eol(" */");
    }

    /**
     * Method is used to find out if given type implements any interface from uses.
     */
    private boolean hasImplementsFromUses(final LegacyArchetype type) {
        return getAllIfcs(type).stream()
            .anyMatch(impl -> impl instanceof LegacyArchetype genType && hasNonDefaultMethods(genType));
    }

    private @Nullable BlockBuilder generateIfCheck(final Type impl, final List<Type> done) {
        return !(impl instanceof LegacyArchetype implType) || !hasNonDefaultMethods(implType) ? null : newBlockBuilder()
            .str("if (arg instanceof ").str(importedName(implType)).str(" castArg)").oB()
                .blk(printPropertySetter(implType))
                .eol("isValidArg = true;")
            .cB();
    }

    private @Nullable BlockBuilder printPropertySetter(final Type implementedIfc) {
        if (!(implementedIfc instanceof LegacyArchetype ifc)) {
            return null;
        }

        final var bb = newBlockBuilder();
        for (var getter : nonDefaultMethods(ifc)) {
            if (isGetterMethodName(getter.getName()) && !hasOverrideAnnotation(getter)) {
                bb.eol(printPropertySetter(getter, "castArg", propertyNameFromGetter(getter)));
            }
        }
        return bb;
    }

    // FIXME: return BlockBuilder
    @NonNullByDefault
    private String printPropertySetter(final MethodSignature getter, final String receiver, final String propertyName) {
        final var getterName =  getter.getName();

        final var ownGetter = findGetter(getterName);
        final var ownGetterType = ownGetter.getReturnType();
        if (strictTypeEquals(getter.getReturnType(), ownGetterType)) {
            return "this._" + propertyName + " = " + receiver + '.' + getterName + "();";
        }
        if (ownGetterType instanceof ParameterizedType parameterized) {
            final var itemType = parameterized.getActualTypeArguments().getFirst();
            if (isListType(parameterized)) {
                return printPropertySetter(getterName, receiver, propertyName, "checkListFieldCast",
                    importedName(itemType));
            }
            if (isSetType(parameterized)) {
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
            + className + ".class, \"" + propertyName + "\", " + receiver + '.' + getterName + "());";
    }

    @NonNullByDefault
    private MethodSignature findGetter(final String getterName) {
        final var getter = getterByName(targetType, getterName);
        if (getter == null) {
            throw new IllegalStateException(
                "%s should be present in %s type or in one of its ancestors as getter".formatted(
                    propertyNameFromGetter(getterName), targetType));
        }
        return getter;
    }

    private static @Nullable MethodSignature getterByName(final LegacyArchetype implType, final String getterName) {
        final var getter = getterByName(nonDefaultMethods(implType), getterName);
        if (getter != null) {
            return getter;
        }
        for (var ifc : implType.getImplements()) {
            if (ifc instanceof LegacyArchetype genInterface) {
                final var getterImpl = getterByName(genInterface, getterName);
                if (getterImpl != null) {
                    return getterImpl;
                }
            }
        }

        return null;
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

    private static List<Type> getBaseIfcs(final LegacyArchetype type) {
        final var baseIfcs = new ArrayList<Type>();
        for (var ifc : type.getImplements()) {
            if (ifc instanceof LegacyArchetype genType && hasNonDefaultMethods(genType)) {
                baseIfcs.add(genType);
            }
        }
        return baseIfcs;
    }

    private Set<Type> getAllIfcs(final Type type) {
        if (!(type instanceof LegacyArchetype ifc)) {
            return Set.of();
        }

        final var baseIfcs = new HashSet<Type>();
        for (var impl : ifc.getImplements()) {
            if (impl instanceof LegacyArchetype genType && hasNonDefaultMethods(genType)) {
                baseIfcs.add(genType);
            }
            baseIfcs.addAll(getAllIfcs(impl));
        }
        return baseIfcs;
    }

    @NonNullByDefault
    private BlockBuilder constantsDeclarations() {
        final var bb = newBlockBuilder();
        for (var def : targetType.getConstantDefinitions()) {
            if (!def.getName().startsWith(PATTERN_CONSTANT_NAME)) {
                // other constants are emitted separately
                continue;
            }

            // FIXME: these are not populated anywhere and this whole method does not work :(
            final var xsdToPattern = (Map<String, String>) def.getValue();
            final var fieldSuffix = def.getName().substring(PATTERN_CONSTANT_NAME.length());
            final var jurPatternRef = importedName(JUR_PATTERN);
            if (xsdToPattern.size() == 1) {
                final var firstEntry = xsdToPattern.entrySet().iterator().next();
                bb
                    .str("private static final ").str(jurPatternRef).str(" " + MEMBER_PATTERN_LIST).str(fieldSuffix)
                        .str(" = ").str(jurPatternRef).str(".compile(").jString(firstEntry.getKey()).eol(");")
                    .str("private static final String " + MEMBER_REGEX_LIST).str(fieldSuffix).str(" = ")
                        .jString(firstEntry.getValue()).eS();
                continue;
            }

            bb
                .str("private static final ").str(jurPatternRef).str("[] " + MEMBER_PATTERN_LIST).str(fieldSuffix)
                    .str(" = ").str(importedName(CODEHELPERS)).str(".compilePatterns(").str(importedName(JU_LIST))
                    .eol(".of(");
            {
                boolean first = true;
                for (var xsd : xsdToPattern.keySet()) {
                    if (first) {
                        first = false;
                    } else {
                        bb.str(", ");
                    }
                    bb.jString(xsd);
                }
            }
            bb
                .eol("));")
                .str("private static final String[] " + MEMBER_REGEX_LIST).str(fieldSuffix).str(" = { ");
            {
                boolean first = true;
                for (var pattern : xsdToPattern.values()) {
                    if (first) {
                        first = false;
                    } else {
                        bb.str(", ");
                    }
                    bb.jString(pattern);
                }
            }
            bb.eol(" };");
        }
        return bb;
    }

    /**
     * {@return string with getter methods}
     */
    private @NonNull BlockBuilder generateGetters(final boolean addOverride) {
        final var bb = newBlockBuilder();

        if (keyType != null) {
            if (!addOverride) {
                bb
                    .eol("/**")
                    .str(" * Return current value associated with the property corresponding to {@link ")
                        .str(importedName(targetType)).eol('#' + KEY_AWARE_KEY_NAME + "()}.")
                    .eol(" *")
                    .eol(" * @return current value")
                    .eol(" */");
            } else {
                bb
                    .at().eol(importedName(OVERRIDE));
            }
            bb
                .str("public ").str(importedName(keyType)).str(' ' + KEY_AWARE_KEY_NAME + "()").oB()
                    .eol("return key;")
                .cB()
                .newLine();
        }

        if (properties.isEmpty()) {
            return bb;
        }

        final var it = properties.iterator();
        while (true) {
            final var field = it.next();
            if (!addOverride) {
                bb
                    .eol("/**")
                    .str(" * Return current value associated with the property corresponding to {@link ")
                        .str(importedName(targetType)).str("#").str(field.getGetterName()).eol("()}.")
                    .eol(" *")
                    .eol(" * @return current value")
                    .eol(" */");
            } else {
                bb
                    .at().eol(importedName(OVERRIDE));
            }
            bb.blk(asGetterMethod(field));

            if (!it.hasNext()) {
                return bb;
            }

            bb.newLine();
        }
    }

    private @NonNull BlockBuilder generateSetter(final BuilderGeneratedProperty field) {
        final var returnType = field.getReturnType();
        if (returnType instanceof ParameterizedType parameterized) {
            if (isListType(parameterized) || isSetType(parameterized)) {
                final var arguments = parameterized.getActualTypeArguments();
                return arguments.isEmpty() ? generateListSetter(field, objectType())
                    : generateListSetter(field, arguments.getFirst());
            }
            if (isMapType(parameterized)) {
                return generateMapSetter(field, parameterized.getActualTypeArguments().get(1));
            }
        }
        return generateSimpleSetter(field, returnType);
    }

    private @NonNull BlockBuilder generateListSetter(final BuilderGeneratedProperty field, final Type actualType) {
        final var bb = newBlockBuilder();
        final BlockBuilder argumentCheck;
        final var restrictions = restrictionsForSetter(actualType);
        if (restrictions != null) {
            bb.blk(generateCheckers(field, restrictions, actualType));
            argumentCheck = newBlockBuilder()
                .str("if (values != null)").oB()
                    .str("for (").str(importedName(actualType)).str(" value : values)").oB()
                        .blk(checkFieldValue(targetType, field, restrictions, actualType, "value"))
                    .cB()
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
            .str("public ").str(simpleName()).str(" set").str(toFirstUpper(field.getName())).str("(final ")
                .str(importedReturnType(field)).str(" values)").oB()
                .blk(argumentCheck)
                .str("this.").str(fieldName(field)).eol(" = values;")
                .eol("return this;")
            .cB()
            .nl();
    }

    private @NonNull BlockBuilder generateMapSetter(final BuilderGeneratedProperty field, final Type actualType) {
        final var bb = newBlockBuilder();
        final var restrictions = JavaFileTemplate.restrictionsForSetter(actualType);
        if (restrictions != null) {
            bb.blk(generateCheckers(field, restrictions, actualType));
        }

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
            .str("public ").str(simpleName()).str(" set").str(toFirstUpper(field.getName())).str("(final ")
                .str(importedReturnType(field)).str(" values)").oB();

        if (restrictions != null) {
            bb
                .eol("if (values != null)").oB()
                    .str("for (").str(importedName(actualType)).str(" value : values.values())").oB()
                        .blk(checkFieldValue(targetType, field, restrictions, actualType, "value"))
                    .cB()
                .cB();
        }

        return bb
            .str("this.").str(fieldName(field)).eol(" = values;")
            .eol("return this;")
            .cB();
    }

    private @NonNull BlockBuilder generateSimpleSetter(final BuilderGeneratedProperty field, final Type actualType) {
        final var bb = newBlockBuilder();
        final var restrictions = restrictionsForSetter(actualType);
        if (restrictions != null) {
            bb.nl().blk(generateCheckers(field, restrictions, actualType));
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
            .str("public ").str(simpleName()).str(" set").str(toFirstUpper(field.getName())).str("(final ")
                .str(importedReturnType(field)).str(" value)").oB();
        if (restrictions != null) {
            bb
                .str("if (value != null)").oB()
                    .blk(checkFieldValue(targetType, field, restrictions, actualType, "value"))
                .cB();
        }
        return bb
            .str("this.").str(fieldName(field)).eol(" = value;")
            .eol("return this;")
            .cB();
    }

    /**
     * {@return string with the setter methods}
     */
    private @NonNull BlockBuilder generateSetters() {
        final var bb = newBlockBuilder();
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
                .str("public ").str(simpleName()).str(" withKey(final ").str(importedName(keyType))
                    .str(" key)").oB()
                    .eol("this.key = key;")
                    .eol("return this;")
                .cB();
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
                .str("public ").str(simpleName()).str(" addAugmentation(").str(augmentTypeRef)
                    .str(" augmentation)").oB()
                    .str("if (!(this." + AUGMENTATION_FIELD + " instanceof ").str(hashMapRef).str("))").oB()
                        .str("this." + AUGMENTATION_FIELD + " = new ").str(hashMapRef).eol("<>();")
                    .cB()
                    .eol("this." + AUGMENTATION_FIELD + ".put(augmentation."
                        + BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME + "(), augmentation);")
                    .eol("return this;")
                .cB()
                .nl()
                .txt("""
                      /**
                       * Remove an augmentation from this builder's product. If this builder does not track such an \
                      augmentation
                       * type, this method does nothing.
                       *
                       * @param augmentationType augmentation type to be removed
                       * @return this builder
                       */
                      """)
                .str("public ").str(simpleName()).str(" removeAugmentation(").str(importedName(CLASS))
                    .str("<? extends ").str(augmentTypeRef).str("> augmentationType)").oB()
                    .str("if (this." + AUGMENTATION_FIELD  + " instanceof ").str(hashMapRef).str(")").oB()
                        .eol("this." + AUGMENTATION_FIELD + ".remove(augmentationType);")
                    .cB()
                    .eol("return this;")
                .cB();
        }
        return bb;
    }

    private @NonNull BlockBuilder createDescription() {
        final var target = importedName(targetType);

        return newBlockBuilder()
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

    @NonNullByDefault
    private BlockBuilder generateAugmentation() {
        return newBlockBuilder()
            .txt("""
                  /**
                   * Return the specified augmentation, if it is present in this builder.
                   *
                   * @param <E$$> augmentation type
                   * @param augmentationType augmentation type class
                   * @return Augmentation object from this builder, or {@code null} if not present
                  """)
            .str(" * @throws ").str(importedName(NPE)).eol(" if {@code augmentType} is {@code null}")
            .eol(" */")
            .at().str(importedName(SUPPRESS_WARNINGS)).eol("({ \"unchecked\", \"checkstyle:methodTypeParameterName\"})")
            .str("public <E$$ extends ").str(importedName(augmentType)).str("> E$$ augmentation(")
                .gen(importedName(CLASS), "E$$").str(" augmentationType)").oB()
                .str("return (E$$) " + AUGMENTATION_FIELD + ".get(").str(importedName(JU_OBJECTS))
                    .eol(".requireNonNull(augmentationType));")
            .cB();
    }

    /**
     * Append the code to copy non-key-components, with four spaces of indentation.
     */
    private static void appendCopyNonKeys(final BlockBuilder bb, final Collection<BuilderGeneratedProperty> props) {
        for (var field : props) {
            bb.str("this.").str(fieldName(field)).str(" = base.").str(field.getGetterName()).eol("();");
        }
    }

    /**
     * Return properties participating in the construction of a key type. Returned list is guaranteed to be ordered to
     * match order the type constructor expects.
     *
     * @param keyType key type
     * @return properties participating in the construction of a key type, in constructor order
     */
    @NonNullByDefault
    static List<GeneratedProperty> keyConstructorArgs(final KeyArchetype keyType) {
        return keyType.getProperties().stream()
            .sorted(Comparator.comparing(GeneratedProperty::getName))
            .collect(Collectors.toList());
    }

    static void removeProperty(final Collection<BuilderGeneratedProperty> props, final String name) {
        final var it = props.iterator();
        while (it.hasNext()) {
            if (name.equals(it.next().getName())) {
                it.remove();
                return;
            }
        }
    }

    @NonNullByDefault
    static boolean hasNonDefaultMethods(final LegacyArchetype type) {
        return type.getMethodDefinitions().stream().anyMatch(def -> !def.isDefault());
    }

    @NonNullByDefault
    static Collection<MethodSignature> nonDefaultMethods(final LegacyArchetype type) {
        return Collections2.filter(type.getMethodDefinitions(), def -> !def.isDefault());
    }

    /**
     * Check if the {@code type} represents non-presence container.
     *
     * @param type {@link GeneratedType} to be checked if represents container without presence statement.
     * @return {@code true} if specified {@code type} is a container without presence statement,
     *     {@code false} otherwise.
     */
    // FIXME: YANGTOOLS-1876: remove this method
    @NonNullByDefault
    static boolean isNonPresenceContainer(final LegacyArchetype type) {
        final var sourceDef = type.yangSourceDefinition();
        return sourceDef != null && sourceDef.getNode() instanceof ContainerSchemaNode container
            && !container.isPresenceContainer();
    }
}
