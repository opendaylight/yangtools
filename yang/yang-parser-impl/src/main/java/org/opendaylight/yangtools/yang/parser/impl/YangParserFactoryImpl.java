/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;

@Beta
// FIXME: this is just an unfinished skeleton holder to host common parser configuration.
public class YangParserFactoryImpl {
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

    public BuildAction createParser() {
        return createParser(StatementParserMode.DEFAULT_MODE);
    }

    public BuildAction createParser(final StatementParserMode parserMode) {
        return reactor.newBuild(parserMode);
    }

    @Beta
    @VisibleForTesting
    public static BuildAction defaultParser() {
        return getInstance().createParser();
    }

    @Beta
    @VisibleForTesting
    public static BuildAction semVerParser() {
        return getInstance().createParser(StatementParserMode.SEMVER_MODE);
    }
}
