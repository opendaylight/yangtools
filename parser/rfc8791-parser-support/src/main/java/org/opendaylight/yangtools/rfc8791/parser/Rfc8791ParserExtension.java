/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8791.parser;

import static org.opendaylight.yangtools.yang.common.YangVersion.VERSION_1;
import static org.opendaylight.yangtools.yang.common.YangVersion.VERSION_1_1;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.rfc8791.model.api.AugmentStructureStatement;
import org.opendaylight.yangtools.rfc8791.model.api.StructureStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.AbstractParserExtension;
import org.opendaylight.yangtools.yang.parser.spi.ParserExtension;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;
import org.osgi.service.component.annotations.Component;

/**
 * Parser support for {@code ietf-yang-structure-ext.yang}.
 *
 * @since 14.0.21
 */
@NonNullByDefault
@MetaInfServices(ParserExtension.class)
@Component(service = ParserExtension.class)
public final class Rfc8791ParserExtension extends AbstractParserExtension {
    /**
     * Default constructor.
     */
    public Rfc8791ParserExtension() {
        super(AugmentStructureStatement.DEFINITION, StructureStatement.DEFINITION);
    }

    @Override
    public StatementSupportBundle configureBundle(final YangParserConfiguration config) {
        return StatementSupportBundle.builder(StatementSupportBundle.VERSIONS_RFC7950)
            .addVersionSpecificSupport(VERSION_1, StructureStatementSupport.rfc6020(config))
            .addVersionSpecificSupport(VERSION_1_1, StructureStatementSupport.rfc7950(config))
            .addVersionSpecificSupport(VERSION_1, AugmentStructureStatementSupport.rfc6020(config))
            .addVersionSpecificSupport(VERSION_1_1, AugmentStructureStatementSupport.rfc7950(config))
            .build();
    }
}
