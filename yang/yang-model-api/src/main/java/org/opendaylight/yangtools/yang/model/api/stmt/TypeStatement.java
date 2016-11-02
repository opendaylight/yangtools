/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

@Rfc6020AbnfRule("type-stmt")
public interface TypeStatement extends DeclaredStatement<String> {

    @Nonnull String getName();

    @Rfc6020AbnfRule("numerical-restrictions")
    interface NumericalRestrictions extends TypeStatement {

        @Nonnull RangeStatement getRange();
    }

    @Rfc6020AbnfRule("decimal64-specification")
    interface Decimal64Specification extends TypeStatement {

        @Nonnull FractionDigitsStatement getFractionDigits();

        @Nullable RangeStatement getRange();
    }

    @Rfc6020AbnfRule("string-restrictions")
    interface StringRestrictions extends TypeStatement {

        @Nullable LengthStatement getLength();

        @Nonnull Collection<? extends PatternStatement> getPatterns();
    }

    @Rfc6020AbnfRule("enum-specification")
    interface EnumSpecification extends TypeStatement {

        @Nonnull Collection<? extends EnumStatement> getEnums();
    }

    @Rfc6020AbnfRule("leafref-specification")
    interface LeafrefSpecification extends TypeStatement {

        @Nonnull PathStatement getPath();

        /**
         * All implementations should override this method.
         * The default definition of this method is used only in YANG 1.0 (RFC6020) implementation of
         * LeafrefSpecification which does not support require-instance statement.
         * YANG leafref type has been changed in YANG 1.1 (RFC7950) and now allows require-instance statement.
         *
         * @return require-instance statement
         */
        @Nullable default RequireInstanceStatement getRequireInstance() {
            return null;
        }
    }

    @Rfc6020AbnfRule("instanceidentifier-specification")
    interface InstanceIdentifierSpecification extends TypeStatement {

        @Nullable RequireInstanceStatement getRequireInstance();
    }

    @Rfc6020AbnfRule("identityref-specification")
    interface IdentityRefSpecification extends TypeStatement {

        @Nonnull BaseStatement getBase();
    }

    @Rfc6020AbnfRule("bits-specification")
    interface BitsSpecification extends TypeStatement {

        @Nonnull Collection<? extends BitStatement> getBits();
    }

    @Rfc6020AbnfRule("union-specification")
    interface UnionSpecification extends TypeStatement {

        @Nonnull Collection<? extends TypeStatement> getTypes();
    }

    @Rfc6020AbnfRule("binary-specification")
    interface BinarySpecification extends TypeStatement {

        @Nullable LengthStatement getLength();
    }
}
