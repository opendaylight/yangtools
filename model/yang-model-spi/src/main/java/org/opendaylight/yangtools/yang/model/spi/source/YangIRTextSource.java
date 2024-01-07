/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import java.io.IOException;
import java.io.Reader;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.util.StringIteratorReader;
import org.opendaylight.yangtools.yang.ir.IRStatement;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;

/**
 * A {@link YangTextSource} backed by an {@link IRStatement}.
 */
@NonNullByDefault
public final class YangIRTextSource extends AbstractYangTextSource<IRStatement> {
    private final @Nullable String symbolicName;

    public YangIRTextSource(final SourceIdentifier sourceId, final IRStatement statement,
            final @Nullable String symbolicName) {
        super(sourceId, statement);
        this.symbolicName = symbolicName;
    }

    @Override
    public Reader openStream() throws IOException {
        return new StringIteratorReader(new IRStatementIterator(getDelegate()));
    }

    @Override
    public @Nullable String symbolicName() {
        return symbolicName;
    }
}
