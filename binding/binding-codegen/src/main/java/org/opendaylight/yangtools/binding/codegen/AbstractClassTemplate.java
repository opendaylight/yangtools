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
import org.eclipse.xtend2.lib.StringConcatenation;
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
 * Abstract base class holding pure-Java parts of {@link ClassTemplate}.
 */
abstract class AbstractClassTemplate extends BaseTemplate {
    private static final Comparator<GeneratedProperty> PROP_COMPARATOR =
        Comparator.comparing(GeneratedProperty::getName);

    static final Set<ConcreteType> VALUEOF_TYPES = Set.<ConcreteType>of(
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
    static final @NonNull JavaTypeName BOOLEAN = JavaTypeName.create(Boolean.class);
    /**
     * {@code com.google.common.collect.ImmutableSet} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName IMMUTABLE_SET = JavaTypeName.create(ImmutableSet.class);

    final @NonNull GeneratedTransferObject genTO;
    final @NonNull List<GeneratedProperty> properties;
    final @NonNull List<GeneratedProperty> finalProperties;
    final @NonNull List<GeneratedProperty> parentProperties;
    final @NonNull List<GeneratedProperty> allProperties;

    /**
     * List of enumeration which are generated as JAVA enum type.
     */
    final @NonNull List<EnumTypeObjectArchetype> enums;
    /**
     * List of constant instances which are generated as JAVA public static final attributes.
     */
    final @NonNull List<Constant> consts;

    final AbstractRangeGenerator<?> rangeGenerator;
    final Restrictions restrictions;

    @NonNullByDefault
    AbstractClassTemplate(final AbstractJavaGeneratedType javaType, final GeneratedTransferObject genType) {
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
    abstract CharSequence generateBody(boolean isInnerClass);

    // FIXME: this method should live in (the now non-existent) BitsTypeObjectTemplate
    final String bitsDefaultInstanceBody() {
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

    final @NonNull String suidDeclaration() {
        final var suid = genTO.getSUID();
        return suid == null ? ""
            : "@java.io.Serial\n"
            + "private static final long serialVersionUID = " + suid.getValue() + "L;\n";
    }

    final String annotationDeclaration() {
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
     * Template method which generates JAVA class declaration.
     *
     * @param isInnerClass boolean value which specify if generated class is|isn't inner
     * @return string with class declaration in JAVA format
     */
    CharSequence generateClassDeclaration(final boolean isInnerClass) {
        final var type = type();

        final var sc = new StringConcatenation();
        sc.append("public");
        if (isInnerClass) {
            sc.append(" static final ");
        } else {
            sc.append(type.isAbstract() ? " abstract " : finalClass());
        }
        sc.append("class ");
        sc.append(type.simpleName());

        final var superType = genTO.getSuperType();
        if (superType != null) {
            sc.append(" extends ");
            sc.append(importedName(superType));
        }

        final var ifaces = type.getImplements();
        if (!ifaces.isEmpty()) {
            sc.newLine();
            sc.append(" implements ");

            final var it = ifaces.iterator();
            while (true) {
                sc.append(importedName(it.next()));
                if (!it.hasNext()) {
                    break;
                }
                sc.append(", ");
            }
        }
        return sc;
    }

    /**
     * Template method which generates JAVA constants.
     *
     * @return string with constants in JAVA format
     */
    final CharSequence constantsDeclarations() {
        if (consts.isEmpty()) {
            return "";
        }

        //        «FOR c : consts»
        //            «IF TypeConstants.PATTERN_CONSTANT_NAME.equals(c.name)»
        //                «val cValue = c.value as Map<String, String>»
        //                «val jurPatternRef = JUR_PATTERN.importedName»
        //                public static final «JU_LIST.importedName»<String> «TypeConstants.PATTERN_CONSTANT_NAME» =
        // «JU_LIST.importedName».of(«
        //                FOR v : cValue.keySet SEPARATOR ", "»"«v.escapeJava»"«ENDFOR»);
        //                «IF cValue.size == 1»
        //                    private static final «jurPatternRef» «Constants.MEMBER_PATTERN_LIST» = «jurPatternRef»
        //.compile(«TypeConstants.PATTERN_CONSTANT_NAME».getFirst());
        //                    private static final String «Constants.MEMBER_REGEX_LIST» = "«cValue.values.iterator.next
        //.escapeJava»";
        //                «ELSE»
        //                    private static final «jurPatternRef»[] «Constants.MEMBER_PATTERN_LIST» = «CODEHELPERS
        //.importedName».compilePatterns(«TypeConstants.PATTERN_CONSTANT_NAME»);
        //                    private static final String[] «Constants.MEMBER_REGEX_LIST» = { «
        //                    FOR v : cValue.values SEPARATOR ", "»"«v.escapeJava»"«ENDFOR» };
        //                «ENDIF»
        //            «ELSEIF TypeConstants.VALID_NAMES_NAME.equals(c.name)»
        //                «val cValue = c.value as BitsTypeDefinition»
        //                «val immutableSet = IMMUTABLE_SET.importedName»
        //                protected static final «immutableSet»<«STRING.importedName»> «TypeConstants.VALID_NAMES_NAME»
        // = «immutableSet».of(«
        //                FOR bit : cValue.bits SEPARATOR ", "»"«bit.name»"«ENDFOR»);
        //            «ELSE»
        //                «emitConstant(c)»
        //            «ENDIF»
        //        «ENDFOR»

        final var sc = new StringConcatenation();
        for (var c : consts) {
            switch (c.getName()) {
                case TypeConstants.PATTERN_CONSTANT_NAME -> {
                    final var constValue = (Map<String, String>) c.getValue();
                    final var jurPattern = importedName(JUR_PATTERN);
                    final var juList = importedName(JU_LIST);

                    sc.newLineIfNotEmpty();
                    sc.append("public static final ");
                    sc.append(juList);
                    sc.append("<String> ");
                    sc.append(TypeConstants.PATTERN_CONSTANT_NAME);
                    sc.append(" = ");
                    sc.append(juList);
                    sc.append(".of(");
                    {
                        boolean first = true;
                        for (var value : constValue.keySet()) {
                            if (first) {
                                first = false;
                            } else {
                                sc.appendImmediate(", ", "");
                            }
                            sc.append("\"");
                            sc.append(StringEscapeUtils.escapeJava(value));
                            sc.append("\"");
                        }
                    }
                    sc.append(");\n");
                    sc.append("private static final ");
                    sc.append(jurPattern);
                    if (constValue.size() == 1) {
                        sc.append(" ");
                        sc.append(Constants.MEMBER_PATTERN_LIST);
                        sc.append(" = ");
                        sc.append(jurPattern);
                        sc.append(".compile(");
                        sc.append(TypeConstants.PATTERN_CONSTANT_NAME);
                        sc.append(".getFirst());\n");
                        sc.append("private static final String ");
                        sc.append(Constants.MEMBER_REGEX_LIST);
                        sc.append(" = \"");
                        sc.append(StringEscapeUtils.escapeJava(constValue.values().iterator().next()));
                        sc.append("\";\n");
                    } else {
                        sc.append("[] ");
                        sc.append(Constants.MEMBER_PATTERN_LIST);
                        sc.append(" = ");
                        sc.append(importedName(CODEHELPERS));
                        sc.append(".compilePatterns(");
                        sc.append(TypeConstants.PATTERN_CONSTANT_NAME);
                        sc.append(");\n");
                        sc.append("private static final String[] ");
                        sc.append(Constants.MEMBER_REGEX_LIST);
                        sc.append(" = { ");
                        {
                            boolean first = true;
                            for (var value : constValue.values()) {
                                if (first) {
                                    first = false;
                                } else {
                                    sc.appendImmediate(", ", "");
                                }
                                sc.append("\"");
                                sc.append(StringEscapeUtils.escapeJava(value));
                                sc.append("\"");
                            }
                        }
                        sc.append(" };\n");
                    }
                }
                case TypeConstants.VALID_NAMES_NAME -> {
                    final var immutableSet = importedName(IMMUTABLE_SET);
                    final var bitsType = (BitsTypeDefinition) c.getValue();
                    sc.append("protected static final ");
                    sc.append(immutableSet);
                    sc.append("<");
                    sc.append(importedName(Types.STRING));
                    sc.append("> ");
                    sc.append(TypeConstants.VALID_NAMES_NAME);
                    sc.append(" = ");
                    sc.append(immutableSet);
                    sc.append(".of(");
                    {
                        boolean first = true;
                        for (var bit : bitsType.getBits()) {
                            if (first) {
                                first = false;
                            } else {
                                sc.appendImmediate(", ", "");
                            }
                            sc.append("\"");
                            sc.append(bit.getName());
                            sc.append("\"");
                        }
                    }
                    sc.append(");\n");
                }
                default -> sc.append(emitConstant(c));
            }
        }
        return sc;
    }

    // FIXME: this method should be specialized in BitsTypeObjectTemplate, as 'type bits' is an animal completely
    //        different from ScalarTypeObjects the rest of this method handles.
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
        final var sc = new StringConcatenation();
        sc.append("public static ");
        sc.append(simpleName);
        sc.append(" getDefaultInstance(final String defaultValue) {\n");
        if (propType.equals(Types.primitiveBooleanType())) {
            sc.append("    ");
            sc.append(bitsDefaultInstanceBody(), "    ");
            sc.newLineIfNotEmpty();
        } else if (ClassTemplate.VALUEOF_TYPES.contains(propType)) {
            sc.append("    return new ");
            sc.append(simpleName);
            sc.append("(");
            sc.append(importedName(propType));
            sc.append(".valueOf(defaultValue));\n");
        } else if (propType instanceof Decimal64Type decimal64) {
            sc.append("    return new ");
            sc.append(simpleName);
            sc.append("(");
            sc.append(importedName(propType));
            sc.append(".valueOf(defaultValue).scaleTo(");
            sc.append(decimal64.fractionDigits());
            sc.append("));\n");
        } else if (BaseYangTypes.STRING_TYPE.equals(propType)) {
            sc.append("    return new ");
            sc.append(simpleName);
            sc.append("(defaultValue);\n");
        } else if (BaseYangTypes.BINARY_TYPE.equals(propType)) {
            sc.append("    return new ");
            sc.append(simpleName);
            sc.append("(");
            sc.append(importedName(JU_BASE64));
            sc.append(".getDecoder().decode(defaultValue));\n");
        } else if (BaseYangTypes.EMPTY_TYPE.equals(propType)) {
            sc.append("    return new ");
            sc.append(simpleName);
            sc.append("(");
            sc.append(importedName(CODEHELPERS));
            sc.append(".emptyFor(defaultValue));\n");
        } else {
            sc.append("    return new ");
            sc.append(simpleName);
            sc.append("(new ");
            sc.append(importedName(propType));
            sc.append("(defaultValue));\n");
        }
        sc.append("}\n");
        return sc;
    }

    /**
     * {@return string with the class attributes in JAVA format}
     */
    final String generateFields() {
        if (properties.isEmpty()) {
            return "";
        }

        //    «FOR f : properties»
        //        private«IF isReadOnly(f)» final«ENDIF» «f.returnType.importedName» «f.fieldName»;
        //    «ENDFOR»
        final var sb = new StringBuilder();
        for (var field : properties) {
            sb.append(field.isReadOnly() ? "private final " : "private ").append(importedReturnType(field)).append(' ')
                .append(fieldName(field)).append(";\n");
        }
        return sb.toString();
    }

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

        final var sc = new StringConcatenation();
        if (genTO.isTypedef() && allProperties.size() == 1
            && TypeConstants.VALUE_PROP.equals(allProperties.getFirst().getName())) {
            sc.append(typedefConstructor());
            sc.newLineIfNotEmpty();
        } else {
            sc.append(allValuesConstructor());
            sc.newLineIfNotEmpty();
        }
        sc.newLine();
        if (!allProperties.isEmpty()) {
            sc.append(copyConstructor());
            sc.newLineIfNotEmpty();
        }
        if (properties.isEmpty() && !parentProperties.isEmpty()) {
            sc.append(parentConstructor());
            sc.newLineIfNotEmpty();
        }
        return sc;
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

        //    «FOR field : properties SEPARATOR "\n"»
        //        «field.asGetterMethod»
        //        «IF !field.readOnly»
        //            «field.asSetterMethod»
        //        «ENDIF»
        //    «ENDFOR»

        final var sc = new StringConcatenation();
        final var it = properties.iterator();
        while (true) {
            final var field = it.next();
            sc.append(asGetterMethod(field));
            if (!field.isReadOnly()) {
                sc.newLine();
                sc.append(asSetterMethod(field));
            }

            if (!it.hasNext()) {
                return sc;
            }
            sc.newLine();
        }
    }

    /**
     * {@return string with the {@code hashCode()} method definition in JAVA format}
     */
    final String generateHashCode() {
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
        final var sc = new StringConcatenation();
        sc.append("@");
        sc.append(importedName(OVERRIDE));
        sc.newLine();
        sc.append("public int hashCode() {\n");
        if (size == 1) {
            sc.append("    return ");
            final var prop = props.getFirst();
            if (prop.getReturnType().equals(Types.primitiveBooleanType())) {
                sc.append(importedName(BOOLEAN), "    ");
                sc.append(".hashCode(");
            } else {
                sc.append(importedName(CODEHELPERS), "    ");
                sc.append(".wrapperHashCode(");
            }
            sc.append(fieldName(prop), "    ");
            sc.append(");\n");
        } else {
            sc.append("    final int prime = 31;\n");
            sc.append("    int result = 1;\n");
            for (var property : props) {
                sc.append("    result = prime * result + ");
                final var type = property.getReturnType();
                sc.append(type.equals(Types.primitiveBooleanType()) ? importedName(BOOLEAN) : importedUtilClass(type));
                sc.append(".hashCode(");
                sc.append(fieldName(property), "    ");
                sc.append(");\n");
            }
            sc.append("    return result;\n");
        }
        sc.append("}\n");
        return sc.toString();
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

    CharSequence allValuesConstructor() {
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

        final var sc = new StringConcatenation();
        sc.append("public ");
        sc.append(type().simpleName());
        sc.append("(");
        sc.append(asArgumentsDeclaration(allProperties));
        sc.append(") {\n");
        if (!parentProperties.isEmpty()) {
            sc.append("    super(");
            sc.append(asArguments(parentProperties));
            sc.append(");\n");
        }
        for (var prop : allProperties) {
            sc.append("    ");
            sc.append(generateRestrictions(type(), BaseTemplate.fieldName(prop), prop.getReturnType()), "    ");
            sc.newLineIfNotEmpty();
        }
        sc.newLine();
        for (var prop : properties) {
            final var fieldName = BaseTemplate.fieldName(prop);
            if (prop.getReturnType().simpleName().endsWith("[]")) {
                sc.append("    this.");
                sc.append(fieldName);
                sc.append(" = ");
                sc.append(importedName(CODEHELPERS));
                sc.append(".copyArray(");
                sc.append(fieldName);
                sc.append(");\n");
            } else {
                sc.append("    this.");
                sc.append(fieldName);
                sc.append(" = ");
                sc.append(fieldName);
                sc.append(";\n");
            }
        }
        sc.append("}\n");
        return sc;
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
    final CharSequence parentConstructor() {
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

        final var sc = new StringConcatenation();
        sc.append("/**\n");
        sc.append(" * Creates a new instance from ");
        sc.append(importedSuper);
        sc.newLine();
        sc.append(" *\n");
        sc.append(" * @param source Source object\n");
        sc.append(" */\n");
        sc.append("public ");
        sc.append(type().simpleName());
        sc.append("(");
        sc.append(importedSuper);
        sc.append(" source) {\n");
        sc.append("    super(source);\n");
        sc.append("    ");
        sc.append(genPatternEnforcer("getValue()"));
        sc.newLineIfNotEmpty();
        sc.append("}\n");
        return sc;
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

        final var sc = new StringConcatenation();
        sc.append("@");
        sc.append(importedName(CONSTRUCTOR_PARAMETERS));
        sc.append("(\"");
        sc.append(TypeConstants.VALUE_PROP);
        sc.append("\")\n");
        sc.append("public ");
        sc.append(type().simpleName());
        sc.append("(");
        sc.append(asArgumentsDeclaration(allProperties));
        sc.append(") {\n");
        if (!parentProperties.isEmpty()) {
            sc.append("    super(");
            sc.append(asArguments(parentProperties), "    ");
            sc.append(");\n");
        }

        final var value = valueProperty(allProperties);
        if (value == null) {
            throw new VerifyException("missing value property");
        }

        final var fieldName = fieldName(value);
        if (valueProperty(properties) != null) {
            sc.append("    this.");
            sc.append(fieldName);
            sc.append(" = ");
            sc.append(importedName(CODEHELPERS));
            sc.append(".requireValue(");
            sc.append(fieldName);
            if (value.getReturnType() instanceof Decimal64Type decimal64) {
                sc.append(", " + decimal64.fractionDigits());
            }
            sc.append(")");
            sc.append(cloneCall(value));
            sc.append(";\n");
        }
        sc.append("    ");
        sc.append(generateRestrictions(type(), fieldName, value.getReturnType()), "    ");
        sc.newLineIfNotEmpty();
        sc.append("    ");
        sc.newLine();
        sc.append("    ");
        sc.append(genPatternEnforcer(fieldName), "    ");
        sc.newLineIfNotEmpty();
        sc.append("}\n");
        return sc;
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
