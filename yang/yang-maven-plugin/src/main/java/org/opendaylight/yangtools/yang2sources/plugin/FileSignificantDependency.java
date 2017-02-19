/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import org.opendaylight.yangtools.yang.parser.util.NamedFileInputStream;

final class FileSignificantDependency extends SignificantDependency {
    FileSignificantDependency(final File file) {
        super(file);
    }

    @Override
    public void close() {
        // No-op
    }

    @Override
    Collection<ByteSource> asSources() {
        return ImmutableList.of(new ByteSource() {
            @Override
            public InputStream openStream() throws IOException {
                return new NamedFileInputStream(file(), YangToSourcesProcessor.META_INF_YANG_STRING + File.separator
                    + file().getName());
            }
        });
    }
}
