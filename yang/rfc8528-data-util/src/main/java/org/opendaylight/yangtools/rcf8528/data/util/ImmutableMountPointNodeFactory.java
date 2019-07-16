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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.AbstractIdentifiable;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointIdentifier;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointNode;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointNodeFactory;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointMetadata;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

@Beta
@NonNullByDefault
public abstract class ImmutableMountPointNodeFactory extends AbstractIdentifiable<MountPointIdentifier>
        implements MountPointNodeFactory {
    private static final class Inline extends ImmutableMountPointNodeFactory {
        Inline(final MountPointIdentifier identifier, final MountPointMetadata delegate) {
            super(identifier, delegate);
        }

        @Override
        public MountPointNode createMountPoint(final ContainerNode delegate) {
            return ImmutableMountPointNode.inlineOf(getIdentifier(), getSchemaContext(), delegate);
        }
    }

    private static final class SharedSchema extends ImmutableMountPointNodeFactory {
        SharedSchema(final MountPointIdentifier identifier, final MountPointMetadata delegate) {
            super(identifier, delegate);
        }

        @Override
        public MountPointNode createMountPoint(final ContainerNode delegate) {
            return ImmutableMountPointNode.sharedSchemaOf(getIdentifier(), getSchemaContext(), delegate);
        }
    }

    private final MountPointMetadata delegate;

    ImmutableMountPointNodeFactory(final MountPointIdentifier identifier, final MountPointMetadata delegate) {
        super(identifier);
        this.delegate = requireNonNull(delegate);
    }

    public static ImmutableMountPointNodeFactory inlineFor(final MountPointIdentifier identifier,
            final MountPointMetadata delegate) {
        return new Inline(identifier, delegate);
    }


    public static ImmutableMountPointNodeFactory sharedSchemaFor(final MountPointIdentifier identifier,
            final MountPointMetadata delegate) {
        return new SharedSchema(identifier, delegate);
    }

    @Override
    public final SchemaContext getSchemaContext() {
        return delegate.getSchemaContext();
    }
}
