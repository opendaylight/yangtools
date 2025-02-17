/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi;

import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;

/**
 * Service discovery mechanism and instantiation for extensions to a parser reactor. Implementations are encouraged to
 * take advantage of {@link AbstractParserExtension}.
 */
@NonNullByDefault
public interface ParserExtension {
    /**
     * Configure this extension with {@link YangParserConfiguration}, returning the resultas a
     * {@link StatementSupportBundle}.
     *
     * @param config requested configuration
     * @return A {@link StatementSupportBundle}
     */
    StatementSupportBundle configureBundle(YangParserConfiguration config);

    /**
     * {@return the set {@link StatementDefinition}s supported by this extension. Guaranteed to contain at least one
     * element.}
     */
    Set<StatementDefinition> supportedStatements();
}
