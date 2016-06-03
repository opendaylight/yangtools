/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.validation.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collection;

import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;

public class TestUtils {

    private TestUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static SchemaContext parseYangSources(StatementStreamSource... sources) throws SourceException,
            ReactorException {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(sources);

        return reactor.buildEffective();
    }

    public static SchemaContext parseYangSources(File... files) throws SourceException, ReactorException,
            FileNotFoundException {

        StatementStreamSource[] sources = new StatementStreamSource[files.length];

        for (int i = 0; i < files.length; i++) {
            sources[i] = new YangStatementSourceImpl(new FileInputStream(files[i]));
        }

        return parseYangSources(sources);
    }

    public static SchemaContext parseYangSources(Collection<File> files) throws SourceException, ReactorException,
            FileNotFoundException {
        return parseYangSources(files.toArray(new File[files.size()]));
    }
}
