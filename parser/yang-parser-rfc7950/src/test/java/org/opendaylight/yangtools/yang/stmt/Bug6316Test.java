/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;

class Bug6316Test extends AbstractYangTest {
    @Test
    void test() {
        final var context = assertEffectiveModelDir("/bugs/bug6316");
        verifyEnumTypedefinition(context);
        verifyBitsTypedefinition(context);
    }

    private static void verifyEnumTypedefinition(final SchemaContext context) {
        final DataSchemaNode dataChildByName = context.getDataChildByName(QName.create("foo", "enum-leaf"));
        assertTrue(dataChildByName instanceof LeafSchemaNode);
        final LeafSchemaNode enumLeaf = (LeafSchemaNode) dataChildByName;
        final TypeDefinition<? extends TypeDefinition<?>> type = enumLeaf.getType();
        assertTrue(type instanceof EnumTypeDefinition);
        final EnumTypeDefinition myEnumeration = (EnumTypeDefinition) type;
        final List<EnumPair> values = myEnumeration.getValues();
        for (final EnumPair enumPair : values) {
            final String name = enumPair.getName();
            switch (name) {
                case "zero":
                    assertEquals(0, enumPair.getValue());
                    break;
                case "twenty":
                    assertEquals(20, enumPair.getValue());
                    break;
                case "twenty-one":
                    assertEquals(21, enumPair.getValue());
                    break;
                case "two":
                    assertEquals(2, enumPair.getValue());
                    break;
                case "twenty-two":
                    assertEquals(22, enumPair.getValue());
                    break;
                default:
                    fail("Unexpected enum name.");
            }
        }
    }

    private static void verifyBitsTypedefinition(final SchemaContext context) {
        final DataSchemaNode dataChildByName = context.getDataChildByName(QName.create("foo", "bits-leaf"));
        assertTrue(dataChildByName instanceof LeafSchemaNode);
        final LeafSchemaNode bitsLeaf = (LeafSchemaNode) dataChildByName;
        final TypeDefinition<? extends TypeDefinition<?>> type = bitsLeaf.getType();
        assertTrue(type instanceof BitsTypeDefinition);
        final BitsTypeDefinition myBits = (BitsTypeDefinition) type;
        for (final Bit bit : myBits.getBits()) {
            final String name = bit.getName();
            switch (name) {
                case "zero":
                    assertEquals(Uint32.ZERO, bit.getPosition());
                    break;
                case "twenty":
                    assertEquals(Uint32.valueOf(20), bit.getPosition());
                    break;
                case "twenty-one":
                    assertEquals(Uint32.valueOf(21), bit.getPosition());
                    break;
                case "two":
                    assertEquals(Uint32.TWO, bit.getPosition());
                    break;
                case "twenty-two":
                    assertEquals(Uint32.valueOf(22), bit.getPosition());
                    break;
                default:
                    fail("Unexpected bit name.");
            }
        }
    }
}
