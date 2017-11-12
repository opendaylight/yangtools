/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.treeScoped;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.parser.odlext.namespace.AnyxmlSchemaLocationNamespace;
import org.opendaylight.yangtools.yang.parser.odlext.stmt.AnyxmlSchemaLocationSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.CustomCrossSourceStatementReactorBuilder;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.Builder;
import org.opendaylight.yangtools.yang.parser.stmt.rfc8040.YangDataStatementSupport;

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
        return RFC7950Reactors.defaultReactorBuilder()
                // AnyxmlSchemaLocation support
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, AnyxmlSchemaLocationSupport.getInstance())
                .addNamespaceSupport(ModelProcessingPhase.FULL_DECLARATION,
                    treeScoped(AnyxmlSchemaLocationNamespace.class))
                // RFC8040 yang-data support.
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, YangDataStatementSupport.getInstance());
    }
}
