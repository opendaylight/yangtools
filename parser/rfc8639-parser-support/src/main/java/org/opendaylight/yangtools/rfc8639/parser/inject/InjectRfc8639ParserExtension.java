/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8639.parser.inject;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.rfc8639.parser.impl.Rfc8639ParserExtension;

/**
 * Parser support for {@code ietf-subscribed-notifications.yang} exposed into the {@code javax.inject} world.
 *
 * @since 14.0.20
 */
@Singleton
@NonNullByDefault
@SuppressWarnings("exports")
public final class InjectRfc8639ParserExtension extends Rfc8639ParserExtension {
    /**
     * Default constructor.
     */
    @Inject
    public InjectRfc8639ParserExtension() {
        // visible for DI
    }
}
