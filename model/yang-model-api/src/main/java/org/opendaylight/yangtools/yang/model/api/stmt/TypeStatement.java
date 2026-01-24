/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code type} statement.
 */
public sealed interface TypeStatement extends DeclaredStatement<QName> {
    /**
     * A {@link TypeStatement} conforming to {@code binary-specification} ABNF production.
     */
    non-sealed interface OfBinary extends WithLength {
        // Nothing else
    }

    /**
     * A {@link TypeStatement} conforming to {@code bits-specification} ABNF production.
     */
    non-sealed interface OfBits extends TypeStatement {
        // FIXME: document
        default @NonNull Collection<? extends @NonNull BitStatement> bits() {
            return declaredSubstatements(BitStatement.class);
        }
    }

    /**
     * A {@link TypeStatement} conforming to {@code decimal64-specification} ABNF production.
     */
    non-sealed interface OfDecimal64 extends WithRange {
        // FIXME: document
        default @NonNull FractionDigitsStatement fractionDigits() {
            return findFirstDeclaredSubstatement(FractionDigitsStatement.class).orElseThrow();
        }
    }

    /**
     * A {@link TypeStatement} conforming to {@code enum-specification} ABNF production.
     */
    non-sealed interface OfEnum extends TypeStatement {
        // FIXME: document
        default @NonNull Collection<? extends @NonNull EnumStatement> enums() {
            return declaredSubstatements(EnumStatement.class);
        }
    }

    /**
     * A {@link TypeStatement} conforming to {@code identityref-specification} ABNF production.
     */
    non-sealed interface OfIdentityref extends TypeStatement {
        /**
         * Returns the base statements.
         *
         * @return collection of base statements (in YANG 1.1 models) or a collection containing just one base
         *         statement (in YANG 1.0 models)
         */
        default @NonNull Collection<? extends @NonNull BaseStatement> bases() {
            return declaredSubstatements(BaseStatement.class);
        }
    }

    /**
     * A {@link TypeStatement} conforming to {@code instance-identifier-specification} ABNF production.
     */
    non-sealed interface OfInstanceIdentifier extends WithRequireInstance {
        // Nothing else
    }

    /**
     * A {@link TypeStatement} conforming to {@code leafref-specification} ABNF production.
     */
    non-sealed interface OfLeafref extends WithRequireInstance {
        // FIXME: document
        default @NonNull PathStatement path() {
            return findFirstDeclaredSubstatement(PathStatement.class).orElseThrow();
        }
    }

    /**
     * A {@link TypeStatement} conforming to {@code numerical-restrictions} ABNF production.
     */
    non-sealed interface OfNumerical extends WithRange {
        // Nothing else
    }

    non-sealed interface OfSimple extends TypeStatement {
        // Nothing else
    }

    /**
     * A {@link TypeStatement} conforming to {@code string-restrictions} ABNF production.
     */
    non-sealed interface OfString extends WithLength {
        // FIXME: document
        default @NonNull Collection<? extends @NonNull PatternStatement> patterns() {
            return declaredSubstatements(PatternStatement.class);
        }
    }

    /**
     * A {@link TypeStatement} conforming to {@code union-specification} ABNF production.
     */
     non-sealed interface OfUnion extends TypeStatement {
         // FIXME: document
        default @NonNull Collection<? extends @NonNull TypeStatement> types() {
            return declaredSubstatements(TypeStatement.class);
        }
    }

     // FIXME: document
    sealed interface WithLength extends TypeStatement permits OfBinary, OfString {
        // FIXME: document
        default @Nullable LengthStatement length() {
            final var opt = findFirstDeclaredSubstatement(LengthStatement.class);
            return opt.isPresent() ? opt.orElseThrow() : null;
        }
    }

    // FIXME: document
    sealed interface WithRange extends TypeStatement permits OfDecimal64, OfNumerical {
        // FIXME: document
        default @Nullable RangeStatement range() {
            final var opt = findFirstDeclaredSubstatement(RangeStatement.class);
            return opt.isPresent() ? opt.orElseThrow() : null;
        }
    }

    // FIXME: document
    sealed interface WithRequireInstance extends TypeStatement permits OfInstanceIdentifier, OfLeafref {
        /**
         * Return require-instance statement child, if present. For RFC6020 semantics, this method always returns null.
         *
         * @return require-instance statement, if present.
         */
        default @Nullable RequireInstanceStatement requireInstance() {
            final var opt = findFirstDeclaredSubstatement(RequireInstanceStatement.class);
            return opt.isPresent() ? opt.orElseThrow() : null;
        }
    }

    /**
     * The definition of {@code type} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<QName, @NonNull TypeStatement, @NonNull TypeEffectiveStatement> DEF =
        StatementDefinition.of(TypeStatement.class, TypeEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "type", "name");

    @Override
    default StatementDefinition<QName, ?, ?> statementDefinition() {
        return DEF;
    }
}
