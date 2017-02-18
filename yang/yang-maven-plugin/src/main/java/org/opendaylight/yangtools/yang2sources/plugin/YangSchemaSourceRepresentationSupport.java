/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import com.google.common.io.Files;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang2sources.spi.input.SchemaSourceRepresentationSupport;

/**
 * {@link SchemaSourceRepresentationSupport} for YANG text files.
 *
 * @author Robert Varga
 */
@MetaInfServices
final class YangSchemaSourceRepresentationSupport implements SchemaSourceRepresentationSupport {
    @Override
    public Optional<SchemaSourceRepresentation> acceptPath(final Path path) throws IOException, SchemaSourceException {
        final String fileName = path.getFileName().toString();
        if (!fileName.endsWith(YangConstants.RFC6020_YANG_FILE_EXTENSION)) {
            return Optional.empty();
        }

        return Optional.of(YangTextSchemaSource.delegateForByteSource(
            YangTextSchemaSource.identifierFromFilename(fileName), Files.asByteSource(path.toFile())));
    }
}
