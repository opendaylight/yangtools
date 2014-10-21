/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.builder.impl.IdentityrefTypeBuilder;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

public class IdentityrefTypeBuilderTest {

    private IdentityrefTypeBuilder identityrefTypeBuilder;

    @Rule
    public ExpectedException expectException = ExpectedException.none();

    @Before
    public void init() {
        identityrefTypeBuilder = new IdentityrefTypeBuilder("test-module", 111, "test string", SchemaPath.ROOT);
    }

    @Test
    public void testSetQName() {
        final QName testQName = QName.create("Test");
        identityrefTypeBuilder.setQName(testQName);
        assertEquals(null, identityrefTypeBuilder.getQName());
    }

    @Test
    public void testGetType() {
        assertNull(identityrefTypeBuilder.getType());
    }

    @Test
    public void testGetTypedef() {
        assertNull(identityrefTypeBuilder.getTypedef());
    }

    @Test
    public void testSetType() {
        expectException.expect(YangParseException.class);
        expectException.expectMessage("Can not set type to");

        identityrefTypeBuilder.setType(null);
    }

    @Test
    public void testSetTypedef() {
        expectException.expect(YangParseException.class);
        expectException.expectMessage("Can not set typedef to");

        identityrefTypeBuilder.setTypedef(null);
    }

    @Test
    public void testSetDescription() {
        expectException.expect(YangParseException.class);
        expectException.expectMessage("Can not set description to");

        identityrefTypeBuilder.setDescription(null);
    }

    @Test
    public void testSetReference() {
        expectException.expect(YangParseException.class);
        expectException.expectMessage("Can not set reference to");

        identityrefTypeBuilder.setReference(null);
    }

    @Test
    public void testSetStatus() {
        expectException.expect(YangParseException.class);
        expectException.expectMessage("Can not set status to");

        identityrefTypeBuilder.setStatus(null);
    }

    @Test
    public void testIsAddedByUses() {
        assertFalse(identityrefTypeBuilder.isAddedByUses());
    }

    @Test
    public void testSetAddedByUses() {
        expectException.expect(YangParseException.class);
        expectException.expectMessage("Identityref type can not be added by uses");

        identityrefTypeBuilder.setAddedByUses(false);
    }

    @Test
    public void testAddUnknownNodeBuilder() {
        expectException.expect(YangParseException.class);
        expectException.expectMessage("Can not add unknown node to");

        identityrefTypeBuilder.addUnknownNodeBuilder(null);
    }

    @Test
    public void testGetQName() {
        assertNull(identityrefTypeBuilder.getQName());
    }

    @Test
    public void testGetPath() {
        assertEquals(SchemaPath.ROOT, identityrefTypeBuilder.getPath());
    }

    @Test
    public void testSetPath() {
        final SchemaPath testSchemaPath = SchemaPath.create(false, QName.create("test"), QName.create("cont"));
        identityrefTypeBuilder.setPath(testSchemaPath);
        assertEquals(testSchemaPath, identityrefTypeBuilder.getPath());
    }

    @Test
    public void testGetDescription() {
        assertNull(identityrefTypeBuilder.getDescription());
    }

    @Test
    public void testGetReference() {
        assertNull(identityrefTypeBuilder.getReference());
    }

    @Test
    public void testGetStatus() {
        assertNull(identityrefTypeBuilder.getStatus());
    }

    @Test
    public void testGetRanges() {
        assertTrue(identityrefTypeBuilder.getRanges().isEmpty());
    }

    @Test
    public void testSetRanges() {
        expectException.expect(YangParseException.class);
        expectException.expectMessage("Can not set ranges to");

        identityrefTypeBuilder.setRanges(null);
    }

    @Test
    public void testGetLengths() {
        assertTrue(identityrefTypeBuilder.getLengths().isEmpty());
    }

    @Test
    public void testSetLengths() {
        expectException.expect(YangParseException.class);
        expectException.expectMessage("Can not set lengths to");

        identityrefTypeBuilder.setLengths(null);
    }

    @Test
    public void testGetPatterns() {
        assertTrue(identityrefTypeBuilder.getPatterns().isEmpty());
    }

    @Test
    public void testSetPatterns() {
        expectException.expect(YangParseException.class);
        expectException.expectMessage("Can not set patterns to");

        identityrefTypeBuilder.setPatterns(null);
    }

    @Test
    public void testGetFractionDigits() {
        assertNull(identityrefTypeBuilder.getFractionDigits());
    }

    @Test
    public void testSetFractionDigits() {
        expectException.expect(YangParseException.class);
        expectException.expectMessage("Can not set fraction digits to");

        identityrefTypeBuilder.setFractionDigits(null);
    }

    @Test
    public void testGetUnknownNodes() {
        assertTrue(identityrefTypeBuilder.getUnknownNodes().isEmpty());
    }

    @Test
    public void testGetDefaultValue() {
        assertNull(identityrefTypeBuilder.getDefaultValue());
    }

    @Test
    public void testSetDefaultValue() {
        expectException.expect(YangParseException.class);
        expectException.expectMessage("Can not set default value to");

        identityrefTypeBuilder.setDefaultValue(null);
    }

    @Test
    public void testGetUnits() {
        assertNull(identityrefTypeBuilder.getUnits());
    }

    @Test
    public void testSetUnits() {
        expectException.expect(YangParseException.class);
        expectException.expectMessage("Can not set units to");

        identityrefTypeBuilder.setUnits(null);
    }

    @Test
    public void testToString() {
        assertTrue(identityrefTypeBuilder.toString().contains("IdentityrefTypeBuilder"));
    }
}
