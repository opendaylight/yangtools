/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

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
import org.opendaylight.yangtools.binding.contract.Naming;
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
import org.opendaylight.yangtools.binding.model.ri.BaseYangTypes;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.binding.model.ri.TypeConstants;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

/**
- * Template for generating JAVA class.
 */
class ClassTemplate extends BaseTemplate {
    private static final Comparator<GeneratedProperty> PROP_COMPARATOR =
        Comparator.comparing(GeneratedProperty::getName);

    private static final Set<ConcreteType> VALUEOF_TYPES = Set.<ConcreteType>of(
        BaseYangTypes.BOOLEAN_TYPE,
        BaseYangTypes.INT8_TYPE,
        BaseYangTypes.INT16_TYPE,
        BaseYangTypes.INT32_TYPE,
        BaseYangTypes.INT64_TYPE,
        BaseYangTypes.UINT8_TYPE,
        BaseYangTypes.UINT16_TYPE,
        BaseYangTypes.UINT32_TYPE,
        BaseYangTypes.UINT64_TYPE);

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
    final CharSequence body() {
        return generateBody(false);
    }

    /**
     * {@return string with JAVA class body source code}
     */
    final CharSequence generateAsInnerClass() {
        return generateBody(true);
    }

    /**
     * {@return string with class source code in JAVA format}
     * @param isInnerClass {@code true} if generated class is an inner class
     */
    private BlockBuilder generateBody(final boolean isInnerClass) {
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
        bb.newLineIfNotEmpty();

        final var annotation = annotationDeclaration();
        if (!annotation.isEmpty()) {
            bb.append(annotation);
        }

        if (!isInnerClass) {
            bb.append(generatedAnnotation());
            bb.newLine();
        }
        bb.append(generateClassDeclaration(isInnerClass));
        bb.append(" {\n");

        // serialVersionUID
        final var suid = genTO.getSUID();
        if (suid != null) {
            bb.append("    @java.io.Serial\n");
            bb.append("    private static final long serialVersionUID = ");
            bb.append(suid.getValue());
            bb.append("L;\n");
        }

        // inner classes
        final var innerClasses = generateInnerClasses(type().getEnclosedTypes());
        if (!innerClasses.isEmpty()) {
            bb.append("    ");
            bb.append(innerClasses, "    ");
            bb.newLineIfNotEmpty();
        }

        // inner EnumTypeObjects
        final var innerEnumTypeObjects = generateInnerEnumTypeObjects(enums);
        if (!innerEnumTypeObjects.isEmpty()) {
            bb.append("    ");
            bb.append(innerEnumTypeObjects, "    ");
            bb.newLineIfNotEmpty();
        }

        // constants
        final var constants = constantsDeclarations();
        if (!constants.isEmpty()) {
            bb.append("    ");
            bb.append(constants, "    ");
            bb.newLineIfNotEmpty();
        }

        // fields
        if (!properties.isEmpty()) {
            for (var field : properties) {
                bb.append("    private ");
                if (field.isReadOnly()) {
                    bb.append("final ");
                }
                bb.append(importedReturnType(field));
                bb.append(" ");
                bb.append(fieldName(field));
                bb.append(";\n");
            }
        }

        // length/range checkes
        if (restrictions != null) {
            final var length = restrictions.getLengthConstraint();
            if (length.isPresent()) {
                bb.nl().append("    ");
                bb.append(LengthGenerator.generateLengthChecker("_value", TypeUtils.encapsulatedValueType(genTO),
                    length.orElseThrow(), this), "    ");
                bb.newLineIfNotEmpty();
            }
            final var range = restrictions.getRangeConstraint();
            if (range.isPresent()) {
                bb.nl().append("    ");
                bb.append(rangeGenerator.generateRangeChecker("_value", range.orElseThrow(), this), "    ");
                bb.newLineIfNotEmpty();
            }
        }

        bb.nl().append("    ");
        bb.append(constructors(), "    ");
        bb.newLineIfNotEmpty();

        final var defaultInstance = defaultInstance();
        if (!defaultInstance.isEmpty()) {
            bb.nl().append("    ");
            bb.append(defaultInstance, "    ");
            bb.newLineIfNotEmpty();
        }

        final var propertyMethods = propertyMethods();
        if (!propertyMethods.isEmpty()) {
            bb.nl().append("    ");
            bb.append(propertyMethods, "    ");
            bb.newLineIfNotEmpty();
        }

        if (isBitsTypeObject()) {
            for (var c : consts) {
                if (TypeConstants.VALID_NAMES_NAME.equals(c.getName())) {
                    bb.nl().appendIndented(validNamesAndValues((BitsTypeDefinition) c.getValue())).newLineIfNotEmpty();
                }
            }
        }

        final var hashCode = generateHashCode();
        if (!hashCode.isEmpty()) {
            bb.nl().append("    ");
            bb.append(hashCode, "    ");
            bb.newLineIfNotEmpty();
        }

        final var equals = generateEquals();
        if (!equals.isEmpty()) {
            bb.nl().append("    ");
            bb.append(equals, "    ");
            bb.newLineIfNotEmpty();
        }

        final var toString = generateToString(genTO.getToStringIdentifiers());
        if (!toString.isEmpty()) {
            bb.nl().append("    ");
            bb.append(toString, "    ");
            bb.newLineIfNotEmpty();
        }
        bb.append("}\n");
        return bb.nl();
    }

    private boolean isBitsTypeObject() {
        GeneratedTransferObject wlk = genTO;
        do {
            for (var impl : wlk.getImplements()) {
                if (BindingTypes.BITS_TYPE_OBJECT.name().equals(impl.name())) {
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

        final var bb = new BlockBuilder();
        //
        //        @«OVERRIDE.importedName»
        //        public «IMMUTABLE_SET.importedName»<«STRING.importedName»> validNames() {
        //            return «TypeConstants.VALID_NAMES_NAME»;
        //        }
        bb.nl().at().append(override);
        bb.nl().append("public ");
        bb.append(importedName(IMMUTABLE_SET));
        bb.append("<");
        bb.append(importedName(Types.STRING));
        bb.append("> validNames() {\n");
        bb.append("    return ");
        bb.append(TypeConstants.VALID_NAMES_NAME);
        bb.append(";\n");
        bb.append("}\n");

        //
        //        @«OVERRIDE.importedName»
        //        public boolean[] values() {
        //            return new boolean[] {
        //                    «FOR bit : typedef.bits SEPARATOR ','»
        //                        «Naming.getPropertyName(bit.name).getterMethodName»()
        //                    «ENDFOR»
        //                };
        //        }
        bb.nl().at().append(override);
        bb.nl().append("public boolean[] values() {\n");
        bb.append("    return new boolean[] {\n");
        {
            boolean first = true;
            for (var bit : typedef.getBits()) {
                if (first) {
                    first = false;
                } else {
                    bb.append(",\n");
                }
                bb.append("            ");
                bb.append(getterMethodName(Naming.getPropertyName(bit.getName())));
                bb.append("()");
            }
        }
        bb.nl().append("        };\n");
        bb.append("}\n");
        return bb;
    }

    /**
     * {@return string with the {@code equals()} method definition in JAVA format}
     */
    private CharSequence generateEquals() {
        final var equalsIdentifiers = genTO.getEqualsIdentifiers();
        if (equalsIdentifiers.isEmpty()) {
            return "";
        }

        //        @«OVERRIDE.importedName»
        //        public final boolean equals(«OBJECT.importedName» obj) {
        //            return this == obj || obj instanceof «type.simpleName» other
        //                «FOR property : genTO.equalsIdentifiers»
        //                    «val fieldName = property.fieldName»
        //                    «val type = property.returnType»
        //                    «IF type.equals(Types.primitiveBooleanType)»
        //                        && «fieldName» == other.«fieldName»«
        //                    »«ELSE»
        //                        && «type.importedUtilClass».equals(«fieldName», other.«fieldName»)«
        //                    »«ENDIF»«
        //                »«ENDFOR»;
        //        }

        final var bb = new BlockBuilder();
        bb.at().append(importedName(OVERRIDE));
        bb.nl().append("public final boolean equals(");
        bb.append(importedName(OBJECT));
        bb.append(" obj) {\n");
        bb.append("    return this == obj || obj instanceof ");
        bb.append(type().simpleName());
        bb.append(" other");
        for (var property : equalsIdentifiers) {
            bb.nl().append("        && ");

            final var fieldName = fieldName(property);
            final var type = property.getReturnType();
            if (type.equals(Types.primitiveBooleanType())) {
                bb.append(fieldName);
                bb.append(" == other.");
                bb.append(fieldName);
            } else {
                bb.append(importedUtilClass(type));
                bb.append(".equals(");
                bb.append(fieldName);
                bb.append(", other.");
                bb.append(fieldName);
                bb.append(")");
            }
        }
        bb.append(";\n");
        bb.append("}\n");
        return bb;
    }

    private CharSequence generateToString(final List<GeneratedProperty> props) {
        if (props.isEmpty()) {
            return "";
        }

        //        @«OVERRIDE.importedName»
        //        public «STRING.importedName» toString() {
        //            final var helper = «MOREOBJECTS.importedName».toStringHelper(«type.importedName».class);
        //            «FOR property : properties»
        //                «CODEHELPERS.importedName».«property.valueAppender»(helper, "«property.name»",
        // «property.fieldName»);
        //            «ENDFOR»
        //            return helper.toString();
        //        }

        final var bb = new BlockBuilder();
        bb.at().append(importedName(OVERRIDE));
        bb.nl().append("public ");
        bb.append(importedName(Types.STRING));
        bb.append(" toString() {\n");
        bb.append("    final var helper = ");
        bb.append(importedName(MOREOBJECTS));
        bb.append(".toStringHelper(");
        bb.append(importedName(type()), "    ");
        bb.append(".class);\n");
        for (var property : props) {
            bb.append("    ");
            bb.append(importedName(CODEHELPERS));
            bb.append(".");
            bb.append(valueAppender(property));
            bb.append("(helper, \"");
            bb.append(property.getName());
            bb.append("\", ");
            bb.append(fieldName(property));
            bb.append(");\n");
        }
        bb.append("    return helper.toString();\n");
        bb.append("}\n");
        return bb;
    }

    // FIXME: this should be specialized in BitsTypeObjectTemplate
    private static String valueAppender(final GeneratedProperty prop) {
        return prop.getReturnType().equals(Types.primitiveBooleanType()) ? "appendBit" : "appendValue";
    }

    // FIXME: this method should live in (the now non-existent) BitsTypeObjectTemplate
    private String bitsDefaultInstanceBody() {
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

        return sb.append(");\n").toString();
    }

    @NonNullByDefault
    private static void appendValue(final StringBuilder sb, final int index) {
        sb.append("    values[").append(index).append(']');
    }

    @NonNull String finalClass() {
        return " ";
    }

    private String annotationDeclaration() {
        final var annotations = genTO.getAnnotations();
        if (annotations.isEmpty()) {
            return "";
        }

        final var sb = new StringBuilder();
        for (var annotation : annotations) {
            sb.append('@').append(annotation.simpleName()).append('\n');
        }
        return sb.toString();
    }

    /**
     * {@return string with class declaration in JAVA format}
     * @param isInnerClass boolean value which specify if generated class is|isn't inner
     */
    // FIXME: return a Block
    CharSequence generateClassDeclaration(final boolean isInnerClass) {
        final var type = type();

        final var bb = new BlockBuilder();
        bb.append("public");
        if (isInnerClass) {
            bb.append(" static final ");
        } else {
            bb.append(type.isAbstract() ? " abstract " : finalClass());
        }
        bb.append("class ");
        bb.append(type.simpleName());

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

    /**
     * {@return string with constants in JAVA format}
     */
    // FIXME: return a Block
    private CharSequence constantsDeclarations() {
        if (consts.isEmpty()) {
            return "";
        }

        final var bb = new BlockBuilder();
        for (var c : consts) {
            switch (c.getName()) {
                case TypeConstants.PATTERN_CONSTANT_NAME ->
                    appendPatternConstant(bb, (Map<String, String>) c.getValue());
                case TypeConstants.VALID_NAMES_NAME -> appendValidNames(bb, (BitsTypeDefinition) c.getValue());
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
        bb.str("public static final ").str(juList).str("<String> " + TypeConstants.PATTERN_CONSTANT_NAME).str(" = ")
            .str(juList).append(".of(");
        {
            boolean first = true;
            for (var value : constValue.keySet()) {
                if (first) {
                    first = false;
                } else {
                    bb.appendImmediate(", ", "");
                }
                bb.str("\"").append(StringEscapeUtils.escapeJava(value));
                bb.append("\"");
            }
        }
        bb.append(");\n");
        bb.str("private static final ").append(jurPattern);
        if (constValue.size() == 1) {
            bb.str(" " + Constants.MEMBER_PATTERN_LIST + " = ").str(jurPattern)
                .str(".compile(" + TypeConstants.PATTERN_CONSTANT_NAME).append(".getFirst());\n");
            bb.str("private static final String " + Constants.MEMBER_REGEX_LIST + " = \"")
                .append(StringEscapeUtils.escapeJava(constValue.values().iterator().next()));
            bb.append("\";\n");
            return;
        }

        bb.str("[] " + Constants.MEMBER_PATTERN_LIST + " = ").str(importedName(CODEHELPERS))
            .str(".compilePatterns(" + TypeConstants.PATTERN_CONSTANT_NAME).append(");\n");
        bb.str("private static final String[] " + Constants.MEMBER_REGEX_LIST).append(" = { ");
        {
            boolean first = true;
            for (var value : constValue.values()) {
                if (first) {
                    first = false;
                } else {
                    bb.appendImmediate(", ", "");
                }
                bb.append("\"");
                bb.append(StringEscapeUtils.escapeJava(value));
                bb.append("\"");
            }
        }
        bb.append(" };\n");
    }

    private void appendValidNames(final BlockBuilder bb, final BitsTypeDefinition bitsType) {
        final var immutableSet = importedName(IMMUTABLE_SET);
        bb.str("protected static final ").str(immutableSet).str("<").str(importedName(Types.STRING))
            .str("> " + TypeConstants.VALID_NAMES_NAME + " = ").str(immutableSet).append(".of(");
        {
            boolean first = true;
            for (var bit : bitsType.getBits()) {
                if (first) {
                    first = false;
                } else {
                    bb.appendImmediate(", ", "");
                }
                bb.str("\"").str(bit.getName()).append("\"");
            }
        }
        bb.append(");\n");
    }

    // FIXME: this method should be specialized in BitsTypeObjectTemplate, as 'type bits' is an animal completely
    //        different from ScalarTypeObjects the rest of this method handles.
    // FIXME: return a Block
    CharSequence defaultInstance() {
        if (!genTO.isTypedef() || allProperties.isEmpty()) {
            return "";
        }

        final var prop = allProperties.getFirst();
        final var propType = prop.getReturnType();
        if (BaseYangTypes.INSTANCE_IDENTIFIER.name().equals(propType.name())) {
            return "";
        }

        //        public static «genTO.simpleName» getDefaultInstance(final String defaultValue) {
        //            «IF propType.equals(Types.primitiveBooleanType())»
        //                «bitsDefaultInstanceBody»
        //            «ELSEIF VALUEOF_TYPES.contains(propType)»
        //                return new «genTO.simpleName»(«propType.importedName».valueOf(defaultValue));
        //            «ELSEIF propType instanceof Decimal64Type»
        //                return new «genTO.simpleName»(«propType.importedName».valueOf(defaultValue).scaleTo(
        //«propType.fractionDigits»));
        //            «ELSEIF STRING_TYPE.equals(propType)»
        //                return new «genTO.simpleName»(defaultValue);
        //            «ELSEIF BINARY_TYPE.equals(propType)»
        //                return new «genTO.simpleName»(«JU_BASE64.importedName».getDecoder().decode(defaultValue));
        //            «ELSEIF EMPTY_TYPE.equals(propType)»
        //                return new «genTO.simpleName»(«CODEHELPERS.importedName».emptyFor(defaultValue));
        //            «ELSE»
        //                return new «genTO.simpleName»(new «propType.importedName»(defaultValue));
        //            «ENDIF»
        //        }

        final var simpleName = genTO.simpleName();
        final var bb = new BlockBuilder();
        bb.str("public static ").str(simpleName).append(" getDefaultInstance(final String defaultValue) {\n");
        if (propType.equals(Types.primitiveBooleanType())) {
            bb.append("    ");
            bb.append(bitsDefaultInstanceBody(), "    ");
            bb.newLineIfNotEmpty();
        } else if (ClassTemplate.VALUEOF_TYPES.contains(propType)) {
            bb.str("    return new ").str(simpleName).str("(").str(importedName(propType))
                .append(".valueOf(defaultValue));\n");
        } else if (propType instanceof Decimal64Type decimal64) {
            bb.str("    return new ").str(simpleName).str("(").str(importedName(propType))
                .str(".valueOf(defaultValue).scaleTo(").str(decimal64.fractionDigits()).append("));\n");
        } else if (BaseYangTypes.STRING_TYPE.equals(propType)) {
            bb.append("    return new ");
            bb.append(simpleName);
            bb.append("(defaultValue);\n");
        } else if (BaseYangTypes.BINARY_TYPE.equals(propType)) {
            bb.append("    return new ");
            bb.append(simpleName);
            bb.append("(");
            bb.append(importedName(JU_BASE64));
            bb.append(".getDecoder().decode(defaultValue));\n");
        } else if (BaseYangTypes.EMPTY_TYPE.equals(propType)) {
            bb.append("    return new ");
            bb.append(simpleName);
            bb.append("(");
            bb.append(importedName(CODEHELPERS));
            bb.append(".emptyFor(defaultValue));\n");
        } else {
            bb.append("    return new ");
            bb.append(simpleName);
            bb.append("(new ");
            bb.append(importedName(propType));
            bb.append("(defaultValue));\n");
        }
        bb.append("}\n");
        return bb;
    }

    // FIXME: return a Block
    CharSequence constructors() {
        //        «IF genTO.typedef && allProperties.size == 1 && allProperties.first.name.equals(
        //TypeConstants.VALUE_PROP)»
        //            «typedefConstructor»
        //        «ELSE»
        //            «allValuesConstructor»
        //        «ENDIF»
        //
        //        «IF !allProperties.empty»
        //            «copyConstructor»
        //        «ENDIF»
        //        «IF properties.empty && !parentProperties.empty »
        //            «parentConstructor»
        //        «ENDIF»

        final var bb = new BlockBuilder();
        if (genTO.isTypedef() && allProperties.size() == 1
            && TypeConstants.VALUE_PROP.equals(allProperties.getFirst().getName())) {
            bb.append(typedefConstructor());
            bb.newLineIfNotEmpty();
        } else {
            bb.append(allValuesConstructor());
            bb.newLineIfNotEmpty();
        }
        // FIXME: inline into if blocks?
        bb.newLine();
        if (!allProperties.isEmpty()) {
            bb.append(copyConstructor());
            bb.newLineIfNotEmpty();
        }
        if (properties.isEmpty() && !parentProperties.isEmpty()) {
            bb.append(parentConstructor());
            bb.newLineIfNotEmpty();
        }
        return bb;
    }

    CharSequence propertyMethods() {
        if (properties.isEmpty()) {
            return "";
        }
        if (genTO.getImplements().stream().anyMatch(ifc -> BindingTypes.SCALAR_TYPE_OBJECT.name().equals(ifc.name()))) {
            final var field = properties.getFirst();
            return '@' + importedName(OVERRIDE) + '\n'
                +  "public " + importedReturnType(field) + ' ' + Naming.SCALAR_TYPE_OBJECT_GET_VALUE_NAME + "() {\n"
                +  "    return " + fieldName(field) + cloneCall(field) + ";\n"
                +  "}\n";
        }

        final var bb = new BlockBuilder();
        final var it = properties.iterator();
        while (true) {
            final var field = it.next();
            bb.append(asGetterMethod(field));
            if (!field.isReadOnly()) {
                bb.nl().append(asSetterMethod(field));
            }

            if (!it.hasNext()) {
                return bb;
            }
            bb.newLine();
        }
    }

    /**
     * {@return string with the {@code hashCode()} method definition in JAVA format}
     */
    // FIXME: return a Block
    private String generateHashCode() {
        final var props = genTO.getHashCodeIdentifiers();
        final int size = props.size();
        if (size == 0) {
            return "";
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
        final var bb = new BlockBuilder();
        bb.at().append(importedName(OVERRIDE));
        bb.nl().append("public int hashCode() {\n");
        if (size == 1) {
            bb.append("    return ");
            final var prop = props.getFirst();
            if (prop.getReturnType().equals(Types.primitiveBooleanType())) {
                bb.append(importedName(BOOLEAN), "    ");
                bb.append(".hashCode(");
            } else {
                bb.append(importedName(CODEHELPERS), "    ");
                bb.append(".wrapperHashCode(");
            }
            bb.append(fieldName(prop), "    ");
            bb.append(");\n");
        } else {
            bb.append("    final int prime = 31;\n");
            bb.append("    int result = 1;\n");
            for (var property : props) {
                bb.append("    result = prime * result + ");
                final var type = property.getReturnType();
                bb.append(type.equals(Types.primitiveBooleanType()) ? importedName(BOOLEAN) : importedUtilClass(type));
                bb.append(".hashCode(");
                bb.append(fieldName(property), "    ");
                bb.append(");\n");
            }
            bb.append("    return result;\n");
        }
        bb.append("}\n");
        return bb.toRawString();
    }

    @NonNullByDefault
    final CharSequence generateRestrictions(final Type type, final String paramName, final Type returnType) {
        final var typeRestrictions = switch (type) {
            case GeneratedTransferObject gto -> gto.getRestrictions();
            case RestrictedType restricted -> restricted.restrictions();
            case null, default -> null;
        };
        if (typeRestrictions == null) {
            return "";
        }
        final var length = typeRestrictions.getLengthConstraint().orElse(null);
        final var range = typeRestrictions.getRangeConstraint().orElse(null);
        if (length == null && range == null) {
            return "";
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

    // FIXME: return a BlockBuilder
    @NonNull CharSequence allValuesConstructor() {
        //        public «type.simpleName»(«allProperties.asArgumentsDeclaration») {
        //            «IF !parentProperties.empty»
        //                super(«parentProperties.asArguments»);
        //            «ENDIF»
        //            «FOR p : allProperties»
        //                «generateRestrictions(type, p.fieldName, p.returnType)»
        //            «ENDFOR»
        //
        //            «FOR p : properties»
        //                «val fieldName = p.fieldName»
        //                «IF p.returnType.simpleName.endsWith("[]")»
        //                    this.«fieldName» = «CODEHELPERS.importedName».copyArray(«fieldName»);
        //                «ELSE»
        //                    this.«fieldName» = «fieldName»;
        //                «ENDIF»
        //            «ENDFOR»
        //        }

        final var bb = new BlockBuilder();
        bb.str("public ").append(type().simpleName());
        bb.str("(").append(asArgumentsDeclaration(allProperties));
        bb.append(") {\n");
        if (!parentProperties.isEmpty()) {
            bb.str("    super(").append(asArguments(parentProperties));
            bb.append(");\n");
        }
        for (var prop : allProperties) {
            bb.append("    ");
            bb.append(generateRestrictions(type(), BaseTemplate.fieldName(prop), prop.getReturnType()), "    ");
            bb.newLineIfNotEmpty();
        }
        bb.newLine();
        for (var prop : properties) {
            final var fieldName = BaseTemplate.fieldName(prop);
            if (prop.getReturnType().simpleName().endsWith("[]")) {
                bb.str("    this.").append(fieldName);
                bb.append(" = ");
                bb.append(importedName(CODEHELPERS));
                bb.str(".copyArray(").append(fieldName);
                bb.append(");\n");
            } else {
                bb.str("    this.").append(fieldName);
                bb.str(" = ").append(fieldName);
                bb.append(";\n");
            }
        }
        bb.append("}\n");
        return bb;
    }

    String copyConstructor() {
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
        return sb.append("}\n").toString();
    }

    @NonNullByDefault
    final BlockBuilder parentConstructor() {
        //        /**
        //         * Creates a new instance from «genTO.superType.importedName»
        //         *
        //         * @param source Source object
        //         */
        //        public «type.simpleName»(«genTO.superType.importedName» source) {
        //            super(source);
        //            «genPatternEnforcer("getValue()")»
        //        }
        final var importedSuper = importedName(genTO.getSuperType());

        final var bb = new BlockBuilder();
        bb.append("/**\n");
        bb.str(" * Creates a new instance from ").append(importedSuper);
        bb.nl().append(
                  " *\n");
        bb.append(" * @param source Source object\n");
        bb.append(" */\n");
        bb.str("public ").append(type().simpleName());
        bb.str("(").append(importedSuper);
        bb.append(" source) {\n");
        bb.append("    super(source);\n");
        bb.append("    ");
        bb.append(genPatternEnforcer("getValue()"));
        bb.newLineIfNotEmpty();
        bb.append("}\n");
        return bb;
    }

    private CharSequence typedefConstructor() {
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

        final var bb = new BlockBuilder();
        bb.at().append(importedName(CONSTRUCTOR_PARAMETERS));
        bb.str("(\"").append(TypeConstants.VALUE_PROP);
        bb.append("\")\n");
        bb.str("public ").append(type().simpleName());
        bb.str("(").append(asArgumentsDeclaration(allProperties));
        bb.append(") {\n");
        if (!parentProperties.isEmpty()) {
            bb.str("    super(").append(asArguments(parentProperties), "    ");
            bb.append(");\n");
        }

        final var value = valueProperty(allProperties);
        if (value == null) {
            throw new VerifyException("missing value property");
        }

        final var fieldName = fieldName(value);
        if (valueProperty(properties) != null) {
            bb.str("    this.").append(fieldName);
            bb.str(" = ").append(importedName(CODEHELPERS));
            bb.str(".requireValue(").append(fieldName);
            if (value.getReturnType() instanceof Decimal64Type decimal64) {
                bb.append(", " + decimal64.fractionDigits());
            }
            bb.str(")").append(cloneCall(value));
            bb.append(";\n");
        }
        bb.append("    ");
        bb.append(generateRestrictions(type(), fieldName, value.getReturnType()), "    ");
        bb.newLineIfNotEmpty();
        bb.append("    ");
        bb.nl().append("    ");
        bb.append(genPatternEnforcer(fieldName), "    ");
        bb.newLineIfNotEmpty();
        bb.append("}\n");
        return bb;
    }

    private static @Nullable GeneratedProperty valueProperty(final List<GeneratedProperty> props) {
        return switch (props.size()) {
            case 0 -> null;
            case 1 -> {
                final var prop = props.getFirst();
                if (!TypeConstants.VALUE_PROP.equals(prop.getName())) {
                    throw new VerifyException("Unexpected property " + prop);
                }
                yield prop;
            }
            default -> throw new VerifyException("Unexpected properties " + props);
        };
    }

    @NonNullByDefault
    private String genPatternEnforcer(final String ref) {
        final var sb = new StringBuilder();
        for (var constant : consts) {
            if (TypeConstants.PATTERN_CONSTANT_NAME.equals(constant.getName())) {
                sb.append(importedName(CODEHELPERS)).append(".checkPattern(").append(ref).append(", ")
                    .append(Constants.MEMBER_PATTERN_LIST).append(", ").append(Constants.MEMBER_REGEX_LIST)
                    .append(");\n");
            }
        }
        return sb.toString();
    }
}
