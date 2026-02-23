/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.BINARY_TYPE;
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.BOOLEAN_TYPE;
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.EMPTY_TYPE;
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.STRING_TYPE;

import com.google.common.collect.Iterables;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.UnionTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.binding.model.ri.Types;

final class UnionTypeObjectTemplate extends ClassTemplate {
    @NonNullByDefault
    private UnionTypeObjectTemplate(final NestedJavaGeneratedType javaType, final UnionTypeObjectArchetype archetype) {
        super(javaType, archetype);
    }

    @NonNullByDefault
    UnionTypeObjectTemplate(final UnionTypeObjectArchetype archetype) {
        super(archetype);
    }

    static CharSequence generateAsInner(final @NonNull NestedJavaGeneratedType javaType,
            final @NonNull UnionTypeObjectArchetype archetype) {
        return new UnionTypeObjectTemplate(javaType, archetype).generateAsInnerClass();
    }


    @Override
    public CharSequence constructors() {
        //      «unionConstructorsParentProperties»
        //      «unionConstructors»
        //      «IF !allProperties.empty»
        //          «copyConstructor»
        //      «ENDIF»
        //      «IF properties.empty && !parentProperties.empty»
        //          «parentConstructor»
        //      «ENDIF»
        //
        //      «generateStringValue»

        final var sc = new StringConcatenation();
        sc.append(unionConstructorsParentProperties());
        sc.newLineIfNotEmpty();
        sc.append(unionConstructors());
        sc.newLineIfNotEmpty();
        if (!allProperties.isEmpty()) {
            sc.append(copyConstructor());
            sc.newLineIfNotEmpty();
        }
        if (properties.isEmpty() && !parentProperties.isEmpty()) {
            sc.append(parentConstructor());
            sc.newLineIfNotEmpty();
        }
        sc.newLine();
        sc.append(generateStringValue());
        sc.newLineIfNotEmpty();
        return sc;
    }

    private String unionConstructors() {
        if (finalProperties.isEmpty()) {
            return "";
        }

        final var simpleName = type().simpleName();
        final var sb = new StringBuilder();
        final var it = finalProperties.iterator();
        while (true) {
            final var property = it.next();
            final var actualType = property.getReturnType();
            final var propertyAndTopParentProperties = Iterables.concat(parentProperties, List.of(property));
            final var propFieldName = fieldName(property);

            if (restrictions != null) {
                final var checkers = generateCheckers(property, restrictions, actualType);
                if (!checkers.isEmpty()) {
                    sb.append(checkers).append('\n');
                }
            }

            sb.append("public ").append(simpleName).append('(')
                .append(asArgumentsDeclaration(propertyAndTopParentProperties)).append(") {\n");
            if (!parentProperties.isEmpty()) {
                sb.append("    super(").append(asArguments(parentProperties)).append(");\n");
            }

            final var restrictions = restrictionsForSetter(actualType);
            if (restrictions != null) {
                final var checkArg = checkArgument(property, restrictions, actualType, propFieldName);
                if (!checkArg.isEmpty()) {
                    sb.append(checkArg).append('\n');
                }
            }

            for (var other : finalProperties) {
                sb.append("    this.");
                if (property.equals(other)) {
                    sb.append(propFieldName).append(" = ").append(importedName(JU_OBJECTS)).append(".requireNonNull(")
                        .append(propFieldName).append(");\n");
                } else {
                    sb.append(fieldName(other)).append(" = null;\n");
                }
            }

            sb.append("}\n");

            if (!it.hasNext()) {
                return sb.toString();
            }
            sb.append('\n');
        }
    }

    private String unionConstructorsParentProperties() {
        if (parentProperties.isEmpty()) {
            return "";
        }

        final var sb = new StringBuilder();
        final var it = parentProperties.iterator();
        final var simpleName = type().simpleName();
        while (true) {
            final var prop = it.next();
            final var fieldName = fieldName(prop);
            final var propType = importedName(prop.getReturnType());
            sb
                .append("public ").append(simpleName).append('(').append(propType).append(' ').append(fieldName)
                    .append(") {\n")
                .append("    super(").append(fieldName).append(");\n")
                .append("}\n");

            if (!it.hasNext()) {
                return sb.toString();
            }
            sb.append('\n');
        }
    }

    private String generateStringValue() {
        final var sb = new StringBuilder()
            .append("""
                /**
                 * Return a String representing the value of this union.
                 *
                 * @return String representation of this union's value.
                 */
                public\s""").append(importedName(Types.STRING)).append(" stringValue() {\n");


        for (var prop : finalProperties) {
            final var field = fieldName(prop);
            final var type = prop.getReturnType();
            final var fqcn = type.name().fullyQualifiedName();

            sb
                .append("    if (").append(field).append(" != null) {\n")
                .append("        return ");

            if (STRING_TYPE.equals(type)) {
                // type string
                sb.append(field);
            } else if ("org.opendaylight.yangtools.binding.BindingInstanceIdentifier".equals(fqcn)) {
                // type instance-identifier
                sb.append(field).append(".toString()");
            } else if (BINARY_TYPE.equals(type)) {
                // type binary
                sb.append("new ").append(importedName(Types.STRING)).append('(').append(field).append(')');
            } else if (fqcn.startsWith("java.lang") || type instanceof EnumTypeObjectArchetype) {
                // type int* or enumeration*
                sb.append(field).append(".toString()");
            } else if (fqcn.startsWith("org.opendaylight.yangtools.yang.common.Uint")
                        || fqcn.equals("org.opendaylight.yangtools.yang.common.Decimal64")) {
                // type uint*, decimal64
                sb.append(field).append(".toCanonicalString()");
            } else if (type instanceof UnionTypeObjectArchetype) {
                // union type
                sb.append(field).append(".stringValue()");
            } else if (BOOLEAN_TYPE.equals(typedefReturnType(type))) {
                // generated boolean typedef
                sb.append(field).append(".isValue().toString()");
            } else if (BINARY_TYPE.equals(typedefReturnType(type))) {
                // generated byte[] typedef
                sb.append(importedName(JU_BASE64)).append(".getEncoder().encodeToString(").append(field)
                    .append(".getValue())");
            } else if (EMPTY_TYPE.equals(type) || EMPTY_TYPE.equals(typedefReturnType(type))) {
                // generated empty typedef
                sb.append("\"\"");
            } else if (BindingTypes.isBitsType(type)) {
                // generated bits typedef
                sb.append(importedName(JU_ARRAYS)).append(".toString(").append(field).append(".values())");
            } else if (BindingTypes.isIdentityType(type)) {
                // generated identity
                sb.append(field).append('.').append(Naming.BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME)
                    .append("().toString()");
            } else {
                // generated type
                sb.append(field).append(".getValue().toString()");
            }
            sb.append(";\n    }\n");
        }

        return sb
            .append("    throw new IllegalStateException(\"No value assigned\");\n")
            .append("}\n")
            .toString();
    }

    private static @Nullable Type typedefReturnType(final Type type) {
        if (type instanceof GeneratedTransferObject gto && gto.isTypedef()) {
            final var props = gto.getProperties();
            if (props != null && props.size() == 1) {
                final var prop = props.getFirst();
                if (prop.getName().equals("value")) {
                    return prop.getReturnType();
                }
            }
        }
        return null;
    }

    @Override
    protected String copyConstructor() {
        final var type = type();
        final var simpleName = type.simpleName();

        final var sb = new StringBuilder()
            .append("""
                /**
                 * Creates a copy from Source Object.
                 *
                 * @param source Source object
                 */
                """)
            .append("public ").append(simpleName).append('(').append(simpleName).append(" source) {\n");
        if (!parentProperties.isEmpty()) {
            sb.append("    super(source);\n");
        }
        for (var prop : properties) {
            final var fieldName = fieldName(prop);
            sb.append("    this.").append(fieldName).append(" = ");
            if (prop.getReturnType().simpleName().endsWith("[]")) {
                sb.append(importedName(CODEHELPERS)).append(".copyArray(source.").append(fieldName).append(')');
            } else {
                sb.append("source.").append(fieldName);
            }
            sb.append(";\n");
        }
        return sb.append("}\n").toString();
    }

    @Override
    String defaultInstance() {
        return "";
    }
}
