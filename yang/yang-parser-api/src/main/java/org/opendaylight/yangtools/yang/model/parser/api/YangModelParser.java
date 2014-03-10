/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.parser.api;

import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.type.UnknownTypeDefinition;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Yang Model Parser interface is designed for parsing yang models and convert
 * the information to Data Schema Tree.
 * 
 */
// refactor methods returning input streams, after introducing
public interface YangModelParser {

    /**
     * Parse yangFile file and all yang files found in directory.
     *
     * @param yangFile
     *            file to parse
     * @param directory
     *            directory which contains additional yang files
     * @return Set of Yang Modules
     */
    Set<Module> parseYangModels(final File yangFile, final File directory);

    /**
     * Parse one or more Yang model files and return the definitions of Yang
     * modules defined in *.yang files; <br>
     * This method SHOULD be used if user need to parse multiple yang models
     * that are referenced either through import or include statements.
     * 
     * @param yangFiles
     *            yang files to parse
     * @return Set of Yang Modules
     */
    Set<Module> parseYangModels(final List<File> yangFiles);

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
     * @return Set of Yang Modules
     */
    Set<Module> parseYangModels(final List<File> yangFiles, final SchemaContext context);

    /**
     * Equivalent to {@link #parseYangModels(List)} that returns parsed modules
     * mapped to Files from which they were parsed.
     * 
     * @param yangFiles
     *            yang files to parse
     * @return Map of Yang Modules
     */
    Map<File, Module> parseYangModelsMapped(final List<File> yangFiles);

    /**
     * Parse one or more Yang model streams and return the definitions of Yang
     * modules defined in *.yang files; <br>
     * This method SHOULD be used if user need to parse multiple yang models
     * that are referenced either through import or include statements.
     * 
     * @param yangModelStreams
     *            yang streams to parse
     * @return Set of Yang Modules
     */
    Set<Module> parseYangModelsFromStreams(final List<InputStream> yangModelStreams);

    /**
     * Parse one or more Yang model streams and return the definitions of Yang
     * modules defined in *.yang files. <br>
     * This method SHOULD be used if user has already parsed context and need to
     * parse additinal yang models which can have dependencies on models in this
     * context.
     * 
     * @param yangModelStreams
     *            yang streams to parse
     * @param context
     *            SchemaContext containing already parsed yang models
     * @return Set of Yang Modules
     */
    Set<Module> parseYangModelsFromStreams(final List<InputStream> yangModelStreams, final SchemaContext context);

    /**
     * Equivalent to {@link #parseYangModels(List)} that returns parsed modules
     * mapped to IputStreams from which they were parsed.
     * 
     * @param yangModelStreams
     *            yang streams to parse
     * @return Map of Yang Modules
     */
    //TODO: when working with input streams do not swallow IOException, it should be propagated without having to wrap it in a runtime exception
    //FIXME: it is not defined in which state are the returning streams.
    Map<InputStream, Module> parseYangModelsFromStreamsMapped(final List<InputStream> yangModelStreams);

    /**
     * Creates {@link SchemaContext} from specified Modules. The modules SHOULD
     * not contain any unresolved Schema Nodes or Type Definitions. By
     * unresolved Schema Nodes or Type Definitions we mean that the Module
     * should not contain ANY Schema Nodes that contains
     * {@link UnknownTypeDefinition} and all dependencies although via import or
     * include definitions are resolved.
     * 
     * @param modules
     *            Set of Yang Modules
     * @return Schema Context instance constructed from whole Set of Modules.
     */
    SchemaContext resolveSchemaContext(final Set<Module> modules);
}
