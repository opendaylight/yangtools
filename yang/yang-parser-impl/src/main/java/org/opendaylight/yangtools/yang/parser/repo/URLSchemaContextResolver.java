/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import java.io.IOException;
import java.net.URL;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;

/**
 * @deprecated Use {@link YangTextSchemaContextResolver} instead.
 */
@Deprecated
@Beta
public class URLSchemaContextResolver implements AutoCloseable, SchemaSourceProvider<YangTextSchemaSource> {
    private final YangTextSchemaContextResolver resolver;

    private URLSchemaContextResolver(final String name) {
        resolver = YangTextSchemaContextResolver.create(name);
    }

    public static URLSchemaContextResolver create(final String name) {
        return new URLSchemaContextResolver(name);
    }

    /**
     * Register a URL hosting a YANG Text file.
     *
     * @param url URL
     * @throws YangSyntaxErrorException When the YANG file is syntactically invalid
     * @throws IOException when the URL is not readable
     * @throws SchemaSourceException When parsing encounters general error
     * @return new instance of AbstractURLRegistration if the URL is not null
     */
    public URLRegistration registerSource(final URL url) throws SchemaSourceException, IOException, YangSyntaxErrorException {
        final YangTextSchemaSourceRegistration reg = resolver.registerSource(url);
        return new AbstractURLRegistration(reg.getInstance()) {
            @Override
            protected void removeRegistration() {
                reg.close();
            }
        };
    }

    /**
     * Try to parse all currently available yang files and build new schema context.
     * @return new schema context iif there is at least 1 yang file registered and
     *         new schema context was successfully built.
     */
    public Optional<SchemaContext> getSchemaContext() {
        return resolver.getSchemaContext();
    }

    @Override
    public CheckedFuture<YangTextSchemaSource, SchemaSourceException> getSource(final SourceIdentifier sourceIdentifier) {
        return resolver.getSource(sourceIdentifier);
    }

    @Override
    public void close() {
        resolver.close();
    }
}
