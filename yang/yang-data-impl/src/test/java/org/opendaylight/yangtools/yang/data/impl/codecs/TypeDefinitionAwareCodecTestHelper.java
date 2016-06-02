/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.codecs;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.BitsType;
import org.opendaylight.yangtools.yang.model.util.EnumerationType;
import org.opendaylight.yangtools.yang.model.util.UnionType;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public class TypeDefinitionAwareCodecTestHelper {

    @SuppressWarnings("unchecked")
    public static <T> T getCodec( TypeDefinition<?> def, Class<T> clazz ) {
        Object codec = TypeDefinitionAwareCodec.from( def );
        assertNotNull( codec );
        assertTrue( clazz.isAssignableFrom( codec.getClass() ) );
        return (T)codec;
    }

    public static void deserializeWithExpectedIllegalArgEx( Codec<String,?> codec, String param ) {
        try {
            codec.deserialize( param );
            fail( "Expected IllegalArgumentException" );
        } catch (IllegalArgumentException e) {
            // Expected ...
        }
    }

    public static EnumTypeDefinition toEnumTypeDefinition( String... enums ) {
        List<EnumTypeDefinition.EnumPair> enumList = Lists.newArrayList();
        for (String en: enums ) {
            EnumTypeDefinition.EnumPair mockEnum = mock( EnumTypeDefinition.EnumPair.class );
            when( mockEnum.getName() ).thenReturn( en );
            enumList.add( mockEnum );
        }

        return EnumerationType.create( mock( SchemaPath.class ), enumList,
                                       Optional.absent() );
    }

    public static UnionTypeDefinition toUnionTypeDefinition( TypeDefinition<?>... types ) {
        return UnionType.create( Lists.newArrayList( types ) );
    }

    public static BitsTypeDefinition toBitsTypeDefinition( String... bits ) {
        List<BitsTypeDefinition.Bit> bitList = Lists.newArrayList();
        for (String bit: bits ) {
            BitsTypeDefinition.Bit mockBit = mock( BitsTypeDefinition.Bit.class );
            when( mockBit.getName() ).thenReturn( bit );
            bitList.add( mockBit );
        }

        return BitsType.create( mock( SchemaPath.class ), bitList );
    }
}
