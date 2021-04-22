/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.api;

import com.google.common.annotations.Beta;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Basic entry point into a YANG parser implementation. Implementations of this interface are expected to be
 * thread-safe.
 */
public interface YangParserFactory {
    /**
     * Return enumeration of {@link ImportResolutionMode}s supported by this factory.
     *
     * @return Enumeration of supported schema source representations
     */
    @Beta
    Collection<ImportResolutionMode> supportedParserModes();

    /**
     * Create a {@link YangParser} instance operating with default {@link YangParserConfiguration}.
     *
     * @return A new {@link YangParser} instance
     */
    default @NonNull YangParser createParser() {
        return createParser(YangParserConfiguration.DEFAULT);
    }

    /**
     * Create a {@link YangParser} instance operating with specified {@link YangParserConfiguration}.
     *
     * @param configuration Requested parser configuration
     * @return A new {@link YangParser} instance
     * @throws NullPointerException if configuration is null
     * @throws IllegalArgumentException if specified configuration is not supported
     */
    @NonNull YangParser createParser(YangParserConfiguration configuration);
}
