/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6536.parser;

import org.eclipse.jdt.annotation.NonNull;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyAllEffectiveStatement;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyAllStatement;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyWriteEffectiveStatement;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyWriteStatement;
import org.opendaylight.yangtools.rfc6536.model.api.NACMConstants;
import org.opendaylight.yangtools.yang.common.Empty;
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
    private static final @NonNull StatementDefinition<
        Empty, @NonNull DefaultDenyAllStatement, @NonNull DefaultDenyAllEffectiveStatement> RFC8341_DDA =
            StatementDefinition.of(DefaultDenyAllStatement.class, DefaultDenyAllEffectiveStatement.class,
                NACMConstants.RFC8341_MODULE, "default-deny-all");
    private static final @NonNull StatementDefinition<
        Empty, @NonNull DefaultDenyWriteStatement, @NonNull DefaultDenyWriteEffectiveStatement> RFC8341_DDW =
            StatementDefinition.of(DefaultDenyWriteStatement.class, DefaultDenyWriteEffectiveStatement.class,
                NACMConstants.RFC8341_MODULE, "default-deny-write");

    /**
     * Default constructor.
     */
    public Rfc6536ParserExtension() {
        super(DefaultDenyAllStatement.DEF, DefaultDenyWriteStatement.DEF, RFC8341_DDA, RFC8341_DDW);
    }

    @Override
    public StatementSupportBundle configureBundle(final YangParserConfiguration config) {
        return StatementSupportBundle.builder()
            // RFC6536 support
            .addSupport(new DefaultDenyAllStatementSupport(config))
            .addSupport(new DefaultDenyWriteStatementSupport(config))
            // RFC8341 support
            .addSupport(new DefaultDenyAllStatementSupport(config, RFC8341_DDA))
            .addSupport(new DefaultDenyWriteStatementSupport(config, RFC8341_DDW))
            .build();
    }
}
