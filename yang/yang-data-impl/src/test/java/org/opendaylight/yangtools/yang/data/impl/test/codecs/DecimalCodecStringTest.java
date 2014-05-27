/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.test.codecs;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;

import org.junit.Test;

import static org.opendaylight.yangtools.yang.data.impl.test.codecs.TypeDefinitionAwareCodecTestHelper.getCodec;
import static org.opendaylight.yangtools.yang.data.impl.test.codecs.TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx;

import org.opendaylight.yangtools.yang.data.api.codec.DecimalCodec;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.Decimal64;

/**
 * Unit tests for DecimalCodecString.
 *
 * @author Thomas Pantelis
 */
public class DecimalCodecStringTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testSerialize() {
        DecimalCodec<String> codec = getCodec( Decimal64.create( mock( SchemaPath.class ), 2 ), DecimalCodec.class );

        assertEquals( "serialize", "123.456", codec.serialize( new BigDecimal( "123.456" ) ) );
        assertEquals( "serialize", "", codec.serialize( null ) );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeserialize() {
        DecimalCodec<String> codec = getCodec( Decimal64.create( mock( SchemaPath.class ), 2 ), DecimalCodec.class );

        assertEquals( "deserialize", new BigDecimal( "123.456" ), codec.deserialize( "123.456" ) );

        deserializeWithExpectedIllegalArgEx( codec, "12o.3" );
        deserializeWithExpectedIllegalArgEx( codec, "" );
        deserializeWithExpectedIllegalArgEx( codec, null );
    }
}
