/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.openconfig.parser.inject;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.openconfig.parser.dagger.OpenConfigModule;
import org.opendaylight.yangtools.openconfig.parser.impl.OpenConfigParserExtension;

/**
 * Parser support for {@code openconfig-extensions.yang} exposed into the {@code javax.inject} world.
 *
 * @since 14.0.20
 * @deprecated Use {@link OpenConfigModule#provideParserExtension()} instead.
 */
@Singleton
@NonNullByDefault
@Deprecated(since = "14.0.21", forRemoval = true)
public final class InjectOpenConfigParserExtension extends OpenConfigParserExtension {
    /**
     * Default constructor.
     */
    @Inject
    public InjectOpenConfigParserExtension() {
        // visible for DI
    }
}
