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

    static @NonNull BlockBuilder generateAsInner(final @NonNull NestedJavaGeneratedType javaType,
            final @NonNull UnionTypeObjectArchetype archetype) {
        return new UnionTypeObjectTemplate(javaType, archetype).generateAsInnerClass();
    }

    @Override
    BlockBuilder constructors() {
        final var bb = new BlockBuilder()
            .blk(unionConstructorsParentProperties())
            .blk(unionConstructors());
        if (!allProperties.isEmpty()) {
            bb.blk(copyConstructor());
        }
        if (properties.isEmpty() && !parentProperties.isEmpty()) {
            bb.blk(parentConstructor());
        }
        bb.nl().append(generateStringValue());
        return bb;
    }

    private @Nullable BlockBuilder unionConstructors() {
        if (finalProperties.isEmpty()) {
            return null;
        }

        final var simpleName = type().simpleName();
        final var bb = new BlockBuilder().nl();
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
                bb.str("    super(").str(asArguments(parentProperties)).eol(");");
            }

            final var restrictions = restrictionsForSetter(actualType);
            if (restrictions != null) {
                final var checkArg = checkArgument(property, restrictions, actualType, propFieldName);
                if (!checkArg.isEmpty()) {
                    bb.append(checkArg);
                    bb.newLine();
                }
            }

            for (var other : finalProperties) {
                bb.str("    this.");
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

        final var bb = new BlockBuilder();
        final var it = parentProperties.iterator();
        final var simpleName = type().simpleName();
        while (true) {
            final var prop = it.next();
            final var fieldName = fieldName(prop);
            final var propType = importedReturnType(prop);
            bb
                .str("public ").str(simpleName).str("(").str(propType).sp().str(fieldName).str(")").oB()
                    .ind("super(").str(fieldName).eol(");")
                .cB();

            if (!it.hasNext()) {
                return bb;
            }
            bb.newLine();
        }
    }

    // FIXME: return a BlockBuilder
    private StringBuilder generateStringValue() {
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
            final var fqcn = type.canonicalName();

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
            .append("}\n");
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
    BlockBuilder copyConstructor() {
        final var type = type();
        final var simpleName = type.simpleName();

        final var bb = new BlockBuilder().txt("""
                  /**
                   * Creates a copy from Source Object.
                   *
                   * @param source Source object
                   */
                  """)
            .str("public ").str(simpleName).str("(").str(simpleName).str(" source)").oB();
        if (!parentProperties.isEmpty()) {
            bb.eol("    super(source);");
        }
        for (var prop : properties) {
            final var fieldName = fieldName(prop);
            bb.str("    this.").str(fieldName).str(" = ");
            // TODO: figure out a better flow
            if (isArrayProperty(prop)) {
                bb.str(importedName(CODEHELPERS)).str(".copyArray(source.").str(fieldName).str(")");
            } else {
                bb.str("source.").str(fieldName);
            }
            bb.eS();
        }
        return bb.cB();
    }

    @Override
    BlockBuilder defaultInstance() {
        return null;
    }
}
