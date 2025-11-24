/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.parser.inject;

import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.rfc8528.parser.impl.Rfc8528ParserExtension;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.ParserExtension;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;

/**
 * Parser support for {@code ietf-yang-schema-mount.yang} exposed into the {@code javax.inject} world.
 *
 * @since 14.0.20
 */
@Singleton
@NonNullByDefault
@SuppressWarnings("exports")
public final class InjectRfc8528ParserExtension implements ParserExtension {
    private Rfc8528ParserExtension delegate = new Rfc8528ParserExtension();

    /**
     * Default constructor.
     */
    @Inject
    public InjectRfc8528ParserExtension() {
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
