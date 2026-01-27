/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code type} statement.
 */
public interface TypeStatement extends DeclaredStatement<QName> {
    /**
     * A {@link TypeStatement} which is documented not have one some of {@code type-body-stmts} substatements.
     */
    sealed interface WithBodyStatements extends TypeStatement {
        // Nothing else
    }

    /**
     * A {@link TypeStatement} which is documented to not bave any of {@code type-body-stmts} substatements.
     */
    interface WithoutBodyStatements extends TypeStatement {
        // Nothing else
    }

    /**
     * A {@link TypeStatement} conforming to {@code binary-specification} ABNF production.
     */
    non-sealed interface OfBinary extends WithBodyStatements, LengthStatement.OptionalIn<QName> {
        // Nothing else
    }

    /**
     * A {@link TypeStatement} conforming to {@code bits-specification} ABNF production.
     */
    non-sealed interface OfBits extends WithBodyStatements, BitStatement.MultipleIn<QName> {
        // Nothing else
    }

    /**
     * A {@link TypeStatement} conforming to {@code decimal64-specification} ABNF production.
     */
    non-sealed interface OfDecimal64 extends WithBodyStatements, RangeStatement.OptionalIn<QName>,
            FractionDigitsStatement.OptionalIn<QName> {
        // Nothing else
    }

    /**
     * A {@link TypeStatement} conforming to {@code enum-specification} ABNF production.
     */
    non-sealed interface OfEnum extends WithBodyStatements, EnumStatement.MultipleIn<QName> {
        // Nothing else
    }

    /**
     * A {@link TypeStatement} conforming to {@code identityref-specification} ABNF production.
     */
    non-sealed interface OfIdentityref extends WithBodyStatements, BaseStatement.MultipleIn<QName> {
        // Nothing else
    }

    /**
     * A {@link TypeStatement} conforming to {@code instance-identifier-specification} ABNF production.
     */
    non-sealed interface OfInstanceIdentifier extends WithBodyStatements, RequireInstanceStatement.OptionalIn<QName> {
        // Nothing else
    }

    /**
     * A {@link TypeStatement} conforming to {@code leafref-specification} ABNF production.
     */
    non-sealed interface OfLeafref extends WithBodyStatements, PathStatement.OptionalIn<QName>,
            RequireInstanceStatement.OptionalIn<QName> {
        // Nothing else
    }

    /**
     * A {@link TypeStatement} conforming to {@code numerical-restrictions} ABNF production.
     */
    non-sealed interface OfNumerical extends WithBodyStatements, RangeStatement.OptionalIn<QName> {
        // Nothing else
    }

    /**
     * A {@link TypeStatement} conforming to {@code string-restrictions} ABNF production.
     */
    non-sealed interface OfString extends WithBodyStatements, LengthStatement.OptionalIn<QName>,
            PatternStatement.MultipleIn<QName> {
        // Nothing else
    }

    /**
     * A {@link TypeStatement} conforming to {@code union-specification} ABNF production.
     */
    non-sealed interface OfUnion extends WithBodyStatements {
        /**
         * {@return all {@code TypeStatement} substatements}
         */
        default @NonNull Collection<? extends @NonNull TypeStatement> typeStatements() {
            return declaredSubstatements(TypeStatement.class);
        }
    }

    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link TypeStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code TypeStatement} or {@code null} if not present}
         */
        default @Nullable TypeStatement typeStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof TypeStatement type) {
                    return type;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code TypeStatement}}
         */
        default @NonNull Optional<TypeStatement> findTypeStatement() {
            return Optional.ofNullable(typeStatement());
        }

        /**
         * {@return the {@code TypeStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull TypeStatement getTypeStatement() {
            final var type = typeStatement();
            if (type == null) {
                throw new NoSuchElementException("No type statement present in " + this);
            }
            return type;
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
