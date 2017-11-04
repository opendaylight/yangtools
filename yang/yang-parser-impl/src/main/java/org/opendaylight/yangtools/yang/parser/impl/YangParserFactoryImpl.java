/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.opendaylight.yangtools.yang.model.parser.api.YangParser;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.api.YinTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.api.YinXmlSchemaSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;

@Beta
// FIXME: this is just an unfinished skeleton holder to host common parser configuration.
public class YangParserFactoryImpl implements YangParserFactory {
    private static final YangParserFactoryImpl INSTANCE = new YangParserFactoryImpl();

    private final CrossSourceStatementReactor reactor;

    private YangParserFactoryImpl() {
        reactor = YangInferencePipeline.reactorBuilder()
                // FIXME: add default-configured extensions here
                .build();
    }

    public static YangParserFactoryImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public Collection<Class<? extends SchemaSourceRepresentation>> supportedModelRepresentations() {
        return ImmutableList.of(YangTextSchemaSource.class, YinTextSchemaSource.class, YinXmlSchemaSource.class);
    }

    @Override
    public YangParser createParser() {
        return createParser(StatementParserMode.DEFAULT_MODE);
    }

    @Override
    public YangParser createParser(final StatementParserMode parserMode) {
        return new YangParserImpl(reactor.newBuild(parserMode));
    }

}
