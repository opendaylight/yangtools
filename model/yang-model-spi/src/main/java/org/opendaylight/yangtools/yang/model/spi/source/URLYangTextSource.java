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
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;

/**
 * A {@link YangTextSource} backed by a {@link URL}.
 */
@NonNullByDefault
public class URLYangTextSource extends YangTextSource implements Delegator<URL> {
    private final URL url;
    private final Charset charset;

    public URLYangTextSource(final SourceIdentifier sourceId, final URL url, final Charset charset) {
        super(sourceId);
        this.url = requireNonNull(url);
        this.charset = requireNonNull(charset);
    }

    /**
     * Utility constructor. The {@link SourceIdentifier} is derived from {@link URL#getPath()} and the character set is
     * assumed to be UTF-8.
     *
     * @param url backing {@link URL}
     */
    public URLYangTextSource(final URL url) {
        this(SourceIdentifier.ofYangFileName(extractFileName(url.getPath())), url, StandardCharsets.UTF_8);
    }

    @Override
    public final Reader openStream() throws IOException {
        return new InputStreamReader(url.openStream(), charset);
    }

    @Override
    public final @NonNull String symbolicName() {
        return url.toString();
    }

    @Override
    public final URL getDelegate() {
        return url;
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return super.addToStringAttributes(toStringHelper).add("url", url);
    }

    private static String extractFileName(final String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
