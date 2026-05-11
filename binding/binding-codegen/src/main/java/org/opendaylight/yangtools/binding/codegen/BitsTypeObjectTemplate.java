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
import org.opendaylight.yangtools.binding.BitsTypeObject;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.api.BitsTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.ri.TypeConstants;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;

/**
 * A template for {@link BitsTypeObject} specializations.
 */
final class BitsTypeObjectTemplate extends ClassTemplate {
    @NonNullByDefault
    record Builder(BitsTypeObjectArchetype type) implements Template.Builder {
        Builder {
            requireNonNull(type);
        }

        @Override
        public BitsTypeObjectTemplate build() {
            return new BitsTypeObjectTemplate(type);
        }
    }

    @NonNullByDefault
    private BitsTypeObjectTemplate(final GeneratedClass.Nested javaType, final BitsTypeObjectArchetype archetype) {
        super(javaType, archetype);
    }

    @NonNullByDefault
    private BitsTypeObjectTemplate(final BitsTypeObjectArchetype archetype) {
        super(GeneratedClass.of(archetype), archetype);
    }

    @NonNullByDefault
    static BlockBuilder generateAsInner(final GeneratedClass.Nested javaType, final BitsTypeObjectArchetype archetype) {
        return new BitsTypeObjectTemplate(javaType, archetype).generateAsInnerClass();
    }

    @Override
    void appendValidNames(final BlockBuilder bb) {
        for (var c : type().getConstantDefinitions()) {
            if (TypeConstants.VALID_NAMES_NAME.equals(c.getName())) {
                bb.nl().blk(validNamesAndValues((BitsTypeDefinition) c.getValue()));
            }
        }
    }

    @NonNullByDefault
    private BlockBuilder validNamesAndValues(final BitsTypeDefinition typedef) {
        final var override = importedName(OVERRIDE);

        return newBlockBuilder()
            .nl()
            .at().eol(override)
            .str("public ").gen(importedName(IMMUTABLE_SET), importedName(Types.STRING)).str(" validNames()").oB()
                .eol("return " + TypeConstants.VALID_NAMES_NAME + ";")
            .cB()
            .nl()
            .at().eol(override)
            .str("public boolean[] values()").oB()
                .str("return new boolean[]").jBlock(bb -> {
                    final var bits = typedef.getBits();
                    if (bits.isEmpty()) {
                        bb.eol("// empty");
                        return;
                    }

                    final var it = bits.iterator();
                    while (true) {
                        final var bit = it.next();
                        bb.str(getterMethodName(Naming.getPropertyName(bit.getName()))).str("()");
                        if (!it.hasNext()) {
                            bb.nl();
                            break;
                        }
                        bb.eol(",");
                    }
                }).eS()
            .cB()
            .nl();
    }
}
