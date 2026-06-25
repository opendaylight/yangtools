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
import static org.opendaylight.yangtools.binding.model.ri.BindingTypes.UNION_TYPE_OBJECT;
import static org.opendaylight.yangtools.binding.model.ri.Types.STRING;

import com.google.common.collect.Iterables;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.UnionTypeObject;
import org.opendaylight.yangtools.binding.model.api.BitsTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.IdentityArchetype;
import org.opendaylight.yangtools.binding.model.api.Restrictions;
import org.opendaylight.yangtools.binding.model.api.ScalarTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.UnionTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.ri.TypeConstants;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;

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
    private final @NonNull Restrictions restrictions;

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

        // FIXME: YANGTOOLS-1621: this is utterly defunct
        rangeGenerator = restrictions != null && restrictions.getRangeConstraint().isPresent()
            ? requireNonNull(AbstractRangeGenerator.forType(encapsulatedValueType(archetype))) : null;
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
    private static List<GeneratedProperty> propertiesOfAllParents(final UnionTypeObjectArchetype gto) {
        final var superType = gto.getSuperType();
        return superType == null ? List.of() : streamAllProperties(superType).collect(Collectors.toUnmodifiableList());
    }

    @NonNullByDefault
    private static Stream<GeneratedProperty> streamAllProperties(final UnionTypeObjectArchetype gto) {
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
        final var archetype = archetype();
        final var statement = archetype.statement();

        final var bb = newBodyBuilder(statement, statement.typeStatement().typeDefinition(), !isInnerClass)
            .frg(generateClassDeclaration(isInnerClass)).oB()
                .eol("@java.io.Serial")
                .str("private static final long serialVersionUID = ").jLong(archetype().serialVersionUID()).eS()
                 // inner classes
                .blk(generateInnerClasses(root, archetype.getEnclosedTypes()));

        if (statement instanceof TypedefEffectiveStatement typedef) {
            final var units = typedef.unitsStatement();
            if (units != null) {
                bb.str("public static final String UNITS = ").jString(units.argument()).eS();
            }
        }

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

        // FIXME: YANGTOOLS-1621: this is utterly defunct
        // length/range checkers
        if (restrictions != null) {
            final var length = restrictions.getLengthConstraint();
            if (length.isPresent()) {
                bb.nl().blk(LengthGenerator.generateLengthChecker("_value",
                    encapsulatedValueType(archetype()), length.orElseThrow(), javaType()));
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

    @Deprecated(since = "16.0.0", forRemoval = true)
    @NonNullByDefault
    private static Type encapsulatedValueType(final GeneratedTransferObject<?> gto) {
        return gto.findProperty(TypeConstants.VALUE_PROP).orElseThrow().getReturnType();
    }

    /**
     * {@return string with class declaration in JAVA format}
     * @param isInnerClass boolean value which specify if generated class is|isn't inner
     */
    @NonNullByDefault
    private BlockBuilder generateClassDeclaration(final boolean isInnerClass) {
        final var archetype = archetype();

        final var bb = newBlockBuilder()
            .str("public ");
        if (isInnerClass) {
            bb.str("static final ");
        }
        bb.str("class ").str(archetype.simpleName());

        final var superType = archetype.getSuperType();
        if (superType != null) {
            bb.str(" extends ").str(importedName(superType));
        } else {
            bb.str(" implements ").str(importedName(UNION_TYPE_OBJECT)).str(", java.io.Serializable");
        }

        return bb;
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

            if (!restrictions.isEmpty()) {
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

    /**
     * {@return string with the list of the parameter names of the {@code parameters}, separated by {@code ", "}}
     * @param parameters non-empty group of generated property instances which are transformed to the sequence
     *                   of parameter names, must not be empty
     */
    private static @NonNull String asArguments(final @NonNull List<GeneratedProperty> parameters) {
        final var sb = new StringBuilder();
        final var it = parameters.iterator();
        while (true) {
            sb.append(fieldName(it.next()));
            if (!it.hasNext()) {
                return sb.toString();
            }
            sb.append(", ");
        }
    }

    /**
     * Template method which generates method parameters with their types from {@code parameters}.
     *
     * @param parameters group of generated property instances which are transformed to the method parameters
     * @return string with the list of the method parameters with their types in Java format
     */
    private @NonNull String asArgumentsDeclaration(final @NonNull Iterable<GeneratedProperty> parameters) {
        final var it = parameters.iterator();
        if (!it.hasNext()) {
            return "";
        }

        final var sb = new StringBuilder();
        while (true) {
            final var parameter = it.next();
            sb.append(importedReturnType(parameter)).append(' ').append(fieldName(parameter));
            if (!it.hasNext()) {
                return sb.toString();
            }
            sb.append(", ");
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
            .cB();
    }
}
