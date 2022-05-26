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
import org.opendaylight.yangtools.odlext.parser.AugmentIdentifierStatementSupport;
import org.opendaylight.yangtools.odlext.parser.ContextInstanceStatementSupport;
import org.opendaylight.yangtools.odlext.parser.ContextReferenceStatementSupport;
import org.opendaylight.yangtools.odlext.parser.InstanceTargetStatementSupport;
import org.opendaylight.yangtools.odlext.parser.RpcContextReferenceStatementSupport;
import org.opendaylight.yangtools.openconfig.parser.EncryptedValueStatementSupport;
import org.opendaylight.yangtools.openconfig.parser.HashedValueStatementSupport;
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
import org.opendaylight.yangtools.rfc8639.parser.SubscriptionStateNotificationStatementSupport;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
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
        return defaultReactorBuilder(YangParserConfiguration.DEFAULT);
    }

    /**
     * Return a baseline CrossSourceStatementReactor {@link Builder}. The builder is initialized to the equivalent
     * of the reactor returned via {@link #defaultReactor()}, but can be further customized before use.
     *
     * @param config parser configuration
     * @return A populated CrossSourceStatementReactor builder.
     */
    public static @NonNull CustomCrossSourceStatementReactorBuilder defaultReactorBuilder(
            final YangParserConfiguration config) {
        return addExtensions(RFC7950Reactors.defaultReactorBuilder(config), config);
    }

    /**
     * Return a baseline CrossSourceStatementReactor {@link Builder}. The builder is initialized to the equivalent
     * of the reactor returned via {@link #defaultReactor()}, but can be further customized before use.
     *
     * @return A populated CrossSourceStatementReactor builder.
     */
    public static @NonNull CustomCrossSourceStatementReactorBuilder defaultReactorBuilder(
            final YangXPathParserFactory xpathFactory) {
        return defaultReactorBuilder(xpathFactory, YangParserConfiguration.DEFAULT);
    }

    /**
     * Return a baseline CrossSourceStatementReactor {@link Builder}. The builder is initialized to the equivalent
     * of the reactor returned via {@link #defaultReactor()}, but can be further customized before use.
     *
     * @param config parser configuration
     * @return A populated CrossSourceStatementReactor builder.
     */
    public static @NonNull CustomCrossSourceStatementReactorBuilder defaultReactorBuilder(
            final YangXPathParserFactory xpathFactory, final YangParserConfiguration config) {
        return addExtensions(RFC7950Reactors.defaultReactorBuilder(xpathFactory, config), config);
    }

    private static @NonNull CustomCrossSourceStatementReactorBuilder addExtensions(
            final @NonNull CustomCrossSourceStatementReactorBuilder builder, final YangParserConfiguration config) {
        return builder
                // OpenDaylight extensions
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                    new AugmentIdentifierStatementSupport(config))
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, new ContextInstanceStatementSupport(config))
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                    new ContextReferenceStatementSupport(config))
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, new InstanceTargetStatementSupport(config))
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                    new RpcContextReferenceStatementSupport(config))

                // RFC6241 get-filter-element-attributes support
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                    new GetFilterElementAttributesStatementSupport(config))

                // RFC6536 default-deny-{all,write} support
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                    new DefaultDenyAllStatementSupport(config))
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                    new DefaultDenyWriteStatementSupport(config))

                // RFC6643 extensions
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, new DisplayHintStatementSupport(config))
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, new MaxAccessStatementSupport(config))
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, new DefValStatementSupport(config))
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, new ImpliedStatementSupport(config))
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, new AliasStatementSupport(config))
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, new OidStatementSupport(config))
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, new SubIdStatementSupport(config))

                // RFC7952 annotation support
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, new AnnotationStatementSupport(config))

                // RFC8040 yang-data support
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, new YangDataStatementSupport(config))

                // RFC8528 mount-point support
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, new MountPointStatementSupport(config))

                // RFC8639 subscription-state-notification support
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                    new SubscriptionStateNotificationStatementSupport(config))

                // OpenConfig extensions support (except openconfig-version)
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                    new EncryptedValueStatementSupport(config))
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                    new HashedValueStatementSupport(config));
    }
}
