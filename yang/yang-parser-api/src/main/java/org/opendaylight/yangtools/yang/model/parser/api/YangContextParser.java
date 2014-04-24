/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.parser.api;

import org.opendaylight.yangtools.yang.model.api.SchemaContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * Parse yang models and convert data to SchemaContext.
 *
 */
public interface YangContextParser extends YangModelParser {

    /**
     * Parse yangFile file and all yang files found in directory.
     *
     * @param yangFile
     *            file to parse
     * @param dependenciesDirectory
     *            directory which contains additional yang files
     * @return parsed data as SchemaContext
     */
    SchemaContext parse(final File yangFile, final File dependenciesDirectory) throws IOException;

    /**
     * Parse one or more Yang model files and return the definitions of Yang
     * modules defined in *.yang files; <br>
     * This method SHOULD be used if user need to parse multiple yang models
     * that are referenced either through import or include statements.
     *
     * @param yangFiles
     *            yang files to parse
     * @return parsed data as SchemaContext
     */
    SchemaContext parseFiles(final Collection<File> yangFiles);

    /**
     * Parse one or more Yang model files and return the definitions of Yang
     * modules defined in *.yang files. <br>
     * This method SHOULD be used if user has already parsed context and need to
     * parse additinal yang models which can have dependencies on models in this
     * context.
     *
     * @param yangFiles
     *            yang files to parse
     * @param context
     *            SchemaContext containing already parsed yang models
     * @return parsed data as SchemaContext
     */
    SchemaContext parseFiles(final Collection<File> yangFiles, final SchemaContext context) throws IOException;

    /**
     * Parse one or more Yang model streams and return the definitions of Yang
     * modules defined in *.yang files; <br>
     * This method SHOULD be used if user need to parse multiple yang models
     * that are referenced either through import or include statements.
     *
     * @param yangStreams
     *            yang streams to parse
     * @return parsed data as SchemaContext
     */
    SchemaContext parseStreams(final Collection<InputStream> yangStreams);

    /**
     * Parse one or more Yang model streams and return the definitions of Yang
     * modules defined in *.yang files. <br>
     * This method SHOULD be used if user has already parsed context and need to
     * parse additinal yang models which can have dependencies on models in this
     * context.
     *
     * @param yangStreams
     *            yang streams to parse
     * @param context
     *            SchemaContext containing already parsed yang models
     * @return parsed data as SchemaContext
     */
    SchemaContext parseStreams(final Collection<InputStream> yangStreams, final SchemaContext context);

}
