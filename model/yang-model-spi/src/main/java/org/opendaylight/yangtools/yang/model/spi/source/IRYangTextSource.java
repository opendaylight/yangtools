/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;

final class IRYangTextSource extends YangTextSource {
    private final YangIRSource delegate;

    IRYangTextSource(final YangIRSource delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public Reader openStream() throws IOException {
        return new StringReader(delegate.statement().toYangFragment(new StringBuilder()).toString());
    }

    @Override
    public SourceIdentifier sourceId() {
        return delegate.sourceId();
    }

    @Override
    public String symbolicName() {
        return delegate.symbolicName();
    }
}
