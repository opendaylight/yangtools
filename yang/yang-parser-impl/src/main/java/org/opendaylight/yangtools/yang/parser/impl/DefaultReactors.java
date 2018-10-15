/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.odlext.parser.AnyxmlSchemaLocationNamespace;
import org.opendaylight.yangtools.odlext.parser.AnyxmlSchemaLocationStatementSupport;
import org.opendaylight.yangtools.odlext.parser.AnyxmlStatementSupportOverride;
import org.opendaylight.yangtools.openconfig.parser.EncryptedValueStatementSupport;
import org.opendaylight.yangtools.openconfig.parser.HashedValueStatementSupport;
import org.opendaylight.yangtools.rfc6536.parser.DefaultDenyAllStatementSupport;
import org.opendaylight.yangtools.rfc6536.parser.DefaultDenyWriteStatementSupport;
import org.opendaylight.yangtools.rfc7952.parser.AnnotationStatementSupport;
import org.opendaylight.yangtools.rfc8040.parser.YangDataStatementSupport;
import org.opendaylight.yangtools.tailf.common.parser.TailFActionRFC6020StatementSupport;
import org.opendaylight.yangtools.tailf.common.parser.TailFActionRFC7950StatementSupport;
import org.opendaylight.yangtools.yang.common.YangVersion;
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
    private static final @NonNull CrossSourceStatementReactor DEFAULT_REACTOR = defaultReactorBuilder().build();

    private DefaultReactors() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get a shared default-configured reactor instance. This instance is configured to handle both RFC6020 and RFC7950,
     * as well as
     * <ul>
     * <li>RFC6536's default-deny-{all,write} extensions</li>
     * <li>RFC7952's annotation extension</li>
     * <li>RFC8040's yang-data extension</li>
     * <li>OpenConfig extensions</li>
     * <li>OpenDaylight extensions</li>
     * </ul>
     *
     * @return a shared default-configured reactor instance.
     */
    public static @NonNull CrossSourceStatementReactor defaultReactor() {
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
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                    AnyxmlSchemaLocationStatementSupport.getInstance())
                .addNamespaceSupport(ModelProcessingPhase.FULL_DECLARATION, AnyxmlSchemaLocationNamespace.BEHAVIOUR)
                .overrideStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                    AnyxmlStatementSupportOverride.getInstance())

                // RFC6536 default-deny-{all,write} support
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                    DefaultDenyAllStatementSupport.getInstance())
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                    DefaultDenyWriteStatementSupport.getInstance())

                // RFC7952 annotation support
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, AnnotationStatementSupport.getInstance())

                // RFC8040 yang-data support
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, YangDataStatementSupport.getInstance())

                // OpenConfig extensions support (except openconfig-version)
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                    EncryptedValueStatementSupport.getInstance())
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                    HashedValueStatementSupport.getInstance())

                // Tail-F action support
                .addStatementSupport(ModelProcessingPhase.STATEMENT_DEFINITION, YangVersion.VERSION_1,
                    TailFActionRFC6020StatementSupport.getInstance())
                .addStatementSupport(ModelProcessingPhase.STATEMENT_DEFINITION, YangVersion.VERSION_1_1,
                    TailFActionRFC7950StatementSupport.getInstance());
    }
}
