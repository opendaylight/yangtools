/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import javax.annotation.concurrent.ThreadSafe;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.yang.model.parser.api.YangParser;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;

@Beta
@ThreadSafe
@MetaInfServices
public final class YangParserFactoryImpl implements YangParserFactory {
    private static final Collection<StatementParserMode> SUPPORTED_MODES = ImmutableList.of(
        StatementParserMode.DEFAULT_MODE, StatementParserMode.SEMVER_MODE);

    private final CrossSourceStatementReactor reactor;

    public YangParserFactoryImpl() {
        this(DefaultReactors.defaultReactor());
    }

    public YangParserFactoryImpl(final CrossSourceStatementReactor reactor) {
        this.reactor = requireNonNull(reactor);
    }

    @Override
    public Collection<StatementParserMode> supportedParserModes() {
        return SUPPORTED_MODES;
    }

    @Override
    public YangParser createParser(final StatementParserMode parserMode) {
        return new YangParserImpl(reactor.newBuild(parserMode));
    }
}
