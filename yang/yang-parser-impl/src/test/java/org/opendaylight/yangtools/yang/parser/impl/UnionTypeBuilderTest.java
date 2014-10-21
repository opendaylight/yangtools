/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.parser.builder.impl.UnionTypeBuilder;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

public class UnionTypeBuilderTest {

    private UnionTypeBuilder unionTypeBuilder;

    @Rule
    public ExpectedException expectException = ExpectedException.none();

    @Before
    public void init() {
        unionTypeBuilder = new UnionTypeBuilder("test-module", 111);
    }

    @Test
    public void testSetQName() {
        expectException.expect(UnsupportedOperationException.class);
        expectException.expectMessage("Can not set qname to union type");

        unionTypeBuilder.setQName(QName.create("test"));
    }

    @Test
    public void testGetType() {
        assertNull(unionTypeBuilder.getType());
    }

    @Test
    public void testSetDescription() {
        expectException.expect(YangParseException.class);
        expectException.expectMessage("Can not set description to");

        unionTypeBuilder.setDescription("test description");
    }

    @Test
    public void testSetReference() {
        expectException.expect(YangParseException.class);
        expectException.expectMessage("Can not set reference to");

        unionTypeBuilder.setReference("test description");
    }

    @Test
    public void testSetStatus() {
        expectException.expect(YangParseException.class);
        expectException.expectMessage("Can not set status to");

        unionTypeBuilder.setStatus(Status.DEPRECATED);
    }

    @Test
    public void testIsAddedByUses() {
        assertFalse(unionTypeBuilder.isAddedByUses());
    }

    @Test
    public void testSetAddedByUses() {
        expectException.expect(YangParseException.class);
        expectException.expectMessage("Union type can not be added by uses");

        unionTypeBuilder.setAddedByUses(false);
    }

    @Test
    public void testSetPath() {
        expectException.expect(YangParseException.class);
        expectException.expectMessage("Can not set path to");

        unionTypeBuilder.setPath(null);
    }

    @Test
    public void testGetDescription() {
        assertNull(unionTypeBuilder.getDescription());
    }

    @Test
    public void testGetReference() {
        assertNull(unionTypeBuilder.getReference());
    }

    @Test
    public void testGetStatus() {
        assertNull(unionTypeBuilder.getStatus());
    }

    @Test
    public void testGetRanges() {
        assertTrue(unionTypeBuilder.getRanges().isEmpty());
    }

    @Test
    public void testSetRanges() {
        expectException.expect(YangParseException.class);
        expectException.expectMessage("Can not set ranges to");

        unionTypeBuilder.setRanges(null);
    }

    @Test
    public void testGetLengths() {
        assertTrue(unionTypeBuilder.getLengths().isEmpty());
    }

    @Test
    public void testSetLengths() {
        expectException.expect(YangParseException.class);
        expectException.expectMessage("Can not set lengths to");

        unionTypeBuilder.setLengths(null);
    }

    @Test
    public void testGetPatterns() {
        assertTrue(unionTypeBuilder.getPatterns().isEmpty());
    }

    @Test
    public void testSetPatterns() {
        expectException.expect(YangParseException.class);
        expectException.expectMessage("Can not set patterns to");

        unionTypeBuilder.setPatterns(null);
    }

    @Test
    public void testGetFractionDigits() {
        assertNull(unionTypeBuilder.getFractionDigits());
    }

    @Test
    public void testSetFractionDigits() {
        expectException.expect(YangParseException.class);
        expectException.expectMessage("Can not set fraction digits to");

        unionTypeBuilder.setFractionDigits(null);
    }

    @Test
    public void testGetUnknownNodes() {
        assertTrue(unionTypeBuilder.getUnknownNodes().isEmpty());
    }

    @Test
    public void testGetDefaultValue() {
        assertNull(unionTypeBuilder.getDefaultValue());
    }

    @Test
    public void testSetDefaultValue() {
        expectException.expect(YangParseException.class);
        expectException.expectMessage("Can not set default value to");

        unionTypeBuilder.setDefaultValue(null);
    }

    @Test
    public void tesGetUnits() {
        assertNull(unionTypeBuilder.getUnits());
    }

    @Test
    public void testSetUnits() {
        expectException.expect(YangParseException.class);
        expectException.expectMessage("Can not set units to");

        unionTypeBuilder.setUnits(null);
    }

    @Test
    public void testToString() {
        assertTrue(unionTypeBuilder.toString().contains("UnionTypeBuilder"));
    }

    @Test
    public void testGetTypedef() {
        assertNull(unionTypeBuilder.getTypedef());
    }
}
