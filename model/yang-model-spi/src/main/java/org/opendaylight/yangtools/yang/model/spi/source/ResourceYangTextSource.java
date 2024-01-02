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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;

/**
 * A resource-backed {@link YangTextSource}.
 */
final class ResourceYangTextSource extends YangTextSource implements Delegator<URL> {
    private final @NonNull URL url;
    private final @NonNull Charset charset;

    ResourceYangTextSource(final SourceIdentifier sourceId, final URL url, final Charset charset) {
        super(sourceId);
        this.url = requireNonNull(url);
        this.charset = requireNonNull(charset);
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
