/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.Writer;
import java.util.function.Supplier;
import org.opendaylight.yangtools.plugin.generator.api.AbstractGeneratedTextFile;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFileLifecycle;

final class SupplierGeneratedFile extends AbstractGeneratedTextFile {
    private final Supplier<String> supplier;

    SupplierGeneratedFile(final GeneratedFileLifecycle lifecycle, final Supplier<String> supplier) {
        super(lifecycle);
        this.supplier = requireNonNull(supplier);
    }

    @Override
    protected void writeBody(final Writer output) throws IOException {
        output.write(supplier.get());
    }
}
