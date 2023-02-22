/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.mdsal.binding.generator.BindingGeneratorUtil.encodeAngleBrackets;
import static org.opendaylight.mdsal.binding.generator.BindingGeneratorUtil.replaceAllIllegalChars;

import com.google.common.base.CharMatcher;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSortedSet;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.processing.Generated;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import org.opendaylight.mdsal.binding.model.api.AnnotationType;
import org.opendaylight.mdsal.binding.model.api.ConcreteType;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.model.api.Restrictions;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.YangSourceDefinition.Multiple;
import org.opendaylight.mdsal.binding.model.api.YangSourceDefinition.Single;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.mdsal.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.CodeHelpers;
import org.opendaylight.yangtools.yang.binding.contract.Naming;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.export.DeclaredStatementFormatter;

/**
 * Base Java file template. Contains a non-null type and imports which the generated code refers to.
 */
class JavaFileTemplate {
    /**
     * {@code java.lang.Class} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName CLASS = JavaTypeName.create(Class.class);
    /**
     * {@code java.lang.Deprecated} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName DEPRECATED = JavaTypeName.create(Deprecated.class);
    /**
     * {@code java.lang.IllegalArgumentException} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName IAE = JavaTypeName.create(IllegalArgumentException.class);
    /**
     * {@code java.lang.NullPointerException} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName NPE = JavaTypeName.create(NullPointerException.class);
    /**
     * {@code java.lang.NoSuchElementException} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName NSEE = JavaTypeName.create(NoSuchElementException.class);
    /**
     * {@code java.lang.Object} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName OBJECT = JavaTypeName.create(Object.class);
    /**
     * {@code java.lang.Override} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName OVERRIDE = JavaTypeName.create(Override.class);
    /**
     * {@code java.lang.void} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName SUPPRESS_WARNINGS = JavaTypeName.create(SuppressWarnings.class);
    /**
     * {@code java.lang.SuppressWarnings} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName VOID = JavaTypeName.create(void.class);

    /**
     * {@code java.util.Arrays} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName JU_ARRAYS = JavaTypeName.create(Arrays.class);
    /**
     * {@code java.util.HashMap} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName JU_HASHMAP = JavaTypeName.create(HashMap.class);
    /**
     * {@code java.util.List} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName JU_LIST = JavaTypeName.create(List.class);
    /**
     * {@code java.util.Map} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName JU_MAP = JavaTypeName.create(Map.class);
    /**
     * {@code java.util.Objects} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName JU_OBJECTS = JavaTypeName.create(Objects.class);
    /**
     * {@code java.util.regex.Pattern} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName JUR_PATTERN = JavaTypeName.create(Pattern.class);

    /**
     * {@code javax.annotation.processing.Generated} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName GENERATED = JavaTypeName.create(Generated.class);

    /**
     * {@code org.eclipse.jdt.annotation.NonNull} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName NONNULL = JavaTypeName.create("org.eclipse.jdt.annotation", "NonNull");

    /**
     * {@code org.eclipse.jdt.annotation.NonNullByDefault} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName NONNULL_BY_DEFAULT =
        JavaTypeName.create("org.eclipse.jdt.annotation", "NonNullByDefault");

    /**
     * {@code org.eclipse.jdt.annotation.Nullable} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName NULLABLE = JavaTypeName.create("org.eclipse.jdt.annotation", "Nullable");

    /**
     * {@code org.opendaylight.yangtools.yang.binding.CodeHelpers} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName CODEHELPERS = JavaTypeName.create(CodeHelpers.class);

    /**
     * {@code com.google.common.base.MoreObjects} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName MOREOBJECTS = JavaTypeName.create(MoreObjects.class);

    private static final Comparator<MethodSignature> METHOD_COMPARATOR = new AlphabeticallyTypeMemberComparator<>();
    private static final CharMatcher AMP_MATCHER = CharMatcher.is('&');
    private static final Pattern TAIL_COMMENT_PATTERN = Pattern.compile("*/", Pattern.LITERAL);
    private static final DeclaredStatementFormatter YANG_FORMATTER = DeclaredStatementFormatter.builder()
        .addIgnoredStatement(YangStmtMapping.CONTACT)
        .addIgnoredStatement(YangStmtMapping.DESCRIPTION)
        .addIgnoredStatement(YangStmtMapping.REFERENCE)
        .addIgnoredStatement(YangStmtMapping.ORGANIZATION)
        .build();
    private static final int GETTER_PREFIX_LENGTH = Naming.GETTER_PREFIX.length();
    private static final Type AUGMENTATION_RET_TYPE;

    static {
        final Method m;
        try {
            m = Augmentable.class.getDeclaredMethod(Naming.AUGMENTABLE_AUGMENTATION_NAME, Class.class);
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }

        AUGMENTATION_RET_TYPE = Type.of(JavaTypeName.create(m.getReturnType()));
    }

    private final AbstractJavaGeneratedType javaType;
    private final GeneratedType type;

    JavaFileTemplate(final @NonNull GeneratedType type) {
        this(new TopLevelJavaGeneratedType(type), type);
    }

    JavaFileTemplate(final AbstractJavaGeneratedType javaType, final GeneratedType type) {
        this.javaType = requireNonNull(javaType);
        this.type = requireNonNull(type);
    }

    final AbstractJavaGeneratedType javaType() {
        return javaType;
    }

    final GeneratedType type() {
        return type;
    }

    final String generateImportBlock() {
        verify(javaType instanceof TopLevelJavaGeneratedType);
        return ((TopLevelJavaGeneratedType) javaType).imports().map(name -> "import " + name + ";\n")
                .collect(Collectors.joining());
    }

    final @NonNull String importedJavadocName(final @NonNull Type intype) {
        return importedName(intype instanceof ParameterizedType parameterized ? parameterized.getRawType() : intype);
    }

    final @NonNull String importedName(final @NonNull Type intype) {
        return javaType.getReferenceString(intype);
    }

    final @NonNull String importedName(final @NonNull Type intype, final @NonNull String annotation) {
        return javaType.getReferenceString(intype, annotation);
    }

    final @NonNull String importedName(final Class<?> cls) {
        return importedName(Types.typeForClass(cls));
    }

    final @NonNull String importedName(final @NonNull JavaTypeName intype) {
        return javaType.getReferenceString(intype);
    }

    final @NonNull String importedNonNull(final @NonNull Type intype) {
        return importedName(intype, importedName(NONNULL));
    }

    final @NonNull String importedNullable(final @NonNull Type intype) {
        return importedName(intype, importedName(NULLABLE));
    }

    final @NonNull String fullyQualifiedNonNull(final @NonNull Type intype) {
        return fullyQualifiedName(intype, importedName(NONNULL));
    }

    final @NonNull String fullyQualifiedName(final @NonNull Type intype, final @NonNull String annotation) {
        return javaType.getFullyQualifiedReference(intype, annotation);
    }

    // Exposed for BuilderTemplate
    boolean isLocalInnerClass(final JavaTypeName name) {
        final Optional<JavaTypeName> optEnc = name.immediatelyEnclosingClass();
        return optEnc.isPresent() && type.getIdentifier().equals(optEnc.get());
    }

    final CharSequence generateInnerClass(final GeneratedType innerClass) {
        if (!(innerClass instanceof GeneratedTransferObject gto)) {
            return "";
        }

        final NestedJavaGeneratedType innerJavaType = javaType.getEnclosedType(innerClass.getIdentifier());
        return gto.isUnionType() ? new UnionTemplate(innerJavaType, gto).generateAsInnerClass()
                : new ClassTemplate(innerJavaType, gto).generateAsInnerClass();
    }

    /**
     * Return imported name of java.util class, whose hashCode/equals methods we want to invoke on the property. Returns
     * {@link Arrays} if the property is an array, {@link Objects} otherwise.
     *
     * @param property Generated property
     * @return Imported class name
     */
    final String importedUtilClass(final GeneratedProperty property) {
        return importedUtilClass(property.getReturnType());
    }

    /**
     * Return imported name of java.util class, whose hashCode/equals methods we want to invoke for a type. Returns
     * {@link Arrays} if the type is an array, {@link Objects} otherwise.
     *
     * @param returnType A property return Type
     * @return Imported class name
     */
    final String importedUtilClass(final Type returnType) {
        return importedName(returnType.getName().indexOf('[') != -1 ? JU_ARRAYS : JU_OBJECTS);
    }

    final String generatedAnnotation() {
        return "@" + importedName(GENERATED) + "(\"mdsal-binding-generator\")";
    }

    /**
     * Run type analysis, which results in identification of the augmentable type, as well as all methods available
     * to the type, expressed as properties.
     */
    static Map.Entry<Type, Set<BuilderGeneratedProperty>> analyzeTypeHierarchy(final GeneratedType type) {
        final Set<MethodSignature> methods = new LinkedHashSet<>();
        final Type augmentType = createMethods(type, methods);
        final Set<MethodSignature> sortedMethods = ImmutableSortedSet.orderedBy(METHOD_COMPARATOR).addAll(methods)
                .build();

        return new AbstractMap.SimpleImmutableEntry<>(augmentType, propertiesFromMethods(sortedMethods));
    }

    static final Restrictions restrictionsForSetter(final Type actualType) {
        return actualType instanceof GeneratedType ? null : getRestrictions(actualType);
    }

    static final Restrictions getRestrictions(final Type type) {
        if (type instanceof ConcreteType) {
            return ((ConcreteType) type).getRestrictions();
        }
        if (type instanceof GeneratedTransferObject) {
            return ((GeneratedTransferObject) type).getRestrictions();
        }
        return null;
    }

    /**
     * Generate a call to {@link Object#clone()} if target field represents an array. Returns an empty string otherwise.
     *
     * @param property Generated property
     * @return The string used to clone the property, or an empty string
     */
    static final String cloneCall(final GeneratedProperty property) {
        return property.getReturnType().getName().endsWith("[]") ? ".clone()" : "";
    }

    /**
     * Returns set of method signature instances which contains all the methods of the <code>genType</code>
     * and all the methods of the implemented interfaces.
     *
     * @returns set of method signature instances
     */
    private static ParameterizedType createMethods(final GeneratedType type, final Set<MethodSignature> methods) {
        methods.addAll(type.getMethodDefinitions());
        return collectImplementedMethods(type, methods, type.getImplements());
    }

    /**
     * Adds to the <code>methods</code> set all the methods of the <code>implementedIfcs</code>
     * and recursively their implemented interfaces.
     *
     * @param methods set of method signatures
     * @param implementedIfcs list of implemented interfaces
     */
    private static ParameterizedType collectImplementedMethods(final GeneratedType type,
            final Set<MethodSignature> methods, final List<Type> implementedIfcs) {
        if (implementedIfcs == null || implementedIfcs.isEmpty()) {
            return null;
        }

        ParameterizedType augmentType = null;
        for (Type implementedIfc : implementedIfcs) {
            if (implementedIfc instanceof GeneratedType ifc && !(implementedIfc instanceof GeneratedTransferObject)) {
                addImplMethods(methods, ifc);

                final ParameterizedType t = collectImplementedMethods(type, methods, ifc.getImplements());
                if (t != null && augmentType == null) {
                    augmentType = t;
                }
            } else if (Augmentable.class.getName().equals(implementedIfc.getFullyQualifiedName())) {
                augmentType = Types.parameterizedTypeFor(AUGMENTATION_RET_TYPE, Type.of(type.getIdentifier()));
            }
        }

        return augmentType;
    }

    private static void addImplMethods(final Set<MethodSignature> methods, final GeneratedType implType) {
        for (final MethodSignature implMethod : implType.getMethodDefinitions()) {
            if (hasOverrideAnnotation(implMethod)) {
                methods.add(implMethod);
            } else {
                final String implMethodName = implMethod.getName();
                if (Naming.isGetterMethodName(implMethodName)
                        && getterByName(methods, implMethodName).isEmpty()) {

                    methods.add(implMethod);
                }
            }
        }
    }

    protected static Optional<MethodSignature> getterByName(final Iterable<MethodSignature> methods,
            final String implMethodName) {
        for (MethodSignature method : methods) {
            final String methodName = method.getName();
            if (Naming.isGetterMethodName(methodName) && isSameProperty(method.getName(), implMethodName)) {
                return Optional.of(method);
            }
        }
        return Optional.empty();
    }

    protected static String propertyNameFromGetter(final MethodSignature getter) {
        return propertyNameFromGetter(getter.getName());
    }

    protected static String propertyNameFromGetter(final String getterName) {
        final String prefix;
        if (Naming.isGetterMethodName(getterName)) {
            prefix = Naming.GETTER_PREFIX;
        } else if (Naming.isNonnullMethodName(getterName)) {
            prefix = Naming.NONNULL_PREFIX;
        } else if (Naming.isRequireMethodName(getterName)) {
            prefix = Naming.REQUIRE_PREFIX;
        } else {
            throw new IllegalArgumentException(getterName + " is not a getter");
        }
        return StringExtensions.toFirstLower(getterName.substring(prefix.length()));
    }

    /**
     * Check whether specified method has an attached annotation which corresponds to {@code @Override}.
     *
     * @param method Method to examine
     * @return True if there is an override annotation
     */
    static boolean hasOverrideAnnotation(final MethodSignature method) {
        for (final AnnotationType annotation : method.getAnnotations()) {
            if (OVERRIDE.equals(annotation.getIdentifier())) {
                return true;
            }
        }
        return false;
    }

    final void appendSnippet(final StringBuilder sb, final GeneratedType genType) {
        genType.getYangSourceDefinition().ifPresent(def -> {
            sb.append('\n');

            if (def instanceof Single single) {
                final DocumentedNode node = single.getNode();

                sb.append("<p>\n")
                    .append("This class represents the following YANG schema fragment defined in module <b>")
                    .append(def.getModule().argument().getLocalName()).append("</b>\n")
                    .append("<pre>\n");
                appendYangSnippet(sb, def.getModule(), ((EffectiveStatement<?, ?>) node).getDeclared());
                sb.append("</pre>");

                if (node instanceof SchemaNode schema) {
//                    sb.append("The schema path to identify an instance is\n");
//                    appendPath(sb.append("<i>"), def.getModule(), schema.getPath().getPathFromRoot());
//                    sb.append("</i>\n");

                    if (hasBuilderClass(schema)) {
                        final String builderName = genType.getName() + Naming.BUILDER_SUFFIX;

                        sb.append("\n<p>To create instances of this class use {@link ").append(builderName)
                        .append("}.\n")
                        .append("@see ").append(builderName).append('\n');
                        if (node instanceof ListSchemaNode) {
                            final var keyDef = ((ListSchemaNode) node).getKeyDefinition();
                            if (!keyDef.isEmpty()) {
                                sb.append("@see ").append(genType.getName()).append(Naming.KEY_SUFFIX);
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
                        .append("@see ").append(augType.getName());
                    }
                }
            } else if (def instanceof Multiple multiple) {
                sb.append("<pre>\n");
                for (SchemaNode node : multiple.getNodes()) {
                    appendYangSnippet(sb, def.getModule(), ((EffectiveStatement<?, ?>) node).getDeclared());
                }
                sb.append("</pre>\n");
            }
        });
    }

    private static @Nullable Type findAugmentationArgument(final GeneratedType genType) {
        for (var implType : genType.getImplements()) {
            if (implType instanceof ParameterizedType parameterized) {
                final var augmentType = BindingTypes.extractAugmentable(parameterized);
                if (augmentType != null) {
                    return augmentType;
                }
            }
        }
        return null;
    }

    static String encodeJavadocSymbols(final String description) {
        // FIXME: Use String.isBlank()?
        return description == null || description.isEmpty() ? description
            : TAIL_COMMENT_PATTERN.matcher(AMP_MATCHER.replaceFrom(description, "&amp;")).replaceAll("&#42;&#47;");
    }

    private static void appendYangSnippet(final StringBuilder sb, final ModuleEffectiveStatement module,
        final DeclaredStatement<?> stmt) {
        for (String str : YANG_FORMATTER.toYangTextSnippet(module, stmt)) {
            sb.append(replaceAllIllegalChars(encodeAngleBrackets(encodeJavadocSymbols(str))));
        }
    }

//    private static void appendPath(final StringBuilder sb, final ModuleEffectiveStatement module,
//            final List<QName> path) {
//        if (!path.isEmpty()) {
//            // FIXME: this is module name, while when we switch, we end up using QName.toString() -- which is weird
//            sb.append(module.argument().getLocalName());
//            XMLNamespace currentNamespace = path.get(0).getNamespace();
//
//            for (QName pathElement : path) {
//                final XMLNamespace elementNamespace = pathElement.getNamespace();
//                if (!elementNamespace.equals(currentNamespace)) {
//                    sb.append(pathElement);
//                    currentNamespace = elementNamespace;
//                } else {
//                    sb.append(pathElement.getLocalName());
//                }
//            }
//        }
//    }

    private static boolean hasBuilderClass(final SchemaNode schemaNode) {
        return schemaNode instanceof ContainerSchemaNode || schemaNode instanceof ListSchemaNode
                || schemaNode instanceof NotificationDefinition;
    }

    private static boolean isSameProperty(final String getterName1, final String getterName2) {
        return propertyNameFromGetter(getterName1).equals(propertyNameFromGetter(getterName2));
    }

    /**
     * Creates set of generated property instances from getter <code>methods</code>.
     *
     * @param methods set of method signature instances which should be transformed to list of properties
     * @return set of generated property instances which represents the getter <code>methods</code>
     */
    private static Set<BuilderGeneratedProperty> propertiesFromMethods(final Collection<MethodSignature> methods) {
        if (methods == null || methods.isEmpty()) {
            return Collections.emptySet();
        }
        final Set<BuilderGeneratedProperty> result = new LinkedHashSet<>();
        for (MethodSignature m : methods) {
            final BuilderGeneratedProperty createdField = propertyFromGetter(m);
            if (createdField != null) {
                result.add(createdField);
            }
        }
        return result;
    }

    /**
     * Creates generated property instance from the getter <code>method</code> name and return type.
     *
     * @param method method signature from which is the method name and return type obtained
     * @return generated property instance for the getter <code>method</code>
     * @throws IllegalArgumentException <ul>
     *                                    <li>if the {@code method} equals {@code null}</li>
     *                                    <li>if the name of the {@code method} equals {@code null}</li>
     *                                    <li>if the name of the {@code method} is empty</li>
     *                                    <li>if the return type of the {@code method} equals {@code null}</li>
     *                                  </ul>
     */
    private static BuilderGeneratedProperty propertyFromGetter(final MethodSignature method) {
        checkArgument(method != null);
        checkArgument(method.getReturnType() != null);
        checkArgument(method.getName() != null);
        checkArgument(!method.getName().isEmpty());
        if (method.isDefault()) {
            return null;
        }
        if (!Naming.isGetterMethodName(method.getName())) {
            return null;
        }

        final String fieldName = StringExtensions.toFirstLower(method.getName().substring(GETTER_PREFIX_LENGTH));
        return new BuilderGeneratedProperty(fieldName, method);
    }
}
