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
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;

/**
 * A {@link URL}-backed {@link YangTextSource}.
 */
public sealed class URLYangTextSource extends YangTextSource implements Delegator<URL> permits ResourceYangTextSource {
    private final @NonNull URL url;
    private final @NonNull Charset charset;

    /**
     * Default constructor.
     *
     * @param clazz Class reference
     * @param resourceName Resource name
     * @param charset Expected character set
     * @throws IllegalArgumentException if the resource does not exist or if the name has invalid format
     */
    public URLYangTextSource(final SourceIdentifier sourceId, final URL url, final Charset charset) {
        super(sourceId);
        this.url = requireNonNull(url);
        this.charset = requireNonNull(charset);
    }

    /**
     * Constructor using {@link StandardCharsets#UTF_8} character set.
     *
     * @param clazz Class reference
     * @param resourceName Resource name
     * @throws IllegalArgumentException if the resource does not exist or if the name has invalid format
     */
    public URLYangTextSource(final SourceIdentifier sourceId, final URL url) {
        this(sourceId, url, StandardCharsets.UTF_8);
    }

    @Override
    public final URL getDelegate() {
        return url;
    }

    @Override
    protected final ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return super.addToStringAttributes(toStringHelper).add("url", url);
    }

    @Override
    public final Reader openStream() throws IOException {
        return new InputStreamReader(url.openStream(), charset);
    }

    @Override
    public final String symbolicName() {
        return url.toString();
    }
}
