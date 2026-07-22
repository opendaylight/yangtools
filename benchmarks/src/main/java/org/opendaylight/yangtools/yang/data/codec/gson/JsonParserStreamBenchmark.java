/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.common.collect.ImmutableList;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.data.util.ParserStreamUtils;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Benchmarks the per-element schema resolution performed by {@code JsonParserStream} while parsing a JSON payload of a
 * YANG {@code list}. Each list entry shares the same schema, so the parser resolves the same
 * {@code (parent, name, namespace)} tuples once per entry. The point of the caching added to {@code JsonParserStream}
 * is to turn those repeated resolutions into O(1) lookups.
 *
 * <p>The schema is deliberately shaped like the customer's (OpenROADM-style) models: a wide list entry (many leaves)
 * that also carries {@code choice}/{@code case} nodes, so an uncached lookup scans ~200 children (and recurses into
 * choices) every time.
 *
 * <ul>
 *   <li>{@link #resolveSchemaNodesUncached(Blackhole)} — the pre-cache behaviour: calls the unchanged
 *       {@link ParserStreamUtils#findSchemaNodeByNameAndNamespace(DataSchemaNode, String, XMLNamespace)} once per
 *       element, exactly as the old parser did.</li>
 *   <li>{@link #resolveSchemaNodesCached(Blackhole)} — the same lookups routed through a local replica of the
 *       parser's {@code SchemaNodeCache} (memoize + return a fresh {@link ArrayDeque} copy).</li>
 *   <li>{@link #parseLargePayloadWithCache(Blackhole)} — the real, current {@code JsonParserStream.parse()} on a large
 *       payload, reporting end-to-end wall-clock with caching active.</li>
 * </ul>
 *
 * @see <a href="http://openjdk.java.net/projects/code-tools/jmh/">JMH</a>
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class JsonParserStreamBenchmark {
    private static final String MODULE = "bench";
    private static final int FIELD_COUNT = 30;
    private static final int DECOY_COUNT = 170;
    private static final int CHOICE_COUNT = 3;
    // A choice/case leaf that is exercised alongside the direct-child leaves, so lookups also hit the choice recursion
    // path (and produce a multi-element schema deque).
    private static final String CHOICE_LEAF = "choice0leaf";

    @Param({"1000", "10000", "50000"})
    public int entries;

    private EffectiveModelContext modelContext;
    private JSONCodecFactory factory;
    private DataSchemaNode entrySchema;
    private XMLNamespace namespace;
    private String[] fieldNames;
    private String payloadJson;

    public static void main(final String... args) throws RunnerException {
        new Runner(new OptionsBuilder()
            .include(".*" + JsonParserStreamBenchmark.class.getSimpleName() + ".*")
            .forks(1)
            .build())
            .run();
    }

    @Setup(Level.Trial)
    public void setup() {
        modelContext = YangParserTestUtils.parseYang(buildSchema());
        factory = JSONCodecFactorySupplier.RFC7951.getShared(modelContext);

        final var payload = (DataNodeContainer) modelContext.getDataChildByName(QName.create(MODULE, "payload"));
        entrySchema = payload.getDataChildByName(QName.create(MODULE, "entry"));
        namespace = entrySchema.getQName().getNamespace();
        fieldNames = buildFieldNames();
        payloadJson = buildPayload(entries);
    }

    @Benchmark
    public void resolveSchemaNodesUncached(final Blackhole bh) {
        for (int entry = 0; entry < entries; ++entry) {
            for (var name : fieldNames) {
                bh.consume(ParserStreamUtils.findSchemaNodeByNameAndNamespace(entrySchema, name, namespace));
            }
        }
    }

    @Benchmark
    public void resolveSchemaNodesCached(final Blackhole bh) {
        // Fresh per invocation, mirroring the cost of parsing a single payload: the first occurrence of each unique
        // name pays the scan, every later occurrence is an O(1) hit.
        final Map<String, ImmutableList<DataSchemaNode>> cache = new HashMap<>();
        for (int entry = 0; entry < entries; ++entry) {
            for (var name : fieldNames) {
                final var path = cache.computeIfAbsent(name, key -> ImmutableList.copyOf(
                    ParserStreamUtils.findSchemaNodeByNameAndNamespace(entrySchema, key, namespace)));
                // A fresh deque, because CompositeNodeDataWithSchema.addChild(Deque, ...) consumes it via pop().
                bh.consume(new ArrayDeque<>(path));
            }
        }
    }

    @Benchmark
    public void parseLargePayloadWithCache(final Blackhole bh) throws IOException {
        final var result = new NormalizationResultHolder();
        final var writer = ImmutableNormalizedNodeStreamWriter.from(result);
        try (var parser = JsonParserStream.create(writer, factory)) {
            parser.parse(new JsonReader(new StringReader(payloadJson)));
        }
        bh.consume(result.getResult().data());
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

    private static String buildPayload(final int count) {
        final var sb = new StringBuilder().append("{\"").append(MODULE).append(":payload\":{\"entry\":[");
        for (int entry = 0; entry < count; ++entry) {
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
