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

    // FIXME: 7.0.0: this interface does not have an implementation
    interface NumericalRestrictions extends TypeStatement, RangeStatement.OptionalIn<QName> {
        // Nothing else
    }

    interface Decimal64Specification extends TypeStatement, RangeStatement.OptionalIn<QName>,
            FractionDigitsStatement.OptionalIn<QName> {
        // Nothing else
    }

    // FIXME: 7.0.0: this interface does not have an implementation
    interface StringRestrictions extends TypeStatement, LengthStatement.OptionalIn<QName>,
            PatternStatement.MultipleIn<QName> {
        // Nothing else
    }

    interface EnumSpecification extends TypeStatement, EnumStatement.MultipleIn<QName> {
        // Nothing else
    }

    interface LeafrefSpecification extends TypeStatement, PathStatement.OptionalIn<QName>,
            RequireInstanceStatement.OptionalIn<QName> {
        // Nothing else
    }

    interface InstanceIdentifierSpecification extends TypeStatement, RequireInstanceStatement.OptionalIn<QName> {
        // Nothing else
    }

    interface IdentityRefSpecification extends TypeStatement, BaseStatement.MultipleIn<QName> {
        // Nothing else
    }

    interface BitsSpecification extends TypeStatement, BitStatement.MultipleIn<QName> {
        // Nothing else
    }

    interface UnionSpecification extends TypeStatement {
        /**
         * {@return all {@code TypeStatement} substatements}
         */
        default @NonNull Collection<? extends @NonNull TypeStatement> typeStatements() {
            return declaredSubstatements(TypeStatement.class);
        }
    }

    interface BinarySpecification extends TypeStatement, LengthStatement.OptionalIn<QName> {
        // Nothing else
    }
}
