/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Identifier;
import org.opendaylight.yangtools.concepts.WritableObject;
import org.opendaylight.yangtools.yang.common.QName;

@Beta
@NonNullByDefault
public record MountPointLabel(QName qname) implements Identifier, WritableObject {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private static final LoadingCache<QName, MountPointLabel> CACHE = CacheBuilder.newBuilder().weakValues()
            .build(new CacheLoader<QName, MountPointLabel>() {
                @Override
                public MountPointLabel load(final QName key) {
                    return new MountPointLabel(key);
                }
            });

    public MountPointLabel {
        requireNonNull(qname);
    }

    public static MountPointLabel create(final QName qname) {
        final var existing = CACHE.getIfPresent(qname);
        return existing != null ? existing : CACHE.getUnchecked(qname.intern());
    }

    public static MountPointLabel readFrom(final DataInput in) throws IOException {
        return create(QName.readFrom(in));
    }

    @Override
    public void writeTo(final DataOutput out) throws IOException {
        qname.writeTo(out);
    }
}
