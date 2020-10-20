/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8040.parser.inject;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.rfc8040.parser.dagger.Rfc8040Module;
import org.opendaylight.yangtools.rfc8040.parser.impl.Rfc8040ParserExtension;

/**
 * Parser support for {@code ietf-restconf.yang} exposed into the {@code javax.inject} world.
 *
 * @since 14.0.20
 * @deprecated Use {@link Rfc8040Module#provideParserExtension()} instead.
 */
@Singleton
@NonNullByDefault
@Deprecated(since = "14.0.21", forRemoval = true)
public final class InjectRfc8040ParserExtension extends Rfc8040ParserExtension {
    /**
     * Default constructor.
     */
    @Inject
    public InjectRfc8040ParserExtension() {
        // visible for DI
    }
}
