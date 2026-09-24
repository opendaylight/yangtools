/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.google.common.base.Ticker;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

/**
 * Tests for the way {@link SchemaLookupCache} throws away lookups nobody has used for a while. Time is faked through
 * a {@link Ticker}, and the delayed cleanup tasks are collected instead of being run, so each test decides exactly
 * when time passes and when a cleanup happens.
 */
class SchemaLookupCacheTest {
    private static final QName TOP = QName.create("yt1899", "top");
    private static final QName FOO = QName.create(TOP, "foo");

    private static final EffectiveModelContext MODEL_CONTEXT =
        YangParserTestUtils.parseYangResourceDirectory("/yt1899/yang");
    private static final ContainerSchemaNode TOP_CONTAINER =
        assertInstanceOf(ContainerSchemaNode.class, MODEL_CONTEXT.dataChildByName(TOP));
    private static final ListSchemaNode FOO_LIST =
        assertInstanceOf(ListSchemaNode.class, TOP_CONTAINER.dataChildByName(FOO));

    private final AtomicLong nanos = new AtomicLong();
    private final Deque<Runnable> pendingCleanups = new ArrayDeque<>();
    private final SchemaLookupCache cache = new SchemaLookupCache(new Ticker() {
        @Override
        public long read() {
            return nanos.get();
        }
    }, pendingCleanups::add);

    @Test
    void unusedLookupIsRemoved() {
        final var first = cache.lookupFor(FOO_LIST);
        assertEquals(1, cache.size());

        advance(SchemaLookupCache.IDLE_TIMEOUT.plusSeconds(1));
        runPendingCleanup();
        assertEquals(0, cache.size());

        // Asking again builds a new lookup from scratch.
        assertNotSame(first, cache.lookupFor(FOO_LIST));
    }

    @Test
    void recentlyUsedLookupSurvivesCleanup() {
        final var first = cache.lookupFor(FOO_LIST);
        advance(Duration.ofSeconds(20));
        // Using the lookup again restarts its idle timer.
        assertSame(first, cache.lookupFor(FOO_LIST));
        advance(Duration.ofSeconds(15));

        // 35 seconds since the first use, but only 15 since the last one: the lookup stays, and another cleanup is
        // scheduled to look at it again later.
        runPendingCleanup();
        assertEquals(1, cache.size());
        assertEquals(1, pendingCleanups.size());

        advance(SchemaLookupCache.IDLE_TIMEOUT.plusSeconds(1));
        runPendingCleanup();
        assertEquals(0, cache.size());
    }

    @Test
    void onlyOneCleanupIsPending() {
        cache.lookupFor(FOO_LIST);
        cache.lookupFor(FOO_LIST);
        cache.lookupFor(TOP_CONTAINER);
        assertEquals(1, pendingCleanups.size());
    }

    @Test
    void emptyCacheSchedulesNoMoreCleanups() {
        cache.lookupFor(FOO_LIST);
        advance(SchemaLookupCache.IDLE_TIMEOUT.plusSeconds(1));
        runPendingCleanup();

        // Nothing left to clean, so an idle factory costs nothing ...
        assertEquals(0, cache.size());
        assertEquals(0, pendingCleanups.size());

        // ... until it is used again.
        cache.lookupFor(FOO_LIST);
        assertEquals(1, pendingCleanups.size());
    }

    private void advance(final Duration duration) {
        nanos.addAndGet(duration.toNanos());
    }

    private void runPendingCleanup() {
        pendingCleanups.remove().run();
    }
}
