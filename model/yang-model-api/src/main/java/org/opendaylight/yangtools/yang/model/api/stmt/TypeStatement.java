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
public interface TypeStatement extends DeclaredStatement<QName> {
    /**
     * The definition of {@code type} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition DEFINITION = StatementDefinition.of(
        TypeStatement.class, TypeEffectiveStatement.class, YangConstants.RFC6020_YIN_MODULE, "type", "name");

    @Override
    default StatementDefinition statementDefinition() {
        return DEFINITION;
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
    interface StringRestrictions extends TypeStatement {
        default @Nullable LengthStatement getLength() {
            final var opt = findFirstDeclaredSubstatement(LengthStatement.class);
            return opt.isPresent() ? opt.orElseThrow() : null;
        }

        default @NonNull Collection<? extends @NonNull PatternStatement> getPatterns() {
            return declaredSubstatements(PatternStatement.class);
        }
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

    interface BinarySpecification extends TypeStatement {
        default @NonNull Collection<? extends @NonNull LengthStatement> getLength() {
            return declaredSubstatements(LengthStatement.class);
        }
    }
}
