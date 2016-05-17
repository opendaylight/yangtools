/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.sal.java.api.generator.test;

import java.util.List;

import java.io.InputStream;
import java.util.Collection;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import java.io.File;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;

public class RetestUtils {

    private RetestUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static SchemaContext parseYangSources(StatementStreamSource... sources)
            throws SourceException, ReactorException {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR
                .newBuild();
        reactor.addSources(sources);

        return reactor.buildEffective();
    }

    public static SchemaContext parseYangSources(File... files) throws SourceException, ReactorException, FileNotFoundException {

        StatementStreamSource[] sources = new StatementStreamSource[files.length];

        for(int i = 0; i<files.length; i++) {
            sources[i] = new YangStatementSourceImpl(new FileInputStream(files[i]));
        }

        return parseYangSources(sources);
    }

    public static SchemaContext parseYangSources(Collection<File> files) throws SourceException, ReactorException, FileNotFoundException {
        return parseYangSources(files.toArray(new File[files.size()]));
    }


    public static SchemaContext parseYangStreams(List<InputStream> streams)
            throws SourceException, ReactorException {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR
                .newBuild();
        return reactor.buildEffective(streams);
    }
}
