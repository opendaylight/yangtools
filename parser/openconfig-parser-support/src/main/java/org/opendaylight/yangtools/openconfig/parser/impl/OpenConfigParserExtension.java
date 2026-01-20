/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.openconfig.parser.impl;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigHashedValueStatement;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigVersionStatement;
import org.opendaylight.yangtools.openconfig.parser.EncryptedValueStatementSupport;
import org.opendaylight.yangtools.openconfig.parser.HashedValueStatementSupport;
import org.opendaylight.yangtools.openconfig.parser.OpenConfigVersionSupport;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.AbstractParserExtension;
import org.opendaylight.yangtools.yang.parser.spi.ParserExtension;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;
import org.osgi.service.component.annotations.Component;

/**
 * Parser support for {@code openconfig-extensions.yang}.
 *
 * @since 14.0.20
 */
@NonNullByDefault
@MetaInfServices(ParserExtension.class)
@Component(service = ParserExtension.class)
public final class OpenConfigParserExtension extends AbstractParserExtension {
    /**
     * Default constructor.
     */
    public OpenConfigParserExtension() {
        super(
            OpenConfigHashedValueStatement.ENCRYPTED_DEFINITION,
            OpenConfigHashedValueStatement.HASHED_DEFINITION,
            OpenConfigVersionStatement.DEFINITION);
    }

    @Override
    public StatementSupportBundle configureBundle(final YangParserConfiguration config) {
        return StatementSupportBundle.builder()
            .addSupport(new EncryptedValueStatementSupport(config))
            .addSupport(new HashedValueStatementSupport(config))
            .addSupport(new OpenConfigVersionSupport(config))
            .build();
    }
}
