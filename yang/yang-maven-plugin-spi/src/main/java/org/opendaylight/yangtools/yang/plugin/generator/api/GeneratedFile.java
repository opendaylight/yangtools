/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.plugin.generator.api;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.OutputStream;
import org.eclipse.jdt.annotation.NonNull;

/**
 * The contents of a generated file. It includes the specification of the file's lifecycle.
 *
 * @author Robert Varga
 */
public abstract class GeneratedFile {
    private final GeneratedFileLifecycle lifecycle;

    protected GeneratedFile() {
        this(GeneratedFileLifecycle.TRANSIENT);
    }

    protected GeneratedFile(final GeneratedFileLifecycle lifecycle) {
        this.lifecycle = requireNonNull(lifecycle);
    }

    public final @NonNull GeneratedFileLifecycle getLifecycle() {
        return lifecycle;
    }

    public abstract void writeBody(OutputStream output) throws IOException;
}
