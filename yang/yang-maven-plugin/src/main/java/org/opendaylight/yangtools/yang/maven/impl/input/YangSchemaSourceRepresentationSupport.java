/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.maven.impl.input;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import java.io.IOException;
import java.nio.file.Path;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.maven.spi.input.SchemaSourceRepresentationSupport;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

/**
 * {@link SchemaSourceRepresentationSupport} for YANG text files.
 *
 * @author Robert Varga
 */
@MetaInfServices
final class YangSchemaSourceRepresentationSupport implements SchemaSourceRepresentationSupport {
    @Override
    public boolean acceptsPath(final Path path) {
        return path.getFileName().endsWith(YangConstants.RFC6020_YANG_FILE_EXTENSION);
    }

    @Override
    public SchemaSourceRepresentation acceptPath(final Path path) throws IOException, SchemaSourceException {
        Preconditions.checkArgument(acceptsPath(path));

        return YangTextSchemaSource.delegateForByteSource(YangTextSchemaSource.identifierFromFilename(
            path.getFileName().toString()), Files.asByteSource(path.toFile()));
    }
}
