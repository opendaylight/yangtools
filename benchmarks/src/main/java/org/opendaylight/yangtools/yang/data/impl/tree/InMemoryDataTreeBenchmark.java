/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.tree;

import com.google.common.collect.Streams;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.CursorAwareDataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModificationCursor;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.InMemoryDataTreeFactory;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Benchmarking of InMemoryDataTree performance.
 *
 * JMH is used for microbenchmarking.
 *
 * @author Lukas Sedlak &lt;lsedlak@cisco.com&gt;
 *
 * @see <a href="http://openjdk.java.net/projects/code-tools/jmh/">JMH</a>
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
public class InMemoryDataTreeBenchmark {

    private static final int WARMUP_ITERATIONS = 10;
    private static final int MEASUREMENT_ITERATIONS = 10;

    private static final int OUTER_LIST_100K = 100000;
    private static final int OUTER_LIST_50K = 50000;
    private static final int OUTER_LIST_10K = 10000;

    private static final NodeIdentifierWithPredicates[] OUTER_LIST_IDS = Streams.mapWithIndex(
        IntStream.range(0, OUTER_LIST_100K),
        (i, index) -> new NodeIdentifierWithPredicates(BenchmarkModel.OUTER_LIST_QNAME, BenchmarkModel.ID_QNAME, i))
            .collect(Collectors.toList()).toArray(new NodeIdentifierWithPredicates[0]);

    private static final YangInstanceIdentifier[] OUTER_LIST_PATHS = Arrays.stream(OUTER_LIST_IDS)
            .map(id -> BenchmarkModel.OUTER_LIST_PATH.node(id).toOptimized())
            .collect(Collectors.toList()).toArray(new YangInstanceIdentifier[0]);

    private static final MapNode EMPTY_OUTER_LIST = ImmutableNodes.mapNodeBuilder(BenchmarkModel.OUTER_LIST).build();
    private static final MapNode ONE_ITEM_INNER_LIST = initInnerListItems(1);
    private static final MapNode TWO_ITEM_INNER_LIST = initInnerListItems(2);
    private static final MapNode TEN_ITEM_INNER_LIST = initInnerListItems(10);

    private static MapNode initInnerListItems(final int count) {
        final CollectionNodeBuilder<MapEntryNode, MapNode> mapEntryBuilder = ImmutableNodes
            .mapNodeBuilder(BenchmarkModel.INNER_LIST);

        for (int i = 0; i < count; ++i) {
            mapEntryBuilder
                .withChild(ImmutableNodes.mapEntry(BenchmarkModel.INNER_LIST_QNAME, BenchmarkModel.NAME_QNAME, i));
        }

        return mapEntryBuilder.build();
    }

    private static final NormalizedNode<?, ?>[] OUTER_LIST_ONE_ITEM_INNER_LIST = initOuterListItems(OUTER_LIST_100K,
        ONE_ITEM_INNER_LIST);
    private static final NormalizedNode<?, ?>[] OUTER_LIST_TWO_ITEM_INNER_LIST = initOuterListItems(OUTER_LIST_50K,
        TWO_ITEM_INNER_LIST);
    private static final NormalizedNode<?, ?>[] OUTER_LIST_TEN_ITEM_INNER_LIST = initOuterListItems(OUTER_LIST_10K,
        TEN_ITEM_INNER_LIST);

    private static NormalizedNode<?,?>[] initOuterListItems(final int outerListItemsCount, final MapNode innerList) {
        return Arrays.stream(OUTER_LIST_IDS).limit(outerListItemsCount)
                .map(id -> ImmutableNodes.mapEntryBuilder().withNodeIdentifier(id).withChild(innerList).build())
                .collect(Collectors.toList()).toArray(new NormalizedNode[0]);
    }

    private DataTree datastore;

    public static void main(final String... args) throws IOException, RunnerException {
        Options opt = new OptionsBuilder()
            .include(".*" + InMemoryDataTreeBenchmark.class.getSimpleName() + ".*")
            .forks(1)
            .build();

        new Runner(opt).run();
    }

    @Setup(Level.Trial)
    public void setup() throws DataValidationFailedException {
        datastore = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_CONFIGURATION,
            BenchmarkModel.createTestContext());

        final DataTreeModification modification = begin();
        modification.write(BenchmarkModel.TEST_PATH, ImmutableContainerNodeBuilder.create()
            .withNodeIdentifier(BenchmarkModel.TEST).withChild(EMPTY_OUTER_LIST).build());
        commit(modification);
    }

    @TearDown
    public void tearDown() {
        datastore = null;
    }

    @Benchmark
    @Warmup(iterations = WARMUP_ITERATIONS, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = MEASUREMENT_ITERATIONS, timeUnit = TimeUnit.MILLISECONDS)
    public void write100KSingleNodeWithOneInnerItemInOneCommitBenchmark() throws DataValidationFailedException {
        final DataTreeModification modification = begin();
        for (int outerListKey = 0; outerListKey < OUTER_LIST_100K; ++outerListKey) {
            modification.write(OUTER_LIST_PATHS[outerListKey], OUTER_LIST_ONE_ITEM_INNER_LIST[outerListKey]);
        }
        commit(modification);
    }

    @Benchmark
    @Warmup(iterations = WARMUP_ITERATIONS, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = MEASUREMENT_ITERATIONS, timeUnit = TimeUnit.MILLISECONDS)
    public void write100KSingleNodeWithOneInnerItemInOneCommitCursorBenchmark() throws DataValidationFailedException {
        final CursorAwareDataTreeModification modification = begin();
        try (DataTreeModificationCursor cursor = modification.createCursor(BenchmarkModel.OUTER_LIST_PATH)) {
            for (int outerListKey = 0; outerListKey < OUTER_LIST_100K; ++outerListKey) {
                cursor.write(OUTER_LIST_IDS[outerListKey], OUTER_LIST_ONE_ITEM_INNER_LIST[outerListKey]);
            }
        }
        commit(modification);
    }

    @Benchmark
    @Warmup(iterations = WARMUP_ITERATIONS, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = MEASUREMENT_ITERATIONS, timeUnit = TimeUnit.MILLISECONDS)
    public void write100KSingleNodeWithOneInnerItemInCommitPerWriteBenchmark() throws DataValidationFailedException {
        for (int outerListKey = 0; outerListKey < OUTER_LIST_100K; ++outerListKey) {
            final DataTreeModification modification = begin();
            modification.write(OUTER_LIST_PATHS[outerListKey], OUTER_LIST_ONE_ITEM_INNER_LIST[outerListKey]);
            commit(modification);
        }
    }

    @Benchmark
    @Warmup(iterations = WARMUP_ITERATIONS, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = MEASUREMENT_ITERATIONS, timeUnit = TimeUnit.MILLISECONDS)
    public void write50KSingleNodeWithTwoInnerItemsInOneCommitBenchmark() throws DataValidationFailedException {
        final DataTreeModification modification = begin();
        for (int outerListKey = 0; outerListKey < OUTER_LIST_50K; ++outerListKey) {
            modification.write(OUTER_LIST_PATHS[outerListKey], OUTER_LIST_TWO_ITEM_INNER_LIST[outerListKey]);
        }
        commit(modification);
    }

    @Benchmark
    @Warmup(iterations = WARMUP_ITERATIONS, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = MEASUREMENT_ITERATIONS, timeUnit = TimeUnit.MILLISECONDS)
    public void write50KSingleNodeWithTwoInnerItemsInOneCommitCursorBenchmark() throws DataValidationFailedException {
        final CursorAwareDataTreeModification modification = begin();
        try (DataTreeModificationCursor cursor = modification.createCursor(BenchmarkModel.OUTER_LIST_PATH)) {
            for (int outerListKey = 0; outerListKey < OUTER_LIST_50K; ++outerListKey) {
                cursor.write(OUTER_LIST_IDS[outerListKey], OUTER_LIST_TWO_ITEM_INNER_LIST[outerListKey]);
            }
        }
        commit(modification);
    }

    @Benchmark
    @Warmup(iterations = WARMUP_ITERATIONS, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = MEASUREMENT_ITERATIONS, timeUnit = TimeUnit.MILLISECONDS)
    public void write50KSingleNodeWithTwoInnerItemsInCommitPerWriteBenchmark() throws DataValidationFailedException {
        for (int outerListKey = 0; outerListKey < OUTER_LIST_50K; ++outerListKey) {
            final DataTreeModification modification = begin();
            modification.write(OUTER_LIST_PATHS[outerListKey], OUTER_LIST_TWO_ITEM_INNER_LIST[outerListKey]);
            commit(modification);
        }
    }

    @Benchmark
    @Warmup(iterations = WARMUP_ITERATIONS, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = MEASUREMENT_ITERATIONS, timeUnit = TimeUnit.MILLISECONDS)
    public void write10KSingleNodeWithTenInnerItemsInOneCommitBenchmark() throws DataValidationFailedException {
        final DataTreeModification modification = begin();
        for (int outerListKey = 0; outerListKey < OUTER_LIST_10K; ++outerListKey) {
            modification.write(OUTER_LIST_PATHS[outerListKey], OUTER_LIST_TEN_ITEM_INNER_LIST[outerListKey]);
        }
        commit(modification);
    }

    @Benchmark
    @Warmup(iterations = WARMUP_ITERATIONS, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = MEASUREMENT_ITERATIONS, timeUnit = TimeUnit.MILLISECONDS)
    public void write10KSingleNodeWithTenInnerItemsInOneCommitCursorBenchmark() throws DataValidationFailedException {
        final CursorAwareDataTreeModification modification = begin();
        try (DataTreeModificationCursor cursor = modification.createCursor(BenchmarkModel.OUTER_LIST_PATH)) {
            for (int outerListKey = 0; outerListKey < OUTER_LIST_10K; ++outerListKey) {
                cursor.write(OUTER_LIST_IDS[outerListKey], OUTER_LIST_TEN_ITEM_INNER_LIST[outerListKey]);
            }
        }
        commit(modification);
    }

    @Benchmark
    @Warmup(iterations = WARMUP_ITERATIONS, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = MEASUREMENT_ITERATIONS, timeUnit = TimeUnit.MILLISECONDS)
    public void write10KSingleNodeWithTenInnerItemsInCommitPerWriteBenchmark() throws DataValidationFailedException {
        for (int outerListKey = 0; outerListKey < OUTER_LIST_10K; ++outerListKey) {
            final DataTreeModification modification = begin();
            modification.write(OUTER_LIST_PATHS[outerListKey], OUTER_LIST_TEN_ITEM_INNER_LIST[outerListKey]);
            commit(modification);
        }
    }

    private CursorAwareDataTreeModification begin() {
        return (CursorAwareDataTreeModification) datastore.takeSnapshot().newModification();
    }

    private void commit(final DataTreeModification modification) throws DataValidationFailedException {
        modification.ready();
        datastore.validate(modification);
        datastore.commit(datastore.prepare(modification));
    }
}
