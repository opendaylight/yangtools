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
    /**
     * The definition of {@code type} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<QName, @NonNull TypeStatement, @NonNull TypeEffectiveStatement> DEF =
        StatementDefinition.of(TypeStatement.class, TypeEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "type", YangArgumentDefinitions.NAME_AS_QNAME);

    @Override
    default StatementDefinition<QName, ?, ?> statementDefinition() {
        return DEF;
    }

    // FIXME: 7.0.0: this interface does not have an implementation
    interface NumericalRestrictions extends TypeStatement {
        default @Nullable RangeStatement getRange() {
            return findFirstDeclaredSubstatement(RangeStatement.class).orElse(null);
        }
    }

    interface Decimal64Specification extends TypeStatement {
        default @NonNull FractionDigitsStatement getFractionDigits() {
            return findFirstDeclaredSubstatement(FractionDigitsStatement.class).orElseThrow();
        }

        default @Nullable RangeStatement getRange() {
            final var opt = findFirstDeclaredSubstatement(RangeStatement.class);
            return opt.isPresent() ? opt.orElseThrow() : null;
        }
    }

    // FIXME: 7.0.0: this interface does not have an implementation
    interface StringRestrictions extends TypeStatement, LengthStatement.OptionalIn<QName>,
            PatternStatement.MultipleIn<QName> {
        // Nothing else
    }

    interface EnumSpecification extends TypeStatement {
        default @NonNull Collection<? extends @NonNull EnumStatement> getEnums() {
            return declaredSubstatements(EnumStatement.class);
        }
    }

    interface LeafrefSpecification extends TypeStatement {
        default @NonNull PathStatement getPath() {
            return findFirstDeclaredSubstatement(PathStatement.class).orElseThrow();
        }

        /**
         * Return require-instance statement child, if present. For RFC6020 semantics, this method always returns null.
         *
         * @return require-instance statement, if present.
         */
        default @Nullable RequireInstanceStatement getRequireInstance() {
            final var opt = findFirstDeclaredSubstatement(RequireInstanceStatement.class);
            return opt.isPresent() ? opt.orElseThrow() : null;
        }
    }

    interface InstanceIdentifierSpecification extends TypeStatement {
        /**
         * Return require-instance statement child, if present. For RFC6020 semantics, this method always returns
         * null.
         *
         * @return require-instance statement, if present.
         */
        default @Nullable RequireInstanceStatement getRequireInstance() {
            final var opt = findFirstDeclaredSubstatement(RequireInstanceStatement.class);
            return opt.isPresent() ? opt.orElseThrow() : null;
        }
    }

    interface IdentityRefSpecification extends TypeStatement {
        /**
         * Returns the base statements.
         *
         * @return collection of base statements (in YANG 1.1 models) or a collection containing just one base
         *         statement (in YANG 1.0 models)
         */
        default @NonNull Collection<? extends @NonNull BaseStatement> getBases() {
            return declaredSubstatements(BaseStatement.class);
        }
    }

    interface BitsSpecification extends TypeStatement {
        default @NonNull Collection<? extends @NonNull BitStatement> getBits() {
            return declaredSubstatements(BitStatement.class);
        }
    }

    interface UnionSpecification extends TypeStatement {
        default @NonNull Collection<? extends @NonNull TypeStatement> getTypes() {
            return declaredSubstatements(TypeStatement.class);
        }
    }

    interface BinarySpecification extends TypeStatement, LengthStatement.OptionalIn<QName> {
        // Nothing else
    }
}
