/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.io.ByteSource;
import java.io.IOException;
import java.io.InputStream;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YinTextSource;

/**
 * A {@link YinTextSource} with a specific {@link SourceIdentifier} and backed by a {@link ByteSource}, which provides
 * the actual {@link #openStream()} implementation.
 */
@NonNullByDefault
public final class DelegatedYinTextSource extends YinTextSource implements Delegator<ByteSource> {
    private final ByteSource delegate;

    /**
     * Default constructor.
     *
     * @param sourceId source identifier
     * @param delegate Backing {@link ByteSource}
     */
    public DelegatedYinTextSource(final SourceIdentifier sourceId, final ByteSource delegate) {
        super(sourceId);
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public ByteSource getDelegate() {
        return delegate;
    }

    @Override
    public InputStream openStream() throws IOException {
        return delegate.openStream();
    }

    @Override
    public @NonNull String symbolicName() {
        return "[" + delegate.toString() + "]";
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return super.addToStringAttributes(toStringHelper).add("delegate", delegate);
    }
}
