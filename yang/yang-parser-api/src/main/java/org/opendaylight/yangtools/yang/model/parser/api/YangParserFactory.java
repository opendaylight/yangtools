/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.parser.api;

import com.google.common.annotations.Beta;
import java.util.Collection;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;

/**
 * Basic entry point into a YANG parser implementation.
 *
 * @author Robert Varga
 */
@Beta
public interface YangParserFactory {
    /**
     * Return enumeration of concrete types of {@link SchemaSourceRepresentation} parsers created from this factory
     * support. Users can use this
     *
     * @return Enumeration of supported schema source representations.
     */
    Collection<Class<? extends SchemaSourceRepresentation>> supportedModelRepresentations();

    /**
     * Create a {@link YangParser} instance operating in default import resolution mode.
     *
     * @return A new {@link YangParser} instance
     */
    default YangParser createParser() {
        return createParser(StatementParserMode.DEFAULT_MODE);
    }

    /**
     * Create a {@link YangParser} instance operating in specified import resolution mode.
     *
     * @param parserMode Requested parser mode, may not be null.
     * @return A new {@link YangParser} instance
     * @throws NullPointerException if parser mode is null
     */
    YangParser createParser(StatementParserMode parserMode);
}
