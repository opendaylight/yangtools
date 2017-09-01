/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import org.opendaylight.yangtools.concepts.Delegator;

/**
 * A resource-backed {@link YangTextSchemaSource}.
 */
final class ResourceYinTextSchemaSource extends YinTextSchemaSource implements Delegator<URL> {
    private final URL url;

    ResourceYinTextSchemaSource(final SourceIdentifier identifier, final URL url) {
        super(identifier);
        this.url = Preconditions.checkNotNull(url);
    }

    @Override
    public URL getDelegate() {
        return url;
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("url", url);
    }

    @Override
    public InputStream openStream() throws IOException {
        return url.openStream();
    }

    @Override
    public Optional<String> getSymbolicName() {
        return Optional.of(url.toString());
    }
}
