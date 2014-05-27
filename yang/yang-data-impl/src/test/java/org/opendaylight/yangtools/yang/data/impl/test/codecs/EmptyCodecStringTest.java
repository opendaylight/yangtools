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
import static org.opendaylight.yangtools.yang.data.impl.test.codecs.TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx;
import org.opendaylight.yangtools.yang.data.api.codec.EmptyCodec;
import org.opendaylight.yangtools.yang.model.util.EmptyType;

/**
 * Unit tests for EmptyCodecString.
 *
 * @author Thomas Pantelis
 */
public class EmptyCodecStringTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testSerialize() {
        EmptyCodec<String> codec = getCodec( EmptyType.getInstance(), EmptyCodec.class);

        assertEquals( "serialize", "", codec.serialize( null ) );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeserialize() {
        EmptyCodec<String> codec = getCodec( EmptyType.getInstance(), EmptyCodec.class);

        assertEquals( "serialize", "", codec.serialize( null ) );
        assertEquals( "deserialize", null, codec.deserialize( "" ) );
        assertEquals( "deserialize", null, codec.deserialize( null ) );

        deserializeWithExpectedIllegalArgEx( codec, "foo" );
    }

}
