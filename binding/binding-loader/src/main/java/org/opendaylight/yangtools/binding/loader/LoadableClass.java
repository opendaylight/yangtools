/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.loader;

import java.io.IOException;
import java.nio.file.Path;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A source of a {@link Class} definition.
 */
@NonNullByDefault
public interface LoadableClass<T> {
    /**
     * {@return the fully-qualified class name}
     */
    String name();

    /**
     * {@return the class bytes}
     * @param classLoader the {@link BindingClassLoader}
     */
    Class<T> load(BindingClassLoader classLoader);

    /**
     * Save the class into a directory.
     *
     * @param directory the directory
     * @throws IOException if an I/O error occurs
     */
    void saveToDir(Path directory) throws IOException;
}
