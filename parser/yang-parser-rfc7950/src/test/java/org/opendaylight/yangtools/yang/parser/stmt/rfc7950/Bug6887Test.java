/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.model.ri.type.BitBuilder;
import org.opendaylight.yangtools.yang.model.ri.type.EnumPairBuilder;
import org.opendaylight.yangtools.yang.model.ri.type.InvalidBitDefinitionException;
import org.opendaylight.yangtools.yang.model.ri.type.InvalidEnumDefinitionException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class Bug6887Test {

    @Test
    public void testRestrictedEnumeration() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/bug6887/foo.yang");
        assertNotNull(schemaContext);

        final Module foo = schemaContext.findModule("foo", Revision.of("2017-01-26")).get();
        final LeafSchemaNode myEnumerationLeaf = (LeafSchemaNode) foo.getDataChildByName(
                QName.create(foo.getQNameModule(), "my-enumeration-leaf"));

        EnumTypeDefinition enumerationType = (EnumTypeDefinition) myEnumerationLeaf.getType();

        List<EnumPair> enums = enumerationType.getValues();
        assertEquals(2, enums.size());
        final EnumPair yellowEnum = createEnumPair("yellow", 2);
        final EnumPair redEnum = createEnumPair("red", 3);
        assertContainsEnums(enums, yellowEnum, redEnum);

        enumerationType = enumerationType.getBaseType();
        enums = enumerationType.getValues();
        assertEquals(3, enums.size());
        final EnumPair blackEnum = createEnumPair("black", 4);
        assertContainsEnums(enums, yellowEnum, redEnum, blackEnum);

        enumerationType = enumerationType.getBaseType();
        enums = enumerationType.getValues();
        assertEquals(4, enums.size());
        final EnumPair whiteEnum = createEnumPair("white", 1);
        assertContainsEnums(enums, whiteEnum, yellowEnum, redEnum, blackEnum);

        final LeafSchemaNode myEnumerationLeaf2 = (LeafSchemaNode) foo.getDataChildByName(
                QName.create(foo.getQNameModule(), "my-enumeration-leaf-2"));

        enumerationType = (EnumTypeDefinition) myEnumerationLeaf2.getType();
        enums = enumerationType.getValues();
        assertEquals(3, enums.size());
        assertContainsEnums(enums, yellowEnum, redEnum, blackEnum);
    }

    @Test
    public void testInvalidRestrictedEnumeration() {
        final ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSource("/rfc7950/bug6887/foo-invalid.yang"));
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(SourceException.class));
        assertThat(cause.getMessage(), startsWith("Enum 'purple' is not a subset of its base enumeration type "
            + "(foo?revision=2017-02-02)my-derived-enumeration-type."));
    }

    @Test
    public void testInvalidRestrictedEnumeration2() {
        final ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSource("/rfc7950/bug6887/foo-invalid-2.yang"));
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(InvalidEnumDefinitionException.class));
        assertThat(cause.getMessage(), startsWith("Enum 'magenta' is not a subset of its base enumeration type "
            + "(foo?revision=2017-02-02)my-base-enumeration-type."));
    }

    @Test
    public void testInvalidRestrictedEnumeration3() {
        final ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSource("/rfc7950/bug6887/foo-invalid-3.yang"));
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(InvalidEnumDefinitionException.class));
        assertThat(cause.getMessage(), startsWith("Value of enum 'red' must be the same as the value of "
            + "corresponding enum in the base enumeration type (foo?revision=2017-02-02)"
            + "my-derived-enumeration-type."));
    }

    @Test
    public void testInvalidRestrictedEnumeration4() {
        final ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSource("/rfc7950/bug6887/foo-invalid-4.yang"));
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(InvalidEnumDefinitionException.class));
        assertThat(cause.getMessage(), startsWith("Value of enum 'black' must be the same as the value of "
            + "corresponding enum in the base enumeration type (foo?revision=2017-02-02)"
            + "my-base-enumeration-type."));
    }

    @Test
    public void testValidYang10EnumerationWithUnknownStatements() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/bug6887/foo10-valid.yang");
        assertNotNull(schemaContext);
    }

    @Test
    public void testInvalidYang10RestrictedEnumeration() {
        final ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSource("/rfc7950/bug6887/foo10-invalid.yang"));
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(SourceException.class));
        assertThat(cause.getMessage(), startsWith("Restricted enumeration type is not allowed in YANG version 1 [at "));
    }

    @Test
    public void testInvalidYang10RestrictedEnumeration2() {
        final ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSource("/rfc7950/bug6887/foo10-invalid-2.yang"));
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(SourceException.class));
        assertThat(cause.getMessage(), startsWith("Restricted enumeration type is not allowed in YANG version 1 [at "));
    }

    @Test
    public void testRestrictedBits() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/bug6887/bar.yang");
        assertNotNull(schemaContext);

        final Module bar = schemaContext.findModule("bar", Revision.of("2017-02-02")).get();
        final LeafSchemaNode myBitsLeaf = (LeafSchemaNode) bar.getDataChildByName(
                QName.create(bar.getQNameModule(), "my-bits-leaf"));

        BitsTypeDefinition bitsType = (BitsTypeDefinition) myBitsLeaf.getType();

        Collection<? extends Bit> bits = bitsType.getBits();
        assertEquals(2, bits.size());
        Bit bitB = createBit("bit-b", 2);
        Bit bitC = createBit("bit-c", 3);
        assertContainsBits(bits, bitB, bitC);

        bitsType = bitsType.getBaseType();
        bits = bitsType.getBits();
        assertEquals(3, bits.size());
        bitB = createBit("bit-b", 2);
        bitC = createBit("bit-c", 3);
        Bit bitD = createBit("bit-d", 4);
        assertContainsBits(bits, bitB, bitC, bitD);

        bitsType = bitsType.getBaseType();
        bits = bitsType.getBits();
        assertEquals(4, bits.size());
        final Bit bitA = createBit("bit-a", 1);
        bitB = createBit("bit-b", 2);
        bitC = createBit("bit-c", 3);
        bitD = createBit("bit-d", 4);
        assertContainsBits(bits, bitA, bitB, bitC, bitD);

        final LeafSchemaNode myBitsLeaf2 = (LeafSchemaNode) bar.getDataChildByName(
                QName.create(bar.getQNameModule(), "my-bits-leaf-2"));

        bitsType = (BitsTypeDefinition) myBitsLeaf2.getType();
        bits = bitsType.getBits();
        assertEquals(3, bits.size());
        bitB = createBit("bit-b", 2);
        bitC = createBit("bit-c", 3);
        bitD = createBit("bit-d", 4);
        assertContainsBits(bits, bitB, bitC, bitD);
    }

    @Test
    public void testInvalidRestrictedBits() {
        final ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSource("/rfc7950/bug6887/bar-invalid.yang"));
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(SourceException.class));
        assertThat(cause.getMessage(), startsWith("Bit 'bit-w' is not a subset of its base bits type "
            + "(bar?revision=2017-02-02)my-derived-bits-type."));
    }

    @Test
    public void testInvalidRestrictedBits2() {
        final ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSource("/rfc7950/bug6887/bar-invalid-2.yang"));
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(InvalidBitDefinitionException.class));
        assertThat(cause.getMessage(), startsWith("Bit 'bit-x' is not a subset of its base bits type "
            + "(bar?revision=2017-02-02)my-base-bits-type."));
    }

    @Test
    public void testInvalidRestrictedBits3() {
        final ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSource("/rfc7950/bug6887/bar-invalid-3.yang"));
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(InvalidBitDefinitionException.class));
        assertThat(cause.getMessage(), startsWith("Position of bit 'bit-c' must be the same as the position of "
            + "corresponding bit in the base bits type (bar?revision=2017-02-02)my-derived-bits-type."));
    }

    @Test
    public void testInvalidRestrictedBits4() {
        final ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSource("/rfc7950/bug6887/bar-invalid-4.yang"));
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(InvalidBitDefinitionException.class));
        assertThat(cause.getMessage(), startsWith("Position of bit 'bit-d' must be the same as the position of "
            + "corresponding bit in the base bits type (bar?revision=2017-02-02)my-base-bits-type."));
    }

    @Test
    public void testValidYang10BitsWithUnknownStatements() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/bug6887/bar10-valid.yang");
        assertNotNull(schemaContext);
    }

    @Test
    public void testInvalidYang10RestrictedBits() {
        final ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSource("/rfc7950/bug6887/bar10-invalid.yang"));
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(SourceException.class));
        assertThat(cause.getMessage(), startsWith("Restricted bits type is not allowed in YANG version 1 [at "));
    }

    @Test
    public void testInvalidYang10RestrictedBits2() {
        final ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSource("/rfc7950/bug6887/bar10-invalid-2.yang"));
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(SourceException.class));
        assertThat(cause.getMessage(), startsWith("Restricted bits type is not allowed in YANG version 1 [at "));
    }

    private static EnumPair createEnumPair(final String name, final int value) {
        return EnumPairBuilder.create(name, value).build();
    }

    private static void assertContainsEnums(final List<EnumPair> enumList, final EnumPair... enumPairs) {
        for (final EnumPair enumPair : enumPairs) {
            assertTrue(enumList.contains(enumPair));
        }
    }

    private static Bit createBit(final String name, final long position) {
        return BitBuilder.create(name, Uint32.valueOf(position)).build();
    }

    private static void assertContainsBits(final Collection<? extends Bit> bitList, final Bit... bits) {
        for (final Bit bit : bits) {
            assertTrue(bitList.contains(bit));
        }
    }
}
