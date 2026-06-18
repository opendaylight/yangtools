/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.contract.Naming.BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME;
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.BINARY_TYPE;
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.BOOLEAN_TYPE;
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.EMPTY_TYPE;
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.STRING_TYPE;
import static org.opendaylight.yangtools.binding.model.ri.TypeConstants.PATTERN_CONSTANT_NAME;
import static org.opendaylight.yangtools.binding.model.ri.Types.STRING;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.UnionTypeObject;
import org.opendaylight.yangtools.binding.model.api.BitsTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.Constant;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.IdentityArchetype;
import org.opendaylight.yangtools.binding.model.api.Restrictions;
import org.opendaylight.yangtools.binding.model.api.ScalarTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.UnionTypeObjectArchetype;

/**
 * A template for {@link UnionTypeObject} specializations.
 */
final class UnionTypeObjectTemplate extends ArchetypeTemplate<@NonNull UnionTypeObjectArchetype> {
    @NonNullByDefault
    record Builder(UnionTypeObjectArchetype type, DataRootArchetype root) implements Template.Builder {
        Builder {
            requireNonNull(type);
        }

        @Override
        public UnionTypeObjectTemplate build() {
            return new UnionTypeObjectTemplate(type, root);
        }
    }

    private final @NonNull List<GeneratedProperty> allProperties;
    private final @NonNull List<GeneratedProperty> finalProperties;
    private final @NonNull List<GeneratedProperty> parentProperties;
    private final @NonNull List<GeneratedProperty> properties;
    private final Restrictions restrictions;

    /**
     * List of enumeration which are generated as JAVA enum type.
     */
    private final @NonNull List<EnumTypeObjectArchetype> enums;
    /**
     * List of constant instances which are generated as JAVA public static final attributes.
     */
    private final @NonNull List<Constant> consts;
    private final AbstractRangeGenerator<?> rangeGenerator;

    @NonNullByDefault
    private UnionTypeObjectTemplate(final GeneratedClass javaType, final UnionTypeObjectArchetype archetype,
            final DataRootArchetype root) {
        super(javaType, archetype, root);
        properties = archetype.getProperties();
        finalProperties = properties.stream()
            .filter(GeneratedProperty::isReadOnly)
            .collect(Collectors.toUnmodifiableList());
        parentProperties = propertiesOfAllParents(archetype);
        restrictions = archetype.getRestrictions();

        allProperties = Stream.concat(properties.stream(), parentProperties.stream())
            .sorted(PROP_COMPARATOR)
            .collect(Collectors.toUnmodifiableList());

        enums = archetype.getEnumerations();
        consts = archetype.getConstantDefinitions();
        rangeGenerator = restrictions != null && restrictions.getRangeConstraint().isPresent()
            ? requireNonNull(AbstractRangeGenerator.forType(TypeUtils.encapsulatedValueType(archetype))) : null;
    }

    /**
     * Returns the list of the read only properties of all extending generated transfer object from <code>genTO</code>
     * to highest parent generated transfer object.
     *
     * @param gto generated transfer object for which is the list of read only properties generated
     * @return list of all read only properties from actual to highest parent generated transfer object. In case when
     *         extension exists the method is recursive called.
     */
    @NonNullByDefault
    @VisibleForTesting
    static List<GeneratedProperty> propertiesOfAllParents(final GeneratedTransferObject<?> gto) {
        final var superType = gto.getSuperType();
        return superType == null ? List.of() : streamAllProperties(superType).collect(Collectors.toUnmodifiableList());
    }

    @NonNullByDefault
    private static Stream<GeneratedProperty> streamAllProperties(final GeneratedTransferObject<?> gto) {
        final var stream = gto.getProperties().stream().filter(GeneratedProperty::isReadOnly);
        final var superType = gto.getSuperType();
        return superType == null ? stream : Stream.concat(stream, streamAllProperties(superType));
    }

    @NonNullByDefault
    private UnionTypeObjectTemplate(final UnionTypeObjectArchetype archetype, final DataRootArchetype root) {
        this(GeneratedClass.of(archetype), archetype, root);
    }

    @NonNullByDefault
    static BlockBuilder generateInner(final GeneratedClass.Nested javaType, final UnionTypeObjectArchetype archetype,
            final DataRootArchetype root) {
        return new UnionTypeObjectTemplate(javaType, archetype, root).generateBody(true);
    }

    @Override
    BlockBuilder body() {
        return generateBody(false);
    }

    /**
     * {@return string with class source code in JAVA format}
     * @param isInnerClass {@code true} if generated class is an inner class
     */
    @NonNullByDefault
    private BlockBuilder generateBody(final boolean isInnerClass) {
        final var bb = newBlockBuilder()
            .blk(wrapToDocumentation(formatDataForJavaDoc(type())))
            .blk(annotationDeclaration());

        if (!isInnerClass) {
            bb.eol(generatedAnnotation());
        }
        bb
            .frg(generateClassDeclaration(isInnerClass)).oB()
                .eol("@java.io.Serial")
                .str("private static final long serialVersionUID = ").jLong(archetype().serialVersionUID()).eS()
                 // inner classes
                .blk(generateInnerClasses(root, type().getEnclosedTypes()))
                // inner EnumTypeObjects
                .blk(generateInnerEnumTypeObjects(root, enums))
                // constants
                .blk(constantsDeclarations());

        // fields
        if (!properties.isEmpty()) {
            for (var field : properties) {
                bb.str("private ");
                if (field.isReadOnly()) {
                    bb.str("final ");
                }
                bb.str(importedReturnType(field)).sp().str(fieldName(field)).eS();
            }
        }

        // length/range checkes
        if (restrictions != null) {
            final var length = restrictions.getLengthConstraint();
            if (length.isPresent()) {
                bb.nl().blk(LengthGenerator.generateLengthChecker("_value",
                    TypeUtils.encapsulatedValueType(archetype()), length.orElseThrow(), javaType()));
            }
            final var range = restrictions.getRangeConstraint();
            if (range.isPresent()) {
                bb.nl().blk(rangeGenerator.generateRangeChecker("_value", range.orElseThrow(), javaType()));
            }
        }

        bb
            .blk(constructors())
            .blk(propertyMethods());

        if (!properties.isEmpty()) {
            bb
                .nl()
                .blk(generateHashCode(properties))
                .nl()
                .blk(generateEquals(properties))
                .nl()
                .blk(generateToString(properties));
        }

        return bb.cB().nl();
    }

    /**
     * {@return string with class declaration in JAVA format}
     * @param isInnerClass boolean value which specify if generated class is|isn't inner
     */
    @NonNullByDefault
    private BlockBuilder generateClassDeclaration(final boolean isInnerClass) {
        final var archetype = archetype();

        final var bb = newBlockBuilder()
            .str("public");
        if (isInnerClass) {
            bb.str(" static final ");
        } else {
            bb.str(archetype.isAbstract() ? " abstract " : " ");
        }
        bb.str("class ").str(archetype.simpleName());

        final var superType = archetype.getSuperType();
        if (superType != null) {
            bb.str(" extends ").str(importedName(superType));
        }

        final var ifaces = archetype.getImplements();
        if (!ifaces.isEmpty()) {
            bb.nl().str(" implements ");

            final var it = ifaces.iterator();
            while (true) {
                bb.str(importedName(it.next()));
                if (!it.hasNext()) {
                    break;
                }
                bb.str(", ");
            }
        }
        return bb;
    }

    private @Nullable BlockBuilder constantsDeclarations() {
        if (consts.isEmpty()) {
            return null;
        }

        final var bb = newBlockBuilder();
        for (var c : consts) {
            switch (c.getName()) {
                case PATTERN_CONSTANT_NAME -> appendPatternConstant(bb, (Map<String, String>) c.getValue());
                default -> bb.txt(emitConstant(c));
            }
        }
        return bb;
    }

    @NonNullByDefault
    private void appendPatternConstant(final BlockBuilder bb, final Map<String, String> constValue) {
        final var jurPattern = importedName(JUR_PATTERN);
        final var juList = importedName(JU_LIST);

        bb.str("public static final ").str(juList).str("<String> " + PATTERN_CONSTANT_NAME + " = ").str(juList)
            .str(".of(");
        {
            boolean first = true;
            for (var value : constValue.keySet()) {
                if (first) {
                    first = false;
                } else {
                    bb.str(", ");
                }
                bb.jString(requireNonNull(value));
            }
        }
        bb
            .eol(");")
            .str("private static final ").str(jurPattern);
        if (constValue.size() == 1) {
            bb
                .str(" " + MEMBER_PATTERN_LIST + " = ").str(jurPattern)
                .eol(".compile(" + PATTERN_CONSTANT_NAME + ".getFirst());")
                .str("private static final String " + MEMBER_REGEX_LIST + " = ")
                    .jString(constValue.values().iterator().next()).eS();
            return;
        }

        // FIXME: should be multi-line
        bb
            .str("[] " + MEMBER_PATTERN_LIST + " = ").str(importedName(CODEHELPERS))
                .eol(".compilePatterns(" + PATTERN_CONSTANT_NAME + ");")
            .str("private static final String[] " + MEMBER_REGEX_LIST + " = { ");
        {
            boolean first = true;
            for (var value : constValue.values()) {
                if (first) {
                    first = false;
                } else {
                    bb.str(", ");
                }
                bb.jString(requireNonNull(value));
            }
        }
        bb.eol(" };");
    }

    @NonNullByDefault
    private BlockBuilder constructors() {
        final var bb = newBlockBuilder()
            .blk(unionConstructorsParentProperties())
            .blk(unionConstructors());
        // TODO: figure out a better flow here
        if (!allProperties.isEmpty()) {
            bb.blk(copyConstructor());
        }
        if (properties.isEmpty() && !parentProperties.isEmpty()) {
            bb.blk(parentConstructor());
        }
        return bb
            .nl()
            .blk(generateStringValue());
    }

    private @Nullable BlockBuilder unionConstructors() {
        if (finalProperties.isEmpty()) {
            return null;
        }

        final var simpleName = type().simpleName();
        final var bb = newBlockBuilder().nl();
        final var it = finalProperties.iterator();
        while (true) {
            final var property = it.next();
            final var actualType = property.getReturnType();
            final var propertyAndTopParentProperties = Iterables.concat(parentProperties, List.of(property));
            final var propFieldName = fieldName(property);

            if (restrictions != null) {
                bb.blk(generateCheckers(property, restrictions, actualType)).newLine();
            }

            bb
                .str("public ").str(simpleName).str("(").str(asArgumentsDeclaration(propertyAndTopParentProperties))
                    .str(")").oB();
            if (!parentProperties.isEmpty()) {
                bb.str("super(").str(asArguments(parentProperties)).eol(");");
            }

            final var setterRestrictions = restrictionsForSetter(actualType);
            if (setterRestrictions != null) {
                bb.blk(checkArgument(property, setterRestrictions, actualType, propFieldName)).newLine();
            }

            for (var other : finalProperties) {
                bb.str("this.");
                if (property.equals(other)) {
                    bb.str(propFieldName).str(" = ").str(importedName(JU_OBJECTS)).str(".requireNonNull(")
                        .str(propFieldName).eol(");");
                } else {
                    bb.str(fieldName(other)).eol(" = null;");
                }
            }

            bb.cB();

            if (!it.hasNext()) {
                return bb;
            }
            bb.newLine();
        }
    }

    private @Nullable BlockBuilder unionConstructorsParentProperties() {
        if (parentProperties.isEmpty()) {
            return null;
        }

        final var bb = newBlockBuilder();
        final var it = parentProperties.iterator();
        final var simpleName = type().simpleName();
        while (true) {
            final var prop = it.next();
            final var fieldName = fieldName(prop);
            final var propType = importedReturnType(prop);
            bb
                .str("public ").str(simpleName).str("(").str(propType).sp().str(fieldName).str(")").oB()
                    .str("super(").str(fieldName).eol(");")
                .cB();

            if (!it.hasNext()) {
                return bb;
            }
            bb.newLine();
        }
    }

    @NonNullByDefault
    private BlockBuilder generateStringValue() {
        final var bb = newBlockBuilder().txt("""
                      /**
                       * Return a String representing the value of this union.
                       *
                       * @return String representation of this union's value.
                       */
                      """)
                .str("public ").str(importedName(STRING)).str(" stringValue()").oB();

        for (var prop : finalProperties) {
            final var field = fieldName(prop);
            final var type = prop.getReturnType();
            final var fqcn = type.canonicalName();

            bb
                .str("if (").str(field).str(" != null)").oB()
                    .str("return ");

            if (STRING_TYPE.equals(type)) {
                // type string
                bb.str(field).eS();
            } else if ("org.opendaylight.yangtools.binding.BindingInstanceIdentifier".equals(fqcn)) {
                // type instance-identifier
                bb.str(field).eol(".toString();");
            } else if (BINARY_TYPE.equals(type)) {
                // type binary
                bb.str("new ").str(importedName(STRING)).str("(").str(field).eol(");");
            } else if (fqcn.startsWith("java.lang") || type instanceof EnumTypeObjectArchetype) {
                // type int* or enumeration*
                bb.str(field).eol(".toString();");
            } else if (fqcn.startsWith("org.opendaylight.yangtools.yang.common.Uint")
                        || fqcn.equals("org.opendaylight.yangtools.yang.common.Decimal64")) {
                // type uint*, decimal64
                bb.str(field).eol(".toCanonicalString();");
            } else if (type instanceof UnionTypeObjectArchetype) {
                // union type
                bb.str(field).eol(".stringValue();");
            } else if (BOOLEAN_TYPE.equals(typedefReturnType(type))) {
                // generated boolean typedef
                bb.str(field).eol(".isValue().toString();");
            } else if (BINARY_TYPE.equals(typedefReturnType(type))) {
                // generated byte[] typedef
                bb.str(importedName(JU_BASE64)).str(".getEncoder().encodeToString(").str(field).eol(".getValue());");
            } else if (EMPTY_TYPE.equals(type) || EMPTY_TYPE.equals(typedefReturnType(type))) {
                // generated empty typedef
                bb.eol("\"\";");
            } else if (type instanceof BitsTypeObjectArchetype) {
                // generated bits typedef
                bb.str(importedName(JU_ARRAYS)).str(".toString(").str(field).eol(".values());");
            } else if (type instanceof IdentityArchetype) {
                // generated identity
                bb.str(field).eol("." + BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME + "().toString();");
            } else {
                // generated type
                bb.str(field).eol(".getValue().toString();");
            }
            bb
                .cB();
        }

        return bb
            .eol("throw new IllegalStateException(\"No value assigned\");")
            .cB();
    }

    private static @Nullable ConcreteType typedefReturnType(final @NonNull Type type) {
        return type instanceof ScalarTypeObjectArchetype scalar ? scalar.valueType() : null;
    }

    @NonNullByDefault
    private BlockBuilder copyConstructor() {
        final var simpleName = type().simpleName();

        return newBlockBuilder().txt("""
                  /**
                   * Creates a copy from Source Object.
                   *
                   * @param source Source object
                   */
                  """)
            .str("public ").str(simpleName).str("(").str(simpleName).str(" source)").jBlock(bb -> {
                if (!parentProperties.isEmpty()) {
                    bb.eol("super(source);");
                }
                for (var prop : properties) {
                    final var fieldName = fieldName(prop);

                    // TODO: figure out a better flow
                    bb.str("this.").str(fieldName).str(" = ");
                    if (isArrayProperty(prop)) {
                        bb.str(importedName(CODEHELPERS)).str(".copyArray(source.").str(fieldName).str(")");
                    } else {
                        bb.str("source.").str(fieldName);
                    }
                    bb.eS();
                }
            }).nl();
    }

    private @Nullable BlockBuilder propertyMethods() {
        if (properties.isEmpty()) {
            return null;
        }

        final var bb = newBlockBuilder();
        final var it = properties.iterator();
        do {
            final var field = it.next();
            bb.nl().blk(asGetterMethod(field));
            if (!field.isReadOnly()) {
                bb.nl().blk(asSetterMethod(field));
            }
        } while (it.hasNext());
        return bb;
    }

    @NonNullByDefault
    private BlockBuilder parentConstructor() {
        final var importedSuper = importedName(archetype().getSuperType());

        return newBlockBuilder()
            .eol("/**")
            .str(" * Creates a new instance from ").eol(importedSuper)
            .eol(" *")
            .eol(" * @param source Source object")
            .eol(" */")
            .str("public ").str(type().simpleName()).str("(").str(importedSuper).str(" source)").oB()
                .eol("super(source);")
                .blk(genPatternEnforcer("getValue()"))
            .cB();
    }

    private @Nullable BlockBuilder genPatternEnforcer(final @NonNull String ref) {
        for (var constant : consts) {
            if (PATTERN_CONSTANT_NAME.equals(constant.getName())) {
                return newBlockBuilder()
                    .str(importedName(CODEHELPERS)).str(".checkPattern(").str(ref).str(", ")
                        .eol(MEMBER_PATTERN_LIST + ", " + MEMBER_REGEX_LIST + ");");
            }
        }
        return null;
    }
}
