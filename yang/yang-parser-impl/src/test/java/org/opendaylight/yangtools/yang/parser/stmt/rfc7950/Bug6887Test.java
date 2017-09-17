/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.model.util.type.BitBuilder;
import org.opendaylight.yangtools.yang.model.util.type.EnumPairBuilder;
import org.opendaylight.yangtools.yang.model.util.type.InvalidBitDefinitionException;
import org.opendaylight.yangtools.yang.model.util.type.InvalidEnumDefinitionException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class Bug6887Test {

    @Test
    public void testRestrictedEnumeration() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/bug6887/foo.yang");
        assertNotNull(schemaContext);

        final Date revision = SimpleDateFormatUtil.getRevisionFormat().parse("2017-01-26");

        final Module foo = schemaContext.findModuleByName("foo", revision);
        assertNotNull(foo);

        final LeafSchemaNode myEnumerationLeaf = (LeafSchemaNode) foo.getDataChildByName(
                QName.create(foo.getQNameModule(), "my-enumeration-leaf"));
        assertNotNull(myEnumerationLeaf);

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
        assertNotNull(myEnumerationLeaf2);

        enumerationType = (EnumTypeDefinition) myEnumerationLeaf2.getType();
        enums = enumerationType.getValues();
        assertEquals(3, enums.size());
        assertContainsEnums(enums, yellowEnum, redEnum, blackEnum);
    }

    @Test
    public void testInvalidRestrictedEnumeration() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/bug6887/foo-invalid.yang");
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof SourceException);
            assertTrue(cause.getMessage().startsWith("Enum 'purple' is not a subset of its base enumeration type "
                    + "(foo?revision=2017-02-02)my-derived-enumeration-type."));
        }
    }

    @Test
    public void testInvalidRestrictedEnumeration2() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/bug6887/foo-invalid-2.yang");
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof InvalidEnumDefinitionException);
            assertTrue(cause.getMessage().startsWith("Enum 'magenta' is not a subset of its base enumeration type "
                    + "(foo?revision=2017-02-02)my-base-enumeration-type."));
        }
    }

    @Test
    public void testInvalidRestrictedEnumeration3() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/bug6887/foo-invalid-3.yang");
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof InvalidEnumDefinitionException);
            assertTrue(cause.getMessage().startsWith("Value of enum 'red' must be the same as the value of "
                    + "corresponding enum in the base enumeration type (foo?revision=2017-02-02)"
                    + "my-derived-enumeration-type."));
        }
    }

    @Test
    public void testInvalidRestrictedEnumeration4() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/bug6887/foo-invalid-4.yang");
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof InvalidEnumDefinitionException);
            assertTrue(cause.getMessage().startsWith("Value of enum 'black' must be the same as the value of "
                    + "corresponding enum in the base enumeration type (foo?revision=2017-02-02)"
                    + "my-base-enumeration-type."));
        }
    }

    @Test
    public void testValidYang10EnumerationWithUnknownStatements() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/bug6887/foo10-valid.yang");
        assertNotNull(schemaContext);
    }

    @Test
    public void testInvalidYang10RestrictedEnumeration() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/bug6887/foo10-invalid.yang");
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof SourceException);
            assertTrue(cause.getMessage().startsWith(
                "Restricted enumeration type is allowed only in YANG 1.1 version."));
        }
    }

    @Test
    public void testInvalidYang10RestrictedEnumeration2() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/bug6887/foo10-invalid-2.yang");
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof SourceException);
            assertTrue(cause.getMessage().startsWith(
                "Restricted enumeration type is allowed only in YANG 1.1 version."));
        }
    }

    @Test
    public void testRestrictedBits() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/bug6887/bar.yang");
        assertNotNull(schemaContext);

        final Date revision = SimpleDateFormatUtil.getRevisionFormat().parse("2017-02-02");

        final Module bar = schemaContext.findModuleByName("bar", revision);
        assertNotNull(bar);

        final LeafSchemaNode myBitsLeaf = (LeafSchemaNode) bar.getDataChildByName(
                QName.create(bar.getQNameModule(), "my-bits-leaf"));
        assertNotNull(myBitsLeaf);

        BitsTypeDefinition bitsType = (BitsTypeDefinition) myBitsLeaf.getType();

        List<Bit> bits = bitsType.getBits();
        assertEquals(2, bits.size());
        Bit bitB = createBit(createSchemaPath(true, bar.getQNameModule(), ImmutableList.of("my-bits-leaf",
                "my-derived-bits-type", "bit-b")), 2);
        Bit bitC = createBit(createSchemaPath(true, bar.getQNameModule(), ImmutableList.of("my-bits-leaf",
                "my-derived-bits-type", "bit-c")), 3);
        assertContainsBits(bits, bitB, bitC);

        bitsType = bitsType.getBaseType();
        bits = bitsType.getBits();
        assertEquals(3, bits.size());
        bitB = createBit(createSchemaPath(true, bar.getQNameModule(), ImmutableList.of("my-derived-bits-type",
                "my-base-bits-type", "bit-b")), 2);
        bitC = createBit(createSchemaPath(true, bar.getQNameModule(), ImmutableList.of("my-derived-bits-type",
                "my-base-bits-type", "bit-c")), 3);
        Bit bitD = createBit(createSchemaPath(true, bar.getQNameModule(), ImmutableList.of("my-derived-bits-type",
                "my-base-bits-type", "bit-d")), 4);
        assertContainsBits(bits, bitB, bitC, bitD);

        bitsType = bitsType.getBaseType();
        bits = bitsType.getBits();
        assertEquals(4, bits.size());
        final Bit bitA = createBit(createSchemaPath(true, bar.getQNameModule(), ImmutableList.of("my-base-bits-type",
                "bits", "bit-a")), 1);
        bitB = createBit(createSchemaPath(true, bar.getQNameModule(), ImmutableList.of("my-base-bits-type",
                "bits", "bit-b")), 2);
        bitC = createBit(createSchemaPath(true, bar.getQNameModule(), ImmutableList.of("my-base-bits-type",
                "bits", "bit-c")), 3);
        bitD = createBit(createSchemaPath(true, bar.getQNameModule(), ImmutableList.of("my-base-bits-type",
                "bits", "bit-d")), 4);
        assertContainsBits(bits, bitA, bitB, bitC, bitD);

        final LeafSchemaNode myBitsLeaf2 = (LeafSchemaNode) bar.getDataChildByName(
                QName.create(bar.getQNameModule(), "my-bits-leaf-2"));
        assertNotNull(myBitsLeaf2);

        bitsType = (BitsTypeDefinition) myBitsLeaf2.getType();
        bits = bitsType.getBits();
        assertEquals(3, bits.size());
        bitB = createBit(createSchemaPath(true, bar.getQNameModule(), ImmutableList.of("my-derived-bits-type",
                "my-base-bits-type", "bit-b")), 2);
        bitC = createBit(createSchemaPath(true, bar.getQNameModule(), ImmutableList.of("my-derived-bits-type",
                "my-base-bits-type", "bit-c")), 3);
        bitD = createBit(createSchemaPath(true, bar.getQNameModule(), ImmutableList.of("my-derived-bits-type",
                "my-base-bits-type", "bit-d")), 4);
        assertContainsBits(bits, bitB, bitC, bitD);
    }

    @Test
    public void testInvalidRestrictedBits() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/bug6887/bar-invalid.yang");
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof SourceException);
            assertTrue(cause.getMessage().startsWith("Bit 'bit-w' is not a subset of its base bits type "
                    + "(bar?revision=2017-02-02)my-derived-bits-type."));
        }
    }

    @Test
    public void testInvalidRestrictedBits2() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/bug6887/bar-invalid-2.yang");
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof InvalidBitDefinitionException);
            assertTrue(cause.getMessage().startsWith("Bit 'bit-x' is not a subset of its base bits type "
                    + "(bar?revision=2017-02-02)my-base-bits-type."));
        }
    }

    @Test
    public void testInvalidRestrictedBits3() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/bug6887/bar-invalid-3.yang");
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof InvalidBitDefinitionException);
            assertTrue(cause.getMessage().startsWith("Position of bit 'bit-c' must be the same as the position of "
                    + "corresponding bit in the base bits type (bar?revision=2017-02-02)my-derived-bits-type."));
        }
    }

    @Test
    public void testInvalidRestrictedBits4() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/bug6887/bar-invalid-4.yang");
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof InvalidBitDefinitionException);
            assertTrue(cause.getMessage().startsWith("Position of bit 'bit-d' must be the same as the position of "
                    + "corresponding bit in the base bits type (bar?revision=2017-02-02)my-base-bits-type."));
        }
    }

    @Test
    public void testValidYang10BitsWithUnknownStatements() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/bug6887/bar10-valid.yang");
        assertNotNull(schemaContext);
    }

    @Test
    public void testInvalidYang10RestrictedBits() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/bug6887/bar10-invalid.yang");
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof SourceException);
            assertTrue(cause.getMessage().startsWith("Restricted bits type is allowed only in YANG 1.1 version."));
        }
    }

    @Test
    public void testInvalidYang10RestrictedBits2() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/bug6887/bar10-invalid-2.yang");
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof SourceException);
            assertTrue(cause.getMessage().startsWith("Restricted bits type is allowed only in YANG 1.1 version."));
        }
    }

    private static EnumPair createEnumPair(final String name, final int value) {
        return EnumPairBuilder.create(name, value).build();
    }

    private static void assertContainsEnums(final List<EnumPair> enumList, final EnumPair... enumPairs) {
        for (final EnumPair enumPair : enumPairs) {
            assertTrue(enumList.contains(enumPair));
        }
    }

    private static Bit createBit(final SchemaPath path, final long position) {
        return BitBuilder.create(path, position).build();
    }

    private static void assertContainsBits(final List<Bit> bitList, final Bit... bits) {
        for (final Bit bit : bits) {
            assertTrue(bitList.contains(bit));
        }
    }

    private static SchemaPath createSchemaPath(final boolean absolute, final QNameModule qnameModule,
            final Iterable<String> localNames) {
        return SchemaPath.create(Iterables.transform(localNames,
            localName -> QName.create(qnameModule, localName)), true);
    }
}
