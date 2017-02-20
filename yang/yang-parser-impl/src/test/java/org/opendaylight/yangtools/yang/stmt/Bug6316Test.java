/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;

public class Bug6316Test {
    @Test
    public void test() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug6316");
        assertNotNull(context);
        verifyEnumTypedefinition(context);
        verifyBitsTypedefinition(context);
    }

    private static void verifyEnumTypedefinition(final SchemaContext context) {
        final DataSchemaNode dataChildByName = context.getDataChildByName(QName
                .create("foo", "1970-01-01", "enum-leaf"));
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
        final DataSchemaNode dataChildByName = context.getDataChildByName(QName
                .create("foo", "1970-01-01", "bits-leaf"));
        assertTrue(dataChildByName instanceof LeafSchemaNode);
        final LeafSchemaNode bitsLeaf = (LeafSchemaNode) dataChildByName;
        final TypeDefinition<? extends TypeDefinition<?>> type = bitsLeaf.getType();
        assertTrue(type instanceof BitsTypeDefinition);
        final BitsTypeDefinition myBits = (BitsTypeDefinition) type;
        final List<Bit> positions = myBits.getBits();
        for (final Bit bit : positions) {
            final String name = bit.getName();
            switch (name) {
            case "zero":
                assertEquals(0, bit.getPosition());
                break;
            case "twenty":
                assertEquals(20, bit.getPosition());
                break;
            case "twenty-one":
                assertEquals(21, bit.getPosition());
                break;
            case "two":
                assertEquals(2, bit.getPosition());
                break;
            case "twenty-two":
                assertEquals(22, bit.getPosition());
                break;
            default:
                fail("Unexpected bit name.");
            }
        }
    }
}
