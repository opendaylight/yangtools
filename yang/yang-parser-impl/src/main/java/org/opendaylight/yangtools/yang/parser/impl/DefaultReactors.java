/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.odlext.parser.OpenDaylightExtensions;
import org.opendaylight.yangtools.openconfig.parser.OpenConfig;
import org.opendaylight.yangtools.rfc7952.parser.Metadata;
import org.opendaylight.yangtools.rfc8040.parser.YangDataStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.CustomCrossSourceStatementReactorBuilder;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.Builder;

/**
 * Utility class for instantiating default-configured {@link CrossSourceStatementReactor}s.
 *
 * @author Robert Varga
 */
@Beta
public final class DefaultReactors {
    private static final CrossSourceStatementReactor DEFAULT_REACTOR = defaultReactorBuilder().build();

    private DefaultReactors() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get a shared default-configured reactor instance. This instance is configured to handle both RFC6020 and RFC7950,
     * as well as RFC8040's yang-data extension.
     *
     * @return a shared default-configured reactor instance.
     */
    public static CrossSourceStatementReactor defaultReactor() {
        return DEFAULT_REACTOR;
    }

    /**
     * Return a baseline CrossSourceStatementReactor {@link Builder}. The builder is initialized to the equivalent
     * of the reactor returned via {@link #defaultReactor()}, but can be further customized before use.
     *
     * @return A populated CrossSourceStatementReactor builder.
     */
    public static CustomCrossSourceStatementReactorBuilder defaultReactorBuilder() {
        final CustomCrossSourceStatementReactorBuilder builder = RFC7950Reactors.defaultReactorBuilder();

       // OpenDaylight (AnyxmlSchemaLocation) support
        OpenDaylightExtensions.addToReactorBuilder(builder);

        // RFC7952 annotation support
        Metadata.addToReactorBuilder(builder);

        // RFC8040 yang-data support
        builder.addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, YangDataStatementSupport.getInstance());

        // OpenConfig extensions support (except openconfig-version)
        OpenConfig.addToReactorBuilder(builder);

        return builder;
    }
}
