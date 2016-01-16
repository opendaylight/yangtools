/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.test.codecs;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.type.BitsTypeBuilder;
import org.opendaylight.yangtools.yang.model.util.type.EnumerationTypeBuilder;
import org.opendaylight.yangtools.yang.model.util.type.UnionTypeBuilder;

public class TypeDefinitionAwareCodecTestHelper {

    @SuppressWarnings("unchecked")
    public static <T> T getCodec( final TypeDefinition<?> def, final Class<T> clazz ) {
        Object codec = TypeDefinitionAwareCodec.from( def );
        assertNotNull( codec );
        assertTrue( clazz.isAssignableFrom( codec.getClass() ) );
        return (T)codec;
    }

    public static void deserializeWithExpectedIllegalArgEx( final Codec<String,?> codec, final String param ) {
        try {
            codec.deserialize( param );
            fail( "Expected IllegalArgumentException" );
        } catch (IllegalArgumentException e) {
            // Expected ...
        }
    }

    public static EnumTypeDefinition toEnumTypeDefinition( final String... enums ) {
        final EnumerationTypeBuilder builder = BaseTypes.enumerationTypeBuilder(mock(SchemaPath.class));

        for (String e : enums) {
            EnumTypeDefinition.EnumPair mockEnum = mock( EnumTypeDefinition.EnumPair.class );
            when(mockEnum.getName()).thenReturn(e);
            builder.addEnum(mockEnum);
        }

        return builder.build();
    }

    public static UnionTypeDefinition toUnionTypeDefinition(final TypeDefinition<?>... types) {
        final UnionTypeBuilder builder = BaseTypes.unionTypeBuilder(mock(SchemaPath.class));

        for (TypeDefinition<?> t : types) {
            builder.addType(t);
        }

        return builder.build();
    }

    public static BitsTypeDefinition toBitsTypeDefinition(final String... bits) {
        final BitsTypeBuilder builder = BaseTypes.bitsTypeBuilder(mock(SchemaPath.class));

        for (String bit: bits) {
            BitsTypeDefinition.Bit mockBit = mock(BitsTypeDefinition.Bit.class);
            when(mockBit.getName()).thenReturn(bit);

            builder.addBit(mockBit);
        }

        return builder.build();
    }
}
