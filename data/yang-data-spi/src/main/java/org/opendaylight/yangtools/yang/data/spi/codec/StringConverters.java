/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.codec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.codec.FromStringNormalizer;
import org.opendaylight.yangtools.yang.data.api.codec.StringConverter;
import org.opendaylight.yangtools.yang.data.api.codec.ToStringCanonizer;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;

/**
 * Support for simple {@link StringConverter}s.
 */
@NonNullByDefault
public final class StringConverters {
    private StringConverters() {
        // Hidden on purpose
    }

    public static FromStringNormalizer fromStringNormalizerOf(final TypeDefinition<?> typedef) {
        return stringConverterOf(typedef);
    }

    public static ToStringCanonizer toStringCanonizerOf(final TypeDefinition<?> typedef) {
        return stringConverterOf(typedef);
    }

    public static StringConverter stringConverterOf(final TypeDefinition<?> typedef) {
      return switch (typedef) {
          case BinaryTypeDefinition binaryType -> {
              final var optConstraint = binaryType.getLengthConstraint();
              yield optConstraint.isEmpty() ? new BinaryStringConverter.Unrestricted(binaryType)
                  : new BinaryStringConverter.Restricted(binaryType, optConstraint.orElseThrow());
          }
          case BitsTypeDefinition bitsType -> new BitsStringConverter(bitsType);
          case BooleanTypeDefinition booleanType -> new BooleanStringConverter(booleanType);
          case DecimalTypeDefinition decimalType -> {
              final var optConstraint = decimalType.getRangeConstraint();
              yield optConstraint.isEmpty() ? new Decimal64StringConverter.Unrestricted(decimalType)
                  :  new Decimal64StringConverter.Restricted(decimalType, optConstraint.orElseThrow());
          }
          case EmptyTypeDefinition emptyType -> new EmptyStringConverter(emptyType);
//          case EnumTypeDefinition enumType -> EnumStringCodec.from(enumType);
//          case Int8TypeDefinition int8Type -> AbstractIntegerStringCodec.from(int8Type);
//          case Int16TypeDefinition int16Type -> AbstractIntegerStringCodec.from(int16Type);
//          case Int32TypeDefinition int32Type -> AbstractIntegerStringCodec.from(int32Type);
//          case Int64TypeDefinition int64Type -> AbstractIntegerStringCodec.from(int64Type);
//          case StringTypeDefinition stringType -> StringStringCodec.from(stringType);
//          case Uint8TypeDefinition uint8Type -> AbstractIntegerStringCodec.from(uint8Type);
//          case Uint16TypeDefinition uint16Type -> AbstractIntegerStringCodec.from(uint16Type);
//          case Uint32TypeDefinition uint32Type -> AbstractIntegerStringCodec.from(uint32Type);
//          case Uint64TypeDefinition uint64Type -> AbstractIntegerStringCodec.from(uint64Type);
          // FIXME: all other types via a passed factory
          default -> throw new IllegalArgumentException("Unhandled " + typedef);
      };
    }
}
