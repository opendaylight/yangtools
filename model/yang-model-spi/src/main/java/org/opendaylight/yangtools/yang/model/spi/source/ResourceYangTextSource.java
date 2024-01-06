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
import com.google.common.io.Resources;
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
 * A resource-backed {@link YangTextSource}.
 */
final class ResourceYangTextSource extends YangTextSource implements Delegator<URL> {
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
    public ResourceYangTextSource(final Class<?> clazz, final String resourceName, final Charset charset) {
        super(SourceIdentifier.ofYangFileName(resourceName.substring(resourceName.lastIndexOf('/') + 1)));
        url = Resources.getResource(clazz, resourceName);
        this.charset = requireNonNull(charset);
    }

    /**
     * Constructor using {@link StandardCharsets#UTF_8} character set.
     *
     * @param clazz Class reference
     * @param resourceName Resource name
     * @throws IllegalArgumentException if the resource does not exist or if the name has invalid format
     */
    public ResourceYangTextSource(final Class<?> clazz, final String resourceName) {
        this(clazz, resourceName, StandardCharsets.UTF_8);
    }

    @Override
    public URL getDelegate() {
        return url;
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return super.addToStringAttributes(toStringHelper).add("url", url);
    }

    @Override
    public Reader openStream() throws IOException {
        return new InputStreamReader(url.openStream(), charset);
    }

    @Override
    public String symbolicName() {
        return url.toString();
    }
}
