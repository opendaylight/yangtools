/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.parser.api;

import com.google.common.io.ByteSource;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Parse YANG models and convert data to a {@link SchemaContext}. Implementations of this interface are not required
 * to be thread-safe.
 */
public interface YangContextParser extends YangModelParser {

    /**
     * Parse yangFile file and all yang files found in directory.
     *
     * @param yangFile
     *            file to parse
     * @param dependenciesDirectory
     *            directory which contains additional yang files
     * @return parsed data as SchemaContext. Resulting context will contains
     *         only module parsed from yangFile and modules which yangFile needs
     *         as dependencies.
     */
    SchemaContext parseFile(final File yangFile, final File dependenciesDirectory) throws IOException, YangSyntaxErrorException;

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
    SchemaContext parseFiles(final Collection<File> yangFiles) throws IOException;

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
    SchemaContext parseFiles(final Collection<File> yangFiles, final SchemaContext context) throws IOException, YangSyntaxErrorException;

    /**
     * Parse one or more Yang model streams and return the definitions of Yang
     * modules defined in *.yang files; <br>
     * This method SHOULD be used if user need to parse multiple yang models
     * that are referenced either through import or include statements.
     *
     * @param sources
     *            yang streams to parse
     * @return parsed data as SchemaContext
     */
    SchemaContext parseSources(final Collection<ByteSource> sources) throws IOException, YangSyntaxErrorException;

    /**
     * Parse one or more Yang model streams and return the definitions of Yang
     * modules defined in *.yang files. <br>
     * This method SHOULD be used if user has already parsed context and need to
     * parse additinal yang models which can have dependencies on models in this
     * context.
     *
     * @param sources
     *            yang streams to parse
     * @param context
     *            SchemaContext containing already parsed yang models
     * @return parsed data as SchemaContext
     */
    SchemaContext parseSources(final Collection<ByteSource> sources, final SchemaContext context) throws IOException, YangSyntaxErrorException;

}
