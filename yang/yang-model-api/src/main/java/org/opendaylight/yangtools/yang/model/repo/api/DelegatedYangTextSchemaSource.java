/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.io.ByteSource;
import java.io.IOException;
import java.io.InputStream;
import org.opendaylight.yangtools.concepts.Delegator;

final class DelegatedYangTextSchemaSource extends YangTextSchemaSource implements Delegator<ByteSource> {
    private final ByteSource delegate;

    DelegatedYangTextSchemaSource(final SourceIdentifier identifier, final ByteSource delegate) {
        super(identifier);
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public ByteSource getDelegate() {
        return delegate;
    }

    @Override
    public InputStream openStream() throws IOException {
        return delegate.openStream();
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("delegate", delegate);
    }
}