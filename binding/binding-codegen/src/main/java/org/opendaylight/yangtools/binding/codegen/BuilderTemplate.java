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

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.codegen.AugmentationTemplate.augmentationOfIn;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.CLASS;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.CODEHELPERS;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.IAE;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.JU_HASHMAP;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.JU_MAP;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.JU_OBJECTS;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.NPE;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.OVERRIDE;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.SUPPRESS_WARNINGS;
import static org.opendaylight.yangtools.binding.contract.Naming.BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.KEY_AWARE_KEY_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.isGetterMethodName;
import static org.opendaylight.yangtools.binding.contract.Naming.toFirstUpper;
import static org.opendaylight.yangtools.binding.model.ri.Types.OBJECT;
import static org.opendaylight.yangtools.binding.model.ri.Types.isListType;
import static org.opendaylight.yangtools.binding.model.ri.Types.isMapType;
import static org.opendaylight.yangtools.binding.model.ri.Types.isSetType;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.Grouping;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.api.AugmentableArchetype;
import org.opendaylight.yangtools.binding.model.api.ContainerObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.DataContainerArchetype;
import org.opendaylight.yangtools.binding.model.api.DeprecatedAnnotation;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.KeyArchetype;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;

/**
 * Template for generating JAVA builder classes.
 */
final class BuilderTemplate extends BaseTemplate {

    @NonNullByDefault
    sealed interface Props {

        List<GetterShape> allGetters();
    }

    @NonNullByDefault
    record WithKey(
            List<GetterShape> allGetters,
            List<GetterShape> keyGetters,
            List<GetterShape> implGetters,
            KeyArchetype key) implements Props {
        WithKey {
            requireNonNull(allGetters);
            requireNonNull(implGetters);
            requireNonNull(keyGetters);
        }
    }

    @NonNullByDefault
    record WithoutKey(List<GetterShape> allGetters) implements Props {
        WithoutKey {
            requireNonNull(allGetters);
        }
    }

    /**
     * The name of the field holding augmentations.
     */
    static final @NonNull String AUGMENTATION_FIELD = "augmentation";

    private static final @NonNull JavaTypeName GROUPING = JavaTypeName.create(Grouping.class);

    // FIXME: better description: 'targetType' in the context of BuilderImplTemplate is type returned
    //        from BindingContract.implementedInterface() -- and is expected to extend JavaContract and provide default
    //        implementations of its methods
    private final GeneratedClass.@NonNull Nested implJavaType;

    final @NonNull DataContainerArchetype targetType;
    final @NonNull Props props;

    @NonNullByDefault
    private BuilderTemplate(final GeneratedClass.TopLevel javaType, final GeneratedClass.Nested implJavaType,
            final InterfaceTemplate<?> targetTemplate) {
        super(javaType);
        this.implJavaType = requireNonNull(implJavaType);
        targetType = targetTemplate.archetype;

        final var allMethods = targetTemplate.getters.allMethods().sorted().toList();
        if (targetTemplate instanceof EntryObjectTemplate entryTarget) {
            final var key = entryTarget.key;
            final var keyMethods = key.methods().values().stream()
                .collect(Collectors.toMap(MethodSignature::name, method -> new GetterShape(method, false)));
            props = new WithKey(allMethods, keyMethods.values().stream().sorted().toList(),
                allMethods.stream().filter(getter -> !keyMethods.containsKey(getter.name())).toList(), key);
        } else {
            props = new WithoutKey(allMethods);
        }
    }

    // FIXME: there are three cases here:
    //        - non-augmentable
    //        - augmentable
    //        - entry object (implies augmentable)
    //        we should have three separate classes instead of @Nullable fields for the latter two cases and a separate
    //        static method
    @NonNullByDefault
    static BuilderTemplate of(final InterfaceTemplate<?> targetTemplate) {
        final var type = targetTemplate.archetype;
        final var targetName = type.name();
        final var simpleName = targetName.simpleName();
        final var builderName = targetName.createSibling(simpleName + Naming.BUILDER_SUFFIX);
        final var implName = simpleName + "Impl";
        final var javaType = GeneratedClass.of(builderName, implName, type);

        return new BuilderTemplate(javaType, javaType.getNestedClass(implName), targetTemplate);
    }

    private @NonNull String simpleName() {
        return typeName().simpleName();
    }

    @Override
    BlockBuilder body() {
        final var simpleName = simpleName();

        final var bb = newBlockBuilder()
            .blk(wrapToDocumentation(createDescription().toRawString()))
            .frg(generateDeprecatedAnnotation())
            .eol(generatedAnnotation())
            .str("public class ").str(simpleName).oB()
            // FIXME: remove this newline
            .nl()
            .frg(builderFields())
//            .nl()
//            .blk(constantsDeclarations())
            .nl();

        final var isAugmentable = targetType instanceof AugmentableArchetype;
        if (isAugmentable) {
            final var augmentTypeRef = augmentationOfIn(targetType, javaType());
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
        if (isAugmentable) {
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

    private @NonNull BlockFragment builderFields() {
        return bb -> {
            for (var getter : props.allGetters()) {
                bb.str("private ").str(importedName(getter.type())).sp().str(getter.fieldName()).eS();
            }
            if (props instanceof WithKey with) {
                bb.str("private ").str(importedName(with.key)).eol(" key;");
            }
        };
    }

    @Nullable DeprecatedAnnotation deprecatedAnnotation() {
        return targetType.statement() instanceof DocumentedNode.WithStatus withStatus
            ? DeprecatedAnnotation.ofStatus(withStatus.getStatus()) : null;
    }

    private @Nullable BlockFragment generateDeprecatedAnnotation() {
        if (targetType.statement() instanceof DocumentedNode.WithStatus withStatus) {
            return switch (withStatus.getStatus()) {
                case CURRENT -> null;
                case DEPRECATED -> bb -> bb.at().str(importedName(SUPPRESS_WARNINGS)).eol("(\"deprecation\")");
                case OBSOLETE -> new org.opendaylight.yangtools.binding.codegen.DeprecatedAnnotation(javaType(), true);
            };
        }
        return null;
    }

    private @Nullable BlockBuilder generateConstructorsFromIfcs() {
        final var bb = newBlockBuilder().nl();
        boolean first = true;
        for (var partial : targetType.partials()) {
            if (first) {
                first = false;
            } else {
                bb.newLine();
            }
            bb.blk(generateConstructorFromIfc(partial));
        }
        return bb;
    }

    /**
     * Generate constructor with argument of given type.
     */
    private @NonNull BlockBuilder generateConstructorFromIfc(final @NonNull DataContainerArchetype archetype) {
        final var bb = newBlockBuilder();
        if (!archetype.getMethodDefinitions().isEmpty()) {
            final var typeName = importedName(archetype);
            bb
                .eol("/**")
                .str(" * Construct a new builder initialized from specified {@link ").str(typeName).eol("}.")
                .eol(" *")
                .str(" * @param arg ").str(typeName).eol(" from which the builder should be initialized")
                .eol(" */")
                .str("public ").str(simpleName()).str("(").str(typeName).str(" arg)").oB()
                    .blk(printConstructorPropertySetter(archetype))
                .cB()
                .newLine();
        }
        for (var partial : archetype.partials()) {
            bb.blk(generateConstructorFromIfc(partial));
        }
        return bb;
    }

    private @Nullable BlockBuilder printConstructorPropertySetter(final Type implementedIfc) {
        // FIXME: narrow down?
        if (!(implementedIfc instanceof DataContainerArchetype ifc)) {
            return null;
        }

        final var bb = newBlockBuilder();
        for (var getter : ifc.getMethodDefinitions()) {
            if (isGetterMethodName(getter.name())) {
                bb.eol(printPropertySetter(getter, "arg", propertyNameFromGetter(getter)));
            }
        }

        for (var partial : ifc.partials()) {
            bb.blk(printConstructorPropertySetter(partial, getSpecifiedGetters(ifc)));
        }
        return bb;
    }

    private @Nullable BlockBuilder printConstructorPropertySetter(final DataContainerArchetype.Partial implementedIfc,
            final Set<MethodSignature> alreadySetProperties) {
        final var bb = newBlockBuilder();
        for (var getter : implementedIfc.getMethodDefinitions()) {
            if (isGetterMethodName(getter.name()) && getterByName(alreadySetProperties, getter.name()) == null) {
                bb.eol(printPropertySetter(getter, "arg", propertyNameFromGetter(getter)));
            }
        }

        for (var partial : implementedIfc.partials()) {
            bb.blk(printConstructorPropertySetter(partial,
                Sets.union(alreadySetProperties, getSpecifiedGetters(implementedIfc))));
        }
        return bb;
    }

    private static Set<MethodSignature> getSpecifiedGetters(final DataContainerArchetype type) {
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
                for (var partial : getAllIfcs(targetType)) {
                    bb.blk(generateIfCheck(partial, done));
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
                if (targetType instanceof AugmentableArchetype) {
                    bb
                        .eol("final var aug = base.augmentations();")
                        .str("if (!aug.isEmpty())").oB()
                            .str("this." + AUGMENTATION_FIELD + " = new ").str(importedName(JU_HASHMAP)).eol("<>(aug);")
                        .cB();
                }

                switch (props) {
                    case WithKey with -> {
                        bb.eol("this.key = base." + KEY_AWARE_KEY_NAME + "();");
                        for (var getter : with.keyGetters) {
                            bb.str("this.").str(getter.fieldName()).str(" = base.").str(getter.name()).eol("();");
                        }
                        appendCopyNonKeys(bb, with.implGetters);
                    }
                    case WithoutKey without -> appendCopyNonKeys(bb, without.allGetters);
                }
            }).nl();
    }

    @NonNullByDefault
    private BlockBuilder generateMethodFieldsFromComment(final DataContainerArchetype type) {
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
    @NonNullByDefault
    private boolean hasImplementsFromUses(final DataContainerArchetype type) {
        // FIXME: narrow down?
        return getAllIfcs(type).stream().anyMatch(ifc -> !ifc.getMethodDefinitions().isEmpty());
    }

    private @Nullable BlockBuilder generateIfCheck(final @NonNull DataContainerArchetype archetype,
            final List<DataContainerArchetype> done) {
        return archetype.getMethodDefinitions().isEmpty() ? null : newBlockBuilder()
            .str("if (arg instanceof ").str(importedName(archetype)).str(" castArg)").oB()
                .blk(printPropertySetter(archetype))
                .eol("isValidArg = true;")
            .cB();
    }

    private @Nullable BlockBuilder printPropertySetter(final Type implementedIfc) {
        // FIXME: narrow down?
        if (!(implementedIfc instanceof DataContainerArchetype ifc)) {
            return null;
        }

        final var bb = newBlockBuilder();
        for (var getter : ifc.getMethodDefinitions()) {
            if (isGetterMethodName(getter.name()) && !hasOverrideAnnotation(getter)) {
                bb.eol(printPropertySetter(getter, "castArg", propertyNameFromGetter(getter)));
            }
        }
        return bb;
    }

    // FIXME: return BlockBuilder
    @NonNullByDefault
    private String printPropertySetter(final MethodSignature getter, final String receiver, final String propertyName) {
        final var getterName = getter.name();

        final var ownGetter = findGetter(getterName);
        final var ownGetterType = ownGetter.returnType();
        if (strictTypeEquals(getter.returnType(), ownGetterType)) {
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

    private static @Nullable MethodSignature getterByName(final @NonNull DataContainerArchetype implType,
            final @NonNull String getterName) {
        final var getter = getterByName(implType.getMethodDefinitions(), getterName);
        if (getter != null) {
            return getter;
        }
        for (var partial : implType.partials()) {
            final var getterImpl = getterByName(partial, getterName);
            if (getterImpl != null) {
                return getterImpl;
            }
        }

        return null;
    }

    static @Nullable MethodSignature getterByName(final @NonNull Collection<@NonNull MethodSignature> methods,
        final @NonNull String implMethodName) {
        for (var method : methods) {
            final var methodName = method.name();
            if (isGetterMethodName(methodName) && isSameProperty(method.name(), implMethodName)) {
                return method;
            }
        }
        return null;
    }

    private static boolean isSameProperty(final String getterName1, final String getterName2) {
        return propertyNameFromGetter(getterName1).equals(propertyNameFromGetter(getterName2));
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

    private static List<DataContainerArchetype> getBaseIfcs(final DataContainerArchetype type) {
        final var baseIfcs = new ArrayList<DataContainerArchetype>();
        for (var partial : type.partials()) {
            if (!partial.getMethodDefinitions().isEmpty()) {
                baseIfcs.add(partial);
            }
        }
        return baseIfcs;
    }

    @NonNullByDefault
    private Set<DataContainerArchetype.Partial> getAllIfcs(final DataContainerArchetype archetype) {
        final var baseIfcs = new HashSet<DataContainerArchetype.Partial>();
        for (var partial : archetype.partials()) {
            if (!partial.getMethodDefinitions().isEmpty()) {
                baseIfcs.add(partial);
            }
            baseIfcs.addAll(getAllIfcs(partial));
        }
        return baseIfcs;
    }

//    @NonNullByDefault
//    private BlockBuilder constantsDeclarations() {
//        final var bb = newBlockBuilder();
//        for (var def : targetType.getConstantDefinitions()) {
//            if (!def.name().startsWith(PATTERN_CONSTANT_NAME)) {
//                // other constants are emitted separately
//                continue;
//            }
//
//            // FIXME: these are not populated anywhere and this whole method does not work :(
//            final var xsdToPattern = (Map<String, String>) def.value();
//            final var fieldSuffix = def.name().substring(PATTERN_CONSTANT_NAME.length());
//            final var jurPatternRef = importedName(JUR_PATTERN);
//            if (xsdToPattern.size() == 1) {
//                final var firstEntry = xsdToPattern.entrySet().iterator().next();
//                bb
//                    .str("private static final ").str(jurPatternRef).str(" " + MEMBER_PATTERN_LIST).str(fieldSuffix)
//                        .str(" = ").str(jurPatternRef).str(".compile(").jString(firstEntry.getKey()).eol(");")
//                    .str("private static final String " + MEMBER_REGEX_LIST).str(fieldSuffix).str(" = ")
//                        .jString(firstEntry.getValue()).eS();
//                continue;
//            }
//
//            bb
//                .str("private static final ").str(jurPatternRef).str("[] " + MEMBER_PATTERN_LIST).str(fieldSuffix)
//                    .str(" = ").str(importedName(CODEHELPERS)).str(".compilePatterns(").str(importedName(JU_LIST))
//                    .eol(".of(");
//            {
//                boolean first = true;
//                for (var xsd : xsdToPattern.keySet()) {
//                    if (first) {
//                        first = false;
//                    } else {
//                        bb.str(", ");
//                    }
//                    bb.jString(xsd);
//                }
//            }
//            bb
//                .eol("));")
//                .str("private static final String[] " + MEMBER_REGEX_LIST).str(fieldSuffix).str(" = { ");
//            {
//                boolean first = true;
//                for (var pattern : xsdToPattern.values()) {
//                    if (first) {
//                        first = false;
//                    } else {
//                        bb.str(", ");
//                    }
//                    bb.jString(pattern);
//                }
//            }
//            bb.eol(" };");
//        }
//        return bb;
//    }

    /**
     * {@return string with getter methods}
     */
    private @NonNull BlockBuilder generateGetters(final boolean addOverride) {
        final var bb = newBlockBuilder();

        if (props instanceof WithKey withKey) {
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
                .str("public ").str(importedName(withKey.key)).str(' ' + KEY_AWARE_KEY_NAME + "()").oB()
                    .eol("return key;")
                .cB()
                .newLine();
        }

        final var it = props.allGetters().iterator();
        if (!it.hasNext()) {
            return bb;
        }

        while (true) {
            final var getter = it.next();
            if (!addOverride) {
                bb
                    .eol("/**")
                    .str(" * Return current value associated with the property corresponding to {@link ")
                        .str(importedName(targetType)).str("#").str(getter.name()).eol("()}.")
                    .eol(" *")
                    .eol(" * @return current value")
                    .eol(" */");
            } else {
                bb
                    .at().eol(importedName(OVERRIDE));
            }
            bb.blk(asGetterMethod(getter.propName(), getter.type()));

            if (!it.hasNext()) {
                return bb;
            }

            bb.newLine();
        }
    }

    private @NonNull BlockBuilder generateSetter(final GetterShape getter) {
        final var returnType = getter.type();
        if (returnType instanceof ParameterizedType parameterized) {
            if (isListType(parameterized) || isSetType(parameterized)) {
                final var arguments = parameterized.getActualTypeArguments();
                return arguments.isEmpty() ? generateListSetter(getter, OBJECT)
                    : generateListSetter(getter, arguments.getFirst());
            }
            if (isMapType(parameterized)) {
                return generateMapSetter(getter, parameterized.getActualTypeArguments().get(1));
            }
        }
        return generateSimpleSetter(getter, returnType);
    }

    private @NonNull BlockBuilder generateListSetter(final GetterShape getter, final Type actualType) {
        final var bb = newBlockBuilder();
        final BlockBuilder argumentCheck;
        final var restrictions = restrictionsForSetter(actualType);
        if (restrictions != null) {
            bb.blk(generateCheckers(getter.propName(), restrictions, actualType));
            argumentCheck = newBlockBuilder()
                .str("if (values != null)").oB()
                    .str("for (").str(importedName(actualType)).str(" value : values)").oB()
                        .blk(checkFieldValue(targetType, getter.propName(), restrictions, actualType, "value"))
                    .cB()
                .cB();
        } else {
            argumentCheck = null;
        }

        return bb
            .nl()
            .eol("/**")
            .str(" * Set the property corresponding to {@link ").str(importedName(targetType)).str("#")
                .str(getter.name()).eol("()} to the specified")
            .eol(" * value.")
            .eol(" *")
            .eol(" * @param values desired value")
            .eol(" * @return this builder")
            .eol(" */")
            .str("public ").str(simpleName()).str(" set").str(getter.suffix()).str("(final ")
                .str(importedName(getter.type())).str(" values)").oB()
                .blk(argumentCheck)
                .str("this.").str(getter.fieldName()).eol(" = values;")
                .eol("return this;")
            .cB()
            .nl();
    }

    private @NonNull BlockBuilder generateMapSetter(final GetterShape getter, final Type actualType) {
        final var propName = getter.propName();
        final var bb = newBlockBuilder();
        final var restrictions = JavaFileTemplate.restrictionsForSetter(actualType);
        if (restrictions != null) {
            bb.blk(generateCheckers(propName, restrictions, actualType));
        }

        bb
            .nl()
            .eol("/**")
            .str(" * Set the property corresponding to {@link ").str(importedName(targetType)).str("#")
                .str(getter.name()).eol("()} to the specified")
            .txt("""
                 * value.
                 *
                 * @param values desired value
                 * @return this builder
                 */
                """)
            .str("public ").str(simpleName()).str(" set").str(toFirstUpper(propName)).str("(final ")
                .str(importedName(getter.type())).str(" values)").oB();

        if (restrictions != null) {
            bb
                .eol("if (values != null)").oB()
                    .str("for (").str(importedName(actualType)).str(" value : values.values())").oB()
                        .blk(checkFieldValue(targetType, propName, restrictions, actualType, "value"))
                    .cB()
                .cB();
        }

        return bb
            .str("this.").str(getter.fieldName()).eol(" = values;")
            .eol("return this;")
            .cB();
    }

    @NonNullByDefault
    private BlockBuilder generateSimpleSetter(final GetterShape getter, final Type actualType) {
        final var bb = newBlockBuilder();
        final var restrictions = restrictionsForSetter(actualType);
        final var propName = getter.propName();
        if (restrictions != null) {
            bb.nl().blk(generateCheckers(propName, restrictions, actualType));
        }
        bb
            .nl()
            .eol("/**")
            .str(" * Set the property corresponding to {@link ").str(importedName(targetType)).str("#")
                .str(getter.name()).eol("()} to the specified")
            .eol(" * value.")
            .eol(" *")
            .eol(" * @param value desired value")
            .eol(" * @return this builder")
            .eol(" */")
            .str("public ").str(simpleName()).str(" set").str(toFirstUpper(propName)).str("(final ")
                .str(importedName(getter.type())).str(" value)").oB();
        if (restrictions != null) {
            bb
                .str("if (value != null)").oB()
                    .blk(checkFieldValue(targetType, propName, restrictions, actualType, "value"))
                .cB();
        }
        return bb
            .str("this.").str(getter.fieldName()).eol(" = value;")
            .eol("return this;")
            .cB();
    }

    /**
     * {@return string with the setter methods}
     */
    private @NonNull BlockBuilder generateSetters() {
        final var bb = newBlockBuilder();
        if (props instanceof WithKey withKey) {
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
                .str("public ").str(simpleName()).str(" withKey(final ").str(importedName(withKey.key)).str(" key)")
                    .oB()
                    .eol("this.key = key;")
                    .eol("return this;")
                .cB();
        }
        for (var getter : props.allGetters()) {
            bb.blk(generateSetter(getter));
        }
        bb.newLine();
        if (targetType instanceof AugmentableArchetype) {
            final var augmentTypeRef = augmentationOfIn(targetType, javaType());
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
            .str("public <E$$ extends ").str(augmentationOfIn(targetType, javaType()))
                .str("> E$$ augmentation(").gen(importedName(CLASS), "E$$").str(" augmentationType)").oB()
                .str("return (E$$) " + AUGMENTATION_FIELD + ".get(").str(importedName(JU_OBJECTS))
                    .eol(".requireNonNull(augmentationType));")
            .cB();
    }

    /**
     * Append the code to copy non-key-components, with four spaces of indentation.
     */
    private static void appendCopyNonKeys(final BlockBuilder bb, final List<GetterShape> getters) {
        for (var getter : getters) {
            bb.str("this.").str(getter.fieldName()).str(" = base.").str(getter.name()).eol("();");
        }
    }

    /**
     * Check if the {@code type} represents non-presence container.
     *
     * @param type the archetype to be checked if represents container without presence statement.
     * @return {@code true} if specified {@code type} is a container without presence statement,
     *     {@code false} otherwise.
     */
    @NonNullByDefault
    static boolean isNonPresenceContainer(final DataContainerArchetype type) {
        return type instanceof ContainerObjectArchetype container && container.statement().presenceStatement() == null;
    }
}
