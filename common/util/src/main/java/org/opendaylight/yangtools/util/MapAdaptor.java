/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.yangtools.triemap.MutableTrieMap;
import org.opendaylight.yangtools.triemap.TrieMap;
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

    private static int getProperty(final String name, final int defaultValue) {
        final int val = Integer.getInteger(name, defaultValue).intValue();
        if (val > 0) {
            return val;
        }

        LOG.warn("Ignoring illegal value of {}: has to be a positive number", name);
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

    public static MapAdaptor getInstance(final boolean useSingleton, final int copyMaxItems,
            final int persistMinItems) {
        checkArgument(copyMaxItems >= 0, "copyMaxItems has to be a non-negative integer");
        checkArgument(persistMinItems >= 0, "persistMinItems has to be a positive integer");
        checkArgument(persistMinItems <= copyMaxItems, "persistMinItems must be less than or equal to copyMaxItems");
        return new MapAdaptor(useSingleton, copyMaxItems, persistMinItems);
    }

    /**
     * Creates an initial snapshot. The backing map is selected according to
     * the expected size.
     *
     * @param expectedSize Expected map size
     * @return An empty mutable map.
     */
    public <K, V> Map<K, V> initialSnapshot(final int expectedSize) {
        checkArgument(expectedSize >= 0);
        if (expectedSize > persistMinItems) {
            return new ReadWriteTrieMap<>();
        }

        if (expectedSize < 2) {
            return new HashMap<>(1);
        }
        if (expectedSize == 2) {
            return new HashMap<>(2);
        }
        return Maps.newHashMapWithExpectedSize(expectedSize);
    }

    /**
     * Input is treated is supposed to be left unmodified, result must be mutable.
     */
    @SuppressWarnings("static-method")
    public <K, V> Map<K, V> takeSnapshot(final Map<K, V> input) {
        if (input instanceof ReadOnlyTrieMap) {
            return ((ReadOnlyTrieMap<K, V>)input).toReadWrite();
        }

        LOG.trace("Converting input {} to a HashMap", input);

        /*
         * The default HashMap copy constructor performs a bad thing for small maps, using the default capacity of 16
         * as the minimum sizing hint, which can lead to wasted memory. Since the HashMap grows in powers-of-two, we
         * only kick this in if we are storing 6 entries or less, as that results in 8-entry map -- the next power is
         * 16, which is the default.
         */
        final Map<K, V> ret;
        final int size = input.size();
        if (size <= 6) {
            final int target;
            switch (size) {
                case 0:
                case 1:
                    target = 1;
                    break;
                case 2:
                    target = 2;
                    break;
                case 3:
                    target = 4;
                    break;
                default:
                    target = 8;
            }

            ret = new HashMap<>(target);
            ret.putAll(input);
        } else if (input instanceof HashMap) {
            // HashMap supports cloning, but we want to make sure we trim it down if entries were removed, so we do
            // this only after having checked for small sizes.
            @SuppressWarnings("unchecked")
            final Map<K, V> tmp = (Map<K, V>) ((HashMap<K, V>) input).clone();
            ret = tmp;
        } else {
            ret = new HashMap<>(input);
        }

        LOG.trace("Read-write HashMap is {}", ret);
        return ret;
    }

    /**
     * Input will be thrown away, result will be retained for read-only access or
     * {@link #takeSnapshot(Map)} purposes.
     *
     * @param input non-optimized (read-write) map
     * @return  optimized read-only map
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
            return ImmutableMap.of();
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
            final Entry<K, V> e = input.entrySet().iterator().next();
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
        final MutableTrieMap<K, V> map = TrieMap.create();
        map.putAll(input);
        final Map<K, V> ret = new ReadOnlyTrieMap<>(map, size);
        LOG.trace("Read-only TrieMap is {}", ret);
        return ret;
    }
}
