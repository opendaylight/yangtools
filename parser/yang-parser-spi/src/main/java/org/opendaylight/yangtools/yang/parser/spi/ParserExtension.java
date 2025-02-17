/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;

/**
 * Service discovery mechanism and instantiation for extensions to a parser reactor.
 */
@Beta
@NonNullByDefault
public abstract class ParserExtension {
    /**
     * Configure this extension with {@link YangParserConfiguration}, returning the resultas a
     * {@link StatementSupportBundle}.
     *
     * @param config requested configuration
     * @return A {@link StatementSupportBundle}
     */
    public abstract StatementSupportBundle configureBundle(YangParserConfiguration config);

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return super.equals(obj);
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).toString();
    }
}
