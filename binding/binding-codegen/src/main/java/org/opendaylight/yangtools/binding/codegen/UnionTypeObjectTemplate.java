/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.opendaylight.yangtools.binding.codegen.TypeNames.CODEHELPERS;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.JU_ARRAYS;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.JU_BASE64;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.JU_OBJECTS;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.STRING;
import static org.opendaylight.yangtools.binding.contract.Naming.BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME;

import com.google.common.collect.Iterables;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
import org.opendaylight.yangtools.binding.model.api.IdentityArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.ScalarTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.UnionTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.UnionTypeObjectArchetype.Tag;
import org.opendaylight.yangtools.binding.model.ri.BaseYangTypes;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;

/**
 * A template for {@link UnionTypeObject} specializations.
 */
final class UnionTypeObjectTemplate extends ArchetypeTemplate<@NonNull UnionTypeObjectArchetype> {
    private static final @NonNull JavaTypeName UNION_TYPE_OBJECT = JavaTypeName.create(UnionTypeObject.class);
    private static final Comparator<Tag> TAG_COMPARATOR = Comparator.comparing(Tag::name);

    private final @NonNull List<Tag> allProperties;
    private final @NonNull List<Tag> finalProperties;
    private final @NonNull List<Tag> parentProperties;
    private final @NonNull List<Tag> properties;

    @NonNullByDefault
    private UnionTypeObjectTemplate(final GeneratedClass javaType, final UnionTypeObjectArchetype archetype,
            final DataRootArchetype root) {
        super(javaType, archetype, root);
        properties = archetype.tags();
        finalProperties = properties.stream().toList();
        parentProperties = propertiesOfAllParents(archetype);

        allProperties = Stream.concat(properties.stream(), parentProperties.stream()).sorted(TAG_COMPARATOR).toList();
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
    private static List<Tag> propertiesOfAllParents(final UnionTypeObjectArchetype gto) {
        final var superType = gto.getSuperType();
        return superType == null ? List.of() : streamAllProperties(superType).collect(Collectors.toUnmodifiableList());
    }

    @NonNullByDefault
    private static Stream<Tag> streamAllProperties(final UnionTypeObjectArchetype gto) {
        final var stream = gto.tags().stream();
        final var superType = gto.getSuperType();
        return superType == null ? stream : Stream.concat(stream, streamAllProperties(superType));
    }

    @NonNullByDefault
    static UnionTypeObjectTemplate of(final DataRootArchetype root, final UnionTypeObjectArchetype archetype) {
        return new UnionTypeObjectTemplate(GeneratedClass.of(archetype), archetype, root);
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
        final var statement = archetype.statement();

        final var bb = newBodyBuilder(statement, statement.typeStatement().typeDefinition(), !isInnerClass)
            .frg(generateClassDeclaration(isInnerClass)).oB()
                .eol("@java.io.Serial")
                .str("private static final long serialVersionUID = ").jLong(serialVersionUID(archetype)).eS()
                 // inner classes
                .blk(generateInnerClasses(root, archetype.enclosedTypes()));

        if (statement instanceof TypedefEffectiveStatement typedef) {
            final var units = typedef.unitsStatement();
            if (units != null) {
                bb.str("public static final String UNITS = ").jString(units.argument()).eS();
            }
        }

        // fields
        if (!properties.isEmpty()) {
            for (var field : properties) {
                bb.str("private final ").str(importedName(field.type())).str(" _").str(field.name()).eS();
            }
        }

        bb
            .blk(constructors())
            .blk(propertyMethods());

        if (archetype.getSuperType() == null) {
            // FIXME: YANGTOOLS-1621: here we want to specialize for the single tagged value we carry
            KeyTemplate.appendEquality(bb, javaType(),
                properties.stream().map(prop -> Map.entry(prop.name(), prop.type())).toList(), true);
        }

        return bb.cB().nl();
    }

    /**
     * {@return string with class declaration in JAVA format}
     * @param isInnerClass boolean value which specify if generated class is|isn't inner
     */
    @NonNullByDefault
    private BlockBuilder generateClassDeclaration(final boolean isInnerClass) {
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

        final var simpleName = archetype.simpleName();
        final var bb = newBlockBuilder().nl();
        final var it = finalProperties.iterator();
        while (true) {
            final var property = it.next();
            final var actualType = property.type();
            final var propertyAndTopParentProperties = Iterables.concat(parentProperties, List.of(property));
            final var propFieldName = fieldName(property);

            final var setterRestrictions = restrictionsForSetter(actualType);
            if (setterRestrictions != null) {
                bb.blk(generateCheckers(property.name(), setterRestrictions, actualType)).newLine();
            }

            bb
                .str("public ").str(simpleName).str("(").str(asArgumentsDeclaration(propertyAndTopParentProperties))
                    .str(")").oB();
            if (!parentProperties.isEmpty()) {
                bb.str("super(").str(asArguments(parentProperties)).eol(");");
            }

// FIXME: YANGTOOLS-1621: this relies of fields being defined, which we
//            if (setterRestrictions != null) {
//                bb.blk(checkFieldValue(property, setterRestrictions, actualType, propFieldName)).newLine();
//            }

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
     * @param tags non-empty group of generated property instances which are transformed to the sequence
     *                   of parameter names, must not be empty
     */
    private static @NonNull String asArguments(final @NonNull List<Tag> tags) {
        final var sb = new StringBuilder();
        final var it = tags.iterator();
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
     * @param tags group of generated property instances which are transformed to the method parameters
     * @return string with the list of the method parameters with their types in Java format
     */
    private @NonNull String asArgumentsDeclaration(final @NonNull Iterable<Tag> tags) {
        final var it = tags.iterator();
        if (!it.hasNext()) {
            return "";
        }

        final var sb = new StringBuilder();
        while (true) {
            final var tag = it.next();
            sb.append(importedName(tag.type())).append(" _").append(tag.name());
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
        final var simpleName = archetype.simpleName();
        while (true) {
            final var prop = it.next();
            final var fieldName = fieldName(prop);
            final var propType = importedName(prop.type());
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
            final var type = prop.type();
            final var fqcn = type.canonicalName();

            bb
                .str("if (").str(field).str(" != null)").oB()
                    .str("return ");

            if (BaseYangTypes.STRING_TYPE.equals(type)) {
                // type string
                bb.str(field).eS();
            } else if ("org.opendaylight.yangtools.binding.BindingInstanceIdentifier".equals(fqcn)) {
                // type instance-identifier
                bb.str(field).eol(".toString();");
            } else if (BaseYangTypes.BINARY_TYPE.equals(type)) {
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
            } else if (BaseYangTypes.BOOLEAN_TYPE.equals(typedefReturnType(type))) {
                // generated boolean typedef
                bb.str(field).eol(".isValue().toString();");
            } else if (BaseYangTypes.BINARY_TYPE.equals(typedefReturnType(type))) {
                // generated byte[] typedef
                bb.str(importedName(JU_BASE64)).str(".getEncoder().encodeToString(").str(field).eol(".getValue());");
            } else if (BaseYangTypes.EMPTY_TYPE.equals(type)
                       || BaseYangTypes.EMPTY_TYPE.equals(typedefReturnType(type))) {
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
        final var simpleName = archetype.simpleName();

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
                    // FIXME: check for BaseYangTypes.BINARY instead
                    if (prop.type().isArray()) {
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
            final var tag = it.next();
            bb.nl().blk(asGetterMethod(tag.name(), tag.type()));
        } while (it.hasNext());
        return bb;
    }

    @NonNullByDefault
    private BlockBuilder parentConstructor() {
        final var importedSuper = importedName(archetype.getSuperType());

        return newBlockBuilder()
            .eol("/**")
            .str(" * Creates a new instance from ").eol(importedSuper)
            .eol(" *")
            .eol(" * @param source Source object")
            .eol(" */")
            .str("public ").str(archetype.simpleName()).str("(").str(importedSuper).str(" source)").oB()
                .eol("super(source);")
            .cB();
    }

    @NonNullByDefault
    private static long serialVersionUID(final UnionTypeObjectArchetype archetype) {
        final var svb = new SerialVersionHelper(archetype.name())
            .setAbstract(false)
            .addInterface(SerialVersionHelper.SERIALIZABLE);

        archetype.typePropertyNames().stream().distinct().forEach(svb::addField);

        return svb.computeSerialVersion();
    }

    private static String fieldName(final Tag tag) {
        return fieldName(tag.name());
    }
}
