/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.codegen.YangModuleInfoTemplate.CONST_STO_REGISTRAR;
import static org.opendaylight.yangtools.binding.codegen.YangModuleInfoTemplate.nameInModuleOf;
import static org.opendaylight.yangtools.binding.contract.Naming.SCALAR_TYPE_OBJECT_GET_VALUE_NAME;
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.BINARY_TYPE;
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.BOOLEAN_TYPE;
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.EMPTY_TYPE;
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.INSTANCE_IDENTIFIER;
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.INT16_TYPE;
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.INT32_TYPE;
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.INT64_TYPE;
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.INT8_TYPE;
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.STRING_TYPE;
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.UINT16_TYPE;
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.UINT32_TYPE;
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.UINT64_TYPE;
import static org.opendaylight.yangtools.binding.model.ri.BaseYangTypes.UINT8_TYPE;
import static org.opendaylight.yangtools.binding.model.ri.TypeConstants.PATTERN_CONSTANT_NAME;
import static org.opendaylight.yangtools.binding.model.ri.Types.STRING;

import com.google.common.base.MoreObjects;
import java.util.LinkedHashMap;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.ScalarTypeObject;
import org.opendaylight.yangtools.binding.UnsafeSecret;
import org.opendaylight.yangtools.binding.contract.RegexPatterns;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.Decimal64Type;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.Restrictions;
import org.opendaylight.yangtools.binding.model.api.ScalarTypeObjectArchetype;

/**
 * A template for {@link ScalarTypeObject} specializations.
 */
@NonNullByDefault
abstract sealed class ScalarTypeObjectTemplate extends ArchetypeTemplate<ScalarTypeObjectArchetype> {
    record Builder(ScalarTypeObjectArchetype type, DataRootArchetype root) implements Template.Builder {
        Builder {
            requireNonNull(type);
            requireNonNull(root);
        }

        @Override
        public ScalarTypeObjectTemplate build() {
            return ScalarTypeObjectTemplate.of(GeneratedClass.of(type), type, root);
        }
    }

    private static final class Base extends ScalarTypeObjectTemplate {
        private static final JavaTypeName SCALAR_TYPE_OBJECT = JavaTypeName.create(ScalarTypeObject.class);

        Base(final GeneratedClass javaType, final ScalarTypeObjectArchetype archetype, final DataRootArchetype root) {
            super(javaType, archetype, root);
        }

        @Override
        BlockFragment implFragment(final String valueType) {
            return bb -> bb.str(" implements ").gen(importedName(SCALAR_TYPE_OBJECT), valueType)
                .str(", java.io.Serializable");
        }

        @Override
        void appendFieldDeclaration(final BlockBuilder bb, final String valueType) {
            bb
                .nl()
                .str("private final ").str(valueType).eol(" _value;");
        }

        @Override
        void appendFieldInitialization(final BlockBuilder bb, final ConcreteType valueType) {
            bb.str("this._value = ").str(importedName(CODEHELPERS)).str(".requireValue(_value");
            if (valueType instanceof Decimal64Type decimal64) {
                bb.str(", ").jInt(decimal64.fractionDigits());
            }
            bb.str(")");
            if (valueType.isArray()) {
                bb.str(".clone()");
            }
            bb.eS();
        }

        @Override
        void appendFieldCopy(final BlockBuilder bb) {
            bb.eol("this._value = source._value;");
        }

        @Override
        void appendParentConstructor(final BlockBuilder bb, final ValueCheckers valueCheckers) {
            // no-op
        }

        @Override
        void appendMethods(final BlockBuilder bb, final ConcreteType valueType) {
            final var override = importedName(OVERRIDE);
            final var codeHelpers = importedName(CODEHELPERS);

            bb
                .nl()
                .at().eol(override)
                .str("public final ").str(importedName(valueType)).str(' ' + SCALAR_TYPE_OBJECT_GET_VALUE_NAME + "()")
                .oB()
                .str("return _value");
            if (valueType.isArray()) {
                bb.str(".clone()");
            }
            bb
                .eS()
                .cB()
                .nl()
                .at().eol(override)
                .str("public final int hashCode()").oB()
                    .str("return ").str(codeHelpers).eol(".wrapperHashCode(_value);")
                .cB()
                .nl()
                .at().eol(override)
                .str("public final boolean equals(").str(importedName(OBJECT)).str(" obj)").oB()
                    .str("return this == obj || obj instanceof ").str(archetype.simpleName()).str(" other && ");
            if (valueType.isArray()) {
                bb.str(importedName(JU_ARRAYS)).eol(".equals(_value, other._value);");
            } else {
                bb.eol("_value.equals(other._value);");
            }
            bb
                .cB()
                .nl()
                .at().eol(override)
                .str("public final ").str(importedName(STRING)).str(" toString()").oB()
                    .str("return ").str(codeHelpers).eol(".stoTS(getClass(), _value);")
                .cB();
        }
    }

    private static final class Derived extends ScalarTypeObjectTemplate {
        private final ScalarTypeObjectArchetype superType;

        Derived(final GeneratedClass javaType, final ScalarTypeObjectArchetype archetype, final DataRootArchetype root,
                final ScalarTypeObjectArchetype superType) {
            super(javaType, archetype, root);
            this.superType = requireNonNull(superType);
        }

        @Override
        BlockFragment implFragment(final String valueType) {
            return bb -> bb.str(" extends ").str(importedName(superType.name()));
        }

        @Override
        void appendFieldDeclaration(final BlockBuilder bb, final String valueType) {
            // no-op
        }

        @Override
        void appendFieldInitialization(final BlockBuilder bb, final ConcreteType valueType) {
            bb.eol("super(_value);");
        }

        @Override
        void appendFieldCopy(final BlockBuilder bb) {
            bb.eol("super(source);");
        }

        @Override
        void appendParentConstructor(final BlockBuilder bb, final ValueCheckers valueCheckers) {
            final var importedSuper = importedName(superType);

            bb
                .nl()
                .eol("/**")
                .str(" * Creates a new instance from ").eol(importedSuper)
                .eol(" *")
                .eol(" * @param source Source object")
                .eol(" */")
                .str("public ").str(archetype.simpleName()).str("(").str(importedSuper).str(" source)").oB()
                    .eol("super(source);");
            valueCheckers.appendInvocations(bb, javaType(), "getValue()");
            bb.cB();
        }

        @Override
        void appendMethods(final BlockBuilder bb, final ConcreteType valueType) {
            // no-op
        }
    }

    /**
     * Support for checking whether an encapsulated value meets required criteria.
     */
    private abstract static sealed class ValueCheckers {
        /**
         * {@return a ValueCheckers instance appropriate for the specified {@link ScalarTypeObjectArchetype}}
         * @param archetype the {@link ScalarTypeObjectArchetype}
         */
        static final ValueCheckers of(final ScalarTypeObjectArchetype archetype) {
            final var restrictions = archetype.restrictions();
            if (restrictions == null) {
                return NoopValueCheckers.INSTANCE;
            }

            final var valueType = archetype.valueType();
            return restrictions.getRangeConstraint().isEmpty()
                ? new BaseValueCheckers(valueType, restrictions)
                : new RangeValueCheckers(valueType, restrictions, AbstractRangeGenerator.forType(valueType));
        }

        abstract void appendConstants(BlockBuilder bb, GeneratedClass javaType);

        abstract void appendDeclarations(BlockBuilder bb, GeneratedClass javaType);

        abstract void appendInvocations(BlockBuilder bb, GeneratedClass javaType, String valueRef);
    }

    private static final class NoopValueCheckers extends ValueCheckers {
        static final NoopValueCheckers INSTANCE = new NoopValueCheckers();

        private NoopValueCheckers() {
            // hidden on purpose
        }

        @Override
        void appendConstants(final BlockBuilder bb, final GeneratedClass javaType) {
            // no-op
        }

        @Override
        void appendDeclarations(final BlockBuilder bb, final GeneratedClass javaType) {
            // no-op
        }

        @Override
        void appendInvocations(final BlockBuilder bb, final GeneratedClass javaType, final String valueRef) {
            // no-op
        }

        @Override
        public String toString() {
            return NoopValueCheckers.class.getSimpleName();
        }
    }

    private static sealed class BaseValueCheckers extends ValueCheckers {
        final Restrictions restrictions;
        final ConcreteType valueType;

        BaseValueCheckers(final ConcreteType valueType, final Restrictions restrictions) {
            this.valueType = requireNonNull(valueType);
            this.restrictions = restrictions;
        }

        @Override
        final void appendConstants(final BlockBuilder bb, final GeneratedClass javaType) {
            final var patterns = restrictions.getPatternConstraints();
            if (patterns.isEmpty()) {
                return;
            }

            final var regExps = LinkedHashMap.<String, String>newLinkedHashMap(patterns.size());
            for (var pattern : patterns) {
                var regEx = pattern.getJavaPatternString();

                // The pattern can be inverted
                final var optModifier = pattern.getModifier();
                if (optModifier.isPresent()) {
                    regEx = switch (optModifier.orElseThrow()) {
                        case INVERT_MATCH -> RegexPatterns.negatePatternString(regEx);
                    };
                }

                regExps.put(regEx, pattern.getRegularExpressionString());
            }

            final var juList = javaType.getReferenceString(JU_LIST);
            final var jurPattern = javaType.getReferenceString(JUR_PATTERN);

            bb
                .str("public static final ").str(juList).str("<String> " + PATTERN_CONSTANT_NAME + " = ").str(juList)
                    .str(".of(");
            var it = regExps.keySet().iterator();
            while (true) {
                bb.jString(it.next());
                if (!it.hasNext()) {
                    break;
                }
                bb.str(", ");
            }

            bb
                .eol(");")
                .str("private static final ").str(jurPattern);
            switch (regExps.size()) {
                case 1 -> {
                    bb
                        .str(" " + MEMBER_PATTERN_LIST + " = ").str(jurPattern)
                            .eol(".compile(" + PATTERN_CONSTANT_NAME + ".getFirst());")
                        .str("private static final String " + MEMBER_REGEX_LIST + " = ")
                            .jString(regExps.values().iterator().next()).eS();
                }
                default -> {
                    // FIXME: should be multi-line for clarity
                    bb
                        .str("[] " + MEMBER_PATTERN_LIST + " = ").str(javaType.getReferenceString(CODEHELPERS))
                            .eol(".compilePatterns(" + PATTERN_CONSTANT_NAME + ");")
                         .str("private static final String[] " + MEMBER_REGEX_LIST + " = { ");

                    it = regExps.values().iterator();
                    while (true) {
                        bb.jString(it.next());
                        if (!it.hasNext()) {
                            break;
                        }
                        bb.str(", ");
                    }

                    bb.eol(" };");
                }
            }
        }

        @Override
        void appendDeclarations(final BlockBuilder bb, final GeneratedClass javaType) {
            final var length = restrictions.getLengthConstraint();
            if (length.isPresent()) {
                bb.nl().blk(LengthGenerator.generateLengthChecker("_value", valueType, length.orElseThrow(), javaType));
            }
        }

        @Override
        void appendInvocations(final BlockBuilder bb, final GeneratedClass javaType, final String valueRef) {
            final var length = restrictions.getLengthConstraint();
            if (length.isPresent()) {
                LengthGenerator.appendCheckerCall(bb, "_value", valueRef);
            }

            final var patterns = restrictions.getPatternConstraints();
            if (!patterns.isEmpty()) {
                bb
                    .str(javaType.getReferenceString(CODEHELPERS)).str(".checkPattern(").str(valueRef)
                        .eol(", " + MEMBER_PATTERN_LIST + ", " + MEMBER_REGEX_LIST + ");");
            }
        }

        @Override
        public final String toString() {
            return MoreObjects.toStringHelper(this).add("restrictions", restrictions).toString();
        }
    }

    private static final class RangeValueCheckers extends BaseValueCheckers {
        private final AbstractRangeGenerator<?> rangeGenerator;

        RangeValueCheckers(final ConcreteType valueType, final Restrictions restrictions,
                final AbstractRangeGenerator<?> rangeGenerator) {
            super(valueType, restrictions);
            this.rangeGenerator = requireNonNull(rangeGenerator);
        }

        @Override
        void appendDeclarations(final BlockBuilder bb, final GeneratedClass javaType) {
            super.appendDeclarations(bb, javaType);
            bb
                .nl()
                .blk(rangeGenerator.generateRangeChecker("_value", restrictions.getRangeConstraint().orElseThrow(),
                    javaType));
        }

        @Override
        void appendInvocations(final BlockBuilder bb, final GeneratedClass javaType, final String valueRef) {
            super.appendInvocations(bb, javaType, valueRef);
            rangeGenerator.appendCheckerCall(bb, "_value", valueRef);
        }
    }

    /**
     * {@code org.opendaylight.yangtools.binding.UnsafeSecret} as a JavaTypeName.
     */
    private static final JavaTypeName UNSAFE_SECRET = JavaTypeName.create(UnsafeSecret.class);
    /**
     * All types that have a {@code valueOf(String)} static factory method suitable for directly implementing
     * {@code getDefaultValue(String}}.
     */
    private static final Set<ConcreteType> VALUEOF_TYPES = Set.of(
        BOOLEAN_TYPE, INT8_TYPE, INT16_TYPE, INT32_TYPE, INT64_TYPE, UINT8_TYPE, UINT16_TYPE, UINT32_TYPE, UINT64_TYPE);

    // FIXME: integrate this enum into ScalarTypeObjectArchetype type class hierarchy
    private final ScalarTypeKind scalarType;

    private ScalarTypeObjectTemplate(final GeneratedClass javaType, final ScalarTypeObjectArchetype archetype,
            final DataRootArchetype root) {
        super(javaType, archetype, root);
        scalarType = ScalarTypeKind.of(archetype);
    }

    private static ScalarTypeObjectTemplate of(final GeneratedClass javaType, final ScalarTypeObjectArchetype archetype,
            final DataRootArchetype root) {
        final var superType = archetype.getSuperType();
        return superType == null ? new Base(javaType, archetype, root)
            : new Derived(javaType, archetype, root, superType);
    }

    static BlockBuilder generateInner(final GeneratedClass.Nested javaType,
            final ScalarTypeObjectArchetype archetype, final DataRootArchetype root) {
        return ScalarTypeObjectTemplate.of(javaType, archetype, root).body(false);
    }

    @Override
    final BlockBuilder body() {
        return body(true);
    }

    private BlockBuilder body(final boolean topLevel) {
        final var simpleName = archetype.simpleName();
        final var valueType = archetype.valueType();
        final var importedType = importedName(valueType);
        final var valueCheckers = ValueCheckers.of(archetype);
        final var javaType = javaType();

        final var bb = newBodyBuilder(archetype.statement(), archetype.typeDefinition(), topLevel)
            .str("public").str(topLevel ? " " : "static ").str("class ").str(simpleName).frg(implFragment(importedType))
                .oB()
                .eol("@java.io.Serial")
                .str("private static final long serialVersionUID = ").jLong(archetype.serialVersionUID()).eS();

        archetype.typeDefinition().getUnits().ifPresent(units ->
            bb.str("public static final String UNITS = ").jString(units).eS());

        valueCheckers.appendConstants(bb, javaType);
        // FIXME: importedNonNull
        appendFieldDeclaration(bb, importedType);

        valueCheckers.appendDeclarations(bb, javaType);

        bb
            .nl()
            .at().eol(importedName(NONNULL_BY_DEFAULT))
            .at().str(importedName(CONSTRUCTOR_PARAMETERS)).eol("(\"value\")")
            .str("public ").str(archetype.simpleName()).str("(").str(importedType).str(" _value)").oB();
        appendFieldInitialization(bb, valueType);
        valueCheckers.appendInvocations(bb, javaType, "_value");
        bb.cB();

        if (scalarType.hasRestrictions()) {
            final var yangModuleInfo = nameInModuleOf(archetype);

            bb
                .nl()
                // protected constructor taking an encapsulated Java value and an UnsafeSecret and performs
                // initialization
                .at().eol(importedName(NONNULL_BY_DEFAULT))
                .str("protected ").str(simpleName).str("(").str(importedName(UNSAFE_SECRET)).str(" secret, ")
                    .str(importedType).str(" _value)").oB();

            switch (scalarType) {
                case ROOT_RESTRICTING -> {
                    bb.str(importedName(CODEHELPERS)).eol(".verifySecret(secret);");
                    appendFieldInitialization(bb, valueType);
                }
                case SUBCLASS_INHERITING -> bb.eol("super(secret, _value);");
                case SUBCLASS_RESTRICTING ->
                    bb
                        .eol("super(_value);")
                        .str(importedName(CODEHELPERS)).eol(".verifySecret(secret);");
                default -> verify(scalarType == ScalarTypeKind.SUBCLASS);
            }

            bb
                .cB()
                .nl()
                // static initialization block with registration call to this module's UnsafeAccess
                .str("static").oB()
                    // not 'importedName' on purpose: it would just stand out in imports
                    .str(yangModuleInfo.canonicalName()).eol("." + CONST_STO_REGISTRAR + ".registerUnsafeSTO(")
                    .ind(simpleName).str(".class, ").str(simpleName).str("::new, ").str(simpleName).eol("::new);")
                .cB();
        }

        bb
            .nl()
            .txt("""
                /**
                 * Creates a copy from Source Object.
                 *
                 * @param source Source object
                 */
                """)
            // FIXME: make this constructor protected?
            .str("public ").str(simpleName).str("(").str(simpleName).str(" source)").oB();
        appendFieldCopy(bb);
        bb.cB();

        appendParentConstructor(bb, valueCheckers);

        // TODO: we cannot parser instance-identifier because we do not have information to encode namespaces etc.
        if (!INSTANCE_IDENTIFIER.name().equals(valueType.name())) {
            bb
                .nl()
                .str("public static ").str(simpleName).str(" getDefaultInstance(final String defaultValue)").oB()
                    .str("return new ").str(simpleName).str("(");
            if (VALUEOF_TYPES.contains(valueType)) {
                bb.str(importedType).str(".valueOf(defaultValue)");
            } else if (valueType instanceof Decimal64Type decimal64) {
                bb.str(importedType).str(".valueOf(defaultValue).scaleTo(").jInt(decimal64.fractionDigits()).str(")");
            } else if (valueType.equals(STRING_TYPE)) {
                bb.str("defaultValue");
            } else if (valueType.equals(BINARY_TYPE)) {
                bb.str(importedName(JU_BASE64)).str(".getDecoder().decode(defaultValue)");
            } else if (valueType.equals(EMPTY_TYPE)) {
                bb.str(importedName(CODEHELPERS)).str(".emptyFor(defaultValue)");
            } else {
                bb.str("new ").str(importedType).str("(defaultValue)");
            }
            bb
                .eol(");")
                .cB();
        }

        appendMethods(bb, valueType);
        return bb.cB();
    }

    abstract BlockFragment implFragment(String valueType);

    abstract void appendFieldDeclaration(BlockBuilder bb, String valueType);

    abstract void appendFieldInitialization(BlockBuilder bb, ConcreteType valueType);

    abstract void appendFieldCopy(BlockBuilder bb);

    abstract void appendParentConstructor(BlockBuilder bb, ValueCheckers valueCheckers);

    abstract void appendMethods(BlockBuilder bb, ConcreteType valueType);
}
