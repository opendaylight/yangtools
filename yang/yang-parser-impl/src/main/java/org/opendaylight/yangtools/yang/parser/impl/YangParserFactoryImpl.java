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
import org.opendaylight.yangtools.yang.model.parser.api.YangParser;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParserFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Reference {@link YangParserFactory} implementation.
 */
@Beta
@MetaInfServices
@Singleton
@Component(immediate = true)
public final class YangParserFactoryImpl implements YangParserFactory {
    private static final List<StatementParserMode> SUPPORTED_MODES = List.of(
        StatementParserMode.DEFAULT_MODE, StatementParserMode.SEMVER_MODE);

    private final ConcurrentMap<YangParserConfiguration, CrossSourceStatementReactor> reactors =
        new ConcurrentHashMap<>(2);
    private final Function<YangParserConfiguration, CrossSourceStatementReactor> reactorFactory;

    /**
     * Construct a new {@link YangParserFactory} backed by {@link DefaultReactors#defaultReactor()}.
     */
    public YangParserFactoryImpl() {
        reactorFactory = config -> DefaultReactors.defaultReactorBuilder(config).build();
        // Make sure default reactor is available
        reactorFactory.apply(YangParserConfiguration.DEFAULT);
    }

    @Inject
    @Activate
    public YangParserFactoryImpl(final @Reference YangXPathParserFactory xpathFactory) {
        reactorFactory = config -> DefaultReactors.defaultReactorBuilder(xpathFactory, config).build();
    }

    @Override
    public Collection<StatementParserMode> supportedParserModes() {
        return SUPPORTED_MODES;
    }

    @Override
    public @NonNull YangParser createParser(final YangParserConfiguration configuration) {
        final StatementParserMode mode = configuration.parserMode();
        checkArgument(SUPPORTED_MODES.contains(mode), "Unsupported parser mode %s", mode);

        final CrossSourceStatementReactor reactor = reactors.computeIfAbsent(configuration, reactorFactory);
        return new YangParserImpl(reactor.newBuild(mode));
    }
}
