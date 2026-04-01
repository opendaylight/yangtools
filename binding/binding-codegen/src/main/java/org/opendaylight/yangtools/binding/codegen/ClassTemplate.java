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
import static org.opendaylight.yangtools.binding.contract.Naming.SCALAR_TYPE_OBJECT_GET_VALUE_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.getPropertyName;
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
import static org.opendaylight.yangtools.binding.model.ri.BindingTypes.BITS_TYPE_OBJECT;
import static org.opendaylight.yangtools.binding.model.ri.BindingTypes.SCALAR_TYPE_OBJECT;
import static org.opendaylight.yangtools.binding.model.ri.TypeConstants.PATTERN_CONSTANT_NAME;
import static org.opendaylight.yangtools.binding.model.ri.TypeConstants.VALID_NAMES_NAME;
import static org.opendaylight.yangtools.binding.model.ri.TypeConstants.VALUE_PROP;
import static org.opendaylight.yangtools.binding.model.ri.Types.PRIMITIVE_BOOLEAN;
import static org.opendaylight.yangtools.binding.model.ri.Types.STRING;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.Constant;
import org.opendaylight.yangtools.binding.model.api.Decimal64Type;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.RestrictedType;
import org.opendaylight.yangtools.binding.model.api.Restrictions;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;

/**
- * Template for generating JAVA class.
 */
class ClassTemplate extends BaseTemplate {
    private static final Comparator<GeneratedProperty> PROP_COMPARATOR =
        Comparator.comparing(GeneratedProperty::getName);

    private static final Set<ConcreteType> VALUEOF_TYPES = Set.of(
        BOOLEAN_TYPE, INT8_TYPE, INT16_TYPE, INT32_TYPE, INT64_TYPE, UINT8_TYPE, UINT16_TYPE, UINT32_TYPE, UINT64_TYPE);

    /**
     * {@code java.lang.Boolean} as a JavaTypeName.
     */
    private static final @NonNull JavaTypeName BOOLEAN = JavaTypeName.create(Boolean.class);
    /**
     * {@code com.google.common.collect.ImmutableSet} as a JavaTypeName.
     */
    private static final @NonNull JavaTypeName IMMUTABLE_SET = JavaTypeName.create(ImmutableSet.class);

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
    private final @NonNull GeneratedTransferObject genTO;
    private final AbstractRangeGenerator<?> rangeGenerator;

    @NonNullByDefault
    ClassTemplate(final GeneratedTransferObject genType) {
        this(new TopLevelJavaGeneratedType(genType), genType);
    }

    @NonNullByDefault
    ClassTemplate(final AbstractJavaGeneratedType javaType, final GeneratedTransferObject genType) {
        super(javaType, genType);
        genTO = requireNonNull(genType);
        properties = genTO.getProperties();
        finalProperties = properties.stream()
            .filter(GeneratedProperty::isReadOnly)
            .collect(Collectors.toUnmodifiableList());
        parentProperties = propertiesOfAllParents(genTO);
        restrictions = genTO.getRestrictions();

        allProperties = Stream.concat(properties.stream(), parentProperties.stream())
            .sorted(PROP_COMPARATOR)
            .collect(Collectors.toUnmodifiableList());

        enums = genType.getEnumerations();
        consts = genType.getConstantDefinitions();
        rangeGenerator = restrictions != null && restrictions.getRangeConstraint().isPresent()
            ? requireNonNull(AbstractRangeGenerator.forType(TypeUtils.encapsulatedValueType(genType)))
                : null;
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
    static final @NonNull List<GeneratedProperty> propertiesOfAllParents(final @NonNull GeneratedTransferObject gto) {
        final var superType = gto.getSuperType();
        return superType == null ? List.of() : streamAllProperties(superType).collect(Collectors.toUnmodifiableList());
    }

    private static Stream<GeneratedProperty> streamAllProperties(final @NonNull GeneratedTransferObject gto) {
        final var stream = gto.getProperties().stream().filter(GeneratedProperty::isReadOnly);
        final var superType = gto.getSuperType();
        return superType == null ? stream : Stream.concat(stream, streamAllProperties(superType));
    }

    @Override
    final BlockBuilder body() {
        return generateBody(false);
    }

    /**
     * {@return string with JAVA class body source code}
     */
    final @NonNull BlockBuilder generateAsInnerClass() {
        return generateBody(true);
    }

    /**
     * {@return string with class source code in JAVA format}
     * @param isInnerClass {@code true} if generated class is an inner class
     */
    private @NonNull BlockBuilder generateBody(final boolean isInnerClass) {
        //        «type.formatDataForJavaDoc.wrapToDocumentation»
        //        «annotationDeclaration»
        //        «IF !isInnerClass»
        //            «generatedAnnotation»
        //        «ENDIF»
        //        «generateClassDeclaration(isInnerClass)» {
        //            «suidDeclaration»
        //            «generateInnerClasses(type.enclosedTypes)»
        //            «generateInnerEnumTypeObjects(enums)»
        //            «constantsDeclarations»
        //            «generateFields»
        //
        //            «IF restrictions !== null»
        //                «IF restrictions.lengthConstraint.present»
        //                    «LengthGenerator.generateLengthChecker("_value", TypeUtils.encapsulatedValueType(genTO),
        //                        restrictions.lengthConstraint.orElseThrow, this)»
        //                «ENDIF»
        //                «IF restrictions.rangeConstraint.present»
        //                    «rangeGenerator.generateRangeChecker("_value", restrictions.rangeConstraint.orElseThrow,
        // this)»
        //                «ENDIF»
        //            «ENDIF»
        //
        //            «constructors»
        //
        //            «defaultInstance»
        //
        //            «propertyMethods»
        //
        //            «IF isBitsTypeObject»
        //                «validNamesAndValues»
        //            «ENDIF»
        //
        //            «generateHashCode»
        //
        //            «generateEquals»
        //
        //            «generateToString(genTO.toStringIdentifiers)»
        //        }

        final var bb = new BlockBuilder()
            .blk(wrapToDocumentation(formatDataForJavaDoc(type())))
            .blk(annotationDeclaration());

        if (!isInnerClass) {
            bb.eol(generatedAnnotation());
        }
        bb.blk(generateClassDeclaration(isInnerClass)).oB();

        // serialVersionUID
        final var suid = genTO.getSUID();
        if (suid != null) {
            bb
                .eol("    @java.io.Serial")
                .str("    private static final long serialVersionUID = ").str(suid.getValue()).eol("L;");
        }

        bb
            // inner classes
            .indented(generateInnerClasses(type().getEnclosedTypes()))
            // inner EnumTypeObjects
            .indented(generateInnerEnumTypeObjects(enums))
            // constants
            .indented(constantsDeclarations());

        // fields
        if (!properties.isEmpty()) {
            for (var field : properties) {
                bb.str("    private ");
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
                bb.nl().indented(
                    LengthGenerator.generateLengthChecker("_value", TypeUtils.encapsulatedValueType(genTO),
                        length.orElseThrow(), this));
            }
            final var range = restrictions.getRangeConstraint();
            if (range.isPresent()) {
                bb.nl().indented(rangeGenerator.generateRangeChecker("_value", range.orElseThrow(), this));
            }
        }

        bb
            .indented(constructors())
            .indented(defaultInstance())
            .indented(propertyMethods());

        if (isBitsTypeObject()) {
            for (var c : consts) {
                if (VALID_NAMES_NAME.equals(c.getName())) {
                    bb.nl().indented(validNamesAndValues((BitsTypeDefinition) c.getValue()));
                }
            }
        }

        final var hashCode = generateHashCode();
        if (hashCode != null) {
            bb.nl().indented(hashCode);
        }
        final var equals = generateEquals();
        if (equals != null) {
            bb.nl().indented(equals);
        }

        final var toString = generateToString(genTO.getToStringIdentifiers());
        if (toString != null) {
            bb.nl().indented(toString);
        }
        return bb.cB().nl();
    }

    private boolean isBitsTypeObject() {
        GeneratedTransferObject wlk = genTO;
        do {
            for (var impl : wlk.getImplements()) {
                if (BITS_TYPE_OBJECT.name().equals(impl.name())) {
                    return true;
                }
            }
            wlk = wlk.getSuperType();
        } while (wlk != null);
        return false;
    }

    @NonNullByDefault
    private BlockBuilder validNamesAndValues(final BitsTypeDefinition typedef) {
        final var override = importedName(OVERRIDE);

        final var bb = new BlockBuilder()
            .nl()
            .at().eol(override)
            .str("public ").gen(importedName(IMMUTABLE_SET), importedName(STRING)).str(" validNames()").oB()
                .ind("return " + VALID_NAMES_NAME + ";").nl()
            .cB()
            .nl()
            .at().eol(override)
            .str("public boolean[] values()").oB()
                .ind("return new boolean[]").oB();
        appendBooleanValues(bb, typedef);
        return bb
            .str("        ").cS()
            .cB();
    }

    @NonNullByDefault
    private static void appendBooleanValues(final BlockBuilder bb, final BitsTypeDefinition typedef) {
        final var bits = typedef.getBits();
        if (bits.isEmpty()) {
            bb.eol("        // empty");
            return;
        }

        final var it = bits.iterator();
        while (true) {
            final var bit = it.next();
            bb.str("            ").str(getterMethodName(getPropertyName(bit.getName()))).str("()");
            if (!it.hasNext()) {
                bb.newLine();
                break;
            }
            bb.eol(",");
        }
    }

    private @Nullable BlockBuilder generateEquals() {
        final var equalsIdentifiers = genTO.getEqualsIdentifiers();
        if (equalsIdentifiers.isEmpty()) {
            return null;
        }

        final var bb = new BlockBuilder()
            .at().eol(importedName(OVERRIDE))
            .str("public final boolean equals(").str(importedName(OBJECT)).str(" obj)").oB()
            .str("    return this == obj || obj instanceof ").str(type().simpleName()).str(" other");
        for (var property : equalsIdentifiers) {
            bb.nl().str("        && ");

            final var fieldName = fieldName(property);
            final var type = property.getReturnType();
            if (type.equals(PRIMITIVE_BOOLEAN)) {
                bb.str(fieldName).str(" == other.").str(fieldName);
            } else {
                bb.str(importedUtilClass(type)).str(".equals(").str(fieldName).str(", other.").str(fieldName).str(")");
            }
        }
        return bb
            .eS()
            .cB();
    }

    private @Nullable BlockBuilder generateToString(final List<GeneratedProperty> props) {
        if (props.isEmpty()) {
            return null;
        }

        final var bb = new BlockBuilder()
            .at().eol(importedName(OVERRIDE))
            .str("public ").str(importedName(STRING)).str(" toString()").oB()
                .ind("final var helper = ").str(importedName(MOREOBJECTS)).str(".toStringHelper(")
                    .str(importedName(type())).eol(".class);");
        for (var property : props) {
            bb
                .str("    ").str(importedName(CODEHELPERS)).str(".").str(valueAppender(property)).str("(helper, ")
                .quoted(property.getName()).str(", ").str(fieldName(property)).eol(");");
        }
        return bb
            .eol("    return helper.toString();")
            .cB();
    }

    // FIXME: this should be specialized in BitsTypeObjectTemplate
    private static String valueAppender(final GeneratedProperty prop) {
        return PRIMITIVE_BOOLEAN.equals(prop.getReturnType()) ? "appendBit" : "appendValue";
    }

    // FIXME: this method should live in (the now non-existent) BitsTypeObjectTemplate
    private BlockBuilder bitsDefaultInstanceBody() {
        final var bb = new BlockBuilder()
            .str("var values = ").str(importedName(CODEHELPERS)).str(".parseBitsDefaultValue(defaultValue, ");
        final var size = allProperties.size();
        if (size != 0) {
            final var it = allProperties.iterator();
            while (true) {
                final var prop = it.next();
                bb.quoted(prop.getName());
                if (!it.hasNext()) {
                    break;
                }
                bb.eol(",").ind();
            }
        }

        bb
            .eol(");")
            .str("return new ").str(genTO.simpleName()).str("(");
        if (size != 0) {
            bb.newLine();

            final var last = size - 1;
            for (int i = 0; i < last; ++i) {
                appendValue(bb, i);
                bb.eol(",");
            }
            appendValue(bb, last);
        }

        return bb.eol(");");
    }

    @NonNullByDefault
    private static void appendValue(final BlockBuilder bb, final int index) {
        bb.ind("values[").iStr(index).str("]");
    }

    @NonNull String finalClass() {
        return " ";
    }

    private @Nullable BlockBuilder annotationDeclaration() {
        final var annotations = genTO.getAnnotations();
        if (annotations.isEmpty()) {
            return null;
        }

        final var bb = new BlockBuilder();
        for (var annotation : annotations) {
            bb.at().eol(annotation.simpleName());
        }
        return bb;
    }

    /**
     * {@return string with class declaration in JAVA format}
     * @param isInnerClass boolean value which specify if generated class is|isn't inner
     */
    @Nullable BlockBuilder generateClassDeclaration(final boolean isInnerClass) {
        final var type = type();

        final var bb = new BlockBuilder()
            .str("public");
        if (isInnerClass) {
            bb.str(" static final ");
        } else {
            bb.str(type.isAbstract() ? " abstract " : finalClass());
        }
        bb.str("class ").str(type.simpleName());

        final var superType = genTO.getSuperType();
        if (superType != null) {
            bb.str(" extends ").str(importedName(superType));
        }

        final var ifaces = type.getImplements();
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

        final var bb = new BlockBuilder();
        for (var c : consts) {
            switch (c.getName()) {
                case PATTERN_CONSTANT_NAME -> appendPatternConstant(bb, (Map<String, String>) c.getValue());
                case VALID_NAMES_NAME -> appendValidNames(bb, (BitsTypeDefinition) c.getValue());
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
                bb.quotedJava(requireNonNull(value));
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
                    .quotedJava(constValue.values().iterator().next()).eS();
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
                bb.quotedJava(requireNonNull(value));
            }
        }
        bb.eol(" };");
    }

    private void appendValidNames(final BlockBuilder bb, final BitsTypeDefinition bitsType) {
        final var immutableSet = importedName(IMMUTABLE_SET);
        bb.str("protected static final ").gen(immutableSet, importedName(STRING)).str(" " + VALID_NAMES_NAME + " = ")
            .str(immutableSet).str(".of(");
        // FIXME: refactor this block
        {
            boolean first = true;
            for (var bit : bitsType.getBits()) {
                if (first) {
                    first = false;
                } else {
                    bb.str(", ");
                }
                bb.quoted(bit.getName());
            }
        }
        bb.eol(");");
    }

    // FIXME: this method should be specialized in BitsTypeObjectTemplate, as 'type bits' is an animal completely
    //        different from ScalarTypeObjects the rest of this method handles.
    @Nullable BlockBuilder defaultInstance() {
        if (!genTO.isTypedef() || allProperties.isEmpty()) {
            return null;
        }

        final var prop = allProperties.getFirst();
        final var propType = prop.getReturnType();
        if (INSTANCE_IDENTIFIER.name().equals(propType.name())) {
            return null;
        }

        final var simpleName = genTO.simpleName();
        final var bb = new BlockBuilder()
            .nl()
            .str("public static ").str(simpleName).str(" getDefaultInstance(final String defaultValue)").oB();
        // FIXME: unify handling here ...
        if (VALUEOF_TYPES.contains(propType)) {
            bb.str("    return new ").str(simpleName).str("(").str(importedName(propType))
                .eol(".valueOf(defaultValue));");
        } else if (propType.equals(PRIMITIVE_BOOLEAN)) {
            // ... this case is different from all others: is this for type=bits?
            bb.indented(bitsDefaultInstanceBody());
        } else if (propType instanceof Decimal64Type decimal64) {
            bb.str("    return new ").str(simpleName).str("(").str(importedName(propType))
                .str(".valueOf(defaultValue).scaleTo(").iStr(decimal64.fractionDigits()).eol("));");
        } else if (propType.equals(STRING_TYPE)) {
            bb.str("    return new ").str(simpleName).eol("(defaultValue);");
        } else if (propType.equals(BINARY_TYPE)) {
            bb.str("    return new ").str(simpleName).str("(").str(importedName(JU_BASE64))
                .eol(".getDecoder().decode(defaultValue));");
        } else if (propType.equals(EMPTY_TYPE)) {
            bb.str("    return new ").str(simpleName).str("(").str(importedName(CODEHELPERS))
                .eol(".emptyFor(defaultValue));");
        } else {
            bb.str("    return new ").str(simpleName).str("(new ").str(importedName(propType)).eol("(defaultValue));");
        }
        return bb.cB();
    }

    @Nullable BlockBuilder constructors() {
        final var bb = new BlockBuilder()
            .nl();
        if (genTO.isTypedef() && allProperties.size() == 1 && VALUE_PROP.equals(allProperties.getFirst().getName())) {
            bb.blk(typedefConstructor());
        } else {
            bb.blk(allValuesConstructor());
        }
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
        if (genTO.getImplements().stream().anyMatch(ifc -> SCALAR_TYPE_OBJECT.name().equals(ifc.name()))) {
            final var field = properties.getFirst();
            return new BlockBuilder()
                .nl()
                .at().eol(importedName(OVERRIDE))
                .str("public ").str(importedReturnType(field)).str(' ' + SCALAR_TYPE_OBJECT_GET_VALUE_NAME + "()").oB()
                    .ind("return ").str(fieldName(field), cloneOrNull(field)).eS()
                .cB();
        }

        final var bb = new BlockBuilder();
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
        final var props = genTO.getHashCodeIdentifiers();
        final int size = props.size();
        if (size == 0) {
            return null;
        }

        //      @«OVERRIDE.importedName»
        //      public int hashCode() {
        //          «IF size != 1»
        //              final int prime = 31;
        //              int result = 1;
        //              «FOR property : props»
        //                  result = prime * result + «property.importedHashCodeUtilClass».hashCode(
        //«property.fieldName»);
        //              «ENDFOR»
        //              return result;
        //          «ELSE»
        //              «val prop = props.first»
        //              «IF prop.returnType.equals(Types.primitiveBooleanType())»
        //                  return «BOOLEAN.importedName».hashCode(«prop.fieldName»);
        //              «ELSE»
        //                  return «CODEHELPERS.importedName».wrapperHashCode(«prop.fieldName»);
        //              «ENDIF»
        //          «ENDIF»
        //      }
        final var bb = new BlockBuilder()
            .at().eol(importedName(OVERRIDE))
            .str("public int hashCode()").oB();
        if (size == 1) {
            bb.str("    return ");
            final var prop = props.getFirst();
            if (PRIMITIVE_BOOLEAN.equals(prop.getReturnType())) {
                bb.str(importedName(BOOLEAN)).str(".hashCode(");
            } else {
                bb.str(importedName(CODEHELPERS)).str(".wrapperHashCode(");
            }
            bb.str(fieldName(prop)).eol(");");
        } else {
            bb
                .eol("    final int prime = 31;")
                .eol("    int result = 1;");
            for (var property : props) {
                final var type = property.getReturnType();
                bb
                    .str("    result = prime * result + ")
                        .str(type.equals(PRIMITIVE_BOOLEAN) ? importedName(BOOLEAN) : importedUtilClass(type))
                        .str(".hashCode(").str(fieldName(property)).eol(");");
            }
            bb.eol("    return result;");
        }
        return bb.cB();
    }

    final @Nullable BlockBuilder generateRestrictions(final @NonNull Type type, final @NonNull String paramName,
            final @NonNull Type returnType) {
        final var typeRestrictions = switch (type) {
            case GeneratedTransferObject gto -> gto.getRestrictions();
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

        final var checkerCalls = new BlockBuilder();
        final var paramValue = returnType instanceof ConcreteType ? paramName : paramName + ".getValue()";
        if (length != null) {
            LengthGenerator.appendCheckerCall(checkerCalls, paramName, paramValue);
        }
        if (range != null) {
            rangeGenerator.appendCheckerCall(checkerCalls, paramName, paramValue);
        }

        // FIXME: this wrapping should be specialized in ScalarTypeObjectTemplate vs. others (BitsTO, EnumTO, UnionTO)
        return paramName.equals("_value") ? checkerCalls : new BlockBuilder()
            .str("if (").str(paramName).str(" != null)").oB()
                .indented(checkerCalls)
                .cB();
    }

    @NonNull BlockBuilder allValuesConstructor() {
        final var bb = new BlockBuilder()
            .str("public ").str(type().simpleName()).str("(").str(asArgumentsDeclaration(allProperties)).str(")").oB();
        if (!parentProperties.isEmpty()) {
            bb.str("    super(").str(asArguments(parentProperties)).eol(");");
        }
        for (var prop : allProperties) {
            bb.indented(generateRestrictions(type(), BaseTemplate.fieldName(prop), prop.getReturnType()));
        }
        for (var prop : properties) {
            final var fieldName = fieldName(prop);

            if (isArrayProperty(prop)) {
                bb.str("    this.").str(fieldName).str(" = ").str(importedName(CODEHELPERS)).str(".copyArray(")
                    .str(fieldName).eol(");");
            } else {
                bb.str("    this.").str(fieldName).str(" = ").str(fieldName).eS();
            }
        }
        return bb.cB();
    }

    @NonNullByDefault
    BlockBuilder copyConstructor() {
        final var simpleName = type().simpleName();

        final var bb = new BlockBuilder().txt("""
                  /**
                   * Creates a copy from Source Object.
                   *
                   * @param source Source object
                   */
                  """)
            .str("public ").str(simpleName).str("(").str(simpleName).str(" source)").oB();
        // TODO: consider splitting into a 'Block copyConstructorBody()' once we can do efficient block copies
        if (!parentProperties.isEmpty()) {
            bb.eol("    super(source);");
        }
        for (var prop : properties) {
            final var fieldName = fieldName(prop);
            bb.str("    this.").str(fieldName).str(" = source.").str(fieldName).eS();
        }
        return bb.cB();
    }

    @NonNullByDefault
    final BlockBuilder parentConstructor() {
        final var importedSuper = importedName(genTO.getSuperType());

        return new BlockBuilder()
            .eol("/**")
            .str(" * Creates a new instance from ").eol(importedSuper)
            .eol(" *")
            .eol(" * @param source Source object")
            .eol(" */")
            .str("public ").str(type().simpleName()).str("(").str(importedSuper).str(" source)").oB()
            .eol("    super(source);")
            .indented(genPatternEnforcer("getValue()"))
            .cB();
    }

    private BlockBuilder typedefConstructor() {
        //        @«CONSTRUCTOR_PARAMETERS.importedName»("«TypeConstants.VALUE_PROP»")
        //        public «type.simpleName»(«allProperties.asArgumentsDeclaration») {
        //            «IF !parentProperties.empty»
        //                super(«parentProperties.asArguments»);
        //            «ENDIF»
        //            «val value = Verify.verifyNotNull(allProperties.valueProperty)»
        //            «val fieldName = value.fieldName»
        //            «IF properties.valueProperty !== null»
        //                this.«fieldName» = «CODEHELPERS.importedName».requireValue(«fieldName»«value.assignFieldTail»)
        //«value.cloneCall»;
        //            «ENDIF»
        //            «generateRestrictions(type, fieldName, value.returnType)»
        //            «/*
        //             * If we have patterns, we need to apply them to the value field. This is a sad consequence of how
        //this code is
        //             * structured.
        //             */»
        //            «genPatternEnforcer(fieldName)»
        //        }

        final var bb = new BlockBuilder()
            .at().str(importedName(CONSTRUCTOR_PARAMETERS)).str("(").quoted(VALUE_PROP).eol(")")
            .str("public ").str(type().simpleName()).str("(").str(asArgumentsDeclaration(allProperties)).str(")").oB();
        if (!parentProperties.isEmpty()) {
            bb.str("    super(").str(asArguments(parentProperties)).eol(");");
        }

        final var value = valueProperty(allProperties);
        if (value == null) {
            throw new VerifyException("missing value property");
        }

        final var fieldName = fieldName(value);
        if (valueProperty(properties) != null) {
            bb.str("    this.").str(fieldName).str(" = ").str(importedName(CODEHELPERS)).str(".requireValue(")
                .str(fieldName);
            if (value.getReturnType() instanceof Decimal64Type decimal64) {
                bb.str(", ").iStr(decimal64.fractionDigits());
            }
            bb.str(")", cloneOrNull(value)).eS();
        }
        return bb
            .indented(generateRestrictions(type(), fieldName, value.getReturnType()))
            .nl()
            .indented(genPatternEnforcer(fieldName))
            .cB();
    }

    private static @Nullable GeneratedProperty valueProperty(final List<GeneratedProperty> props) {
        return switch (props.size()) {
            case 0 -> null;
            case 1 -> {
                final var prop = props.getFirst();
                if (!VALUE_PROP.equals(prop.getName())) {
                    throw new VerifyException("Unexpected property " + prop);
                }
                yield prop;
            }
            default -> throw new VerifyException("Unexpected properties " + props);
        };
    }

    private @NonNull BlockBuilder genPatternEnforcer(final @NonNull String ref) {
        final var bb = new BlockBuilder();
        for (var constant : consts) {
            if (PATTERN_CONSTANT_NAME.equals(constant.getName())) {
                bb.str(importedName(CODEHELPERS)).str(".checkPattern(").str(ref).str(", ")
                    .eol(MEMBER_PATTERN_LIST + ", " + MEMBER_REGEX_LIST + ");");
            }
        }
        return bb;
    }
}
