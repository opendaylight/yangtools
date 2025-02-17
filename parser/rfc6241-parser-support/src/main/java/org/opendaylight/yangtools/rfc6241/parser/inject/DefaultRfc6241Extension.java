/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6241.parser.inject;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.rfc6241.parser.Rfc6241Extension;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.ParserExtension;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;

/**
 * Our extension exposed into the DI world.
 */
@Singleton
@NonNullByDefault
public final class DefaultRfc6241Extension extends ParserExtension {
    /**
     * Default constructor.
     */
    @Inject
    public DefaultRfc6241Extension() {
        // visible for DI
    }

    @Override
    public StatementSupportBundle configureBundle(final YangParserConfiguration config) {
        return Rfc6241Extension.configureBundleImpl(config);
    }
}
