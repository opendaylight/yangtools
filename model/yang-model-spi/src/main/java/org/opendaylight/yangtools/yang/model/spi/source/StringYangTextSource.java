/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import java.io.Reader;
import java.io.StringReader;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;

/**
 * A {@link YangTextSource} with content readily available.
 */
public class StringYangTextSource extends AbstractYangTextSource<String> {
    private final @Nullable String symbolicName;

    public StringYangTextSource(final SourceIdentifier sourceId, final String content) {
        this(sourceId, content, null);
    }

    public StringYangTextSource(final SourceIdentifier sourceId, final String content,
            final @Nullable String symbolicName) {
        super(sourceId, content);
        this.symbolicName = symbolicName;
    }

    @Override
    public final String symbolicName() {
        return symbolicName;
    }

    @Override
    public final Reader openStream() {
        return new StringReader(getDelegate());
    }
}
