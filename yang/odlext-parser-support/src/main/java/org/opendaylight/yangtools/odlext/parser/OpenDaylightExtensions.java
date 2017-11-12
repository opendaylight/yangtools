/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.parser;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.CustomCrossSourceStatementReactorBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;

/**
 * Primary entry point into this implementation. Use {@link #getStatements()} to acquire a collection of
 * {@link StatementSupport} instances, which need to be registered with the parser.
 *
 * @author Robert Varga
 */
@Beta
public final class OpenDaylightExtensions {

    private OpenDaylightExtensions() {
        throw new UnsupportedOperationException();
    }

    /**
     * Configure specified {@link CustomCrossSourceStatementReactorBuilder} with support for OpenDaylight extensions.
     *
     * @param builder Builder to configure
     * @return Specified builder
     */
    public static CustomCrossSourceStatementReactorBuilder addToReactorBuilder(
            final CustomCrossSourceStatementReactorBuilder builder) {
        return builder
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, AnyxmlSchemaLocationSupport.getInstance())
                .addNamespaceSupport(ModelProcessingPhase.FULL_DECLARATION, AnyxmlSchemaLocationNamespace.BEHAVIOR)
                .overrideStatementSupport(ModelProcessingPhase.FULL_DECLARATION, YangModeledAnyxml.getInstance());
    }
}
