/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Preconditions;
import com.google.common.io.Resources;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

/**
 * A resource-backed {@link YangTextSchemaSource}.
 */
@Beta
final class ResourceYangTextSchemaSource extends YangTextSchemaSource implements Delegator<URL> {
    private final URL url;

    ResourceYangTextSchemaSource(final SourceIdentifier identifier, final URL url) {
        super(identifier);
        this.url = Preconditions.checkNotNull(url);
    }

    /**
     * Create a new {@link YangTextSchemaSource} backed by a resource.
     *
     * @param resourceName Resource name
     * @return A new instance.
     * @throws IllegalArgumentException if the resource does not exist or if the name has invalid format
     */
    public static ResourceYangTextSchemaSource create(final String resourceName) {
        final SourceIdentifier identifier = identifierFromFilename(resourceName);
        final URL url = Resources.getResource(resourceName);
        return new ResourceYangTextSchemaSource(identifier, url);
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

}
