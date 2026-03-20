/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6536.parser.impl;

import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.rfc6536.model.api.NACMStatements;
import org.opendaylight.yangtools.rfc6536.parser.DefaultDenyAllStatementSupport;
import org.opendaylight.yangtools.rfc6536.parser.DefaultDenyWriteStatementSupport;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.AbstractParserExtension;
import org.opendaylight.yangtools.yang.parser.spi.ParserExtension;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;
import org.osgi.service.component.annotations.Component;

/**
 * Parser support for {@code ietf-netconf-acm.yang}.
 *
 * @since 14.0.20
 */
@MetaInfServices(ParserExtension.class)
@Component(service = ParserExtension.class)
public final class Rfc6536ParserExtension extends AbstractParserExtension {
    /**
     * Default constructor.
     */
    public Rfc6536ParserExtension() {
        super(NACMStatements.DEFAULT_DENY_ALL, NACMStatements.DEFAULT_DENY_WRITE,
            DefaultDenyAllStatementSupport.RFC8341_DEF, DefaultDenyWriteStatementSupport.RFC8341_DEF);
    }

    @Override
    public StatementSupportBundle configureBundle(final YangParserConfiguration config) {
        return StatementSupportBundle.builder()
            // RFC6536 support
            .addSupport(new DefaultDenyAllStatementSupport(config))
            .addSupport(new DefaultDenyWriteStatementSupport(config))
            // RFC8341 support
            .addSupport(new DefaultDenyAllStatementSupport(config, DefaultDenyAllStatementSupport.RFC8341_DEF))
            .addSupport(new DefaultDenyWriteStatementSupport(config, DefaultDenyWriteStatementSupport.RFC8341_DEF))
            .build();
    }
}
