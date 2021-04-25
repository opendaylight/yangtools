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
import com.google.common.collect.Collections2;
import java.util.Collection;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNull;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.yang.model.parser.api.YangParser;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParserFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Reference {@link YangParserFactory} implementation.
 *
 * @deprecated Use {@link DefaultYangParserFactory} instead.
 */
@Beta
@MetaInfServices
@Singleton
@Component(immediate = true)
@Deprecated(since = "7.0.0", forRemoval = true)
public final class YangParserFactoryImpl implements YangParserFactory {
    private final DefaultYangParserFactory delegate;

    private YangParserFactoryImpl(final DefaultYangParserFactory delegate) {
        this.delegate = requireNonNull(delegate);
    }

    /**
     * Construct a new {@link YangParserFactory} backed by {@link DefaultReactors#defaultReactor()}.
     */
    public YangParserFactoryImpl() {
        this(new DefaultYangParserFactory());
    }

    @Inject
    @Activate
    public YangParserFactoryImpl(final @Reference YangXPathParserFactory xpathFactory) {
        this(new DefaultYangParserFactory(xpathFactory));
    }

    @Override
    public Collection<StatementParserMode> supportedParserModes() {
        return Collections2.transform(delegate.supportedImportResolutionModes(), mode -> {
            switch (mode) {
                case DEFAULT:
                    return StatementParserMode.DEFAULT_MODE;
                case OPENCONFIG_SEMVER:
                    return StatementParserMode.SEMVER_MODE;
                default:
                    throw new IllegalStateException("Unhandled mode " + mode);
            }
        });
    }

    @Override
    public @NonNull YangParser createParser(final YangParserConfiguration configuration) {
        return new YangParserImpl(delegate.createParser(configuration));
    }
}
