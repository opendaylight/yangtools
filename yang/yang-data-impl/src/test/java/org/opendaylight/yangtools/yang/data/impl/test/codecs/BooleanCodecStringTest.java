/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.test.codecs;

import static org.junit.Assert.*;

import org.junit.Test;

import static org.opendaylight.yangtools.yang.data.impl.test.codecs.TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx;
import static org.opendaylight.yangtools.yang.data.impl.test.codecs.TypeDefinitionAwareCodecTestHelper.getCodec;

import org.opendaylight.yangtools.yang.data.api.codec.BooleanCodec;
import org.opendaylight.yangtools.yang.model.util.BooleanType;

/**
 * Unit tests for BooleanCodecString.
 *
 * @author Thomas Pantelis
 */
public class BooleanCodecStringTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testSerialize() {
        BooleanCodec<String> codec = getCodec( BooleanType.getInstance(), BooleanCodec.class);

        assertEquals( "serialize", "", codec.serialize( null ) );
        assertEquals( "serialize", "true", codec.serialize( true ) );
        assertEquals( "serialize", "false", codec.serialize( false ) );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeserialize() {
        BooleanCodec<String> codec = getCodec( BooleanType.getInstance(), BooleanCodec.class);

        assertEquals( "deserialize", Boolean.TRUE, codec.deserialize( "true" ) );
        assertEquals( "deserialize", Boolean.TRUE, codec.deserialize( "TRUE" ) );
        assertEquals( "deserialize", Boolean.FALSE, codec.deserialize( "FALSE" ) );
        assertEquals( "deserialize", Boolean.FALSE, codec.deserialize( "false" ) );
        assertEquals( "deserialize", null, codec.deserialize( null ) );
        deserializeWithExpectedIllegalArgEx(codec, "foo");
        deserializeWithExpectedIllegalArgEx(codec, "");
    }
}
