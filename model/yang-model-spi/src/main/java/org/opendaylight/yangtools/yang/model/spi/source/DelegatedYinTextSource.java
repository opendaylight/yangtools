/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.io.ByteSource;
import java.io.IOException;
import java.io.InputStream;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YinTextSource;

/**
 * A {@link YinTextSource} delegating to a {@link ByteSource}.
 */
@NonNullByDefault
public class DelegatedYinTextSource extends AbstractYinTextSource<ByteSource> {
    /**
     * Default constructor.
     *
     * @param sourceId {@link SourceIdentifier} of the resulting schema source
     * @param delegate backing {@link ByteSource} instance
     */
    public DelegatedYinTextSource(final SourceIdentifier sourceId, final ByteSource delegate) {
        super(sourceId, delegate);
    }

    @Override
    public final InputStream openStream() throws IOException {
        return getDelegate().openStream();
    }

    @Override
    public final @NonNull String symbolicName() {
        return "[" + getDelegate().toString() + "]";
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return super.addToStringAttributes(toStringHelper).add("delegate", getDelegate());
    }
}
