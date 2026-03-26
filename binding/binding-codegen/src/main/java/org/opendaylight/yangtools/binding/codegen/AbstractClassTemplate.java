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
import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.Constant;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.RestrictedType;
import org.opendaylight.yangtools.binding.model.api.Restrictions;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.ri.BaseYangTypes;
import org.opendaylight.yangtools.binding.model.ri.TypeConstants;
import org.opendaylight.yangtools.binding.model.ri.Types;
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
                sc.append(importedHashCodeUtilClass(property), "    ");
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
    final String importedHashCodeUtilClass(final GeneratedProperty prop) {
        final var propType = prop.getReturnType();
        return propType.equals(Types.primitiveBooleanType()) ? importedName(BOOLEAN) : importedUtilClass(propType);
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

    @NonNullByDefault
    final String genPatternEnforcer(final String ref) {
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
