/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.codegen.Constants.MEMBER_PATTERN_LIST;
import static org.opendaylight.yangtools.binding.codegen.Constants.MEMBER_REGEX_LIST;
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.BINARY_TYPE;
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.BOOLEAN_TYPE;
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.EMPTY_TYPE;
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.INSTANCE_IDENTIFIER;
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.INT16_TYPE;
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.INT32_TYPE;
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.INT64_TYPE;
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.INT8_TYPE;
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.STRING_TYPE;
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.UINT16_TYPE;
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.UINT32_TYPE;
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.UINT64_TYPE;
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.UINT8_TYPE;
import static org.opendaylight.yangtools.binding.model.ri.TypeConstants.PATTERN_CONSTANT_NAME;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.VerifyException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.BitsTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.Constant;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.Decimal64Type;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.RestrictedType;
import org.opendaylight.yangtools.binding.model.api.Restrictions;
import org.opendaylight.yangtools.binding.model.api.ScalarTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.UnionTypeObjectArchetype;

/**
 * Template for generating JAVA class.
 */
// FIXME: eliminate this class
abstract sealed class ClassTemplate<T extends @NonNull GeneratedTransferObject<?>> extends ArchetypeTemplate<T>
        permits ScalarTypeObjectTemplate, UnionTypeObjectTemplate {
    private static final Set<ConcreteType> VALUEOF_TYPES = Set.of(
        BOOLEAN_TYPE, INT8_TYPE, INT16_TYPE, INT32_TYPE, INT64_TYPE, UINT8_TYPE, UINT16_TYPE, UINT32_TYPE, UINT64_TYPE);

    final @NonNull List<GeneratedProperty> allProperties;
    final @NonNull List<GeneratedProperty> finalProperties;
    final @NonNull List<GeneratedProperty> parentProperties;
    final @NonNull List<GeneratedProperty> properties;
    final Restrictions restrictions;

    /**
     * List of enumeration which are generated as JAVA enum type.
     */
    private final @NonNull List<EnumTypeObjectArchetype> enums;
    /**
     * List of constant instances which are generated as JAVA public static final attributes.
     */
    private final @NonNull List<Constant> consts;
    private final AbstractRangeGenerator<?> rangeGenerator;
    private final @NonNull DataRootArchetype root;

    @NonNullByDefault
    ClassTemplate(final GeneratedClass javaType, final T archetype, final DataRootArchetype root) {
        super(javaType, archetype, root);
        this.root = requireNonNull(root);

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

    @NonNullByDefault
    static final BlockBuilder generateAsInner(final GeneratedClass.Nested javaType,
            final GeneratedTransferObject<?> gto, final DataRootArchetype root) {
        return switch (gto) {
            case BitsTypeObjectArchetype archetype ->
                BitsTypeObjectTemplate.generateInner(javaType, archetype, root);
            case ScalarTypeObjectArchetype archetype ->
                ScalarTypeObjectTemplate.generateInner(javaType, archetype, root);
            case UnionTypeObjectArchetype archetype ->
                UnionTypeObjectTemplate.generateInner(javaType, archetype, root);
            default -> throw new VerifyException("Unhandled inner class " + gto);
        };
    }

    /**
     * {@return string with JAVA class body source code}
     */
    final @NonNull BlockBuilder generateAsInnerClass() {
        return generateBody(true);
    }

    /**
     * Returns the list of the read only properties of all extending generated transfer object from <code>genTO</code>
     * to highest parent generated transfer object.
     *
     * @param gto generated transfer object for which is the list of read only properties generated
     * @return list of all read only properties from actual to highest parent generated transfer object. In case when
     *         extension exists the method is recursive called.
     */
    @VisibleForTesting
    @NonNullByDefault
    static final List<GeneratedProperty> propertiesOfAllParents(final GeneratedTransferObject<?> gto) {
        final var superType = gto.getSuperType();
        return superType == null ? List.of() : streamAllProperties(superType).collect(Collectors.toUnmodifiableList());
    }

    @NonNullByDefault
    private static Stream<GeneratedProperty> streamAllProperties(final GeneratedTransferObject<?> gto) {
        final var stream = gto.getProperties().stream().filter(GeneratedProperty::isReadOnly);
        final var superType = gto.getSuperType();
        return superType == null ? stream : Stream.concat(stream, streamAllProperties(superType));
    }

    @Override
    final BlockBuilder body() {
        return generateBody(false);
    }

    /**
     * {@return string with class source code in JAVA format}
     * @param isInnerClass {@code true} if generated class is an inner class
     */
    private @NonNull BlockBuilder generateBody(final boolean isInnerClass) {
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
            .blk(defaultInstance())
            .blk(propertyMethods());

        // call out to BitsTypeObjectTemplate
        appendValidNames(bb);

        final var hashCode = generateHashCode();
        if (hashCode != null) {
            bb.nl().blk(hashCode);
        }
        final var equals = generateEquals();
        if (equals != null) {
            bb.nl().blk(equals);
        }

        final var toString = generateToString();
        if (toString != null) {
            bb.nl().blk(toString);
        }
        return bb.cB().nl();
    }

    private @Nullable BlockBuilder generateEquals() {
        return properties.isEmpty() ? null : generateEquals(properties);
    }

    private @Nullable BlockBuilder generateToString() {
        return properties.isEmpty() ? null : generateToString(properties);
    }

    @NonNull String finalClass() {
        return " ";
    }

    /**
     * {@return string with class declaration in JAVA format}
     * @param isInnerClass boolean value which specify if generated class is|isn't inner
     */
    private @NonNull BlockBuilder generateClassDeclaration(final boolean isInnerClass) {
        final var archetype = archetype();

        final var bb = newBlockBuilder()
            .str("public");
        if (isInnerClass) {
            bb.str(" static final ");
        } else {
            bb.str(archetype.isAbstract() ? " abstract " : finalClass());
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

    // exposed for BitsTypeObjectTemplate
    @NonNullByDefault
    void appendValidNames(final BlockBuilder bb) {
        // no-op
    }

    // FIXME: this method should be specialized in BitsTypeObjectTemplate, as 'type bits' is an animal completely
    //        different from ScalarTypeObjects the rest of this method handles.
    @Nullable BlockBuilder defaultInstance() {
        final var archetype = archetype();
        if (!archetype.isTypedef() || allProperties.isEmpty()) {
            return null;
        }

        final var prop = allProperties.getFirst();
        final var propType = prop.getReturnType();
        if (INSTANCE_IDENTIFIER.name().equals(propType.name())) {
            return null;
        }

        final var simpleName = archetype.simpleName();
        return newBlockBuilder()
            .nl()
            .str("public static ").str(simpleName).str(" getDefaultInstance(final String defaultValue)").jBlock(bb -> {
                // FIXME: unify handling here ...
                if (VALUEOF_TYPES.contains(propType)) {
                    bb.str("return new ").str(simpleName).str("(").str(importedName(propType))
                    .eol(".valueOf(defaultValue));");
                } else if (propType instanceof Decimal64Type decimal64) {
                    bb.str("return new ").str(simpleName).str("(").str(importedName(propType))
                    .str(".valueOf(defaultValue).scaleTo(").jInt(decimal64.fractionDigits()).eol("));");
                } else if (propType.equals(STRING_TYPE)) {
                    bb.str("return new ").str(simpleName).eol("(defaultValue);");
                } else if (propType.equals(BINARY_TYPE)) {
                    bb.str("return new ").str(simpleName).str("(").str(importedName(JU_BASE64))
                    .eol(".getDecoder().decode(defaultValue));");
                } else if (propType.equals(EMPTY_TYPE)) {
                    bb.str("return new ").str(simpleName).str("(").str(importedName(CODEHELPERS))
                    .eol(".emptyFor(defaultValue));");
                } else {
                    bb.str("return new ").str(simpleName).str("(new ").str(importedName(propType))
                        .eol("(defaultValue));");
                }
            }).nl();
    }

    @Nullable BlockBuilder constructors() {
        final var bb = newBlockBuilder()
            .nl()
            .blk(defaultConstructor());
        if (!allProperties.isEmpty()) {
            bb.nl().blk(copyConstructor());
        }
        if (properties.isEmpty() && !parentProperties.isEmpty()) {
            // FIXME: nl()?
            bb.blk(parentConstructor());
        }
        return bb;
    }

    @Nullable BlockBuilder propertyMethods() {
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

    private @Nullable BlockBuilder generateHashCode() {
        return properties.isEmpty() ? null : generateHashCode(properties);
    }

    final @Nullable BlockBuilder generateRestrictions(final @NonNull Type type, final @NonNull String paramName,
            final @NonNull Type returnType) {
        final var typeRestrictions = switch (type) {
            case GeneratedTransferObject<?> gto -> gto.getRestrictions();
            case RestrictedType restricted -> restricted.restrictions();
            case null, default -> null;
        };
        if (typeRestrictions == null) {
            return null;
        }
        final var length = typeRestrictions.getLengthConstraint().orElse(null);
        final var range = typeRestrictions.getRangeConstraint().orElse(null);
        if (length == null && range == null) {
            return null;
        }

        final var checkerCalls = newBlockBuilder();
        final var paramValue = returnType instanceof ConcreteType ? paramName : paramName + ".getValue()";
        if (length != null) {
            LengthGenerator.appendCheckerCall(checkerCalls, paramName, paramValue);
        }
        if (range != null) {
            rangeGenerator.appendCheckerCall(checkerCalls, paramName, paramValue);
        }

        // FIXME: this wrapping should be specialized in ScalarTypeObjectTemplate vs. others (BitsTO, EnumTO, UnionTO)
        return paramName.equals("_value") ? checkerCalls : newBlockBuilder()
            .str("if (").str(paramName).str(" != null)").oB()
                .blk(checkerCalls)
            .cB();
    }

    @NonNull BlockBuilder defaultConstructor() {
        return newBlockBuilder()
            .str("public ").str(type().simpleName()).str("(").str(asArgumentsDeclaration(allProperties)).str(")")
            .jBlock(bb -> {
                if (!parentProperties.isEmpty()) {
                    bb.str("super(").str(asArguments(parentProperties)).eol(");");
                }
                for (var prop : allProperties) {
                    bb.blk(generateRestrictions(type(), BaseTemplate.fieldName(prop), prop.getReturnType()));
                }
                for (var prop : properties) {
                    final var fieldName = fieldName(prop);

                    if (isArrayProperty(prop)) {
                        bb.str("this.").str(fieldName).str(" = ").str(importedName(CODEHELPERS)).str(".copyArray(")
                            .str(fieldName).eol(");");
                    } else {
                        bb.str("this.").str(fieldName).str(" = ").str(fieldName).eS();
                    }
                }
            }).nl();
    }

    @NonNullByDefault
    BlockBuilder copyConstructor() {
        final var simpleName = type().simpleName();

        return newBlockBuilder().txt("""
                  /**
                   * Creates a copy from Source Object.
                   *
                   * @param source Source object
                   */
                  """)
            .str("public ").str(simpleName).str("(").str(simpleName).str(" source)").jBlock(bb -> {
                // TODO: consider splitting into a 'Block copyConstructorBody()' once we can do efficient block copies
                if (!parentProperties.isEmpty()) {
                    bb.eol("super(source);");
                }
                for (var prop : properties) {
                    final var fieldName = fieldName(prop);
                    bb.str("this.").str(fieldName).str(" = source.").str(fieldName).eS();
                }
            }).nl();
    }

    @NonNullByDefault
    final BlockBuilder parentConstructor() {
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

    final @Nullable BlockBuilder genPatternEnforcer(final @NonNull String ref) {
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
