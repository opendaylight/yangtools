/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.tree;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeSnapshot;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
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

    private static final int WARMUP_ITERATIONS = 20;
    private static final int MEASUREMENT_ITERATIONS = 20;

    private static final int OUTER_LIST_100K = 100000;
    private static final int OUTER_LIST_50K = 50000;
    private static final int OUTER_LIST_10K = 10000;

    private static final YangInstanceIdentifier[] OUTER_LIST_100K_PATHS = initOuterListPaths(OUTER_LIST_100K);
    private static final YangInstanceIdentifier[] OUTER_LIST_50K_PATHS = initOuterListPaths(OUTER_LIST_50K);
    private static final YangInstanceIdentifier[] OUTER_LIST_10K_PATHS = initOuterListPaths(OUTER_LIST_10K);

    private static YangInstanceIdentifier[] initOuterListPaths(final int outerListPathsCount) {
        final YangInstanceIdentifier[] paths = new YangInstanceIdentifier[outerListPathsCount];

        for (int outerListKey = 0; outerListKey < outerListPathsCount; ++outerListKey) {
            paths[outerListKey] = YangInstanceIdentifier.builder(BenchmarkModel.OUTER_LIST_PATH)
                .nodeWithKey(BenchmarkModel.OUTER_LIST_QNAME, BenchmarkModel.ID_QNAME, outerListKey)
                .build();
        }
        return paths;
    }

    private static final MapNode ONE_ITEM_INNER_LIST = initInnerListItems(1);
    private static final MapNode TWO_ITEM_INNER_LIST = initInnerListItems(2);
    private static final MapNode TEN_ITEM_INNER_LIST = initInnerListItems(10);

    private static MapNode initInnerListItems(final int count) {
        final CollectionNodeBuilder<MapEntryNode, MapNode> mapEntryBuilder = ImmutableNodes
            .mapNodeBuilder(BenchmarkModel.INNER_LIST_QNAME);

        for (int i = 1; i <= count; ++i) {
            mapEntryBuilder
                .withChild(ImmutableNodes.mapEntry(BenchmarkModel.INNER_LIST_QNAME, BenchmarkModel.NAME_QNAME, i));
        }

        return mapEntryBuilder.build();
    }

    private static final NormalizedNode<?, ?>[] OUTER_LIST_ONE_ITEM_INNER_LIST = initOuterListItems(OUTER_LIST_100K, ONE_ITEM_INNER_LIST);
    private static final NormalizedNode<?, ?>[] OUTER_LIST_TWO_ITEM_INNER_LIST = initOuterListItems(OUTER_LIST_50K, TWO_ITEM_INNER_LIST);
    private static final NormalizedNode<?, ?>[] OUTER_LIST_TEN_ITEM_INNER_LIST = initOuterListItems(OUTER_LIST_10K, TEN_ITEM_INNER_LIST);

    private static NormalizedNode<?,?>[] initOuterListItems(final int outerListItemsCount, final MapNode innerList) {
        final NormalizedNode<?,?>[] outerListItems = new NormalizedNode[outerListItemsCount];

        for (int i = 0; i < outerListItemsCount; ++i) {
            int outerListKey = i;
            outerListItems[i] = ImmutableNodes.mapEntryBuilder(BenchmarkModel.OUTER_LIST_QNAME, BenchmarkModel.ID_QNAME, outerListKey)
                .withChild(innerList).build();
        }
        return outerListItems;
    }

    private SchemaContext schemaContext;
    private DataTree datastore;

    public static void main(final String... args) throws IOException, RunnerException {
        Options opt = new OptionsBuilder()
            .include(".*" + InMemoryDataTreeBenchmark.class.getSimpleName() + ".*")
            .forks(1)
            .build();

        new Runner(opt).run();
    }

    @Setup(Level.Trial)
    public void setup() throws DataValidationFailedException, SourceException, ReactorException {
        schemaContext = BenchmarkModel.createTestContext();
        final InMemoryDataTreeFactory factory = InMemoryDataTreeFactory.getInstance();
        datastore = factory.create(DataTreeConfiguration.DEFAULT_CONFIGURATION);
        datastore.setSchemaContext(schemaContext);
        final DataTreeSnapshot snapshot = datastore.takeSnapshot();
        initTestNode(snapshot);
    }

    @TearDown
    public void tearDown() {
        schemaContext = null;
        datastore = null;
    }

    private void initTestNode(final DataTreeSnapshot snapshot) throws DataValidationFailedException {
        final DataTreeModification modification = snapshot.newModification();
        final YangInstanceIdentifier testPath = YangInstanceIdentifier.builder(BenchmarkModel.TEST_PATH)
            .build();

        modification.write(testPath, provideOuterListNode());
        datastore.validate(modification);
        final DataTreeCandidate candidate = datastore.prepare(modification);
        datastore.commit(candidate);
    }

    private static DataContainerChild<?, ?> provideOuterListNode() {
        return ImmutableContainerNodeBuilder
            .create()
            .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(BenchmarkModel.TEST_QNAME))
            .withChild(
                ImmutableNodes.mapNodeBuilder(BenchmarkModel.OUTER_LIST_QNAME)
                    .build()).build();
    }

    @Benchmark
    @Warmup(iterations = WARMUP_ITERATIONS, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = MEASUREMENT_ITERATIONS, timeUnit = TimeUnit.MILLISECONDS)
    public void write100KSingleNodeWithOneInnerItemInOneCommitBenchmark() throws Exception {
        final DataTreeSnapshot snapshot = datastore.takeSnapshot();
        final DataTreeModification modification = snapshot.newModification();
        for (int outerListKey = 0; outerListKey < OUTER_LIST_100K; ++outerListKey) {
            modification.write(OUTER_LIST_100K_PATHS[outerListKey], OUTER_LIST_ONE_ITEM_INNER_LIST[outerListKey]);
        }
        datastore.validate(modification);
        final DataTreeCandidate candidate = datastore.prepare(modification);
        datastore.commit(candidate);
    }

    @Benchmark
    @Warmup(iterations = WARMUP_ITERATIONS, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = MEASUREMENT_ITERATIONS, timeUnit = TimeUnit.MILLISECONDS)
    public void write100KSingleNodeWithOneInnerItemInCommitPerWriteBenchmark() throws Exception {
        final DataTreeSnapshot snapshot = datastore.takeSnapshot();
        for (int outerListKey = 0; outerListKey < OUTER_LIST_100K; ++outerListKey) {
            final DataTreeModification modification = snapshot.newModification();
            modification.write(OUTER_LIST_100K_PATHS[outerListKey], OUTER_LIST_ONE_ITEM_INNER_LIST[outerListKey]);
            datastore.validate(modification);
            final DataTreeCandidate candidate = datastore.prepare(modification);
            datastore.commit(candidate);
        }
    }

    @Benchmark
    @Warmup(iterations = WARMUP_ITERATIONS, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = MEASUREMENT_ITERATIONS, timeUnit = TimeUnit.MILLISECONDS)
    public void write50KSingleNodeWithTwoInnerItemsInOneCommitBenchmark() throws Exception {
        final DataTreeSnapshot snapshot = datastore.takeSnapshot();
        final DataTreeModification modification = snapshot.newModification();
        for (int outerListKey = 0; outerListKey < OUTER_LIST_50K; ++outerListKey) {
            modification.write(OUTER_LIST_50K_PATHS[outerListKey], OUTER_LIST_TWO_ITEM_INNER_LIST[outerListKey]);
        }
        datastore.validate(modification);
        final DataTreeCandidate candidate = datastore.prepare(modification);
        datastore.commit(candidate);
    }

    @Benchmark
    @Warmup(iterations = WARMUP_ITERATIONS, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = MEASUREMENT_ITERATIONS, timeUnit = TimeUnit.MILLISECONDS)
    public void write50KSingleNodeWithTwoInnerItemsInCommitPerWriteBenchmark() throws Exception {
        final DataTreeSnapshot snapshot = datastore.takeSnapshot();
        for (int outerListKey = 0; outerListKey < OUTER_LIST_50K; ++outerListKey) {
            final DataTreeModification modification = snapshot.newModification();
            modification.write(OUTER_LIST_50K_PATHS[outerListKey], OUTER_LIST_TWO_ITEM_INNER_LIST[outerListKey]);
            datastore.validate(modification);
            final DataTreeCandidate candidate = datastore.prepare(modification);
            datastore.commit(candidate);
        }
    }

    @Benchmark
    @Warmup(iterations = WARMUP_ITERATIONS, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = MEASUREMENT_ITERATIONS, timeUnit = TimeUnit.MILLISECONDS)
    public void write10KSingleNodeWithTenInnerItemsInOneCommitBenchmark() throws Exception {
        final DataTreeSnapshot snapshot = datastore.takeSnapshot();
        final DataTreeModification modification = snapshot.newModification();
        for (int outerListKey = 0; outerListKey < OUTER_LIST_10K; ++outerListKey) {
            modification.write(OUTER_LIST_10K_PATHS[outerListKey], OUTER_LIST_TEN_ITEM_INNER_LIST[outerListKey]);
        }
        datastore.validate(modification);
        final DataTreeCandidate candidate = datastore.prepare(modification);
        datastore.commit(candidate);
    }

    @Benchmark
    @Warmup(iterations = WARMUP_ITERATIONS, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = MEASUREMENT_ITERATIONS, timeUnit = TimeUnit.MILLISECONDS)
    public void write10KSingleNodeWithTenInnerItemsInCommitPerWriteBenchmark() throws Exception {
        final DataTreeSnapshot snapshot = datastore.takeSnapshot();
        for (int outerListKey = 0; outerListKey < OUTER_LIST_10K; ++outerListKey) {
            final DataTreeModification modification = snapshot.newModification();
            modification.write(OUTER_LIST_10K_PATHS[outerListKey], OUTER_LIST_TEN_ITEM_INNER_LIST[outerListKey]);
            datastore.validate(modification);
            final DataTreeCandidate candidate = datastore.prepare(modification);
            datastore.commit(candidate);
        }
    }
}
