/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.parser.inject;

import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.odlext.parser.dagger.YangExtModule;
import org.opendaylight.yangtools.odlext.parser.impl.YangExtParserExtension;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.ParserExtension;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;

/**
 * Our extension exposed into the {@code javax.inject} world.
 *
 * @since 14.0.20
 * @deprecated Use {@link YangExtModule#provideParserExtension()} instead.
 */
@Singleton
@NonNullByDefault
@SuppressWarnings("exports")
@Deprecated(since = "14.0.21", forRemoval = true)
public final class InjectYangExtParserExtension implements ParserExtension {
    private final YangExtParserExtension delegate = new YangExtParserExtension();

    /**
     * Default constructor.
     */
    @Inject
    public InjectYangExtParserExtension() {
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
