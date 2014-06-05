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
import static org.opendaylight.yangtools.yang.data.impl.test.codecs.TypeDefinitionAwareCodecTestHelper.getCodec;
import org.opendaylight.yangtools.yang.data.api.codec.BinaryCodec;
import org.opendaylight.yangtools.yang.model.util.BinaryType;

import com.google.common.io.BaseEncoding;

/**
 * Unit tests for BinaryCodecString.
 *
 * @author Thomas Pantelis
 */
public class BinaryCodecStringTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testSerialize() {
        BinaryCodec<String> codec = getCodec( BinaryType.getInstance(), BinaryCodec.class);

        assertEquals( "serialize", BaseEncoding.base64().encode( new byte[]{1,2,3,4} ),
                      codec.serialize( new byte[]{1,2,3,4} ) );
        assertEquals( "serialize", "", codec.serialize( null ) );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDererialize() {
        BinaryCodec<String> codec = getCodec( BinaryType.getInstance(), BinaryCodec.class);

        assertArrayEquals( "deserialize", new byte[]{1,2,3,4},
                      codec.deserialize( BaseEncoding.base64().encode( new byte[]{1,2,3,4} ) ) );
        assertEquals( "deserialize", null, codec.deserialize( null ) );
    }
}
