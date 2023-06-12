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
import com.google.common.io.CharSource;
import java.io.IOException;
import java.io.Reader;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Delegator;

final class DelegatedYangTextSchemaSource extends YangTextSchemaSource implements Delegator<CharSource> {
    private final @NonNull CharSource delegate;

    DelegatedYangTextSchemaSource(final SourceIdentifier identifier, final CharSource delegate) {
        super(identifier);
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public CharSource getDelegate() {
        return delegate;
    }

    @Override
    public Reader openStream() throws IOException {
        return delegate.openStream();
    }

    @Override
    public Optional<String> getSymbolicName() {
        return Optional.of("[" + delegate.toString() + "]");
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return super.addToStringAttributes(toStringHelper).add("delegate", delegate);
    }
}
