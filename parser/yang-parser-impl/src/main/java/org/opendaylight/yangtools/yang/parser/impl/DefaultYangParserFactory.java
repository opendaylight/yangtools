/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static com.google.common.base.Verify.verifyNotNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.opendaylight.yangtools.yang.parser.api.ImportResolutionMode;
import org.opendaylight.yangtools.yang.parser.api.YangParser;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.parser.inject.InjectYangParserFactory;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParserFactory;

/**
 * Reference {@link YangParserFactory} implementation.
 */
@Deprecated(since = "14.0.21", forRemoval = true)
public sealed class DefaultYangParserFactory implements YangParserFactory permits InjectYangParserFactory {
    private static final List<ImportResolutionMode> SUPPORTED_MODES = List.of(ImportResolutionMode.DEFAULT);

    private final ConcurrentHashMap<YangParserConfiguration, CrossSourceStatementReactor> reactors =
        new ConcurrentHashMap<>(2);
    private final Function<YangParserConfiguration, CrossSourceStatementReactor> reactorFactory;

    /**
     * Construct a new {@link YangParserFactory} backed by {@link DefaultReactors#defaultReactor()}.
     */
    public DefaultYangParserFactory() {
        reactorFactory = config -> DefaultReactors.defaultReactorBuilder(config).build();
        // Make sure default reactor is available
        verifyNotNull(reactorFactory.apply(YangParserConfiguration.DEFAULT));
    }

    public DefaultYangParserFactory(final YangXPathParserFactory xpathFactory) {
        reactorFactory = config -> DefaultReactors.defaultReactorBuilder(xpathFactory, config).build();
    }

    @Override
    public Collection<ImportResolutionMode> supportedImportResolutionModes() {
        return SUPPORTED_MODES;
    }

    @Override
    public YangParser createParser(final YangParserConfiguration configuration) {
        final var importMode = configuration.importResolutionMode();
        if (!SUPPORTED_MODES.contains(importMode)) {
            throw new IllegalArgumentException("Unsupported import resolution mode " + importMode);
        }
        return new DefaultYangParser(reactors.computeIfAbsent(configuration, reactorFactory).newBuild());
    }
}
