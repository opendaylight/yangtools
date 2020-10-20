/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.parser.inject;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.rfc7952.parser.dagger.Rfc7952Module;
import org.opendaylight.yangtools.rfc7952.parser.impl.Rfc7952ParserExtension;

/**
 * Parser support for {@code ietf-yang-metadata.yang} exposed into the {@code javax.inject} world.
 *
 * @since 14.0.20
 * @deprecated Use {@link Rfc7952Module#provideParserExtension()} instead.
 */
@Singleton
@NonNullByDefault
@Deprecated(since = "14.0.21", forRemoval = true)
public final class InjectRfc7952ParserExtension extends Rfc7952ParserExtension {
    /**
     * Default constructor.
     */
    @Inject
    public InjectRfc7952ParserExtension() {
        // visible for DI
    }
}
