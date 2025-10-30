/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6536.parser.impl;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.rfc6536.model.api.NACMStatements;
import org.opendaylight.yangtools.rfc6536.parser.DefaultDenyAllStatementSupport;
import org.opendaylight.yangtools.rfc6536.parser.DefaultDenyWriteStatementSupport;
import org.opendaylight.yangtools.rfc6536.parser.inject.InjectRfc6536ParserExtension;
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
@MetaInfServices
@NonNullByDefault
@Component(service = ParserExtension.class)
public sealed class Rfc6536ParserExtension extends AbstractParserExtension permits InjectRfc6536ParserExtension {
    /**
     * Default constructor.
     */
    public Rfc6536ParserExtension() {
        super(NACMStatements.values());
    }

    @Override
    public final StatementSupportBundle configureBundle(final YangParserConfiguration config) {
        return StatementSupportBundle.builder()
            .addSupport(new DefaultDenyAllStatementSupport(config))
            .addSupport(new DefaultDenyWriteStatementSupport(config))
            .build();
    }
}
