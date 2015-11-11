/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import com.google.common.io.ByteSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangContextParser;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;

final class YangContextParserImpl implements YangContextParser {

    @Override
    public Set<Module> parseYangModels(final File yangFile, final File directory) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Module> parseYangModels(final List<File> yangFiles) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Module> parseYangModels(final List<File> yangFiles, final SchemaContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<File, Module> parseYangModelsMapped(final Collection<File> yangFiles) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Module> parseYangModelsFromStreams(final List<InputStream> yangModelStreams) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Module> parseYangModelsFromStreams(final List<InputStream> yangModelStreams, final SchemaContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<InputStream, Module> parseYangModelsFromStreamsMapped(final Collection<InputStream> yangModelStreams) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SchemaContext resolveSchemaContext(final Set<Module> modules) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SchemaContext parseFile(final File yangFile, final File dependenciesDirectory)
            throws IOException, YangSyntaxErrorException {
    }

    @Override
    public SchemaContext parseFiles(final Collection<File> yangFiles) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SchemaContext parseFiles(final Collection<File> yangFiles, final SchemaContext context)
            throws IOException, YangSyntaxErrorException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SchemaContext parseSources(final Collection<ByteSource> sources) throws IOException, YangSyntaxErrorException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();


        // TODO Auto-generated method stub
        try {
            return reactor.buildEffective(sources);
        } catch (SourceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ReactorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public SchemaContext parseSources(final Collection<ByteSource> sources, final SchemaContext context)
            throws IOException, YangSyntaxErrorException {
        // TODO Auto-generated method stub
        return null;
    }
}
