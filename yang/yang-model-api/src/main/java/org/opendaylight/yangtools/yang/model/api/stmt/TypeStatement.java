/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static com.google.common.base.Verify.verifyNotNull;

import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

@Rfc6020AbnfRule("type-stmt")
public interface TypeStatement extends DeclaredStatement<String> {
    default @NonNull String getName() {
        // FIXME: YANGTOOLS-908: verifyNotNull() should not be needed here
        return verifyNotNull(argument());
    }

    @Rfc6020AbnfRule("numerical-restrictions")
    interface NumericalRestrictions extends TypeStatement {

        default @NonNull RangeStatement getRange() {
            return findFirstDeclaredSubstatement(RangeStatement.class).get();
        }
    }

    @Rfc6020AbnfRule("decimal64-specification")
    interface Decimal64Specification extends TypeStatement {
        default @NonNull FractionDigitsStatement getFractionDigits() {
            return findFirstDeclaredSubstatement(FractionDigitsStatement.class).get();
        }

        default @Nullable RangeStatement getRange() {
            final Optional<RangeStatement> opt = findFirstDeclaredSubstatement(RangeStatement.class);
            return opt.isPresent() ? opt.get() : null;
        }
    }

    @Rfc6020AbnfRule("string-restrictions")
    interface StringRestrictions extends TypeStatement {
        default @Nullable LengthStatement getLength() {
            final Optional<LengthStatement> opt = findFirstDeclaredSubstatement(LengthStatement.class);
            return opt.isPresent() ? opt.get() : null;
        }

        default @NonNull Collection<? extends PatternStatement> getPatterns() {
            return declaredSubstatements(PatternStatement.class);
        }
    }

    @Rfc6020AbnfRule("enum-specification")
    interface EnumSpecification extends TypeStatement {

        default @NonNull Collection<? extends EnumStatement> getEnums() {
            return declaredSubstatements(EnumStatement.class);
        }
    }

    @Rfc6020AbnfRule("leafref-specification")
    interface LeafrefSpecification extends TypeStatement {
        default @NonNull PathStatement getPath() {
            return findFirstDeclaredSubstatement(PathStatement.class).get();
        }

        /**
         * Return require-instance statement child, if present. For RFC6020 semantics, this method always returns
         * null.
         *
         * @return require-instance statement, if present.
         */
        default @Nullable RequireInstanceStatement getRequireInstance() {
            final Optional<RequireInstanceStatement> opt =
                    findFirstDeclaredSubstatement(RequireInstanceStatement.class);
            return opt.isPresent() ? opt.get() : null;
        }
    }

    @Rfc6020AbnfRule("instanceidentifier-specification")
    interface InstanceIdentifierSpecification extends TypeStatement {
        /**
         * Return require-instance statement child, if present. For RFC6020 semantics, this method always returns
         * null.
         *
         * @return require-instance statement, if present.
         */
        default @Nullable RequireInstanceStatement getRequireInstance() {
            final Optional<RequireInstanceStatement> opt =
                    findFirstDeclaredSubstatement(RequireInstanceStatement.class);
            return opt.isPresent() ? opt.get() : null;
        }
    }

    @Rfc6020AbnfRule("identityref-specification")
    interface IdentityRefSpecification extends TypeStatement {
        /**
         * Returns the base statements.
         *
         * @return collection of base statements (in YANG 1.1 models) or a collection containing just one base
         *         statement (in YANG 1.0 models)
         */
        default @NonNull Collection<? extends BaseStatement> getBases() {
            return declaredSubstatements(BaseStatement.class);
        }
    }

    @Rfc6020AbnfRule("bits-specification")
    interface BitsSpecification extends TypeStatement {
        default @NonNull Collection<? extends BitStatement> getBits() {
            return declaredSubstatements(BitStatement.class);
        }
    }

    @Rfc6020AbnfRule("union-specification")
    interface UnionSpecification extends TypeStatement {
        default @NonNull Collection<? extends TypeStatement> getTypes() {
            return declaredSubstatements(TypeStatement.class);
        }
    }

    @Rfc6020AbnfRule("binary-specification")
    interface BinarySpecification extends TypeStatement {
        default @NonNull Collection<? extends LengthStatement> getLength() {
            return declaredSubstatements(LengthStatement.class);
        }
    }
}
