/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.codecs;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.codec.StringCodec;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;

/**
 * Unit tests for StringCodecString.
 *
 * @author Thomas Pantelis
 */
public class StringCodecStringTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testSerialize() {
        StringCodec<String> codec = TypeDefinitionAwareCodecTestHelper.getCodec(BaseTypes.stringType(),
            StringCodec.class);

        assertEquals("serialize", "foo", codec.serialize("foo"));
        assertEquals("serialize", "", codec.serialize(""));
        assertEquals("serialize", "", codec.serialize(null));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeserialize() {
        StringCodec<String> codec = TypeDefinitionAwareCodecTestHelper.getCodec(BaseTypes.stringType(),
            StringCodec.class);

        assertEquals("deserialize", "bar", codec.deserialize("bar"));
        assertEquals("deserialize", "", codec.deserialize(""));
        assertEquals("deserialize", "", codec.deserialize(null));
    }
}
