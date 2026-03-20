/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6536.parser.impl;

import org.eclipse.jdt.annotation.NonNull;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyAllEffectiveStatement;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyAllStatement;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyWriteEffectiveStatement;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyWriteStatement;
import org.opendaylight.yangtools.rfc6536.model.api.NACMConstants;
import org.opendaylight.yangtools.rfc6536.model.api.NACMStatements;
import org.opendaylight.yangtools.rfc6536.parser.DefaultDenyAllStatementSupport;
import org.opendaylight.yangtools.rfc6536.parser.DefaultDenyWriteStatementSupport;
import org.opendaylight.yangtools.yang.model.api.meta.DefaultStatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
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
    private static final @NonNull StatementDefinition RFC8341_DDA = DefaultStatementDefinition.of(
        NACMStatements.DEFAULT_DENY_ALL.getStatementName().bindTo(NACMConstants.RFC8341_MODULE).intern(),
        DefaultDenyAllStatement.class, DefaultDenyAllEffectiveStatement.class);
    private static final @NonNull StatementDefinition RFC8341_DDW = DefaultStatementDefinition.of(
        NACMStatements.DEFAULT_DENY_WRITE.getStatementName().bindTo(NACMConstants.RFC8341_MODULE).intern(),
        DefaultDenyWriteStatement.class, DefaultDenyWriteEffectiveStatement.class);

    /**
     * Default constructor.
     */
    public Rfc6536ParserExtension() {
        super(NACMStatements.DEFAULT_DENY_ALL, NACMStatements.DEFAULT_DENY_WRITE, RFC8341_DDA, RFC8341_DDW);
    }

    @Override
    public StatementSupportBundle configureBundle(final YangParserConfiguration config) {
        return StatementSupportBundle.builder()
            // RFC6536 support
            .addSupport(new DefaultDenyAllStatementSupport(config, NACMStatements.DEFAULT_DENY_ALL))
            .addSupport(new DefaultDenyWriteStatementSupport(config, NACMStatements.DEFAULT_DENY_WRITE))
            // RFC8341 support
            .addSupport(new DefaultDenyAllStatementSupport(config, RFC8341_DDA))
            .addSupport(new DefaultDenyWriteStatementSupport(config, RFC8341_DDW))
            .build();
    }
}
