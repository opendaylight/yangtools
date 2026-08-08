/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.opendaylight.yangtools.binding.codegen.TypeNames.CODEHELPERS;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.IAE;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.NPE;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.OVERRIDE;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.STRING;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.EnumTypeObject;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.ri.BaseYangTypes;

/**
 * Template for {@link EnumTypeObject}s.
 */
@NonNullByDefault
final class EnumTypeObjectTemplate extends ArchetypeTemplate<EnumTypeObjectArchetype> {
    private static final TypeName ENUM_TYPE_OBJECT = TypeName.ofClass(EnumTypeObject.class);

    private EnumTypeObjectTemplate(final GeneratedClass javaType, final EnumTypeObjectArchetype archetype,
            final DataRootArchetype root) {
        super(javaType, archetype, root);
    }

    static EnumTypeObjectTemplate of(final DataRootArchetype root, final EnumTypeObjectArchetype archetype) {
        return new EnumTypeObjectTemplate(GeneratedClass.of(archetype), archetype, root);
    }

    static BlockBuilder generateInner(final GeneratedClass javaType, final EnumTypeObjectArchetype archetype,
            final DataRootArchetype root) {
        return new EnumTypeObjectTemplate(javaType, archetype, root).body();
    }

    @Override
    BlockBuilder body() {
        final var simpleName = archetype.simpleName();

        final var bb = newBodyBuilder(archetype.statement(), archetype.typeDefinition())
            .str("public enum ").str(simpleName).str(" implements ").str(importedName(ENUM_TYPE_OBJECT)).oB();

        final var valueToConstant = archetype.valueToConstant();
        final var it = valueToConstant.entrySet().iterator();
        if (it.hasNext()) {
            while (true) {
                final var entry = it.next();
                final var pair = entry.getKey();
                pair.getDescription().ifPresent(desc -> {
                    final var doc = encodeJavadocSymbols(DocUtils.encodeAngleBrackets(desc.trim()));
                    if (!doc.isEmpty()) {
                        appendAsJavadoc(bb, doc);
                        bb.newLine();
                    }
                });
                bb.str(entry.getValue()).str("(").jInt(pair.getValue()).str(", ").jStr(pair.getName()).str(")");

                if (!it.hasNext()) {
                    break;
                }

                bb.eol(",");
            }

            bb.eS();
        }

        final var codeHelpers = importedName(CODEHELPERS);
        final var iae = importedName(IAE);
        final var nonnullSelf = importedNonNull(archetype);
        // FIXME: add a utility to work on TypeName for this
        final var nonnullString = importedNonNull(BaseYangTypes.STRING_TYPE);
        final var npe = importedName(NPE);
        final var nullableSelf = importedNullable(archetype);
        final var override = importedName(OVERRIDE);
        final var string = importedName(STRING);

        bb
            .str("private final ").str(nonnullString).eol(" name;")
            .eol("private final int value;")
            .nl()
            .str("private ").str(simpleName).str("(int value, ").str(nonnullString).str(" name)").oB()
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
            .str(" * @return corresponding ").str(simpleName).eol(" item, or {@code null} if no such item exists")
            .str(" * @throws ").str(npe).eol(" if {@code name} is null")
            .eol(" */")
            .str("public static ").str(nullableSelf).str(" forName(").str(string).str(" name)").oB()
                .str("return switch (name)").oB();
        for (var entry : valueToConstant.entrySet()) {
            bb.str("case ").jStr(entry.getKey().getName()).str(" -> ").str(entry.getValue()).eS();
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
            .str(" * @return corresponding ").str(simpleName).eol(" item, or {@code null} if no such item exists")
            .eol(" */")
            .str("public static ").str(nullableSelf).str(" forValue(int intValue)").oB()
                .str("return switch (intValue)").oB();
        for (var entry : valueToConstant.entrySet()) {
            bb.str("case ").jInt(entry.getKey().getValue()).str(" -> ").str(entry.getValue()).eS();
        }
        return bb
            .eol("default -> null;")
            .cb().eS()
            .cB()
            .nl()
            // FIXME: txt()
            .eol("/**").nl()
            .str(" * Return the enumeration member whose {@link #getName()} matches specified assigned name.").nl()
            .str(" *").nl()
            .str(" * @param name YANG assigned name").nl()
            .str(" * @return corresponding ").str(simpleName).str(" item").nl()
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
            .str(" * @return corresponding ").str(simpleName).str(" item").nl()
            .str(" * @throws ").str(iae).str(" if {@code intValue} does not match any item").nl()
            .str(" */").nl()
            .str("public static ").str(nonnullSelf).str(" ofValue(int intValue)").oB()
                .str("return ").str(codeHelpers).eol(".checkEnum(forValue(intValue), intValue);")
            .cB()
            .cB();
    }
}
