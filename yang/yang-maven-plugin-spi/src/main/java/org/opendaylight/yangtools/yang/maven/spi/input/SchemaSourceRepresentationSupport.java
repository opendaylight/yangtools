/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.maven.spi.input;

import com.google.common.annotations.Beta;
import java.io.IOException;
import java.nio.file.Path;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;

/**
 * Base interface which needs to be exposed as a {@link java.util.ServiceLoader} service. Plugin core will scan all
 * eligible model files and offer them to all registered implementations
 *
 * @author Robert Varga
 */
@Beta
public interface SchemaSourceRepresentationSupport {
    /**
     * Examine a path to determine if it is supported by this object. Implementations should not be examining
     * the contents of the path.
     *
     * @param path Path to examine
     * @return If the path is supported, false otherwise.
     */
    boolean acceptsPath(Path path);

    /**
     * Examine a file identified by {@link Path} and return a {@link SchemaSourceProvider} which will be used to access
     * and process it.
     *
     * @param path Path to examine
     * @return SchemaSourceProvider backed by the path.
     * @throws IllegalArgumentException if {@link #acceptsPath(Path)} would return false
     * @throws IOException If an I/O error occurs
     * @throws SchemaSourceException If the file contents are invalid
     */
    SchemaSourceRepresentation acceptPath(Path path) throws IOException, SchemaSourceException;
}
