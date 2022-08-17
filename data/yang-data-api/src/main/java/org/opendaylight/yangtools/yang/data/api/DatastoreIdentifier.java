/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serial;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.WritableObject;
import org.opendaylight.yangtools.util.AbstractIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.XMLNamespace;

/**
 * Identifier of a RFC8342 (NMDA) datastore. This class is backed by the QName of the datastore, i.e.
 * the {@code identity} which defines the datastore. This class does not allow creation of identifiers which are
 * defined as abstract, that is "datastore", "conventional" and "dynamic" in the namespace of {@code ietf-datastores}.
 */
@Beta
@NonNullByDefault
public final class DatastoreIdentifier extends AbstractIdentifier<QName> implements WritableObject {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final XMLNamespace IETF_DATASTORES_NAMESPACE =
        XMLNamespace.of("urn:ietf:params:xml:ns:yang:ietf-datastores").intern();
    private static final ImmutableSet<String> KNOWN_ABSTRACTS = ImmutableSet.of("datastore", "conventional", "dynamic");

    private static final LoadingCache<QName, DatastoreIdentifier> CACHE = CacheBuilder.newBuilder().weakValues()
            .build(new CacheLoader<QName, DatastoreIdentifier>() {
                @Override
                public DatastoreIdentifier load(final QName key) {
                    return of(key);
                }
            });

    private DatastoreIdentifier(final QName qname) {
        super(qname);
        if (IETF_DATASTORES_NAMESPACE.equals(qname.getNamespace())) {
            checkArgument(!KNOWN_ABSTRACTS.contains(qname.getLocalName()), "%s refers to a known-abstract datastore",
                qname);
        }
    }

    public static DatastoreIdentifier of(final QName qname) {
        return new DatastoreIdentifier(qname);
    }

    public static DatastoreIdentifier create(final QName qname) {
        final DatastoreIdentifier existing = CACHE.getIfPresent(qname);
        return existing != null ? existing : CACHE.getUnchecked(qname.intern());
    }

    public static DatastoreIdentifier readFrom(final DataInput in) throws IOException {
        return create(QName.readFrom(in));
    }

    @Override
    public void writeTo(final DataOutput out) throws IOException {
        getValue().writeTo(out);
    }

    @Serial
    private Object writeReplace() {
        return new DSIv1(getValue());
    }
}
