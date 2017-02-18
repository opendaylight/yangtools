/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.spi.input;

import com.google.common.annotations.Beta;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
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
     * Examine a file identified by {@link Path}. If it is supported, return a {@link SchemaSourceProvider} which
     * will be used to access and process it.
     *
     * @param path Path to examine
     * @return An optional SchemaSourceProvider backed by the path.
     * @throws IOException If an I/O error occurs
     * @throws SchemaSourceException If the file contents are invalid
     */
    Optional<SchemaSourceRepresentation> acceptPath(Path path) throws IOException, SchemaSourceException;
}
