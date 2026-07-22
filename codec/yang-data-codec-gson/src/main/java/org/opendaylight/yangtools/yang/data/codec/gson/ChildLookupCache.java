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
 * A thread-safe, lazily-populated cache of {@link ChildLookup}s -- one per parent schema node.
 *
 * <p>Every {@link JSONCodecFactory} owns one of these (rebased factories included) and hands it to each
 * {@link JsonParserStream} it creates, so a lookup computed while parsing one document is still there for the next
 * one.
 */
final class ChildLookupCache {
    // Every key comes from the one model context our factory is bound to: JsonParserStream refuses an inference from
    // anywhere else, and no other factory shares this cache. A cached answer also never goes out of date, because a
    // schema node's children never change.
    //
    // weakKeys() drops an entry once nothing else uses its key. Not every parent is held by the model context: some,
    // such as the one for an rpc or a notification, are created anew for each parser, and strong keys would keep one
    // useless entry per parsed document. It also makes keys compare by identity (==), which is fine here.
    //
    // softValues() lets the JVM discard a lookup nobody is using when memory runs short. SharedCodecCache caches
    // codecs the same way: a cache must never be what causes an OutOfMemoryError.
    private final LoadingCache<DataSchemaNode, ChildLookup> lookups = CacheBuilder.newBuilder()
        .weakKeys().softValues().build(CacheLoader.from(ChildLookup::new));

    /**
     * {@return the lookup for a parent schema node, creating it on first use}
     *
     * <p>The returned lookup refers to {@code parent} only weakly, so the caller has to hold on to {@code parent} for
     * as long as it uses the lookup. {@link JsonParserStream} does exactly that: it keeps the parent schema node in a
     * local variable for the whole JSON object it is parsing.
     *
     * @param parent parent schema node
     */
    @NonNull ChildLookup lookupFor(final @NonNull DataSchemaNode parent) {
        return lookups.getUnchecked(parent);
    }
}
