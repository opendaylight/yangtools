/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static com.google.common.base.Verify.verify;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;

import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public abstract class AbstractYT1027Test {
    private static final QName DECIMAL = QName.create("yt1027.test", "decimal");
    private static final QName INT64 = QName.create(DECIMAL, "int64");
    private static final QName UINT64 = QName.create(DECIMAL, "uint64");

    static final LeafNode<?> DECIMAL_DATA = ImmutableNodes.leafNode(DECIMAL, new BigDecimal("1.1"));
    static final LeafNode<?> INT64_DATA = ImmutableNodes.leafNode(INT64, 2L);
    static final LeafNode<?> UINT64_DATA = ImmutableNodes.leafNode(UINT64, Uint64.ONE);

    static final String UNQUOTED_DECIMAL = "{\n"
            + "  \"yt1027:decimal\": 1.1\n"
            + "}";
    static final String UNQUOTED_INT64 = "{\n"
            + "  \"yt1027:int64\": 2\n"
            + "}";
    static final String UNQUOTED_UINT64 = "{\n"
            + "  \"yt1027:uint64\": 1\n"
            + "}";

    static EffectiveModelContext SCHEMA_CONTEXT;
    private static DecimalTypeDefinition DECIMAL_TYPE;
    private static Int64TypeDefinition INT64_TYPE;
    private static Uint64TypeDefinition UINT64_TYPE;

    @BeforeClass
    public static void beforeClass() {
        SCHEMA_CONTEXT = YangParserTestUtils.parseYangResourceDirectory("/yt1027");
        DECIMAL_TYPE = (DecimalTypeDefinition) getTypeDefinition(DECIMAL);
        INT64_TYPE = (Int64TypeDefinition) getTypeDefinition(INT64);
        UINT64_TYPE = (Uint64TypeDefinition) getTypeDefinition(UINT64);
    }

    private static TypeDefinition<?> getTypeDefinition(final QName name) {
        DataSchemaNode child = SCHEMA_CONTEXT.findDataTreeChild(name).get();
        verify(child instanceof LeafSchemaNode);
        return ((LeafSchemaNode) child).getType();
    }

    @AfterClass
    public static void afterClass() {
        DECIMAL_TYPE = null;
        INT64_TYPE = null;
        UINT64_TYPE = null;
        SCHEMA_CONTEXT = null;
    }

    @Test
    public void testDecimal() {
        assertThat(codecFactory().decimalCodec(DECIMAL_TYPE), instanceOf(wrapperClass()));
    }

    @Test
    public void testInt64() {
        assertThat(codecFactory().int64Codec(INT64_TYPE), instanceOf(wrapperClass()));
    }

    @Test
    public void testUint64() {
        assertThat(codecFactory().uint64Codec(UINT64_TYPE), instanceOf(wrapperClass()));
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

    final NormalizedNode<?, ?> fromJSON(final String input) throws IOException {
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter, codecFactory());
        jsonParser.parse(new JsonReader(new StringReader(input)));
        return result.getResult();
    }

    private String toJSON(final NormalizedNode<?, ?> input) throws IOException {
        final Writer writer = new StringWriter();
        final NormalizedNodeStreamWriter jsonStream = JSONNormalizedNodeStreamWriter.createExclusiveWriter(
            codecFactory(), SchemaPath.ROOT, null, JsonWriterFactory.createJsonWriter(writer, 2));
        try (NormalizedNodeWriter nodeWriter = NormalizedNodeWriter.forStreamWriter(jsonStream)) {
            nodeWriter.write(input);
        }

        return writer.toString();
    }
}
