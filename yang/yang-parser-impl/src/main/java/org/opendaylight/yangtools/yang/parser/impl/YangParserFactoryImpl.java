/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNull;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.yang.model.parser.api.YangParser;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserConfiguration;
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
        StatementParserMode.DEFAULT_MODE,
        StatementParserMode.SEMVER_MODE);

    private final CrossSourceStatementReactor reactor;

    private YangParserFactoryImpl(final @NonNull CrossSourceStatementReactor reactor) {
        this.reactor = requireNonNull(reactor);
    }

    /**
     * Construct a new {@link YangParserFactory} backed by {@link DefaultReactors#defaultReactor()}.
     */
    public YangParserFactoryImpl() {
        this(DefaultReactors.defaultReactor());
    }

    @Inject
    @Activate
    public YangParserFactoryImpl(final @Reference YangXPathParserFactory xpathFactory) {
        this(DefaultReactors.defaultReactorBuilder(xpathFactory).build());
    }

    /**
     * Construct a new {@link YangParserFactory} backed by specified reactor.
     *
     * @param reactor Backing reactor
     * @return A new YangParserFactory
     */
    public static @NonNull YangParserFactory forReactor(final @NonNull CrossSourceStatementReactor reactor) {
        return new YangParserFactoryImpl(reactor);
    }

    @Override
    public Collection<StatementParserMode> supportedParserModes() {
        return SUPPORTED_MODES;
    }

    @Override
    public @NonNull YangParser createParser(final YangParserConfiguration configuration) {
        final StatementParserMode mode = configuration.parserMode();
        checkArgument(SUPPORTED_MODES.contains(mode), "Unsupported parser mode %s", mode);
        return new YangParserImpl(reactor.newBuild(mode));
    }
}
