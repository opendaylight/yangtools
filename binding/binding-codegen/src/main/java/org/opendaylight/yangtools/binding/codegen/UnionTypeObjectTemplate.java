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
import static org.opendaylight.yangtools.binding.model.ri.Types.STRING;

import com.google.common.collect.Iterables;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.UnionTypeObject;
import org.opendaylight.yangtools.binding.model.api.BitsTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.IdentityArchetype;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.UnionTypeObjectArchetype;

/**
 * A template for {@link UnionTypeObject} specializations.
 */
final class UnionTypeObjectTemplate extends ClassTemplate {
    @NonNullByDefault
    record Builder(UnionTypeObjectArchetype type) implements Template.Builder {
        Builder {
            requireNonNull(type);
        }

        @Override
        public UnionTypeObjectTemplate build() {
            return new UnionTypeObjectTemplate(type);
        }
    }

    @NonNullByDefault
    private UnionTypeObjectTemplate(final GeneratedClass.Nested javaType, final UnionTypeObjectArchetype archetype) {
        super(javaType, archetype);
    }

    @NonNullByDefault
    private UnionTypeObjectTemplate(final UnionTypeObjectArchetype archetype) {
        super(GeneratedClass.of(archetype), archetype);
    }

    @NonNullByDefault
    static BlockBuilder generateAsInner(final GeneratedClass.Nested javaType,
            final UnionTypeObjectArchetype archetype) {
        return new UnionTypeObjectTemplate(javaType, archetype).generateAsInnerClass();
    }

    @Override
    BlockBuilder constructors() {
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

            final var restrictions = restrictionsForSetter(actualType);
            if (restrictions != null) {
                bb.blk(checkArgument(property, restrictions, actualType, propFieldName)).newLine();
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

    @Override
    BlockBuilder defaultInstance() {
        return null;
    }
}
