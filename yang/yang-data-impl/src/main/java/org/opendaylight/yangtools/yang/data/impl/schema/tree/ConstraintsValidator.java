package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid.DataValidationException;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnknownTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.Decimal64;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
import org.opendaylight.yangtools.yang.model.util.Uint64;

public class ConstraintsValidator {
    private static final String EXCEPTION_TEXT = "Constraint validation error: ";

    public static void validate(TypeDefinition<?> type, Object value) {
        if (type instanceof IntegerTypeDefinition) {
            for (RangeConstraint constraint : ((IntegerTypeDefinition) type).getRangeConstraints()) {
                checkRange(constraint, (Number) value, type);
            }
        } else if (type instanceof UnsignedIntegerTypeDefinition) {
            for (RangeConstraint constraint : ((UnsignedIntegerTypeDefinition) type).getRangeConstraints()) {
                checkRange(constraint, (Number) value, type);
            }
        } else if (type instanceof StringTypeDefinition) {
            final StringTypeDefinition stringType = (StringTypeDefinition) type;
            for (LengthConstraint constraint : stringType.getLengthConstraints()) {
                checkLength(constraint, value);
            }
            for (PatternConstraint constraint : stringType.getPatternConstraints()) {
                checkPattern(constraint, (String) value);
            }
        } else if (type instanceof DecimalTypeDefinition) {
            for (RangeConstraint constraint : ((DecimalTypeDefinition) type).getRangeConstraints()) {
                checkRange(constraint, (Number) value, type);
            }
        } else if (type instanceof BinaryTypeDefinition) {
            for (LengthConstraint constraint : ((BinaryTypeDefinition) type).getLengthConstraints()) {
                checkLength(constraint, value);
            }
        } else if (type instanceof EnumTypeDefinition) {
            boolean found = false;
            for (EnumTypeDefinition.EnumPair enumPair : ((EnumTypeDefinition) type).getValues()) {
                if (enumPair.getName().equals(value)) {
                    found = true;
                }
            }
            if (!found) {
                throw new DataValidationException(EXCEPTION_TEXT + "Value '" + value + "' " +
                        "does not correspond to any specified enumeration type.");
            }
        } else if (type instanceof ExtendedType) {
            final ExtendedType extendedType = (ExtendedType) type;
            for (RangeConstraint constraint : extendedType.getRangeConstraints()) {
                checkRange(constraint, (Number) value, type);
            }
            for (LengthConstraint constraint : extendedType.getLengthConstraints()) {
                checkLength(constraint, value);
            }
            for (PatternConstraint constraint : extendedType.getPatternConstraints()) {
                checkPattern(constraint, (String) value);
            }
            // if type is derived then check also constraints of base type
            validate(type.getBaseType(), value);
        } else if (type instanceof UnknownTypeDefinition) {
            final UnknownTypeDefinition unknownType = (UnknownTypeDefinition) type;
            for (RangeConstraint constraint : unknownType.getRangeConstraints()) {
                checkRange(constraint, (Number) value, type);
            }
            for (LengthConstraint constraint : unknownType.getLengthConstraints()) {
                checkLength(constraint, value);
            }
            for (PatternConstraint constraint : unknownType.getPatternConstraints()) {
                checkPattern(constraint, (String) value);
            }
            validate(type.getBaseType(), value); // ?
        } else if (type instanceof UnionTypeDefinition) {
            boolean passed = false;
            for (TypeDefinition<?> unionType : ((UnionTypeDefinition) type).getTypes()) {
                try {
                    validate(unionType, value);
                    passed = true;
                } catch (Exception e) {
                }
            }
            if (!passed) {
                throw new DataValidationException(EXCEPTION_TEXT + "Value '" + value
                        + "' did not pass validation against any of the specified union types");
            }
        }
    }

    private static void checkRange(RangeConstraint constraint, Number value, final TypeDefinition<?> type) {
        final TypeDefinition<?> baseType = getBaseType(type);
        if (baseType instanceof Uint64) {
            if (value instanceof BigInteger) {
                if (((BigInteger) value).compareTo((BigInteger) constraint.getMax()) == 1 ||
                        ((BigInteger) value).compareTo((BigInteger.valueOf(constraint.getMin().longValue()))) == -1) {
                    throw new DataValidationException(EXCEPTION_TEXT + "Value: '" + value + "'. " +
                            constraint.getErrorMessage());
                }
            } else {
                if (BigInteger.valueOf(value.longValue()).compareTo((BigInteger) constraint.getMax()) == 1 ||
                        (BigInteger.valueOf(value.longValue()).compareTo((BigInteger.valueOf(constraint.getMin().longValue()))) == -1)) {
                    throw new DataValidationException(EXCEPTION_TEXT + "Value: '" + value + "'. " +
                            constraint.getErrorMessage());
                }
            }
        } else if (baseType instanceof Decimal64) {
            if (value instanceof BigDecimal) {
                if (((BigDecimal) value).compareTo((BigDecimal) constraint.getMax()) == 1 ||
                        ((BigDecimal) value).compareTo((BigDecimal) constraint.getMin()) == -1) {
                    throw new DataValidationException(EXCEPTION_TEXT + "Value: '" + value + "'. " +
                            constraint.getErrorMessage());
                }
            } else {
                if (BigDecimal.valueOf(value.doubleValue()).compareTo((BigDecimal) constraint.getMax()) == 1 ||
                        (BigDecimal.valueOf(value.doubleValue()).compareTo((BigDecimal) constraint.getMin()) == -1)) {
                    throw new DataValidationException(EXCEPTION_TEXT + "Value: '" + value + "'. " +
                            constraint.getErrorMessage());
                }
            }
        } else {
            if (constraint.getMax().longValue() < value.longValue()
                    || constraint.getMin().longValue() > value.longValue()) {
                throw new DataValidationException(EXCEPTION_TEXT + "Value: '" + value + "'. " +
                        constraint.getErrorMessage());
            }
        }
    }

    private static TypeDefinition<?> getBaseType(TypeDefinition<?> type) {
        TypeDefinition<?> baseType = type;
        while (baseType.getBaseType() != null) {
            baseType = baseType.getBaseType();
        }
        return baseType;
    }

    private static void checkLength(LengthConstraint constraint, Object value) {
        if (value instanceof String) {
            final String stringValue = (String) value;
            if (stringValue.length() > constraint.getMax().longValue() ||
                    stringValue.length() < constraint.getMin().longValue()) {
                throw new DataValidationException(EXCEPTION_TEXT + "Value: '" + value + "'. " +
                        constraint.getErrorMessage());
            }
        } else if (value instanceof byte[]) {
            final byte[] byteValue = (byte[]) value;
            if (byteValue.length > constraint.getMax().longValue() ||
                    byteValue.length < constraint.getMin().longValue()) {
                throw new DataValidationException(EXCEPTION_TEXT + "Value: '" + value + "'. " +
                        constraint.getErrorMessage());
            }
        }
    }

    private static void checkPattern(PatternConstraint constraint, String value) {
        if (!value.matches(constraint.getRegularExpression())) {
            throw new DataValidationException(EXCEPTION_TEXT + "Value: '" + value + "'. " +
                    constraint.getErrorMessage());
        }
    }
}
