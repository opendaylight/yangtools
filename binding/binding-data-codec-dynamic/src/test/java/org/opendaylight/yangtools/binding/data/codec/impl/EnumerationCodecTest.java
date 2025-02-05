/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.binding.EnumTypeObject;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;

@ExtendWith(MockitoExtension.class)
class EnumerationCodecTest {
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

    @Mock
    private EnumPair pair;
    @Mock
    private EnumTypeDefinition definition;

    @Test
    void basicTest() {
        doReturn(TestEnum.ENUM.name()).when(pair).getName();
        doReturn(List.of(pair)).when(definition).getValues();

        final var codec = assertDoesNotThrow(() -> EnumerationCodec.of(TestEnum.class, definition));
        assertEquals(codec.deserialize(codec.serialize(TestEnum.ENUM)), TestEnum.ENUM);
        assertEquals(codec.serialize(codec.deserialize(TestEnum.ENUM.name())), TestEnum.ENUM.name());
    }
}
