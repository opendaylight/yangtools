/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;

/**
 * A {@link AbstractYinTextSource}.backed by a {@link URL}.
 */
@NonNullByDefault
public class URLYinTextSource extends AbstractYinTextSource implements Delegator<URL> {
    private final URL url;

    public URLYinTextSource(final SourceIdentifier sourceId, final URL url) {
        super(sourceId);
        this.url = requireNonNull(url);
    }

    public URLYinTextSource(final URL url) {
        this(SourceIdentifier.ofYinFileName(extractFileName(url.getPath())), url);
    }

    @Override
    public final URL getDelegate() {
        return url;
    }

    @Override
    public final InputStream openStream() throws IOException {
        return url.openStream();
    }

    @Override
    public final @NonNull String symbolicName() {
        return url.toString();
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return super.addToStringAttributes(toStringHelper).add("url", url);
    }

    private static String extractFileName(final String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
