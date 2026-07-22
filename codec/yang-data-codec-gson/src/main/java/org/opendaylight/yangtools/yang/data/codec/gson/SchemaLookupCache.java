/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * A thread-safe, lazily-populated cache of {@link SchemaNodeLookup}s -- one per parent schema node.
 *
 * <p>Every {@link JSONCodecFactory} owns one of these and hands it to each {@link JsonParserStream} it creates, so a
 * lookup computed while parsing one document is still there for the next one. Rebased factories share their original
 * factory's cache.
 */
final class SchemaLookupCache {
    // Weak keys do two things here: keys are matched by identity (==) rather than equals(), and an entry disappears
    // once its schema node is garbage collected. The second part matters, because a parser can be rooted at a node
    // from any model context -- this cache must never be the reason a schema stays in memory. It only works because
    // SchemaNodeLookup refers back to its own node weakly: a strong reference from the value to the key would keep
    // that key reachable, and the entry would then never expire.
    //
    // Soft values mean an unused lookup survives ordinary garbage collections, but is discarded when the JVM runs
    // short of memory. SharedCodecCache caches codecs the same way, for the same reason: a cache must never be the
    // thing that causes an OutOfMemoryError.
    private final LoadingCache<DataSchemaNode, SchemaNodeLookup> lookups = CacheBuilder.newBuilder()
        .weakKeys().softValues().build(CacheLoader.from(SchemaNodeLookup::new));

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
        return lookups.getUnchecked(parent);
    }
}
