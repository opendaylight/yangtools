/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.EnumTypeObject;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.ri.DocUtils;
import org.opendaylight.yangtools.binding.model.ri.Types;

/**
 * Template for {@link EnumTypeObject}s.
 */
@NonNullByDefault
final class EnumTypeObjectTemplate extends ArchetypeTemplate<EnumTypeObjectArchetype> {
    record Builder(EnumTypeObjectArchetype type, DataRootArchetype root) implements Template.Builder {
        Builder {
            requireNonNull(type);
            requireNonNull(root);
        }

        @Override
        public EnumTypeObjectTemplate build() {
            return new EnumTypeObjectTemplate(GeneratedClass.of(type), type, root);
        }
    }

    private static final JavaTypeName ENUM_TYPE_OBJECT = JavaTypeName.create(EnumTypeObject.class);

    private EnumTypeObjectTemplate(final GeneratedClass javaType, final EnumTypeObjectArchetype archetype,
            final DataRootArchetype root) {
        super(javaType, archetype, root);
    }

    static void generateAsInner(final GeneratedClass javaType, final EnumTypeObjectArchetype archetype,
            final DataRootArchetype root, final BlockBuilder bb) {
        new EnumTypeObjectTemplate(javaType, archetype, root).appendBody(bb);
    }

    @Override
    BlockBuilder body() {
        final var bb = newBlockBuilder();
        appendBody(bb);
        return bb;
    }

    private void appendBody(final BlockBuilder bb) {
        // calculate imports up front
        final var archetype = archetype();
        final var codeHelpers = importedName(CODEHELPERS);
        final var enumTypeObject = importedName(ENUM_TYPE_OBJECT);
        final var iae = importedName(IAE);
        final var nonnullSelf = importedNonNull(archetype);
        final var nonnullString = importedNonNull(Types.STRING);
        final var npe = importedName(NPE);
        final var nullableSelf = importedNullable(archetype);
        final var override = importedName(OVERRIDE);
        final var string = importedName(Types.STRING);
        final var typeName = archetype.simpleName();

        // now build the body
        final var javadoc = formatDataForJavaDoc(archetype);
        if (!javadoc.isBlank()) {
            appendAsJavadoc(bb, javadoc);
            bb.newLine();
        }

        bb
            .eol(generatedAnnotation())
            .str("public enum ").str(typeName).str(" implements ").str(enumTypeObject).oB();

        final var it = archetype.values().iterator();
        if (it.hasNext()) {
            while (true) {
                final var value = it.next();

                value.getDescription().ifPresent(desc -> {
                    final var doc = encodeJavadocSymbols(DocUtils.encodeAngleBrackets(desc.trim()));
                    if (!doc.isEmpty()) {
                        appendAsJavadoc(bb, doc);
                        bb.newLine();
                    }
                });
                bb.str(value.constantName()).str("(").jInt(value.value()).str(", ").jStr(value.name()).str(")");

                if (!it.hasNext()) {
                    break;
                }

                bb.eol(",");
            }

            bb.eS();
        }

        bb
            .str("private final ").str(nonnullString).eol(" name;")
            .eol("private final int value;")
            .nl()
            .str("private ").str(typeName).str("(int value, ").str(nonnullString).str(" name)").oB()
                .eol("this.value = value;")
                .eol("this.name = name;")
            .cB()
            .nl()
            .at().eol(override)
            .str("public ").str(string).str(" getName()").oB()
                .eol("return name;")
            .cB()
            .nl()
            .at().eol(override)
            .str("public int getIntValue()").oB()
                .eol("return value;")
            .cB()
            .nl()
            .eol("/**")
            .eol(" * Return the enumeration member whose {@link #getName()} matches specified assigned name.")
            .eol(" *")
            .eol(" * @param name YANG assigned name")
            .str(" * @return corresponding ").str(typeName).eol(" item, or {@code null} if no such item exists")
            .str(" * @throws ").str(npe).eol(" if {@code name} is null")
            .eol(" */")
            .str("public static ").str(nullableSelf).str(" forName(").str(string).str(" name)").oB()
                .str("return switch (name)").oB();
        for (var value : archetype.values()) {
            bb.str("case ").jStr(value.name()).str(" -> ").str(value.constantName()).eS();
        }
        bb
            .eol("default -> null;")
            .cb().eS()
            .cB()
            .nl()
            .eol("/**")
            .eol(" * Return the enumeration member whose {@link #getIntValue()} matches specified value.")
            .eol(" *")
            .eol(" * @param intValue integer value")
            .str(" * @return corresponding ").str(typeName).eol(" item, or {@code null} if no such item exists")
            .eol(" */")
            .str("public static ").str(nullableSelf).str(" forValue(int intValue)").oB()
                .str("return switch (intValue)").oB();
        for (var value : archetype.values()) {
            bb.str("case ").jInt(value.value()).str(" -> ").str(value.constantName()).eS();
        }
        bb
            .eol("default -> null;")
            .cb().eS()
            .cB()
            .nl()
            // FIXME: txt()
            .eol("/**").nl()
            .str(" * Return the enumeration member whose {@link #getName()} matches specified assigned name.").nl()
            .str(" *").nl()
            .str(" * @param name YANG assigned name").nl()
            .str(" * @return corresponding ").str(typeName).str(" item").nl()
            .str(" * @throws ").str(npe).str(" if {@code name} is null").nl()
            .str(" * @throws ").str(iae).str(" if {@code name} does not match any item").nl()
            .str(" */").nl()
            .str("public static ").str(nonnullSelf).str(" ofName(").str(string).str(" name)").oB()
                .str("return ").str(codeHelpers).eol(".checkEnum(forName(name), name);")
            .cB()
            .nl()
            // FIXME: txt()
            .str("/**").nl()
            .str(" * Return the enumeration member whose {@link #getIntValue()} matches specified value.").nl()
            .str(" *").nl()
            .str(" * @param intValue integer value").nl()
            .str(" * @return corresponding ").str(typeName).str(" item").nl()
            .str(" * @throws ").str(iae).str(" if {@code intValue} does not match any item").nl()
            .str(" */").nl()
            .str("public static ").str(nonnullSelf).str(" ofValue(int intValue)").oB()
                .str("return ").str(codeHelpers).eol(".checkEnum(forValue(intValue), intValue);")
            .cB()
            .cB();
    }
}
