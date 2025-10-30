/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;

class YangBinaryOperatorTest {
    @Test
    void serializationCompatibility() throws Exception {
        final byte[] bytes;
        try (var baos = new ByteArrayOutputStream()) {
            try (var oos = new ObjectOutputStream(baos)) {
                oos.writeObject(YangBinaryOperator.EQUALS.exprWith(YangLiteralExpr.empty(), YangLiteralExpr.empty()));
            }
            bytes = baos.toByteArray();
        }

        assertEquals(542, bytes.length);
        assertEquals("""
            ACED0005737200416F72672E6F70656E6461796C696768742E79616E67746F6F6C732E79616E672E78706174682E6170692E59616E6\
            742696E6172794F70657261746F72244578707200000000000000010200014C000674686973243074003E4C6F72672F6F70656E6461\
            796C696768742F79616E67746F6F6C732F79616E672F78706174682F6170692F59616E6742696E6172794F70657261746F723B78720\
            0386F72672E6F70656E6461796C696768742E79616E67746F6F6C732E79616E672E78706174682E6170692E59616E6742696E617279\
            4578707200000000000000010200024C00086C656674457870727400344C6F72672F6F70656E6461796C696768742F79616E67746F6\
            F6C732F79616E672F78706174682F6170692F59616E67457870723B4C000972696768744578707271007E00037870737200396F7267\
            2E6F70656E6461796C696768742E79616E67746F6F6C732E79616E672E78706174682E6170692E59616E674C69746572616C4578707\
            200000000000000010200014C00076C69746572616C7400124C6A6176612F6C616E672F537472696E673B787074000071007E00077E\
            72003C6F72672E6F70656E6461796C696768742E79616E67746F6F6C732E79616E672E78706174682E6170692E59616E6742696E617\
            2794F70657261746F7200000000000000001200007872000E6A6176612E6C616E672E456E756D000000000000000012000078707400\
            06455155414C53""", HexFormat.of().withUpperCase().formatHex(bytes));
    }
}
