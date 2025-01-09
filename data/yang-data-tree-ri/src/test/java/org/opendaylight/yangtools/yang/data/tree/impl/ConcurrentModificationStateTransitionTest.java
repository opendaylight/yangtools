/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;

/**
 * Multi-threaded {@link InMemoryDataTreeModification} state transitions.
 */
@ExtendWith(MockitoExtension.class)
class ConcurrentModificationStateTransitionTest extends AbstractTestModelTest {
    private static final Exchanger<CountDownLatch> BNM_EXCHANGER = new Exchanger<>();

    private final DataTree dataTree =
        new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_OPERATIONAL, SCHEMA_CONTEXT);

    private InMemoryDataTreeModification mod;

    @BeforeEach
    void beforeEach() {
        final var real = assertInstanceOf(InMemoryDataTreeModification.class,
            dataTree.takeSnapshot().newModification());
        mod = spy(real);
    }

    @Test
    @Timeout(value = 2)
    void foo() {
        mod.delete(TestModel.TEST_PATH);
        mod.ready();

        // FIXME: test some transitions
        stubNewModification();
        final var newMod = mod.newModification();
    }

    private void stubNewModification() {
        doAnswer(inv -> exchangeLatches(BNM_EXCHANGER)).when(mod).beforeNewModification();
    }

    private static Void exchangeLatches(final Exchanger<CountDownLatch> exchanger) throws InterruptedException {
        final var ours = new CountDownLatch(1);
        final var theirs = BNM_EXCHANGER.exchange(ours);
        ours.countDown();
        theirs.await();
        return null;
    }
}
