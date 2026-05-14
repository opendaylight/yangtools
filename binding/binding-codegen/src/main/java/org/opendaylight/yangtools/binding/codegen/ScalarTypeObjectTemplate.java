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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.ScalarTypeObject;
import org.opendaylight.yangtools.binding.UnsafeSecret;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.Decimal64Type;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
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
                .str("private final ").str(valueType).eol(" _value;")
                .nl();
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
        void appendMethods(final BlockBuilder bb, final ConcreteType valueType) {
            bb
                .nl()
                .at().eol(importedName(OVERRIDE))
                .str("public final ").str(importedName(valueType)).str(' ' + SCALAR_TYPE_OBJECT_GET_VALUE_NAME + "()")
                .oB()
                .str("return _value");
            if (valueType.isArray()) {
                bb.str(".clone()");
            }
            bb
                .eS()
                .cB();

//            @Override
//            public int hashCode() {
//                return CodeHelpers.wrapperHashCode(_value);
//            }
//
//            @Override
//            public final boolean equals(Object obj) {
//                return this == obj || obj instanceof Ipv4AddressBinary other
//                    && Arrays.equals(_value, other._value);
//            }
//
//            @Override
//            public String toString() {
//                return CodeHelpers.stoTS(Ipv4AddressBinary.class, _value);
//            }
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
        void appendMethods(final BlockBuilder bb, final ConcreteType valueType) {
            // no-op
        }
    }

    /**
     * {@code org.opendaylight.yangtools.binding.UnsafeSecret} as a JavaTypeName.
     */
    private static final JavaTypeName UNSAFE_SECRET = JavaTypeName.create(UnsafeSecret.class);

    // FIXME: this enum should be absorbed into a class hierarchy in ScalarTypeObjectArchetype
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
        final var archetype = archetype();
        final var simpleName = archetype.simpleName();
        final var valueType = archetype.valueType();
        final var importedType = importedName(valueType);

        final var bb = newBodyBuilder(archetype.statement(), archetype.typeDefinition(), topLevel)
            .str("public").str(topLevel ? " " : "static ").str("class ").str(simpleName).frg(implFragment(importedType))
                .oB()
                .eol("@java.io.Serial")
                .str("private static final long serialVersionUID = ").jLong(archetype().serialVersionUID()).eS()
                .nl();


        // FIXME: importedNonNull
        appendFieldDeclaration(bb, importedType);

        // length/range checks
        final var restrictions = archetype.getRestrictions();
        if (!restrictions.isEmpty()) {
            final var length = restrictions.getLengthConstraint();
            if (length.isPresent()) {
                bb.nl().blk(LengthGenerator.generateLengthChecker("_value",
                    TypeUtils.encapsulatedValueType(archetype()), length.orElseThrow(), javaType()));
            }
//            final var range = restrictions.getRangeConstraint();
//            if (range.isPresent()) {
//                bb.nl().blk(rangeGenerator.generateRangeChecker("_value", range.orElseThrow(), javaType()));
//            }
        }

        bb
            .at().eol(importedName(NONNULL_BY_DEFAULT))
            .at().str(importedName(CONSTRUCTOR_PARAMETERS)).eol("(\"value\")")
            .str("public ").str(archetype.simpleName()).str("(").str(importedType).str(" _value)").oB();

        appendFieldInitialization(bb, valueType);

        // FIXME: checker invocations

        bb.cB();

        if (scalarType.hasRestrictions()) {
            final var yangModuleInfo = nameInModuleOf(archetype);

            bb
                .nl()
                // protected constructor taking an encapsulated Java value and an UnsafeSecret and performs
                // initialization
                .at().eol(importedName(NONNULL_BY_DEFAULT))
                .str("protected ").str(type().simpleName()).str("(").str(importedName(UNSAFE_SECRET))
                    .str(" secret, ").str(importedType).str(" _value)").oB();

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

//        .blk(generateRestrictions(type(), fieldName, value.getReturnType()))
//        // If we have patterns, we need to apply them to the value field. This is a sad consequence
//        // of how this code is structured.
//        .blk(genPatternEnforcer(fieldName));


        appendMethods(bb, valueType);

        //    builder.setTypedef(true);
        //    builder.addImplementsType(BindingTypes.scalarTypeObject(javaType));
        //    YangSourceDefinition.of(module, definingStatement).ifPresent(builder::setYangSourceDefinition);
        //
        //    final var genPropBuilder = builder.addProperty(TypeConstants.VALUE_PROP);
        //    genPropBuilder.setReturnType(javaType);
        //    builder.setRestrictions(AbstractTypeObjectGenerator.getRestrictions(typedef));
        //    builder.setModuleName(module.argument().getLocalName());
        //    builderFactory.addCodegenInformation(typedef, builder);
        //
        //    AbstractTypeObjectGenerator.annotateDeprecatedIfNecessary(typedef, builder);
        //
        //    if (javaType instanceof ConcreteType
        //        // FIXME: This looks very suspicious: we should by checking for Types.STRING
        //        && "String".equals(javaType.simpleName()) && typedef.getBaseType() != null) {
        //        AbstractTypeObjectGenerator.addStringRegExAsConstant(builder,
        //            AbstractTypeObjectGenerator.resolveRegExpressions(typedef));
        //    }
        //    AbstractTypeObjectGenerator.addUnits(builder, typedef);
        //
        //    AbstractTypeObjectGenerator.makeSerializable(builder);

        return bb.cB();
    }

    abstract BlockFragment implFragment(String valueType);

    abstract void appendFieldDeclaration(BlockBuilder bb, String valueType);

    abstract void appendFieldInitialization(BlockBuilder bb, ConcreteType valueType);

    abstract void appendMethods(BlockBuilder bb, ConcreteType valueType);
}
