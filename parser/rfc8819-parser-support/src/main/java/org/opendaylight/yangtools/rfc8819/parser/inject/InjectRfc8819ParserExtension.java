/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8819.parser.inject;

import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.rfc8819.parser.dagger.Rfc8819Module;
import org.opendaylight.yangtools.rfc8819.parser.impl.Rfc8819ParserExtension;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.ParserExtension;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;

/**
 * Parser support for {@code ietf-module-tags.yang} exposed into the {@code javax.inject} world.
 *
 * @since 14.0.20
 * @deprecated Use {@link Rfc8819Module#provideParserExtension()} instead.
 */
@Singleton
@NonNullByDefault
@Deprecated(since = "14.0.21", forRemoval = true)
@SuppressWarnings("exports")
public final class InjectRfc8819ParserExtension implements ParserExtension {
    private final Rfc8819ParserExtension delegate = new Rfc8819ParserExtension();

    /**
     * Default constructor.
     */
    @Inject
    public InjectRfc8819ParserExtension() {
        // visible for DI
    }

    @Override
    public StatementSupportBundle configureBundle(YangParserConfiguration config) {
        return delegate.configureBundle(config);
    }

    @Override
    public Set<StatementDefinition> supportedStatements() {
        return delegate.supportedStatements();
    }
}
