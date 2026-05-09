/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import java.io.IOException;
import java.io.UncheckedIOException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.concepts.Mutable;

/**
 * A template for something that can be turned in to the contents of a file.
 */
// FIXME: extends Immutable once we have separated out GeneratedClass lifecycle
@NonNullByDefault
abstract sealed class Template permits JavaFileTemplate, ModuleSupportTemplate, ModelBindingProviderTemplate {
    /**
     * {@return the name of the type this generator is bound to}
     */
    abstract JavaTypeName typeName();

    /**
     * Generate the file into specified {@code Appendable}.
     *
     * @param out the Appendable
     * @throws IOException if an I/O error occurs
     */
    abstract void generateTo(Appendable out) throws IOException;

    /**
     * Generate the file into specified {@code StringBuilder}.
     *
     * @param sb the StringBuilder
     */
    final void generateTo(final StringBuilder sb) {
        try {
            generateTo((Appendable) sb);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * A stateful builder of a {@link Template}.
     */
    interface Builder extends Mutable {
        /**
         * {@return the resulting template}
         */
        Template build();
    }
}
