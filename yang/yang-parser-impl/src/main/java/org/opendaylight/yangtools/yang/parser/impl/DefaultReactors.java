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
import org.opendaylight.yangtools.openconfig.parser.PosixPatternStatementSupport;
import org.opendaylight.yangtools.rfc6241.parser.GetFilterElementAttributesStatementSupport;
import org.opendaylight.yangtools.rfc6536.parser.DefaultDenyAllStatementSupport;
import org.opendaylight.yangtools.rfc6536.parser.DefaultDenyWriteStatementSupport;
import org.opendaylight.yangtools.rfc6643.parser.AliasStatementSupport;
import org.opendaylight.yangtools.rfc6643.parser.DefValStatementSupport;
import org.opendaylight.yangtools.rfc6643.parser.DisplayHintStatementSupport;
import org.opendaylight.yangtools.rfc6643.parser.ImpliedStatementSupport;
import org.opendaylight.yangtools.rfc6643.parser.MaxAccessStatementSupport;
import org.opendaylight.yangtools.rfc6643.parser.OidStatementSupport;
import org.opendaylight.yangtools.rfc6643.parser.SubIdStatementSupport;
import org.opendaylight.yangtools.rfc7952.parser.AnnotationStatementSupport;
import org.opendaylight.yangtools.rfc8040.parser.YangDataStatementSupport;
import org.opendaylight.yangtools.rfc8528.parser.MountPointStatementSupport;
import org.opendaylight.yangtools.yang.parser.openconfig.stmt.RegexpPosixStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.CustomCrossSourceStatementReactorBuilder;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.Builder;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParserFactory;

/**
 * Utility class for instantiating default-configured {@link CrossSourceStatementReactor}s.
 *
 * @author Robert Varga
 */
@Beta
public final class DefaultReactors {
    private static final class DefaultReactor {
        // Thread-safe lazy init
        static final @NonNull CrossSourceStatementReactor INSTANCE = defaultReactorBuilder().build();
    }

    private DefaultReactors() {
        // Hidden on purpose
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
        return DefaultReactor.INSTANCE;
    }

    /**
     * Return a baseline CrossSourceStatementReactor {@link Builder}. The builder is initialized to the equivalent
     * of the reactor returned via {@link #defaultReactor()}, but can be further customized before use.
     *
     * @return A populated CrossSourceStatementReactor builder.
     */
    public static @NonNull CustomCrossSourceStatementReactorBuilder defaultReactorBuilder() {
        return addExtensions(RFC7950Reactors.defaultReactorBuilder());
    }

    /**
     * Return a baseline CrossSourceStatementReactor {@link Builder}. The builder is initialized to the equivalent
     * of the reactor returned via {@link #defaultReactor()}, but can be further customized before use.
     *
     * @return A populated CrossSourceStatementReactor builder.
     */
    public static @NonNull CustomCrossSourceStatementReactorBuilder defaultReactorBuilder(
            final YangXPathParserFactory xpathFactory) {
        return addExtensions(RFC7950Reactors.defaultReactorBuilder(xpathFactory));
    }

    private static @NonNull CustomCrossSourceStatementReactorBuilder addExtensions(
            final @NonNull CustomCrossSourceStatementReactorBuilder builder) {
        return builder
                // AnyxmlSchemaLocation support
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                    AnyxmlSchemaLocationStatementSupport.getInstance())
                .addNamespaceSupport(ModelProcessingPhase.FULL_DECLARATION, AnyxmlSchemaLocationNamespace.BEHAVIOUR)
                .overrideStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                    AnyxmlStatementSupportOverride.getInstance())

                // RFC6241 get-filter-element-attributes support
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                    GetFilterElementAttributesStatementSupport.getInstance())

                // RFC6536 default-deny-{all,write} support
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                    DefaultDenyAllStatementSupport.getInstance())
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                    DefaultDenyWriteStatementSupport.getInstance())

                // RFC6643 extensions
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, DisplayHintStatementSupport.getInstance())
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, MaxAccessStatementSupport.getInstance())
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, DefValStatementSupport.getInstance())
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, ImpliedStatementSupport.getInstance())
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, AliasStatementSupport.getInstance())
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, OidStatementSupport.getInstance())
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, SubIdStatementSupport.getInstance())

                // RFC7952 annotation support
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, AnnotationStatementSupport.getInstance())

                // RFC8040 yang-data support
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, YangDataStatementSupport.getInstance())

                // RFC8528 yang-data support
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, MountPointStatementSupport.getInstance())

                // OpenConfig extensions support (except openconfig-version)
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                    EncryptedValueStatementSupport.getInstance())
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                    HashedValueStatementSupport.getInstance())
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                        PosixPatternStatementSupport.getInstance())
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                        RegexpPosixStatementSupport.getInstance());
    }
}
