/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNull;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.yang.parser.api.ImportResolutionMode;
import org.opendaylight.yangtools.yang.parser.api.YangParser;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParserFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.RequireServiceComponentRuntime;

/**
 * Reference {@link YangParserFactory} implementation.
 */
@Beta
@Component
@Singleton
@MetaInfServices
@RequireServiceComponentRuntime
public final class DefaultYangParserFactory implements YangParserFactory {
    private static final List<ImportResolutionMode> SUPPORTED_MODES = List.of(ImportResolutionMode.DEFAULT);

    private final ConcurrentMap<YangParserConfiguration, CrossSourceStatementReactor> reactors =
        new ConcurrentHashMap<>(2);
    private final Function<YangParserConfiguration, CrossSourceStatementReactor> reactorFactory;

    /**
     * Construct a new {@link YangParserFactory} backed by {@link DefaultReactors#defaultReactor()}.
     */
    public DefaultYangParserFactory() {
        reactorFactory = config -> DefaultReactors.defaultReactorBuilder(config).build();
        // Make sure default reactor is available
        reactorFactory.apply(YangParserConfiguration.DEFAULT);
    }

    @Inject
    @Activate
    public DefaultYangParserFactory(@Reference final YangXPathParserFactory xpathFactory) {
        reactorFactory = config -> DefaultReactors.defaultReactorBuilder(xpathFactory, config).build();
    }

    @Override
    public Collection<ImportResolutionMode> supportedImportResolutionModes() {
        return SUPPORTED_MODES;
    }

    @Override
    public @NonNull YangParser createParser(final YangParserConfiguration configuration) {
        final ImportResolutionMode importMode = configuration.importResolutionMode();
        checkArgument(SUPPORTED_MODES.contains(importMode), "Unsupported import resolution mode %s", importMode);
        return new DefaultYangParser(reactors.computeIfAbsent(configuration, reactorFactory).newBuild());
    }
}
