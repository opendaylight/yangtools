/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6536.parser.inject;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.rfc6536.parser.dagger.Rfc6536Module;
import org.opendaylight.yangtools.rfc6536.parser.impl.Rfc6536ParserExtension;

/**
 * Parser support for {@code ietf-netconf-acm.yang} exposed into the {@code javax.inject} world.
 *
 * @since 14.0.20
 * @deprecated Use {@link Rfc6536Module#provideParserExtension()} instead.
 */
@Singleton
@NonNullByDefault
@Deprecated(since = "14.0.21", forRemoval = true)
public final class InjectRfc6536ParserExtension extends Rfc6536ParserExtension {
    /**
     * Default constructor.
     */
    @Inject
    public InjectRfc6536ParserExtension() {
        // visible for DI
    }
}
