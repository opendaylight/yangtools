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
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.*;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Benchmarking of InMemoryDataTree performance.
 *
 * JMH is used for microbenchmarking.
 *
 * @author Lukas Sedlak <lsedlak@cisco.com>
 *
 * @see <a href="http://openjdk.java.net/projects/code-tools/jmh/">JMH</a>
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class InMemoryDataTreeBenchmark {

    private SchemaContext schemaContext;
    private DataTree datastore;

    public static void main(String... args) throws IOException, RunnerException {
        Options opt = new OptionsBuilder()
            .include(".*" + InMemoryDataTreeBenchmark.class.getSimpleName() + ".*")
            .forks(1)
            .build();

        new Runner(opt).run();
    }

    @Setup(Level.Trial)
    public void setup() throws DataValidationFailedException {
        schemaContext = BenchmarkModel.createTestContext();
        final InMemoryDataTreeFactory factory = InMemoryDataTreeFactory.getInstance();
        datastore = factory.create();
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

        modification.write(testPath, provideTestData());
        datastore.validate(modification);
        final DataTreeCandidate candidate = datastore.prepare(modification);
        datastore.commit(candidate);
    }

    private DataContainerChild<?, ?> provideTestData() {
        return ImmutableContainerNodeBuilder
            .create()
            .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(BenchmarkModel.TEST_QNAME))
            .withChild(
                ImmutableNodes.mapNodeBuilder(BenchmarkModel.OUTER_LIST_QNAME)
                    .build()).build();
    }

    @Benchmark
    @Warmup(iterations = 10, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 20, timeUnit = TimeUnit.MILLISECONDS)
    public void singleNodes100KWriteBenchmark() throws Exception {
        applyWriteSingleNode(100000);
    }

    private void applyWriteSingleNode(final int reps) throws DataValidationFailedException {
        final DataTreeSnapshot snapshot = datastore.takeSnapshot();
        final DataTreeModification modification = snapshot.newModification();
        for (int i = 1; i <= reps; ++i) {
            final int outerListKey = i;
            final int innerListKey = i;

            final YangInstanceIdentifier outerListPath = YangInstanceIdentifier.builder(BenchmarkModel.OUTER_LIST_PATH)
                .nodeWithKey(BenchmarkModel.OUTER_LIST_QNAME, BenchmarkModel.ID_QNAME, outerListKey)
                .build();

            modification.write(outerListPath, addItemIntoInnerList(outerListKey, innerListKey));
        }
        datastore.validate(modification);
        final DataTreeCandidate candidate = datastore.prepare(modification);
        datastore.commit(candidate);
    }

    private NormalizedNode<?, ?> addItemIntoInnerList(final int outerListKey, final int innerListKey) {
        return ImmutableNodes.mapEntryBuilder(BenchmarkModel.OUTER_LIST_QNAME, BenchmarkModel.ID_QNAME, outerListKey) //
            .withChild(ImmutableNodes.mapNodeBuilder(BenchmarkModel.INNER_LIST_QNAME) //
                .withChild(ImmutableNodes.mapEntry(BenchmarkModel.INNER_LIST_QNAME, BenchmarkModel.NAME_QNAME, innerListKey)) //
                .build()) //
            .build();
    }

    @Benchmark
    @Warmup(iterations = 10, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 20, timeUnit = TimeUnit.MILLISECONDS)
    public void twoNodes50KWriteBenchmark() throws Exception {
        applyWriteTwoNodes(50000);
    }

    private void applyWriteTwoNodes(final int reps) throws DataValidationFailedException {
        final DataTreeSnapshot snapshot = datastore.takeSnapshot();
        final DataTreeModification modification = snapshot.newModification();
        for (short i = 1; i <= reps; ++i) {
            final short outerListKey = i;
            final int innerListKey = i;

            final YangInstanceIdentifier outerListPath = YangInstanceIdentifier.builder(BenchmarkModel.OUTER_LIST_PATH)
                .nodeWithKey(BenchmarkModel.OUTER_LIST_QNAME, BenchmarkModel.ID_QNAME, outerListKey)
                .build();

            modification.write(outerListPath, addTwoItemsIntoInnerList(reps, outerListKey, innerListKey));
        }
        datastore.validate(modification);
        final DataTreeCandidate candidate = datastore.prepare(modification);
        datastore.commit(candidate);
    }

    private NormalizedNode<?, ?> addTwoItemsIntoInnerList(final int maxIterations, final int outerListKey, final int innerListKey) {
        final int keyOffset = maxIterations;
        return ImmutableNodes.mapEntryBuilder(BenchmarkModel.OUTER_LIST_QNAME, BenchmarkModel.ID_QNAME, outerListKey) //
            .withChild(ImmutableNodes.mapNodeBuilder(BenchmarkModel.INNER_LIST_QNAME) //
                .withChild(ImmutableNodes.mapEntry(BenchmarkModel.INNER_LIST_QNAME, BenchmarkModel.NAME_QNAME, innerListKey)) //
                .withChild(ImmutableNodes.mapEntry(BenchmarkModel.INNER_LIST_QNAME, BenchmarkModel.NAME_QNAME,
                    String.valueOf(keyOffset + outerListKey)))
                .build()) //
            .build();
    }

    @Benchmark
    @Warmup(iterations = 10, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 20, timeUnit = TimeUnit.MILLISECONDS)
    public void tenNodes10KWriteBenchmark() throws Exception {
        applyWriteTenNodes(10000);
    }

    private void applyWriteTenNodes(final int reps) throws DataValidationFailedException {
        final DataTreeSnapshot snapshot = datastore.takeSnapshot();
        final DataTreeModification modification = snapshot.newModification();
        for (short i = 1; i <= reps; ++i) {
            final short outerListKey = i;
            final int innerListKey = i;

            final YangInstanceIdentifier outerListPath = YangInstanceIdentifier.builder(BenchmarkModel.OUTER_LIST_PATH)
                .nodeWithKey(BenchmarkModel.OUTER_LIST_QNAME, BenchmarkModel.ID_QNAME, outerListKey)
                .build();

            modification.write(outerListPath, addTenItemsIntoInnerList(reps, outerListKey, innerListKey));
        }
        datastore.validate(modification);
        final DataTreeCandidate candidate = datastore.prepare(modification);
        datastore.commit(candidate);
    }

    private NormalizedNode<?, ?> addTenItemsIntoInnerList(final int maxIterations, final int outerListKey, final int innerListKey) {
        int keyOffset = maxIterations;
        return ImmutableNodes.mapEntryBuilder(BenchmarkModel.OUTER_LIST_QNAME, BenchmarkModel.ID_QNAME, outerListKey) //
            .withChild(ImmutableNodes.mapNodeBuilder(BenchmarkModel.INNER_LIST_QNAME) //
                .withChild(ImmutableNodes.mapEntry(BenchmarkModel.INNER_LIST_QNAME, BenchmarkModel.NAME_QNAME, innerListKey)) //
                .withChild(ImmutableNodes.mapEntry(BenchmarkModel.INNER_LIST_QNAME, BenchmarkModel.NAME_QNAME,
                    String.valueOf(keyOffset + outerListKey)))
                .withChild(ImmutableNodes.mapEntry(BenchmarkModel.INNER_LIST_QNAME, BenchmarkModel.NAME_QNAME,
                    String.valueOf((keyOffset += maxIterations) + outerListKey)))
                .withChild(ImmutableNodes.mapEntry(BenchmarkModel.INNER_LIST_QNAME, BenchmarkModel.NAME_QNAME,
                    String.valueOf((keyOffset += maxIterations) + outerListKey)))
                .withChild(ImmutableNodes.mapEntry(BenchmarkModel.INNER_LIST_QNAME, BenchmarkModel.NAME_QNAME,
                    String.valueOf((keyOffset += maxIterations) + outerListKey)))
                .withChild(ImmutableNodes.mapEntry(BenchmarkModel.INNER_LIST_QNAME, BenchmarkModel.NAME_QNAME,
                    String.valueOf((keyOffset += maxIterations) + outerListKey)))
                .withChild(ImmutableNodes.mapEntry(BenchmarkModel.INNER_LIST_QNAME, BenchmarkModel.NAME_QNAME,
                    String.valueOf((keyOffset += maxIterations) + outerListKey)))
                .withChild(ImmutableNodes.mapEntry(BenchmarkModel.INNER_LIST_QNAME, BenchmarkModel.NAME_QNAME,
                    String.valueOf((keyOffset += maxIterations) + outerListKey)))
                .withChild(ImmutableNodes.mapEntry(BenchmarkModel.INNER_LIST_QNAME, BenchmarkModel.NAME_QNAME,
                    String.valueOf((keyOffset += maxIterations) + outerListKey)))
                .withChild(ImmutableNodes.mapEntry(BenchmarkModel.INNER_LIST_QNAME, BenchmarkModel.NAME_QNAME,
                    String.valueOf((keyOffset += maxIterations) + outerListKey)))
                .build()) //
            .build();
    }
}
