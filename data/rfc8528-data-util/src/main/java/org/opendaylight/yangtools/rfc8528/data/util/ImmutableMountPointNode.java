/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.data.util;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.AbstractIdentifiable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.PrettyTree;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointIdentifier;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointNode;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.MountPointContext;
import org.opendaylight.yangtools.yang.data.spi.node.NormalizedNodePrettyTree;

@Beta
public final class ImmutableMountPointNode extends AbstractIdentifiable<PathArgument, MountPointIdentifier>
        implements MountPointNode, Immutable {
    private final @NonNull MountPointContext mountCtx;
    private final @NonNull ContainerNode delegate;

    ImmutableMountPointNode(final @NonNull MountPointIdentifier identifier,
            final @NonNull MountPointContext mountCtx, final @NonNull ContainerNode delegate) {
        super(identifier);
        this.mountCtx = requireNonNull(mountCtx);
        this.delegate = requireNonNull(delegate);
    }

    public static @NonNull ImmutableMountPointNode of(final @NonNull MountPointIdentifier identifier,
            final @NonNull MountPointContext mountCtx, final @NonNull ContainerNode delegate) {
        return new ImmutableMountPointNode(identifier, mountCtx, delegate);
    }

    @Override
    public MountPointContext getMountPointContext() {
        return mountCtx;
    }

    @Override
    public Collection<DataContainerChild> body() {
        return delegate.body();
    }

    @Override
    public DataContainerChild childByArg(final PathArgument child) {
        return delegate.childByArg(child);
    }

    @Override
    public PrettyTree prettyTree() {
        return new NormalizedNodePrettyTree(this);
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return super.addToStringAttributes(helper).add("delegate", delegate);
    }
}
