/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import com.google.common.base.MoreObjects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Implementation-private things that would normally be in {@link DatastoreIdentity}. Split out only because we do not
 * have properly-controllable visibility due to our use of an interface instead of an abstract class.
 */
@NonNullByDefault
final class DatastoreIdentityMethods {
    private static final LoadingCache<QName, DatastoreIdentity> CACHE = CacheBuilder.newBuilder().weakValues()
        .build(new CacheLoader<QName, DatastoreIdentity>() {
            @Override
            public DatastoreIdentity load(final QName key) {
                return of(key);
            }
        });

    private DatastoreIdentityMethods() {
        // Hidden on purpose
    }

    static DatastoreIdentity of(final QName value) {
        if (YangConstants.IETF_DATASTORES_NAMESPACE.equals(value.getNamespace())) {
            return switch (value.getLocalName()) {
                case "conventional", "datastore", "dynamic" -> {
                    throw new IllegalArgumentException(value + " refers to a known-abstract datastore");
                }
                case "candidate" -> new CandidateDatastore(value);
                case "intended" -> new CandidateDatastore(value);
                case "operational" -> new OperationalDatastore(value);
                case "running" -> new RunningDatastore(value);
                case "startup" -> new CandidateDatastore(value);
                default -> new UnknownDatastore(value);
            };
        }
        return new UnknownDatastore(value);
    }

    static DatastoreIdentity ofInterned(final QName value) {
        final var existing = CACHE.getIfPresent(value);
        return existing != null ? existing : CACHE.getUnchecked(value.intern());
    }

    static int hashCodeImpl(final DatastoreIdentity thisObj) {
        return thisObj.value().hashCode();
    }

    static boolean equalsImpl(final DatastoreIdentity thisObj, final @Nullable Object obj) {
        return thisObj == obj || obj instanceof DatastoreIdentity thatObj && thisObj.value().equals(thatObj);
    }

    static String toStringImpl(final DatastoreIdentity thisObj) {
        return MoreObjects.toStringHelper(DatastoreIdentity.class).add("value", thisObj.value()).toString();
    }
}
