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
import org.opendaylight.yangtools.binding.generator.BindingGeneratorUtil;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.ri.Types;

/**
 * Template for {@link EnumTypeObject}s.
 */
@NonNullByDefault
final class EnumTypeObjectTemplate extends BaseTemplate {
    private static final JavaTypeName ENUM_TYPE_OBJECT = JavaTypeName.create(EnumTypeObject.class);

    private final EnumTypeObjectArchetype archetype;

    private EnumTypeObjectTemplate(final AbstractJavaGeneratedType javaType, final EnumTypeObjectArchetype archetype) {
        super(javaType, archetype);
        this.archetype = requireNonNull(archetype);
    }

    EnumTypeObjectTemplate(final EnumTypeObjectArchetype archetype) {
        super(archetype);
        this.archetype = requireNonNull(archetype);
    }

    static void generateAsInner(final AbstractJavaGeneratedType javaType, final EnumTypeObjectArchetype archetype,
            final BlockBuilder bb) {
        new EnumTypeObjectTemplate(javaType, archetype).appendBody(bb);
    }

    @Override
    BlockBuilder body() {
        final var bb = new BlockBuilder();
        appendBody(bb);
        return bb;
    }

    private void appendBody(final BlockBuilder bb) {
        // calculate imports up front
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
            appendAsJavadoc(bb, "", javadoc);
            bb.newLine();
        }

        bb
            .eol(generatedAnnotation())
            .str("public enum ").str(typeName).str(" implements ").str(enumTypeObject).append(" {\n");

        final var it = archetype.values().iterator();
        if (it.hasNext()) {
            while (true) {
                final var value = it.next();

                value.getDescription().ifPresent(desc -> {
                    final var doc = encodeJavadocSymbols(BindingGeneratorUtil.encodeAngleBrackets(desc.trim()));
                    if (!doc.isEmpty()) {
                        appendAsJavadoc(bb, "    ", doc);
                        bb.newLine();
                    }
                });
                bb.str("    ").str(value.constantName()).str("(").strI(value.value()).str(", \"").str(value.name())
                    .append("\")");

                if (!it.hasNext()) {
                    break;
                }

                bb.append(",\n");
            }

            bb.append(";\n\n");
        }

        bb
            .str("    private final ").str(nonnullString).str(" name;").nl()
            .str("    private final int value;").nl()
            .nl()
            .str("    private ").str(typeName).str("(int value, ").str(nonnullString).str(" name) {").nl()
            .str("        this.value = value;").nl()
            .str("        this.name = name;").nl()
            .str("    }").nl()
            .nl()
            .str("    @").str(override).nl()
            .str("    public ").str(string).str(" getName() {").nl()
            .str("        return name;").nl()
            .str("    }").nl()
            .nl()
            .str("    @").str(override).nl()
            .str("    public int getIntValue() {").nl()
            .str("        return value;").nl()
            .str("    }").nl()
            .nl()
            .str("    /**").nl()
            .str("     * Return the enumeration member whose {@link #getName()} matches specified assigned name.").nl()
            .str("     *").nl()
            .str("     * @param name YANG assigned name").nl()
            .str("     * @return corresponding ").str(typeName).str(" item, or {@code null} if no such item exists")
                .nl()
            .str("     * @throws ").str(npe).str(" if {@code name} is null").nl()
            .str("     */").nl()
            .str("    public static ").str(nullableSelf).str(" forName(").str(string).str(" name) {").nl()
            .str("        return switch (name) {").newLine();
        for (var value : archetype.values()) {
            bb.str("            case \"").str(value.name()).str("\" -> ").str(value.constantName()).str(";").newLine();
        }
        bb
            .str("            default -> null;").nl()
            .str("        };").nl()
            .str("    }").nl()
            .nl()
            .str("    /**").nl()
            .str("     * Return the enumeration member whose {@link #getIntValue()} matches specified value.").nl()
            .str("     *").nl()
            .str("     * @param intValue integer value").nl()
            .str("     * @return corresponding ").str(typeName).str(" item, or {@code null} if no such item exists")
                .nl()
            .str("     */").nl()
            .str("    public static ").str(nullableSelf).str(" forValue(int intValue) {").nl()
            .str("        return switch (intValue) {").newLine();
        for (var value : archetype.values()) {
            bb.str("            case ").strI(value.value()).str(" -> ").str(value.constantName()).append(";\n");
        }
        bb
            .str("            default -> null;").nl()
            .str("        };").nl()
            .str("    }").nl()
            .nl()
            .str("    /**").nl()
            .str("     * Return the enumeration member whose {@link #getName()} matches specified assigned name.").nl()
            .str("     *").nl()
            .str("     * @param name YANG assigned name").nl()
            .str("     * @return corresponding ").str(typeName).str(" item").nl()
            .str("     * @throws ").str(npe).str(" if {@code name} is null").nl()
            .str("     * @throws ").str(iae).str(" if {@code name} does not match any item").nl()
            .str("     */").nl()
            .str("    public static ").str(nonnullSelf).str(" ofName(").str(string).str(" name) {").nl()
            .str("        return ").str(codeHelpers).str(".checkEnum(forName(name), name);").nl()
            .str("    }").nl()
            .nl()
            .str("    /**").nl()
            .str("     * Return the enumeration member whose {@link #getIntValue()} matches specified value.").nl()
            .str("     *").nl()
            .str("     * @param intValue integer value").nl()
            .str("     * @return corresponding ").str(typeName).str(" item").nl()
            .str("     * @throws ").str(iae).str(" if {@code intValue} does not match any item").nl()
            .str("     */").nl()
            .str("    public static ").str(nonnullSelf).str(" ofValue(int intValue) {").nl()
            .str("        return ").str(codeHelpers).str(".checkEnum(forValue(intValue), intValue);").nl()
            .str("    }").nl()
            .str("}").newLine();
    }
}
