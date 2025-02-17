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
import org.opendaylight.yangtools.rfc6241.parser.impl.Rfc6241Extension;

/**
 * Our extension exposed into the DI world.
 */
@Singleton
@NonNullByDefault
@SuppressWarnings("exports")
public final class InjectRfc6241Extension extends Rfc6241Extension {
    /**
     * Default constructor.
     */
    @Inject
    public InjectRfc6241Extension() {
        // visible for DI
    }
}
