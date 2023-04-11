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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.TypeDefinitions;

class TypedefConstraintsTest extends AbstractYangTest {
    @Test
    void decimalRangeConstraintsTest() {
        final var context = assertEffectiveModelDir("/stmt-test/constraints");

        assertNotNull(context);

        final var typeDefinitions = context.getTypeDefinitions();
        assertNotNull(typeDefinitions);
        assertEquals(1, typeDefinitions.size());

        final var myDecimal = typeDefinitions.iterator().next();

        final var rangeConstraints = assertInstanceOf(DecimalTypeDefinition.class, myDecimal)
            .getRangeConstraint()
            .orElseThrow().getAllowedRanges().asRanges();

        assertNotNull(rangeConstraints);
        assertEquals(1, rangeConstraints.size());

        final var dataNode = context.getDataChildByName(QName.create("urn:opendaylight.foo", "2013-10-08",
            "id-decimal64"));
        final var leafDecimal = assertInstanceOf(LeafSchemaNode.class, dataNode);

        final var type = leafDecimal.getType();

        final var decType = assertInstanceOf(DecimalTypeDefinition.class, type);
        assertEquals(4, decType.getFractionDigits());

        final var decRangeConstraints = decType.getRangeConstraint().orElseThrow().getAllowedRanges().asRanges();

        assertEquals(1, decRangeConstraints.size());

        final var range = decRangeConstraints.iterator().next();
        assertEquals(Decimal64.of(4, 15000), range.lowerEndpoint());
        assertEquals(4, range.lowerEndpoint().scale());
        assertEquals(Decimal64.of(4, 55000), range.upperEndpoint());
        assertEquals(4, range.upperEndpoint().scale());

        assertEquals(TypeDefinitions.DECIMAL64.bindTo(leafDecimal.getQName().getModule()), decType.getQName());
        assertNull(decType.getBaseType());
    }
}
