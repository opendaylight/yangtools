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
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.BitsTypeObject;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.api.BitsTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
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
            openDefaultCtor(bb, props);
            for (var propName : props.keySet()) {
                bb.str("this._").str(propName).str(" = ").str(propName).eS();
            }
            bb.cB().newLine();

            // copy constructor
            openCopyCtor(bb);
            for (var propName : props.keySet()) {
                bb.str("this._").str(propName).str(" = source._").str(propName).eS();
            }
            bb.cB().newLine();

            appendGetDefaultInstance(bb, props);

            // getters
            for (var propName : props.keySet()) {
                if (!props.containsKey(propName)) {
                    bb
                        .nl()
                        .str("public ").str(getterMethodName(propName)).str("()").oB()
                            .str("return _").str(propName).eS()
                        .cB();
                }
            }

            // FIXME: validNames()
            // FIXME: values()


            // FIXME: hashCode()
            // FIXME: equals()
            // FIXME: toString();
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
            openDefaultCtor(bb, props);
            bb.str("super(");
            final var sit = props.keySet().iterator();
            while (sit.hasNext()) {
                final var propName = sit.next();
                bb.str(superProps.containsKey(propName) ? propName : "false");
                if (!sit.hasNext()) {
                    break;
                }
                bb.str(", ");
            }
            bb.eol(");").cB().newLine();

            // copy constructor
            openCopyCtor(bb);
            bb.eol("super(source);").cB().newLine();

            appendGetDefaultInstance(bb, props);

            if (override != null) {
                // override getters for invalid bits
                for (var propName : superProps.keySet()) {
                    if (!props.containsKey(propName)) {
                        bb
                            .nl()
                            .at().eol(override)
                            .at().str(importedName(DEPRECATED)).eol("(forRemoval = true)")
                            .str("public final ").str(getterMethodName(propName)).str("()").oB()
                                .eol("return false;")
                            .cB();
                    }
                }

                // FIXME: validNames()
                // FIXME: values()
           }
        }
    }

    private static final JavaTypeName IMMUTABLE_SET = JavaTypeName.create(ImmutableSet.class);

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
            .str("public").str(topLevel ? " " : " static ").str("class ").str(simpleName).frg(implFragment()).oB()
                .eol("@java.io.Serial")
                .str("private static final long serialVersionUID = ").jLong(archetype().serialVersionUID()).eS()
                .nl();

        final var bits = archetype.typeDefinition().getBits();
        appendBody(bb, bits, computeProperties(bits));
        return bb.cB();
    }

    abstract BlockFragment implFragment();

    abstract void appendBody(final BlockBuilder bb, Collection<? extends Bit> bits, Map<String, Bit> props);

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

    final void openDefaultCtor(final BlockBuilder bb, final Map<String, Bit> props) {
        bb.str("public ").str(archetype().simpleName()).str("(");
        var it = props.keySet().iterator();
        while (true) {
            bb.str("boolean _").str(it.next());
            if (!it.hasNext()) {
                break;
            }
            bb.str(", ");
        }
        bb.str(")").oB();
    }

    final void openCopyCtor(final BlockBuilder bb) {
        final var simpleName = archetype().simpleName();
        bb.str("public ").str(simpleName).str("(").str(simpleName).str(" source)").oB();
    }

    final BlockBuilder appendGetDefaultInstance(final BlockBuilder bb, final Map<String, Bit> props) {
        final var simpleName = archetype().simpleName();
        bb
            .str("public static ").str(simpleName).str(" getDefaultInstance(final String defaultValue)").oB()
                .str("var values = ").str(importedName(CODEHELPERS)).eol(".parseBitsDefaultValue(defaultValue,");
        final var it = props.values().iterator();
        while (true) {
            bb.ind().jStr(it.next().getName());
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

    private static Map<String, Bit> computeProperties(final Collection<? extends Bit> bits) {
        return bits.stream()
            .map(bit -> Map.entry(Naming.getPropertyName(bit.getName()), bit))
            .sorted(Comparator.comparing(Map.Entry::getKey))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new));
    }

//    private BlockBuilder validNamesAndValues(final BitsTypeDefinition typedef) {
//        final var override = importedName(OVERRIDE);
//
//        return newBlockBuilder()
//            .nl()
//            .at().eol(override)
//            .str("public ").gen(importedName(IMMUTABLE_SET), importedName(Types.STRING)).str(" validNames()").oB()
//                .eol("return " + TypeConstants.VALID_NAMES_NAME + ";")
//            .cB()
//            .nl()
//            .at().eol(override)
//            .str("public boolean[] values()").oB()
//                .str("return new boolean[]").jBlock(bb -> {
//                    final var bits = typedef.getBits();
//                    if (bits.isEmpty()) {
//                        bb.eol("// empty");
//                        return;
//                    }
//
//                    final var it = bits.iterator();
//                    while (true) {
//                        final var bit = it.next();
//                        bb.str(getterMethodName(Naming.getPropertyName(bit.getName()))).str("()");
//                        if (!it.hasNext()) {
//                            bb.nl();
//                            break;
//                        }
//                        bb.eol(",");
//                    }
//                }).eS()
//            .cB()
//            .nl();
//    }
}
