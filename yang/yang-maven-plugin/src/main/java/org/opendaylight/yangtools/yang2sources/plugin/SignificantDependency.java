/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteSource;
import java.io.File;
import java.util.Collection;

abstract class SignificantDependency implements AutoCloseable {
    private final File file;

    SignificantDependency(final File file) {
        this.file = Preconditions.checkNotNull(file);
    }

    final File file() {
        return file;
    }

    abstract Collection<ByteSource> asSources();

    @Override
    public abstract void close();
}
