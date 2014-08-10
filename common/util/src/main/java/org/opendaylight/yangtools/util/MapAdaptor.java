/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.romix.scala.collection.concurrent.TrieMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple layer on top of maps, which performs snapshot mediation and optimization of
 * what the underlying implementation is.
 */
public final class MapAdaptor {
    public static final int DEFAULT_COPY_MAX_ITEMS = 100;
    public static final String COPY_MAX_ITEMS_MAX_PROP = "org.opendaylight.yangtools.util.mapadaptor.maxcopy";

    public static final int DEFAULT_PERSIST_MIN_ITEMS = 50;
    public static final String PERSIST_MIN_ITEMS_PROP = "org.opendaylight.yangtools.util.mapadaptor.minpersist";

    private static final Logger LOG = LoggerFactory.getLogger(MapAdaptor.class);
    private static final MapAdaptor DEFAULT_INSTANCE;

    private final boolean useSingleton;
    private final int persistMinItems;
    private final int copyMaxItems;

    static {
        DEFAULT_INSTANCE = new MapAdaptor(true,
                getProperty(COPY_MAX_ITEMS_MAX_PROP, DEFAULT_COPY_MAX_ITEMS),
                getProperty(PERSIST_MIN_ITEMS_PROP, DEFAULT_PERSIST_MIN_ITEMS));
        LOG.debug("Configured HashMap/TrieMap cutoff at {}/{} entries",
                DEFAULT_INSTANCE.persistMinItems, DEFAULT_INSTANCE.copyMaxItems);
    }

    private static final int getProperty(final String name, final int defaultValue) {
        try {
            final String p = System.getProperty(name);
            if (p != null) {
                try {
                    int pi = Integer.valueOf(p);
                    if (pi <= 0) {
                        LOG.warn("Ignoring illegal value of {}: has to be a positive number", name);
                    } else {
                        return pi;
                    }
                } catch (NumberFormatException e) {
                    LOG.warn("Ignoring non-numerical value of {}", name, e);
                }
            }
        } catch (Exception e) {
            LOG.debug("Failed to get {}", name, e);
        }
        return defaultValue;
    }

    private MapAdaptor(final boolean useSingleton, final int copyMaxItems, final int persistMinItems) {
        this.useSingleton = useSingleton;
        this.copyMaxItems = copyMaxItems;
        this.persistMinItems = persistMinItems;
    }

    /**
     * Return the default-configured instance.
     *
     * @return the singleton global instance
     */
    public static MapAdaptor getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    public static MapAdaptor getInstance(final boolean useSingleton, final int copyMaxItems, final int persistMinItems) {
        Preconditions.checkArgument(copyMaxItems >= 0, "copyMaxItems has to be a non-negative integer");
        Preconditions.checkArgument(persistMinItems >= 0, "persistMinItems has to be a positive integer");
        Preconditions.checkArgument(persistMinItems <= copyMaxItems, "persistMinItems must be less than or equal to copyMaxItems");
        return new MapAdaptor(useSingleton, copyMaxItems, persistMinItems);
    }

    /**
     * Input is treated is supposed to be left unmodified, result must be mutable.
     *
     * @param input
     * @return
     */
    public <K, V> Map<K, V> takeSnapshot(final Map<K, V> input) {
        if (input instanceof ReadOnlyTrieMap) {
            return ((ReadOnlyTrieMap<K, V>)input).toReadWrite();
        }

        LOG.trace("Converting input {} to a HashMap", input);

        // FIXME: be a bit smart about allocation based on observed size

        final Map<K, V> ret = new HashMap<>(input);
        LOG.trace("Read-write HashMap is {}", ret);
        return ret;
    }

    /**
     * Input will be thrown away, result will be retained for read-only access or
     * {@link #takeSnapshot(Map)} purposes.
     *
     * @param input
     * @return
     */
    public <K, V> Map<K, V> optimize(final Map<K, V> input) {
        if (input instanceof ReadOnlyTrieMap) {
            LOG.warn("Optimizing read-only map {}", input);
        }

        final int size = input.size();

        /*
         * No-brainer :)
         */
        if (size == 0) {
            LOG.trace("Reducing input {} to an empty map", input);
            return Collections.<K, V>emptyMap();
        }

        /*
         * We retain the persistent map as long as it holds at least
         * persistMinItems
         */
        if (input instanceof ReadWriteTrieMap && size >= persistMinItems) {
            return ((ReadWriteTrieMap<K, V>)input).toReadOnly();
        }

        /*
         * If the user opted to use singleton maps, use them. Except for the case
         * when persistMinItems dictates we should not move off of the persistent
         * map.
         */
        if (useSingleton && size == 1) {
            final Entry<K, V> e = Iterables.getOnlyElement(input.entrySet());
            final Map<K, V> ret = Collections.singletonMap(e.getKey(), e.getValue());
            LOG.trace("Reducing input {} to singleton map {}", input, ret);
            return ret;
        }

        if (size <= copyMaxItems) {
            /*
             * Favor access speed: use a HashMap and copy it on modification.
             */
            if (input instanceof HashMap) {
                return input;
            }

            LOG.trace("Copying input {} to a HashMap ({} entries)", input, size);
            final Map<K, V> ret = new HashMap<>(input);
            LOG.trace("Read-only HashMap is {}", ret);
            return ret;
        }

        /*
         * Favor isolation speed: use a TrieMap and perform snapshots
         *
         * This one is a bit tricky, as the TrieMap is concurrent and does not
         * keep an uptodate size. Updating it requires a full walk -- which is
         * O(N) and we want to avoid that. So we wrap it in an interceptor,
         * which will maintain the size for us.
         */
        LOG.trace("Copying input {} to a TrieMap ({} entries)", input, size);
        final TrieMap<K, V> map = TrieMap.empty();
        map.putAll(input);
        final Map<K, V> ret = new ReadOnlyTrieMap<>(map, size);
        LOG.trace("Read-only TrieMap is {}", ret);
        return ret;
    }
}
