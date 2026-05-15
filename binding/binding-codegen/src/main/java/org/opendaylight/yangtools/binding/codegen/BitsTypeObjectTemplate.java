/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.contract.Naming.getPropertyName;
import static org.opendaylight.yangtools.binding.model.ri.Types.STRING;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.BitsTypeObject;
import org.opendaylight.yangtools.binding.model.api.BitsTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;

/**
 * A template for {@link BitsTypeObject} specializations.
 */
@NonNullByDefault
abstract sealed class BitsTypeObjectTemplate extends ArchetypeTemplate<BitsTypeObjectArchetype> {
    record Builder(BitsTypeObjectArchetype type, DataRootArchetype root) implements Template.Builder {
        Builder {
            requireNonNull(type);
            requireNonNull(root);
        }

        @Override
        public BitsTypeObjectTemplate build() {
            return BitsTypeObjectTemplate.of(GeneratedClass.of(type), type, root);
        }
    }

    private static final class Base extends BitsTypeObjectTemplate {
        private static final JavaTypeName BITS_TYPE_OBJECT = JavaTypeName.create(BitsTypeObject.class);

        Base(final GeneratedClass javaType, final BitsTypeObjectArchetype archetype, final DataRootArchetype root) {
            super(javaType, archetype, root);
        }

        @Override
        BlockFragment implFragment() {
            return bb -> bb.str(" implements ").str(importedName(BITS_TYPE_OBJECT)).str(", java.io.Serializable");
        }

        @Override
        void appendBody(final BlockBuilder bb, final Collection<? extends Bit> bits, final Map<String, Bit> props) {
            appendValidNamesConstant(bb, bits);

            // fields
            for (var propName : props.keySet()) {
                bb.str("private final boolean _").str(propName).eS();
            }
            bb.newLine();

            // default constructor
            final var archetype = archetype();
            openDefaultCtor(bb, archetype, props);
            for (var propName : props.keySet()) {
                bb.str("this._").str(propName).str(" = _").str(propName).eS();
            }
            bb.cB().newLine();

            // copy constructor: only needed if have subclasses
            if (nonFinal(archetype)) {
                final var simpleName = archetype.simpleName();
                bb.str("protected ").str(simpleName).str("(").str(simpleName).str(" source)").oB();
                for (var propName : props.keySet()) {
                    bb.str("this._").str(propName).str(" = source._").str(propName).eS();
                }
                bb.cB().newLine();
            }

            appendGetDefaultInstance(bb, props);

            // getters
            for (var propName : props.keySet()) {
                bb
                    .nl()
                    .str("public boolean ").str(getterMethodName(propName)).str("()").oB()
                        .str("return _").str(propName).eS()
                    .cB();
            }

            appendValidNamesAndValues(bb, props);

            // hashCode()/equals()/toString()
            final var override = importedName(OVERRIDE);
            final var arrays = importedName(JU_ARRAYS);
            bb
                .nl()
                .at().eol(override)
                .str("public final int hashCode()").oB()
                    .str("return ").str(arrays).eol(".hashCode(values());")
                .cB()
                .nl()
                .at().eol(override)
                .str("public final boolean equals(Object obj)").oB()
                    .str("return this == obj || obj instanceof ").str(archetype().simpleName()).str(" other");
            for (var propName : props.keySet()) {
                bb
                    .nl()
                    .str("    && _").str(propName).str(" == other._").str(propName);
            }
            bb
                .eS()
                .cB()
                .nl()
                .at().eol(override)
                .str("public final String toString()").oB()
                    .str("return ").str(importedName(CODEHELPERS)).str(".jcTSB(getClass())");
            for (var entry : props.entrySet()) {
                bb
                    .nl()
                    .str("    .bit(").jStr(entry.getValue().getName()).str(", _").str(entry.getKey()).str(")");
            }
            bb
                .nl()
                .eol("    .build();")
                .cB();
        }
    }

    private static final class Derived extends BitsTypeObjectTemplate {
        private final BitsTypeObjectArchetype superType;

        Derived(final GeneratedClass javaType, final BitsTypeObjectArchetype archetype, final DataRootArchetype root,
                final BitsTypeObjectArchetype superType) {
            super(javaType, archetype, root);
            this.superType = requireNonNull(superType);
        }

        @Override
        BlockFragment implFragment() {
            return bb -> bb.str(" extends ").str(importedName(superType.name()));
        }

        @Override
        void appendBody(final BlockBuilder bb, final Collection<? extends Bit> bits, final Map<String, Bit> props) {
            final var superProps = computeProperties(superType.typeDefinition().getBits());
            final var override = props.size() == superProps.size() ? null : importedName(OVERRIDE);

            if (override != null) {
                appendValidNamesConstant(bb, bits);
            }

            // default constructor
            final var archetype = archetype();
            openDefaultCtor(bb, archetype, props);
            bb.str("super(");
            final var sit = superProps.keySet().stream().sorted().iterator();
            while (sit.hasNext()) {
                final var propName = sit.next();
                bb.str(props.containsKey(propName) ? "_" + propName : "false");
                if (!sit.hasNext()) {
                    break;
                }
                bb.str(", ");
            }
            bb.eol(");").cB().newLine();

            // copy constructor
            var rootType = superType;
            while (true) {
                final var nextSuper = rootType.superType();
                if (nextSuper == null) {
                    break;
                }
                rootType = nextSuper;
            }
            bb
                .str("public ").str(archetype.simpleName()).str("(").str(importedName(rootType.name())).str(" source)")
                    .oB()
                    .eol("super(source);");
            if (override != null) {
                // check whether any of the restricted bits are set
                final var codeHelpers = importedName(CODEHELPERS);
                for (var entry : superProps.entrySet()) {
                    final var propName = entry.getKey();
                    if (!props.containsKey(propName)) {
                        bb
                            .str(codeHelpers).str(".checkBit(").jStr(entry.getValue().getName()).str(", source.")
                                .str(getterMethodName(propName)).eol("());");
                    }
                }
            }
            bb.cB().newLine();

            appendGetDefaultInstance(bb, props);

            if (override != null) {
                // override getters for invalid bits
                final var deprecated = importedName(DEPRECATED);
                for (var propName : superProps.keySet()) {
                    if (!props.containsKey(propName)) {
                        bb
                            .nl()
                            .at().eol(override)
                            .at().str(deprecated).eol("(forRemoval = true)")
                            .str("public final boolean ").str(getterMethodName(propName)).str("()").oB()
                                .eol("return false;")
                            .cB();
                    }
                }

                appendValidNamesAndValues(bb, props);
            }
        }
    }

    private static final JavaTypeName IMMUTABLE_SET = JavaTypeName.create(ImmutableSet.class);
    private static final String VALID_NAMES_NAME = "VALID_NAMES";

    BitsTypeObjectTemplate(final GeneratedClass javaType, final BitsTypeObjectArchetype archetype,
            final DataRootArchetype root) {
        super(javaType, archetype, root);
    }

    private static BitsTypeObjectTemplate of(final GeneratedClass javaType, final BitsTypeObjectArchetype archetype,
            final DataRootArchetype root) {
        final var superType = archetype.superType();
        return superType == null ? new Base(javaType, archetype, root)
            : new Derived(javaType, archetype, root, superType);
    }

    static BlockBuilder generateInner(final GeneratedClass.Nested javaType, final BitsTypeObjectArchetype archetype,
            final DataRootArchetype root) {
        return of(javaType, archetype, root).body(false);
    }

    @Override
    final BlockBuilder body() {
        return body(true);
    }

    private BlockBuilder body(final boolean topLevel) {
        final var archetype = archetype();
        final var simpleName = archetype.simpleName();

        final var bb = newBodyBuilder(archetype.statement(), archetype.typeDefinition(), topLevel)
            .str("public").str(modifiers(archetype, topLevel)).str("class ").str(simpleName).frg(implFragment()).oB()
                .eol("@java.io.Serial")
                .str("private static final long serialVersionUID = ").jLong(archetype().serialVersionUID()).eS()
                .nl();

        final var bits = archetype.typeDefinition().getBits();
        appendBody(bb, bits, computeProperties(bits));
        return bb.cB();
    }

    private static String modifiers(final BitsTypeObjectArchetype archetype, final boolean topLevel) {
        final var nonFinal = nonFinal(archetype);
        if (topLevel) {
            return nonFinal ? " " : " final ";
        }
        return nonFinal ? " static " : " static final ";
    }

    private static boolean nonFinal(final BitsTypeObjectArchetype archetype) {
        return archetype.statement() instanceof TypedefEffectiveStatement;
    }

    abstract BlockFragment implFragment();

    abstract void appendBody(BlockBuilder bb, Collection<? extends Bit> bits, Map<String, Bit> props);

    final void appendValidNamesConstant(final BlockBuilder bb, final Collection<? extends Bit> bits) {
        final var immutableSet = importedName(IMMUTABLE_SET);
        bb.str("protected static final ").gen(immutableSet, importedName(STRING)).str(" " + VALID_NAMES_NAME + " = ")
            .str(immutableSet).str(".of(");

        final var it = bits.iterator();
        while (true) {
            bb.jStr(it.next().getName());
            if (!it.hasNext()) {
                break;
            }
            bb.str(", ");
        }

        bb.eol(");").newLine();
    }

    private static void openDefaultCtor(final BlockBuilder bb, final BitsTypeObjectArchetype archetype,
            final Map<String, Bit> props) {
        final var alphaSorted = new ArrayList<>(props.keySet());
        alphaSorted.sort(Comparator.naturalOrder());

        bb.str("public ").str(archetype.simpleName()).str("(");
        var it = alphaSorted.iterator();
        while (true) {
            bb.str("boolean _").str(it.next());
            if (!it.hasNext()) {
                break;
            }
            bb.str(", ");
        }
        bb.str(")").oB();
    }

    final BlockBuilder appendGetDefaultInstance(final BlockBuilder bb, final Map<String, Bit> props) {
        final var simpleName = archetype().simpleName();
        final var alphaSorted = props.entrySet().stream()
            .sorted(Comparator.comparing(Map.Entry::getKey))
            .map(entry -> entry.getValue().getName())
            .collect(Collectors.toUnmodifiableList());

        bb
            .str("public static ").str(simpleName).str(" getDefaultInstance(String defaultValue)").oB()
                .str("var values = ").str(importedName(CODEHELPERS)).eol(".parseBitsDefaultValue(defaultValue,");
        // Note: we can use VALID_NAMES here, as the bit order is alpha-sorted.
        // FIXME: we really should be able to parse the bits by position and then just change the order in which we
        //        access the array
        final var it = alphaSorted.iterator();
        while (true) {
            bb.ind().jStr(it.next());
            if (!it.hasNext()) {
                break;
            }
            bb.eol(",");
        }
        bb
            .eol(");")
            .str("return new ").str(simpleName).eol("(");
        final var last = props.size() - 1;
        for (int i = 0; i < last; ++i) {
            bb.ind("values[").jInt(i).eol("],");
        }
        return bb
            .ind("values[").jInt(last).eol("]);")
            .cB();
    }

    final void appendValidNamesAndValues(final BlockBuilder bb, final Map<String, Bit> props) {
        final var override = importedName(OVERRIDE);

        bb
            .nl()
            .at().eol(override)
            .str("public ").gen(importedName(IMMUTABLE_SET), importedName(Types.STRING)).str(" validNames()").oB()
                .eol("return " + VALID_NAMES_NAME + ";")
            .cB()
            .nl()
            .at().eol(override)
            .str("public boolean[] values()").oB()
                .str("return new boolean[]").oB();

        final var it = props.keySet().iterator();
        while (true) {
            final var propName = it.next();
            bb.str(getterMethodName(propName)).str("()");
            if (!it.hasNext()) {
                break;
            }
            bb.eol(",");
        }
        bb
            .nl()
            .cb()
            .eS()
            .cB();
    }

    private static Map<String, Bit> computeProperties(final Collection<? extends Bit> bits) {
        return bits.stream()
            .map(bit -> Map.entry(getPropertyName(bit.getName()), bit))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new));
    }
}
