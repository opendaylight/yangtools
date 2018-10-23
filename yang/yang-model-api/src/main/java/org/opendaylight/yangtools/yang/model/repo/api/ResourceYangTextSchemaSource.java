/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Delegator;

/**
 * A resource-backed {@link YinTextSchemaSource}.
 */
@Beta
// FIXME: YANGTOOLS-849: 3.0.0: hide this class
public final class ResourceYangTextSchemaSource extends YangTextSchemaSource implements Delegator<URL> {
    private final @NonNull URL url;

    ResourceYangTextSchemaSource(final @NonNull SourceIdentifier identifier, final URL url) {
        super(identifier);
        this.url = requireNonNull(url);
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
