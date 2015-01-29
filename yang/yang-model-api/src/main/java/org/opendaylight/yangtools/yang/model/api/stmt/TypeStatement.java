package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

@Rfc6020AbnfRule("type-stmt")
public interface TypeStatement extends DeclaredStatement<QName> {

    String getName();

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

        Collection<? extends PatternStatement> getPatterns();
    }

    @Rfc6020AbnfRule("enum-specification")
    interface EnumSpecification extends TypeStatement {

        Collection<? extends EnumStatement> getEnums();

    }

    @Rfc6020AbnfRule("leafref-specification")
    interface LeafrefSpecification extends TypeStatement {

        @Nullable PathStatement getPath();

    }

    interface InstanceIdentifierSpecification extends TypeStatement {

        @Nullable RequireInstanceStatement getRequireInstance();
    }


    interface IdentityRefSpecification extends TypeStatement {

    }
    interface BitsSpecification extends TypeStatement {

        Collection<? extends BitStatement> getBits();

    }

    interface UnionSpecification extends TypeStatement {

        Collection<? extends TypeStatement> getTypes();

    }
}
