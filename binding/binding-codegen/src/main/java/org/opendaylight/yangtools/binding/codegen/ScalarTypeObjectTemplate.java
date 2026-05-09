/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.codegen.ModuleSupportTemplate.CONST_STO_REGISTRAR;
import static org.opendaylight.yangtools.binding.codegen.ModuleSupportTemplate.nameInModuleOf;
import static org.opendaylight.yangtools.binding.contract.Naming.SCALAR_TYPE_OBJECT_GET_VALUE_NAME;
import static org.opendaylight.yangtools.binding.model.ri.TypeConstants.VALUE_PROP;

import com.google.common.base.VerifyException;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.ScalarTypeObject;
import org.opendaylight.yangtools.binding.UnsafeSecret;
import org.opendaylight.yangtools.binding.model.api.Decimal64Type;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.ScalarTypeObjectArchetype;

/**
 * A template for {@link ScalarTypeObject} specializations.
 */
final class ScalarTypeObjectTemplate extends ClassTemplate {
    @NonNullByDefault
    record Builder(ScalarTypeObjectArchetype type) implements Template.Builder {
        Builder {
            requireNonNull(type);
        }

        @Override
        public ScalarTypeObjectTemplate build() {
            return new ScalarTypeObjectTemplate(type);
        }
    }

    /**
     * {@code org.opendaylight.yangtools.binding.UnsafeSecret} as a JavaTypeName.
     */
    private static final @NonNull JavaTypeName UNSAFE_SECRET = JavaTypeName.create(UnsafeSecret.class);

    // FIXME: this enum should be absorbed into a class hierarchy in ScalarTypeObjectArchetype
    private final @NonNull ScalarTypeKind scalarType;

    @NonNullByDefault
    private ScalarTypeObjectTemplate(final GeneratedClass javaType, final ScalarTypeObjectArchetype archetype) {
        super(javaType, archetype);
        scalarType = ScalarTypeKind.of(archetype);
    }

    @NonNullByDefault
    private ScalarTypeObjectTemplate(final ScalarTypeObjectArchetype archetype) {
        super(archetype);
        scalarType = ScalarTypeKind.of(archetype);
    }

    @NonNullByDefault
    static BlockBuilder generateAsInner(final GeneratedClass.Nested javaType,
            final ScalarTypeObjectArchetype archetype) {
        return new ScalarTypeObjectTemplate(javaType, archetype).generateAsInnerClass();
    }

    @Override
    BlockBuilder defaultConstructor() {
        final var value = valueProperty(allProperties);
        if (value == null) {
            throw new VerifyException("missing value property");
        }
        final var fieldName = fieldName(value);

        // common body for complete field initialization
        final var fieldInit = newBlockBuilder();
        if (valueProperty(properties) != null) {
            fieldInit.str("this.").str(fieldName).str(" = ").str(importedName(CODEHELPERS)).str(".requireValue(")
                .str(fieldName);
            if (value.getReturnType() instanceof Decimal64Type decimal64) {
                fieldInit.str(", ").jInt(decimal64.fractionDigits());
            }
            fieldInit.str(")").frg(cloneOrNull(value)).eS();
        }

        // FIXME: this should be just a single value, right?
        final var argsDeclaration = asArgumentsDeclaration(allProperties);
        // FIXME: base this check on ScalarTypeKind.isRoot(), asserting shape instead
        final var superArgs = parentProperties.isEmpty() ? null : asArguments(parentProperties);

        final var ret = newBlockBuilder()
            // public constructor taking an encapsulated Java value and validating it
            .at().eol(importedName(NONNULL_BY_DEFAULT))
            .at().str(importedName(CONSTRUCTOR_PARAMETERS)).str("(").jStr(VALUE_PROP).eol(")")
            .str("public ").str(type().simpleName()).str("(").str(argsDeclaration).str(")")
                .jBlock(bb -> {
                    if (superArgs != null) {
                        bb.str("super(").str(superArgs).eol(");");
                    }
                    bb
                        .blk(fieldInit)
                        .blk(generateRestrictions(type(), fieldName, value.getReturnType()))
                        // If we have patterns, we need to apply them to the value field. This is a sad consequence
                        // of how this code is structured.
                        .blk(genPatternEnforcer(fieldName));
                }).nl();

        return !scalarType.hasRestrictions() ? ret : ret
            .nl()
            // protected constructor taking an encapsulated Java value and an UnsafeSecret and performs initialization
            .at().eol(importedName(NONNULL_BY_DEFAULT))
            .str("protected ").str(type().simpleName()).str("(final ").str(importedName(UNSAFE_SECRET)).str(" secret, ")
                .str(argsDeclaration).str(")").jBlock(bb -> {
                    switch (scalarType) {
                        case ROOT_RESTRICTING -> {
                            verify(superArgs == null);
                            bb.str(importedName(CODEHELPERS)).eol(".verifySecret(secret);");
                        }
                        case SUBCLASS_INHERITING ->
                            bb.str("super(secret, ").str(verifyNotNull(superArgs)).eol(");");
                        case SUBCLASS_RESTRICTING ->
                            bb
                                .str("super(").str(verifyNotNull(superArgs)).eol(");")
                                .str(importedName(CODEHELPERS)).eol(".verifySecret(secret);");
                        default -> verify(scalarType == ScalarTypeKind.SUBCLASS);
                    }

                    bb.blk(fieldInit);
                }).nl()
            .nl()
            // static initialization block with registration call to this module's UnsafeAccess
            .str("static").jBlock(bb -> {
                // FIXME: self reference
                final var selfRef = type().simpleName();
                final var yangModuleInfo = nameInModuleOf(type());
                bb
                    // not 'importedName' on purpose: it would just stand out in imports
                    .str(yangModuleInfo.canonicalName()).eol("." + CONST_STO_REGISTRAR + ".registerUnsafeSTO(")
                        .ind(selfRef).str(".class, ").str(selfRef).str("::new, ").str(selfRef).eol("::new);");
            }).nl();
    }

    private static @Nullable GeneratedProperty valueProperty(final List<GeneratedProperty> props) {
        return switch (props.size()) {
            case 0 -> null;
            case 1 -> {
                final var prop = props.getFirst();
                if (!VALUE_PROP.equals(prop.getName())) {
                    throw new VerifyException("Unexpected property " + prop);
                }
                yield prop;
            }
            default -> throw new VerifyException("Unexpected properties " + props);
        };
    }

    @Override
    BlockBuilder propertyMethods() {
        if (!scalarType.isRoot()) {
            return null;
        }

        final var field = properties.getFirst();
        return newBlockBuilder()
            .nl()
            .at().eol(importedName(OVERRIDE))
            .str("public final ").str(importedReturnType(field)).str(' ' + SCALAR_TYPE_OBJECT_GET_VALUE_NAME + "()")
            .oB()
            .str("return ").str(fieldName(field)).frg(cloneOrNull(field)).eS()
            .cB();
    }
}
