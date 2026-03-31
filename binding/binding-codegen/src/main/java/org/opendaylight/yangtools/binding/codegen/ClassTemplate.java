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
import org.apache.commons.text.StringEscapeUtils;
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
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

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

        final var bb = new BlockBuilder();
        bb.append(wrapToDocumentation(formatDataForJavaDoc(type())));

        bb.blk(annotationDeclaration());

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
                bb.append("    private ");
                if (field.isReadOnly()) {
                    bb.append("final ");
                }
                bb.str(importedReturnType(field)).str(" ").str(fieldName(field)).eol(";");
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
            .str("public ").str(importedName(IMMUTABLE_SET)).str("<").str(importedName(STRING)).str("> validNames()")
                .oB()
            .eol("    return " + VALID_NAMES_NAME + ";")
            .cB()
            .nl()
            .at().eol(override)
            .str("public boolean[] values()").oB()
            .str("    return new boolean[] {").nl();

        boolean first = true;
        for (var bit : typedef.getBits()) {
            if (first) {
                first = false;
            } else {
                bb.eol(",");
            }
            bb.str("            ").str(getterMethodName(getPropertyName(bit.getName()))).append("()");
        }

        return bb
            .nl()
            .eol("        };")
            .cB();
    }

    private @Nullable BlockBuilder generateEquals() {
        final var equalsIdentifiers = genTO.getEqualsIdentifiers();
        if (equalsIdentifiers.isEmpty()) {
            return null;
        }

        final var bb = new BlockBuilder()
            .at().eol(importedName(OVERRIDE))
            .str("public final boolean equals(").str(importedName(OBJECT)).str(" obj) {").nl()
            .str("    return this == obj || obj instanceof ").str(type().simpleName()).str(" other");
        for (var property : equalsIdentifiers) {
            bb.nl().append("        && ");

            final var fieldName = fieldName(property);
            final var type = property.getReturnType();
            if (type.equals(PRIMITIVE_BOOLEAN)) {
                bb.str(fieldName).str(" == other.").append(fieldName);
            } else {
                bb.str(importedUtilClass(type)).str(".equals(").str(fieldName).str(", other.").str(fieldName)
                    .append(")");
            }
        }
        return bb
            .eol(";")
            .str("}").nl();
    }

    private @Nullable BlockBuilder generateToString(final List<GeneratedProperty> props) {
        if (props.isEmpty()) {
            return null;
        }

        final var bb = new BlockBuilder()
            .at().eol(importedName(OVERRIDE))
            .str("public ").str(importedName(STRING)).str(" toString()").oB()
            .str("    final var helper = ").str(importedName(MOREOBJECTS)).str(".toStringHelper(")
                .str(importedName(type())).eol(".class);");
        for (var property : props) {
            bb
                .str("    ").str(importedName(CODEHELPERS)).str(".").str(valueAppender(property)).str("(helper, \"")
                .str(property.getName()).str("\", ").str(fieldName(property)).eol(");");
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
    private StringBuilder bitsDefaultInstanceBody() {
        final var sb = new StringBuilder()
            .append("var values = ").append(importedName(CODEHELPERS)).append(".parseBitsDefaultValue(defaultValue, ");
        final var size = allProperties.size();
        if (size != 0) {
            final var it = allProperties.iterator();
            while (true) {
                final var prop = it.next();
                sb.append('"').append(prop.getName()).append('"');
                if (!it.hasNext()) {
                    break;
                }
                sb.append(",\n    ");
            }
        }

        sb
            .append(");\n")
            .append("return new ").append(genTO.simpleName()).append("(");
        if (size != 0) {
            sb.append('\n');

            final var last = size - 1;
            for (int i = 0; i < last; ++i) {
                appendValue(sb, i);
                sb.append(",\n");
            }
            appendValue(sb, last);
        }

        return sb.append(");\n");
    }

    @NonNullByDefault
    private static void appendValue(final StringBuilder sb, final int index) {
        sb.append("    values[").append(index).append(']');
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
            bb.append(" static final ");
        } else {
            bb.append(type.isAbstract() ? " abstract " : finalClass());
        }
        bb.str("class ").append(type.simpleName());

        final var superType = genTO.getSuperType();
        if (superType != null) {
            bb.append(" extends ");
            bb.append(importedName(superType));
        }

        final var ifaces = type.getImplements();
        if (!ifaces.isEmpty()) {
            bb.nl().append(" implements ");

            final var it = ifaces.iterator();
            while (true) {
                bb.append(importedName(it.next()));
                if (!it.hasNext()) {
                    break;
                }
                bb.append(", ");
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
                default -> bb.append(emitConstant(c));
            }
        }
        return bb;
    }

    private void appendPatternConstant(final BlockBuilder bb, final Map<String, String> constValue) {
        final var jurPattern = importedName(JUR_PATTERN);
        final var juList = importedName(JU_LIST);

        //    «val jurPatternRef = JUR_PATTERN.importedName»
        //    public static final «JU_LIST.importedName»<String> «TypeConstants.PATTERN_CONSTANT_NAME» =
        // «JU_LIST.importedName».of(«
        //    FOR v : cValue.keySet SEPARATOR ", "»"«v.escapeJava»"«ENDFOR»);
        //    «IF cValue.size == 1»
        //        private static final «jurPatternRef» «Constants.MEMBER_PATTERN_LIST» = «jurPatternRef».compile(
        //«TypeConstants.PATTERN_CONSTANT_NAME».getFirst());
        //        private static final String «Constants.MEMBER_REGEX_LIST» = "«cValue.values.iterator.next
        //.escapeJava»";
        //    «ELSE»
        //        private static final «jurPatternRef»[] «Constants.MEMBER_PATTERN_LIST» = «CODEHELPERS.importedName»
        //.compilePatterns(«TypeConstants.PATTERN_CONSTANT_NAME»);
        //        private static final String[] «Constants.MEMBER_REGEX_LIST» = { «
        //        FOR v : cValue.values SEPARATOR ", "»"«v.escapeJava»"«ENDFOR» };
        //    «ENDIF»
        bb.str("public static final ").str(juList).str("<String> " + PATTERN_CONSTANT_NAME + " = ").str(juList)
            .append(".of(");
        {
            boolean first = true;
            for (var value : constValue.keySet()) {
                if (first) {
                    first = false;
                } else {
                    bb.append(", ");
                }
                bb.str("\"").append(StringEscapeUtils.escapeJava(value));
                bb.append("\"");
            }
        }
        bb
            .eol(");")
            .str("private static final ").append(jurPattern);
        if (constValue.size() == 1) {
            bb
                .str(" " + MEMBER_PATTERN_LIST + " = ").str(jurPattern)
                .eol(".compile(" + PATTERN_CONSTANT_NAME + ".getFirst());")
                .str("private static final String " + MEMBER_REGEX_LIST + " = \"")
                    .append(StringEscapeUtils.escapeJava(constValue.values().iterator().next()));
            bb.eol("\";");
            return;
        }

        bb
            .str("[] " + MEMBER_PATTERN_LIST + " = ").str(importedName(CODEHELPERS))
                .eol(".compilePatterns(" + PATTERN_CONSTANT_NAME + ");")
            .str("private static final String[] " + MEMBER_REGEX_LIST).append(" = { ");
        {
            boolean first = true;
            for (var value : constValue.values()) {
                if (first) {
                    first = false;
                } else {
                    bb.append(", ");
                }
                bb.str("\"").append(StringEscapeUtils.escapeJava(value));
                bb.append("\"");
            }
        }
        bb.eol(" };");
    }

    private void appendValidNames(final BlockBuilder bb, final BitsTypeDefinition bitsType) {
        final var immutableSet = importedName(IMMUTABLE_SET);
        bb.str("protected static final ").str(immutableSet).str("<").str(importedName(STRING))
            .str("> " + VALID_NAMES_NAME + " = ").str(immutableSet).append(".of(");
        {
            boolean first = true;
            for (var bit : bitsType.getBits()) {
                if (first) {
                    first = false;
                } else {
                    bb.append(", ");
                }
                bb.str("\"").str(bit.getName()).append("\"");
            }
        }
        bb.eol(");")
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
            bb.nl().append(copyConstructor());
        }
        if (properties.isEmpty() && !parentProperties.isEmpty()) {
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
                .str("public ").str(importedReturnType(field))
                    .str(' ' + SCALAR_TYPE_OBJECT_GET_VALUE_NAME + "() {").nl()
                .str("    return ").str(fieldName(field)).str(cloneCall(field)).eol(";")
                .str("}").nl();
        }

        final var bb = new BlockBuilder();
        final var it = properties.iterator();
        do {
            final var field = it.next();
            bb.nl().append(asGetterMethod(field));
            if (!field.isReadOnly()) {
                bb.nl().append(asSetterMethod(field));
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
            bb.append("    return ");
            final var prop = props.getFirst();
            if (PRIMITIVE_BOOLEAN.equals(prop.getReturnType())) {
                bb.str(importedName(BOOLEAN)).append(".hashCode(");
            } else {
                bb.str(importedName(CODEHELPERS)).append(".wrapperHashCode(");
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

    final @Nullable StringBuilder generateRestrictions(final @NonNull Type type, final @NonNull String paramName,
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

        final var sb = new StringBuilder();
        if (!paramName.equals("_value")) {
            sb.append("if (").append(paramName).append(" != null) {\n");
            appendCheckerCalls(sb, "    ", paramName, returnType, length, range);
            sb.append("}\n");
        } else {
            appendCheckerCalls(sb, "", paramName, returnType, length, range);
        }
        return sb;
    }

    @NonNullByDefault
    private void appendCheckerCalls(final StringBuilder sb, final String indent, final String paramName,
            final Type returnType, final @Nullable LengthConstraint length, final @Nullable RangeConstraint<?> range) {
        final var paramValue = returnType instanceof ConcreteType ? paramName : paramName + ".getValue()";
        // Note: at least one of these is non-null
        if (length != null) {
            LengthGenerator.appendCheckerCall(sb.append(indent), paramName, paramValue);
        }
        if (range != null) {
            rangeGenerator.appendCheckerCall(sb.append(indent), paramName, paramValue);
        }
    }

    @NonNull BlockBuilder allValuesConstructor() {
        final var bb = new BlockBuilder()
            .str("public ").str(type().simpleName()).str("(").str(asArgumentsDeclaration(allProperties)).str(") {")
                .nl();
        if (!parentProperties.isEmpty()) {
            bb.str("    super(").str(asArguments(parentProperties)).eol(");");
        }
        for (var prop : allProperties) {
            bb.indented(generateRestrictions(type(), BaseTemplate.fieldName(prop), prop.getReturnType()));
        }
        for (var prop : properties) {
            final var fieldName = fieldName(prop);

            if (prop.getReturnType().simpleName().endsWith("[]")) {
                bb.str("    this.").str(fieldName).str(" = ").str(importedName(CODEHELPERS)).str(".copyArray(")
                    .str(fieldName).eol(");");
            } else {
                bb.str("    this.").str(fieldName).str(" = ").str(fieldName).eol(";");
            }
        }
        return bb.eol("}");
    }

    // FIXME: return BlockBuilder
    StringBuilder copyConstructor() {
        final var simpleName = type().simpleName();

        final var sb = new StringBuilder()
            .append("/**\n")
            .append(" * Creates a copy from Source Object.\n")
            .append(" *\n")
            .append(" * @param source Source object\n")
            .append(" */\n")
            .append("public ").append(simpleName).append("(").append(simpleName).append(" source) {\n");
        if (!parentProperties.isEmpty()) {
            sb.append("    super(source);\n");
        }
        for (var prop : properties) {
            final var fieldName = fieldName(prop);
            sb.append("    this.").append(fieldName).append(" = source.").append(fieldName).append(";\n");
        }
        return sb.append("}\n");
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
            .str("public ").str(type().simpleName()).str("(").str(importedSuper).str(" source) {").nl()
            .eol("    super(source);")
            .indented(genPatternEnforcer("getValue()"))
            .eol("}");
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
            .at().str(importedName(CONSTRUCTOR_PARAMETERS)).str("(\"").str(VALUE_PROP).eol("\")")
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
                .append(fieldName);
            if (value.getReturnType() instanceof Decimal64Type decimal64) {
                bb.str(", ").iStr(decimal64.fractionDigits());
            }
            bb.str(")").append(cloneCall(value));
            bb.eol(";");
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

    private @Nullable StringBuilder genPatternEnforcer(final @NonNull String ref) {
        final var sb = new StringBuilder();
        for (var constant : consts) {
            if (PATTERN_CONSTANT_NAME.equals(constant.getName())) {
                sb.append(importedName(CODEHELPERS)).append(".checkPattern(").append(ref).append(", ")
                    .append(MEMBER_PATTERN_LIST).append(", ").append(MEMBER_REGEX_LIST).append(");\n");
            }
        }
        return sb.isEmpty() ? null : sb;
    }
}
