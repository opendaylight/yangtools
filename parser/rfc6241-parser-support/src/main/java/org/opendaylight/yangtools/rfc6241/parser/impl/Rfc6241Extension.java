/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6241.parser.impl;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.rfc6241.parser.GetFilterElementAttributesStatementSupport;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.ParserExtension;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;
import org.osgi.service.component.annotations.Component;

/**
 * Default implementation of our extension.
 */
@MetaInfServices
@NonNullByDefault
@Component(service = ParserExtension.class)
public final class Rfc6241Extension extends ParserExtension {
    /**
     * Default constructor.
     */
    public Rfc6241Extension() {
        // Nothing
    }

    @Override
    public StatementSupportBundle configureBundle(final YangParserConfiguration config) {
        return configureBundleImpl(config);
    }

    // Visible for inject
    public static StatementSupportBundle configureBundleImpl(final YangParserConfiguration config) {
        return StatementSupportBundle.builderWithAllVersions()
            .addSupport(new GetFilterElementAttributesStatementSupport(config))
            .build();
    }
}
