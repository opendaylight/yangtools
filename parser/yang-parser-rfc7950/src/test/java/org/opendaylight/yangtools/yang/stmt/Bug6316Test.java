/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;

class Bug6316Test extends AbstractYangTest {
    @Test
    void test() {
        final var context = assertEffectiveModelDir("/bugs/bug6316");
        verifyEnumTypedefinition(context);
        verifyBitsTypedefinition(context);
    }

    private static void verifyEnumTypedefinition(final SchemaContext context) {
        final var enumLeaf = assertInstanceOf(LeafSchemaNode.class,
            context.dataChildByName(QName.create("foo", "enum-leaf")));
        final var myEnumeration = assertInstanceOf(EnumTypeDefinition.class, enumLeaf.typeDefinition());
        final var values = myEnumeration.getValues();
        for (var enumPair : values) {
            switch (enumPair.getName()) {
                case "zero" -> assertEquals(0, enumPair.getValue());
                case "twenty" -> assertEquals(20, enumPair.getValue());
                case "twenty-one" -> assertEquals(21, enumPair.getValue());
                case "two" -> assertEquals(2, enumPair.getValue());
                case "twenty-two" -> assertEquals(22, enumPair.getValue());
                default -> fail("Unexpected enum name.");
            }
        }
    }

    private static void verifyBitsTypedefinition(final SchemaContext context) {
        final var bitsLeaf = assertInstanceOf(LeafSchemaNode.class,
            context.dataChildByName(QName.create("foo", "bits-leaf")));
        final var myBits = assertInstanceOf(BitsTypeDefinition.class, bitsLeaf.typeDefinition());
        for (var bit : myBits.getBits()) {
            switch (bit.getName()) {
                case "zero" -> assertEquals(Uint32.ZERO, bit.getPosition());
                case "twenty" -> assertEquals(Uint32.valueOf(20), bit.getPosition());
                case "twenty-one" -> assertEquals(Uint32.valueOf(21), bit.getPosition());
                case "two" -> assertEquals(Uint32.TWO, bit.getPosition());
                case "twenty-two" -> assertEquals(Uint32.valueOf(22), bit.getPosition());
                default -> fail("Unexpected bit name.");
            }
        }
    }
}
