/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8040.parser.impl;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataStatements;
import org.opendaylight.yangtools.rfc8040.parser.YangDataStatementSupport;
import org.opendaylight.yangtools.rfc8040.parser.inject.InjectRfc8040ParserExtension;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.AbstractParserExtension;
import org.opendaylight.yangtools.yang.parser.spi.ParserExtension;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;
import org.osgi.service.component.annotations.Component;

/**
 * Parser support for {@code ietf-restconf.yang}.
 *
 * @since 14.0.20
 */
@MetaInfServices
@NonNullByDefault
@Component(service = ParserExtension.class)
public sealed class Rfc8040ParserExtension extends AbstractParserExtension permits InjectRfc8040ParserExtension {
    /**
     * Default constructor.
     */
    public Rfc8040ParserExtension() {
        super(YangDataStatements.YANG_DATA);
    }

    @Override
    public final StatementSupportBundle configureBundle(final YangParserConfiguration config) {
        return StatementSupportBundle.builder()
            .addSupport(new YangDataStatementSupport(config))
            .addSupport(YangDataStatementSupport.BEHAVIOUR)
            .build();
    }
}
