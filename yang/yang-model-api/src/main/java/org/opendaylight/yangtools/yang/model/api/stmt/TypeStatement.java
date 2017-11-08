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
         * Return require-instance statement child, if present. For RFC6020 semantics, this method always returns
         * null.
         *
         * @return require-instance statement, if present.
         */
        @Nullable RequireInstanceStatement getRequireInstance();
    }

    @Rfc6020AbnfRule("instanceidentifier-specification")
    interface InstanceIdentifierSpecification extends TypeStatement {

        @Nullable RequireInstanceStatement getRequireInstance();
    }

    @Rfc6020AbnfRule("identityref-specification")
    interface IdentityRefSpecification extends TypeStatement {
        /**
         * Returns the base statements.
         *
         * @return collection of base statements (in YANG 1.1 models) or a collection containing just one base
         *         statement (in YANG 1.0 models)
         */
        @Nonnull Collection<? extends BaseStatement> getBases();
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
