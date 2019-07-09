/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rcf8528.data.util;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.rfc8528.data.api.InlineMountPointNode;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointIdentifier;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointNode;
import org.opendaylight.yangtools.rfc8528.data.api.SharedSchemaMountpointNode;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.util.AbstractIdentifiableSchemaContextProvider;

@Beta
public abstract class ImmutableMountPointNode<M extends MountPointNode, T extends ImmutableMountPointNode<M, T>>
        extends AbstractIdentifiableSchemaContextProvider<MountPointIdentifier> implements MountPointNode, Immutable {
    @NonNullByDefault
    private static final class Inline extends ImmutableMountPointNode<InlineMountPointNode, Inline>
            implements InlineMountPointNode {

        Inline(final MountPointIdentifier identifier, final SchemaContext schemaContext, final ContainerNode delegate) {
            super(identifier, schemaContext, delegate);
        }
    }

    @NonNullByDefault
    private static final class SharedSchema extends ImmutableMountPointNode<SharedSchemaMountpointNode, SharedSchema>
            implements SharedSchemaMountpointNode {

        SharedSchema(final MountPointIdentifier identifier, final SchemaContext schemaContext,
                final ContainerNode delegate) {
            super(identifier, schemaContext, delegate);
        }
    }

    private final @NonNull ContainerNode delegate;

    ImmutableMountPointNode(final @NonNull MountPointIdentifier identifier,
            final @NonNull SchemaContext schemaContext, final @NonNull ContainerNode delegate) {
        super(schemaContext, identifier);
        this.delegate = requireNonNull(delegate);
    }

    public static @NonNull InlineMountPointNode inlineOf(final @NonNull MountPointIdentifier identifier,
            final @NonNull SchemaContext schemaContext, final @NonNull ContainerNode delegate) {
        return new Inline(identifier, schemaContext, delegate);
    }

    public static @NonNull SharedSchemaMountpointNode sharedSchemaOf(final @NonNull MountPointIdentifier identifier,
            final @NonNull SchemaContext schemaContext, final @NonNull ContainerNode delegate) {
        return new SharedSchema(identifier, schemaContext, delegate);
    }

    @Override
    public final Collection<DataContainerChild<? extends PathArgument, ?>> getValue() {
        return delegate.getValue();
    }

    @Override
    public final Optional<DataContainerChild<? extends PathArgument, ?>> getChild(final PathArgument child) {
        return delegate.getChild(child);
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return super.addToStringAttributes(helper).add("delegate", delegate);
    }
}
