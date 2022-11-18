/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.opendaylight.yangtools.yang.binding.EnumTypeObject;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;

public class EnumerationCodecTest {
    private enum TestEnum implements EnumTypeObject {
        ENUM;

        @Override
        public String getName() {
            return "ENUM";
        }

        @Override
        public int getIntValue() {
            return 0;
        }
    }

    @Test
    public void basicTest() throws Exception {
        final EnumPair pair = mock(EnumPair.class);
        doReturn(TestEnum.ENUM.name()).when(pair).getName();
        doReturn(0).when(pair).getValue();
        EnumTypeDefinition definition = mock(EnumTypeDefinition.class);
        doReturn(ImmutableList.of(pair)).when(definition).getValues();

        final EnumerationCodec codec = EnumerationCodec.of(TestEnum.class, definition);
        assertEquals(codec.deserialize(codec.serialize(TestEnum.ENUM)), TestEnum.ENUM);
        assertEquals(codec.serialize(codec.deserialize(TestEnum.ENUM.name())), TestEnum.ENUM.name());
    }
}