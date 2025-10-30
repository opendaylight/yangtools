/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6643.parser.impl;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.rfc6643.model.api.IetfYangSmiv2ExtensionsMapping;
import org.opendaylight.yangtools.rfc6643.parser.AliasStatementSupport;
import org.opendaylight.yangtools.rfc6643.parser.DefValStatementSupport;
import org.opendaylight.yangtools.rfc6643.parser.DisplayHintStatementSupport;
import org.opendaylight.yangtools.rfc6643.parser.ImpliedStatementSupport;
import org.opendaylight.yangtools.rfc6643.parser.MaxAccessStatementSupport;
import org.opendaylight.yangtools.rfc6643.parser.OidStatementSupport;
import org.opendaylight.yangtools.rfc6643.parser.SubIdStatementSupport;
import org.opendaylight.yangtools.rfc6643.parser.inject.InjectRfc6643ParserExtension;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.AbstractParserExtension;
import org.opendaylight.yangtools.yang.parser.spi.ParserExtension;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;
import org.osgi.service.component.annotations.Component;

/**
 * Parser support for {@code ietf-yang-smiv2.yang}.
 *
 * @since 14.0.20
 */
@NonNullByDefault
@MetaInfServices(ParserExtension.class)
@Component(service = ParserExtension.class)
public sealed class Rfc6643ParserExtension extends AbstractParserExtension permits InjectRfc6643ParserExtension {
    /**
     * Default constructor.
     */
    public Rfc6643ParserExtension() {
        super(IetfYangSmiv2ExtensionsMapping.values());
    }

    @Override
    public final StatementSupportBundle configureBundle(final YangParserConfiguration config) {
        return StatementSupportBundle.builder()
            .addSupport(new DisplayHintStatementSupport(config))
            .addSupport(new MaxAccessStatementSupport(config))
            .addSupport(new DefValStatementSupport(config))
            .addSupport(new ImpliedStatementSupport(config))
            .addSupport(new AliasStatementSupport(config))
            .addSupport(new OidStatementSupport(config))
            .addSupport(new SubIdStatementSupport(config))
            .build();
    }
}
