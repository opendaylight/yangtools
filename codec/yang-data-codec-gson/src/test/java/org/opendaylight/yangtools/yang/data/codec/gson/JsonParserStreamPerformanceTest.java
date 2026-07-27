/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.data.util.ParserStreamUtils;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates the effect of the per-parent schema-lookup cache in {@link JsonParserStream}. While parsing a JSON
 * payload of a YANG {@code list}, every entry shares the same schema, so the parser resolves the same
 * {@code (parent, name, namespace)} tuples once per entry. The cache turns those repeated resolutions into O(1)
 * lookups.
 *
 * <p>The cache is unconditional in {@link JsonParserStream}, so there is no in-code "cache off" switch to compare
 * against. Instead the pre-cache cost is reproduced with the unchanged
 * {@link ParserStreamUtils#findSchemaNodeByNameAndNamespace(DataSchemaNode, String, XMLNamespace)} (exactly what the
 * parser called per element before the cache), and compared against a local replica of the parser's internal cache.
 *
 * <p>The schema is deliberately shaped like the reported (OpenROADM-style) models: a wide list entry (many leaves)
 * that also carries {@code choice}/{@code case} nodes, so an uncached lookup scans ~200 children and recurses into
 * choices on every call.
 */
class JsonParserStreamPerformanceTest {
    private static final Logger LOG = LoggerFactory.getLogger(JsonParserStreamPerformanceTest.class);

    private static final String MODULE = "bench";
    private static final int ENTRIES = 5000;
    private static final int FIELD_COUNT = 30;
    private static final int DECOY_COUNT = 170;
    private static final int CHOICE_COUNT = 3;
    // A choice/case leaf exercised alongside the direct-child leaves, so lookups also hit the choice-recursion path
    // and produce a multi-element schema deque.
    private static final String CHOICE_LEAF = "choice0leaf";
    // The measured gap is ~100-200x; gating at 5x keeps the assertion immune to CI timing noise.
    private static final int MIN_SPEEDUP = 5;

    private static EffectiveModelContext modelContext;
    private static JSONCodecFactory factory;
    private static DataSchemaNode entrySchema;
    private static XMLNamespace namespace;
    private static String[] fieldNames;
    private static String payloadJson;

    @BeforeAll
    static void beforeClass() {
        modelContext = YangParserTestUtils.parseYang(buildSchema());
        factory = JSONCodecFactorySupplier.RFC7951.getShared(modelContext);

        final var payload = (DataNodeContainer) modelContext.getDataChildByName(QName.create(MODULE, "payload"));
        entrySchema = payload.getDataChildByName(QName.create(MODULE, "entry"));
        namespace = entrySchema.getQName().getNamespace();
        fieldNames = buildFieldNames();
        payloadJson = buildPayload();
    }

    @AfterAll
    static void afterClass() {
        modelContext = null;
        factory = null;
        entrySchema = null;
        namespace = null;
        fieldNames = null;
        payloadJson = null;
    }

    @Test
    void cachedLookupsReturnSameResultsAsUncached() {
        final Map<String, ImmutableList<DataSchemaNode>> cache = new HashMap<>();
        for (var name : fieldNames) {
            final var uncached = List.copyOf(
                ParserStreamUtils.findSchemaNodeByNameAndNamespace(entrySchema, name, namespace));
            final var cached = List.copyOf(lookupCached(cache, name));

            assertFalse(uncached.isEmpty(), "Lookup unexpectedly empty for field " + name);
            assertEquals(uncached, cached, "Cached lookup differs from uncached for field " + name);
        }
    }

    @Test
    void cachingIsSignificantlyFaster() {
        // Warm up the JIT on both paths before measuring.
        assertTrue(uncachedRun() > 0);
        assertTrue(cachedRun() > 0);

        final long uncachedStart = System.nanoTime();
        final long uncachedChecksum = uncachedRun();
        final long uncachedNanos = System.nanoTime() - uncachedStart;

        final long cachedStart = System.nanoTime();
        final long cachedChecksum = cachedRun();
        final long cachedNanos = System.nanoTime() - cachedStart;

        // Both paths resolve the same schema nodes, so they must compute the same checksum.
        assertEquals(uncachedChecksum, cachedChecksum);

        LOG.info("Schema resolution over {} entries x {} fields: uncached {} ms, cached {} ms, speedup {}x",
            ENTRIES, fieldNames.length, uncachedNanos / 1_000_000, cachedNanos / 1_000_000,
            String.format("%.1f", (double) uncachedNanos / cachedNanos));

        assertTrue(cachedNanos * MIN_SPEEDUP < uncachedNanos,
            "Expected cached lookups to be at least " + MIN_SPEEDUP + "x faster, but uncached=" + uncachedNanos
                + "ns cached=" + cachedNanos + "ns");
    }

    @Test
    void parsesLargePayloadCorrectly() throws IOException {
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final long start = System.nanoTime();
        try (var parser = JsonParserStream.create(streamWriter, factory)) {
            parser.parse(new JsonReader(new StringReader(payloadJson)));
        }
        final long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        final var payload = assertInstanceOf(ContainerNode.class, result.getResult().data());
        final var entries = assertInstanceOf(MapNode.class,
            payload.childByArg(new NodeIdentifier(QName.create(MODULE, "entry"))));
        assertEquals(ENTRIES, entries.size());

        LOG.info("Parsed {} list entries with caching in {} ms", ENTRIES, elapsedMs);
    }

    /**
     * The pre-cache behaviour: a fresh linear scan (with choice recursion) for every element.
     */
    private static long uncachedRun() {
        long checksum = 0;
        for (int entry = 0; entry < ENTRIES; ++entry) {
            for (var name : fieldNames) {
                checksum += ParserStreamUtils.findSchemaNodeByNameAndNamespace(entrySchema, name, namespace).size();
            }
        }
        return checksum;
    }

    /**
     * The same lookups routed through a fresh replica of the parser's {@code SchemaNodeCache}: the first occurrence of
     * each unique name pays the scan, every later occurrence is an O(1) hit. Fresh per invocation, mirroring the cost
     * of parsing a single payload.
     */
    private static long cachedRun() {
        final Map<String, ImmutableList<DataSchemaNode>> cache = new HashMap<>();
        long checksum = 0;
        for (int entry = 0; entry < ENTRIES; ++entry) {
            for (var name : fieldNames) {
                checksum += lookupCached(cache, name).size();
            }
        }
        return checksum;
    }

    private static ArrayDeque<DataSchemaNode> lookupCached(final Map<String, ImmutableList<DataSchemaNode>> cache,
            final String name) {
        final var path = cache.computeIfAbsent(name, key -> ImmutableList.copyOf(
            ParserStreamUtils.findSchemaNodeByNameAndNamespace(entrySchema, key, namespace)));
        // A fresh deque, because CompositeNodeDataWithSchema.addChild(Deque, ...) consumes it via pop().
        return new ArrayDeque<>(path);
    }

    private static String buildSchema() {
        final var sb = new StringBuilder()
            .append("module ").append(MODULE).append(" {\n")
            .append("  namespace ").append(MODULE).append(";\n")
            .append("  prefix ").append(MODULE).append(";\n")
            .append("  container payload {\n")
            .append("    list entry {\n")
            .append("      key id;\n")
            .append("      leaf id { type uint32; }\n");
        for (int i = 0; i < FIELD_COUNT; ++i) {
            sb.append("      leaf field").append(i).append(" { type string; }\n");
        }
        for (int i = 0; i < DECOY_COUNT; ++i) {
            sb.append("      leaf decoy").append(i).append(" { type string; }\n");
        }
        for (int i = 0; i < CHOICE_COUNT; ++i) {
            sb.append("      choice choice").append(i).append(" {\n")
                .append("        case case").append(i).append(" { leaf choice").append(i).append("leaf")
                .append(" { type string; } }\n")
                .append("      }\n");
        }
        return sb.append("    }\n  }\n}\n").toString();
    }

    private static String[] buildFieldNames() {
        final var names = new ArrayList<String>();
        names.add("id");
        for (int i = 0; i < FIELD_COUNT; ++i) {
            names.add("field" + i);
        }
        names.add(CHOICE_LEAF);
        return names.toArray(new String[0]);
    }

    private static String buildPayload() {
        final var sb = new StringBuilder().append("{\"").append(MODULE).append(":payload\":{\"entry\":[");
        for (int entry = 0; entry < ENTRIES; ++entry) {
            if (entry > 0) {
                sb.append(',');
            }
            sb.append("{\"id\":").append(entry);
            for (int i = 0; i < FIELD_COUNT; ++i) {
                sb.append(",\"field").append(i).append("\":\"v").append(i).append('"');
            }
            sb.append(",\"").append(CHOICE_LEAF).append("\":\"c\"}");
        }
        return sb.append("]}}").toString();
    }
}
