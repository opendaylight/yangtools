/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.io.CharSource;
import java.io.IOException;
import java.io.Reader;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;

/**
 * A {@link YangTextSource} delegating to a {@link CharSource}.
 */
@NonNullByDefault
public final class DelegatedYangTextSource extends YangTextSource implements Delegator<CharSource> {
    private final CharSource delegate;

    /**
     * Default constructor.
     *
     * @param sourceId {@link SourceIdentifier} of the resulting schema source
     * @param delegate Backing {@link CharSource} instance
     */
    public DelegatedYangTextSource(final SourceIdentifier sourceId, final CharSource delegate) {
        super(sourceId);
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public CharSource getDelegate() {
        return delegate;
    }

    @Override
    public Reader openStream() throws IOException {
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
