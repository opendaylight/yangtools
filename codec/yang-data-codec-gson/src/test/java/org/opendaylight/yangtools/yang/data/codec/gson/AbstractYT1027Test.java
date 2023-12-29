/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

abstract class AbstractYT1027Test {
    private static final QName DECIMAL = QName.create("yt1027.test", "decimal");
    private static final QName INT64 = QName.create(DECIMAL, "int64");
    private static final QName UINT64 = QName.create(DECIMAL, "uint64");

    static final LeafNode<?> DECIMAL_DATA = ImmutableNodes.leafNode(DECIMAL, Decimal64.valueOf("1.1"));
    static final LeafNode<?> INT64_DATA = ImmutableNodes.leafNode(INT64, 2L);
    static final LeafNode<?> UINT64_DATA = ImmutableNodes.leafNode(UINT64, Uint64.ONE);

    static final String UNQUOTED_DECIMAL = """
        {
          "yt1027:decimal": 1.1
        }""";
    static final String UNQUOTED_INT64 = """
        {
          "yt1027:int64": 2
        }""";
    static final String UNQUOTED_UINT64 = """
        {
          "yt1027:uint64": 1
        }""";

    static EffectiveModelContext SCHEMA_CONTEXT;
    private static DecimalTypeDefinition DECIMAL_TYPE;
    private static Int64TypeDefinition INT64_TYPE;
    private static Uint64TypeDefinition UINT64_TYPE;

    @BeforeAll
    static final void beforeClass() {
        SCHEMA_CONTEXT = YangParserTestUtils.parseYang("""
            module yt1027 {
              namespace "yt1027.test";
              prefix tst;

              leaf uint64 {
                type uint64;
              }

              leaf int64 {
                type int64;
              }

              leaf decimal {
                type decimal64 {
                  fraction-digits 1;
                }
              }
            }""");
        DECIMAL_TYPE = assertInstanceOf(DecimalTypeDefinition.class, getTypeDefinition(DECIMAL));
        INT64_TYPE = assertInstanceOf(Int64TypeDefinition.class, getTypeDefinition(INT64));
        UINT64_TYPE = assertInstanceOf(Uint64TypeDefinition.class, getTypeDefinition(UINT64));
    }

    private static TypeDefinition<?> getTypeDefinition(final QName name) {
        return assertInstanceOf(LeafSchemaNode.class, SCHEMA_CONTEXT.findDataTreeChild(name).orElseThrow()).getType();
    }

    @AfterAll
    static final void afterClass() {
        DECIMAL_TYPE = null;
        INT64_TYPE = null;
        UINT64_TYPE = null;
        SCHEMA_CONTEXT = null;
    }

    @Test
    public void testDecimal() {
        assertInstanceOf(wrapperClass(), codecFactory().decimalCodec(DECIMAL_TYPE));
    }

    @Test
    public void testInt64() {
        assertInstanceOf(wrapperClass(), codecFactory().int64Codec(INT64_TYPE));
    }

    @Test
    public void testUint64() {
        assertInstanceOf(wrapperClass(), codecFactory().uint64Codec(UINT64_TYPE));
    }

    @Test
    public void testDecimalSerialization() throws IOException {
        assertEquals(expectedDecimal(), toJSON(DECIMAL_DATA));
    }

    @Test
    public void testInt64Serialization() throws IOException {
        assertEquals(expectedInt64(), toJSON(INT64_DATA));
    }

    @Test
    public void testUint64Serialization() throws IOException {
        assertEquals(expectedUint64(), toJSON(UINT64_DATA));
    }

    @Test
    public void testDecimalParsing() throws IOException {
        assertEquals(DECIMAL_DATA, fromJSON(expectedDecimal()));
    }

    @Test
    public void testInt64Parsing() throws IOException {
        assertEquals(INT64_DATA, fromJSON(expectedInt64()));
    }

    @Test
    public void testUint64Parsing() throws IOException {
        assertEquals(UINT64_DATA, fromJSON(expectedUint64()));
    }

    abstract JSONCodecFactory codecFactory();

    abstract Class<?> wrapperClass();

    abstract String expectedDecimal();

    abstract String expectedInt64();

    abstract String expectedUint64();

    final NormalizedNode fromJSON(final String input) throws IOException {
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var jsonParser = JsonParserStream.create(streamWriter, codecFactory());
        jsonParser.parse(new JsonReader(new StringReader(input)));
        return result.getResult().data();
    }

    private String toJSON(final NormalizedNode input) throws IOException {
        final var writer = new StringWriter();
        final var jsonStream = JSONNormalizedNodeStreamWriter.createExclusiveWriter(
            codecFactory(), JsonWriterFactory.createJsonWriter(writer, 2));
        try (var nodeWriter = NormalizedNodeWriter.forStreamWriter(jsonStream)) {
            nodeWriter.write(input);
        }

        return writer.toString();
    }
}
