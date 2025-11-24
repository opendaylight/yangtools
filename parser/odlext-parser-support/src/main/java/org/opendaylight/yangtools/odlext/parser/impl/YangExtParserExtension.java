/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.parser.impl;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.odlext.model.api.OpenDaylightExtensionsStatements;
import org.opendaylight.yangtools.odlext.parser.ContextInstanceStatementSupport;
import org.opendaylight.yangtools.odlext.parser.ContextReferenceStatementSupport;
import org.opendaylight.yangtools.odlext.parser.InstanceTargetStatementSupport;
import org.opendaylight.yangtools.odlext.parser.LegacyAugmentIdentifierStatementSupport;
import org.opendaylight.yangtools.odlext.parser.MountStatementSupport;
import org.opendaylight.yangtools.odlext.parser.RpcContextReferenceStatementSupport;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.AbstractParserExtension;
import org.opendaylight.yangtools.yang.parser.spi.ParserExtension;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;
import org.osgi.service.component.annotations.Component;

/**
 * Parser support for {@code yang-ext.yang}.
 *
 * @since 14.0.20
 */
@NonNullByDefault
@MetaInfServices(ParserExtension.class)
@Component(service = ParserExtension.class)
public final class YangExtParserExtension extends AbstractParserExtension {
    /**
     * Default constructor.
     */
    public YangExtParserExtension() {
        super(OpenDaylightExtensionsStatements.values());
    }

    @Override
    public StatementSupportBundle configureBundle(final YangParserConfiguration config) {
        return StatementSupportBundle.builder()
            .addSupport(new ContextInstanceStatementSupport(config))
            .addSupport(new ContextReferenceStatementSupport(config))
            .addSupport(new InstanceTargetStatementSupport(config))
            .addSupport(new LegacyAugmentIdentifierStatementSupport(config))
            .addSupport(new MountStatementSupport(config))
            .addSupport(new RpcContextReferenceStatementSupport(config))
            .build();
    }
}
