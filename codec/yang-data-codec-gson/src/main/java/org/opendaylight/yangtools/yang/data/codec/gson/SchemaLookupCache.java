/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * A thread-safe, lazily-populated cache of {@link SchemaNodeLookup}s -- one per parent schema node.
 *
 * <p>Every {@link JSONCodecFactory} owns one of these (rebased factories included) and hands it to each
 * {@link JsonParserStream} it creates, so a lookup computed while parsing one document is still there for the next
 * one.
 *
 * <p>A lookup nobody has asked for in 30 seconds is thrown away, so a factory which parsed a document once and then
 * sits unused does not keep its lookups forever.
 */
final class SchemaLookupCache {
    @VisibleForTesting
    static final Duration IDLE_TIMEOUT = Duration.ofSeconds(30);

    // Every key is a schema node of the one model context our factory is bound to: JsonParserStream refuses an
    // inference from anywhere else, and no other factory shares this cache. A cached answer also never goes out of
    // date, because a schema node's children never change.
    //
    // weakKeys() is how we ask Guava to compare keys by identity (==) rather than equals(), which is what we want for
    // schema nodes.
    //
    // expireAfterAccess() marks a lookup as expired once nobody has asked for it in IDLE_TIMEOUT. Guava has no timer of
    // its own, though: it only removes expired entries while the cache is being used. That is why we also schedule
    // cleanup() below -- otherwise a factory that is never used again would keep its expired lookups.
    //
    // softValues() lets the JVM discard a lookup nobody is using when memory runs short, even before IDLE_TIMEOUT has
    // passed. SharedCodecCache caches codecs the same way: a cache must never be what causes an OutOfMemoryError.
    //
    // Removing a lookup while a parser is still using it is harmless: JsonParserStream keeps its own reference to the
    // lookup for the whole JSON object it is parsing, and simply gets a new one for the next object.
    private final LoadingCache<DataSchemaNode, SchemaNodeLookup> lookups;
    // Runs a task IDLE_TIMEOUT after it has been handed over.
    private final Executor cleanupExecutor;
    // True while a cleanup() task is waiting to run, so that we never have more than one waiting.
    private final AtomicBoolean cleanupScheduled = new AtomicBoolean();

    SchemaLookupCache() {
        // CompletableFuture.delayedExecutor() uses a timer thread the JDK already has, so we do not start one of our
        // own. Once the delay is over, the task runs in the common ForkJoinPool.
        this(Ticker.systemTicker(), CompletableFuture.delayedExecutor(IDLE_TIMEOUT.toNanos(), NANOSECONDS));
    }

    @VisibleForTesting
    SchemaLookupCache(final Ticker ticker, final Executor cleanupExecutor) {
        this.cleanupExecutor = requireNonNull(cleanupExecutor);
        lookups = CacheBuilder.newBuilder()
            .weakKeys()
            .softValues()
            .expireAfterAccess(IDLE_TIMEOUT)
            .ticker(ticker)
            .build(CacheLoader.from(SchemaNodeLookup::new));
    }

    /**
     * {@return the lookup for a parent schema node, creating it on first use}
     *
     * <p>The returned lookup refers to {@code parent} only weakly, so the caller has to hold on to {@code parent} for
     * as long as it uses the lookup. {@link JsonParserStream} does exactly that: it keeps the parent schema node in a
     * local variable for the whole JSON object it is parsing.
     *
     * @param parent parent schema node
     */
    @NonNull SchemaNodeLookup lookupFor(final @NonNull DataSchemaNode parent) {
        final var lookup = lookups.getUnchecked(parent);
        scheduleCleanup();
        return lookup;
    }

    @VisibleForTesting
    long size() {
        return lookups.size();
    }

    private void scheduleCleanup() {
        // The plain get() first is cheap and nearly always false, so busy parsers do not all fight over the
        // compareAndSet().
        if (!cleanupScheduled.get() && cleanupScheduled.compareAndSet(false, true)) {
            // The task must not hold this cache strongly: it can wait for up to IDLE_TIMEOUT, and during that time it
            // would keep a factory nobody uses any more -- and all of its lookups -- alive.
            final var ref = new WeakReference<>(this);
            cleanupExecutor.execute(() -> runCleanup(ref));
        }
    }

    // static, so that the lambda above cannot capture 'this' by accident
    private static void runCleanup(final WeakReference<SchemaLookupCache> ref) {
        final var cache = ref.get();
        if (cache != null) {
            cache.cleanup();
        }
    }

    private void cleanup() {
        lookups.cleanUp();

        // Clear the flag before looking at size(). If lookupFor() adds a lookup at the same time, then either it sees
        // the cleared flag and schedules the next cleanup itself, or we see its lookup below and schedule it.
        //
        // A lookup used shortly before this cleanup has not expired yet, so it survives until the next one. A lookup
        // is therefore removed somewhere between one and two IDLE_TIMEOUTs after it was last used.
        cleanupScheduled.set(false);
        if (lookups.size() != 0) {
            scheduleCleanup();
        }
        // Otherwise the cache is empty, and nothing is scheduled until lookupFor() is called again.
    }
}
