/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.Assert.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.codec.xml.XmlParserStream;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

public final class TestUtils {
    private TestUtils() {
        // Hidden on purpose
    }

    static String loadTextFile(final File file) throws IOException {
        final StringBuilder result = new StringBuilder();
        try (BufferedReader bufReader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line = null;
            while ((line = bufReader.readLine()) != null) {
                result.append(line);
            }
        }
        return result.toString();
    }

    static String loadTextFile(final String relativePath) throws IOException, URISyntaxException {
        return loadTextFile(new File(TestUtils.class.getResource(relativePath).toURI()));
    }

    static void loadXmlToNormalizedNodes(final InputStream xmlInputStream, final NormalizationResultHolder result,
            final EffectiveModelContext schemaContext) throws Exception {
        final var reader = UntrustedXML.createXMLStreamReader(xmlInputStream);
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var xmlParser = XmlParserStream.create(streamWriter, schemaContext);
        xmlParser.parse(reader);
    }

    static String normalizedNodesToJsonString(final NormalizedNode data,
            final EffectiveModelContext schemaContext) throws IOException {
        final Writer writer = new StringWriter();
        final NormalizedNodeStreamWriter jsonStream = JSONNormalizedNodeStreamWriter.createExclusiveWriter(
                JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(schemaContext),
                JsonWriterFactory.createJsonWriter(writer, 2));
        try (NormalizedNodeWriter nodeWriter = NormalizedNodeWriter.forStreamWriter(jsonStream)) {
            nodeWriter.write(data);
        }
        return writer.toString();
    }

    static JsonObject childObject(final JsonObject jsonObject, final String... names) {
        for (final String name : names) {
            final JsonObject childJsonObject = jsonObject.getAsJsonObject(name);
            if (childJsonObject != null) {
                return childJsonObject;
            }
        }
        return null;
    }

    static JsonPrimitive childPrimitive(final JsonObject jsonObject, final String... names) {
        for (final String name : names) {
            final JsonPrimitive childJsonPrimitive = jsonObject.getAsJsonPrimitive(name);
            if (childJsonPrimitive != null) {
                return childJsonPrimitive;
            }
        }
        return null;
    }

    static JsonArray childArray(final JsonObject jsonObject, final String... names) {
        for (final String name : names) {
            final JsonArray childJsonArray = jsonObject.getAsJsonArray(name);
            if (childJsonArray != null) {
                return childJsonArray;
            }
        }
        return null;
    }

    static JsonObject resolveCont1(final String jsonOutput) {
        final JsonElement rootElement = JsonParser.parseString(jsonOutput);
        assertTrue(rootElement.isJsonObject());
        final JsonObject rootObject = rootElement.getAsJsonObject();
        final JsonObject cont1 = childObject(rootObject, "complexjson:cont1", "cont1");
        return cont1;
    }

    static JsonObject resolveCont2(final String jsonOutput) {
        final JsonElement rootElement = JsonParser.parseString(jsonOutput);
        assertTrue(rootElement.isJsonObject());
        final JsonObject rootObject = rootElement.getAsJsonObject();
        final JsonObject cont2 = childObject(rootObject, "complexjson:cont2", "cont2");
        return cont2;
    }
}
