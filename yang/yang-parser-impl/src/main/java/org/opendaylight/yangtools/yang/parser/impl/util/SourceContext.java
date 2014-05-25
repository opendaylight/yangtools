/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.YangContext;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.util.repo.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteSource;

final class SourceContext extends AbstractObjectRegistration<URL> //
        implements Identifiable<SourceIdentifier> {

    private final YangModelDependencyInfo dependencyInfo;
    private final URLSchemaContextResolver resolver;
    private final SourceIdentifier identifier;
    private final YangContext validated;

    private SourceContext(final URLSchemaContextResolver resolver, final URL instance, final SourceIdentifier identifier,
            final YangModelDependencyInfo modelInfo, final YangContext validated) {
        super(instance);
        this.resolver = Preconditions.checkNotNull(resolver);
        this.identifier = identifier;
        this.dependencyInfo = modelInfo;
        this.validated = validated;
    }

    public YangContext getValidatedYangModule() {
        return validated;
    }

    @Override
    public SourceIdentifier getIdentifier() {
        return identifier;
    }

    public YangModelDependencyInfo getDependencyInfo() {
        return dependencyInfo;
    }

    @Override
    protected void removeRegistration() {
        resolver.removeSource(this);
    }

    public static SourceContext create(final URLSchemaContextResolver resolver, final URL source,
            final SourceIdentifier identifier, final YangModelDependencyInfo modelInfo) throws IOException, YangSyntaxErrorException {
        final YangContext validated = YangParserImpl.parseYangSource(new ByteSource() {
                @Override
                public InputStream openStream() throws IOException {
                    return source.openStream();
                }
            });

        return new SourceContext(resolver, source, identifier, modelInfo, validated);
    }
}
