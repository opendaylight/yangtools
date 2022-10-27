/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.data.api;

import com.google.common.annotations.Beta;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.WritableObject;
import org.opendaylight.yangtools.util.AbstractIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

@Beta
@NonNullByDefault
public final class MountPointIdentifier extends AbstractIdentifier<QName> implements PathArgument, WritableObject {
    private static final long serialVersionUID = 1L;

    private static final LoadingCache<QName, MountPointIdentifier> CACHE = CacheBuilder.newBuilder().weakValues()
            .build(new CacheLoader<QName, MountPointIdentifier>() {
                @Override
                public MountPointIdentifier load(final QName key) {
                    return of(key);
                }
            });

    private MountPointIdentifier(final QName qname) {
        super(qname);
    }

    public static MountPointIdentifier of(final QName qname) {
        return new MountPointIdentifier(qname);
    }

    public static MountPointIdentifier create(final QName qname) {
        final MountPointIdentifier existing = CACHE.getIfPresent(qname);
        return existing != null ? existing : CACHE.getUnchecked(qname.intern());
    }

    public static MountPointIdentifier readFrom(final DataInput in) throws IOException {
        return create(QName.readFrom(in));
    }

    public QName getLabel() {
        return getValue();
    }

    @Override
    public void writeTo(final DataOutput out) throws IOException {
        getValue().writeTo(out);
    }

    private Object writeReplace() {
        return new MPIv1(getValue());
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public int compareTo(final @Nullable PathArgument o) {
        return o instanceof MountPointIdentifier other ? getValue().compareTo(other.getLabel())
            // TODO: Yeah, okay, this declaration is not quite right, but we are following the lead from others
            : -1;
    }

    @Override
    public QName getNodeType() {
        return getLabel();
    }

    @Override
    public String toRelativeString(final @Nullable PathArgument previous) {
        return toString();
    }
}
