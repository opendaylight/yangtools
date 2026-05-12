/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.model.ri.TypeConstants.VALID_NAMES_NAME;
import static org.opendaylight.yangtools.binding.model.ri.Types.STRING;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.BitsTypeObject;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.api.BitsTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.ri.TypeConstants;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;

/**
 * A template for {@link BitsTypeObject} specializations.
 */
@NonNullByDefault
final class BitsTypeObjectTemplate extends ArchetypeTemplate<BitsTypeObjectArchetype> {
    record Builder(BitsTypeObjectArchetype type, DataRootArchetype root) implements Template.Builder {
        Builder {
            requireNonNull(type);
            requireNonNull(root);
        }

        @Override
        public BitsTypeObjectTemplate build() {
            return new BitsTypeObjectTemplate(type, root);
        }
    }

    private static final JavaTypeName BITS_TYPE_OBJECT = JavaTypeName.create(BitsTypeObject.class);
    private static final JavaTypeName IMMUTABLE_SET = JavaTypeName.create(ImmutableSet.class);

    private BitsTypeObjectTemplate(final GeneratedClass.Nested javaType, final BitsTypeObjectArchetype archetype,
            final DataRootArchetype root) {
        super(javaType, archetype, root);
    }

    private BitsTypeObjectTemplate(final BitsTypeObjectArchetype archetype, final DataRootArchetype root) {
        super(GeneratedClass.of(archetype), archetype, root);
    }

    static BlockBuilder generateInner(final GeneratedClass.Nested javaType, final BitsTypeObjectArchetype archetype,
            final DataRootArchetype root) {
        return new BitsTypeObjectTemplate(javaType, archetype, root).body(false);
    }

    @Override
    BlockBuilder body() {
        return body(true);
    }

    private BlockBuilder body(final boolean topLevel) {
        final var archetype = archetype();
        final var bb = newBodyBuilder(archetype.statement(), archetype.typeDefinition(), topLevel)
            .str("public").str(topLevel ? " " : " static ").str("class ").str(archetype.simpleName());

        final var superType = archetype.superType();
        if (superType != null) {
            bb.str(" extends ").str(importedName(superType.name()));
        } else {
            bb.str(" implements ").str(importedName(BITS_TYPE_OBJECT)).str(", java.io.Serializable");
        }
        bb
            .oB()
                .eol("@java.io.Serial")
                .str("private static final long serialVersionUID = ").jLong(archetype().serialVersionUID()).eS();

        // non-null indicates we have different valid names than super (or there is no super)
        final String immutableSet;
        final var bits = archetype.typeDefinition().getBits();
        if (superType == null || bits.size() != superType.typeDefinition().getBits().size()) {
            immutableSet = importedName(IMMUTABLE_SET);
            bb
                .nl()
                .str("protected static final ").gen(immutableSet, importedName(STRING))
                    .str(" " + VALID_NAMES_NAME + " = ").str(immutableSet).str(".of(");

            // FIXME: we know there is at least one bit, so use an iterator and a the usual convoluted block
            boolean first = true;
            for (var bit : bits) {
                if (first) {
                    first = false;
                } else {
                    bb.str(", ");
                }
                bb.jStr(bit.getName());
            }
            bb.eol(");");
        } else {
            immutableSet = null;
        }

        // end of constants: ensure separation
        bb.newLine();

        // assign property names and sort them into
        final var props = bits.stream()
            .map(bit -> Map.entry(Naming.getPropertyName(bit.getName()), bit))
            .sorted(Comparator.comparing(Map.Entry::getKey))
            .collect(Collectors.toUnmodifiableList());

        // generate fields if there is no superclass
        if (superType == null) {
            for (var prop : props) {
                bb.str("private final boolean ").str(prop.getKey()).eS();
            }
            bb.newLine();
        }

        // FIXME: public constructor with either field init or super invocation
        // FIXME: super copy constructor if needed

        return bb.cB();
    }

    @Override
    void appendValidNames(final BlockBuilder bb) {
        for (var c : type().getConstantDefinitions()) {
            if (TypeConstants.VALID_NAMES_NAME.equals(c.getName())) {
                bb.nl().blk(validNamesAndValues((BitsTypeDefinition) c.getValue()));
            }
        }
    }

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
