/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.ri;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.yang.parser.api.ImportResolutionMode;
import org.opendaylight.yangtools.yang.parser.api.YangParser;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.ParserExtension;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParserFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * Reference {@link YangParserFactory} implementation.
 */
@Beta
@Component
@MetaInfServices
public final class DefaultYangParserFactory implements YangParserFactory {
    private static final List<ImportResolutionMode> SUPPORTED_MODES = List.of(ImportResolutionMode.DEFAULT);

    private final ConcurrentHashMap<YangParserConfiguration, CrossSourceStatementReactor> reactors =
        new ConcurrentHashMap<>(2);
    private final Function<YangParserConfiguration, CrossSourceStatementReactor> reactorFactory;

    /**
     * Default constructor for {@link ServiceLoader} instantiation.
     */
    public DefaultYangParserFactory() {
        this(ServiceLoader.load(YangXPathParserFactory.class).findFirst()
            .orElseThrow(() -> new IllegalStateException("No YangXPathParserFactory found")));
    }

    /**
     * Utility constructor for partial injection.
     *
     * @deprecated Exposed only for InjectYangParserFactory
     */
    @Deprecated(since = "14.0.21", forRemoval = true)
    public DefaultYangParserFactory(final YangXPathParserFactory xpathFactory) {
        this(xpathFactory,
            ServiceLoader.load(ParserExtension.class).stream().map(ServiceLoader.Provider::get).toList());
    }

    /**
     * Default constructor for full injection.
     *
     * @param xpathFactory the {@link YangXPathParserFactory} to use
     * @param extensions the {@link ParserExtension}s to use
     */
    @Activate
    public DefaultYangParserFactory(@Reference final YangXPathParserFactory xpathFactory,
            @Reference(policyOption = ReferencePolicyOption.GREEDY) final Collection<ParserExtension> extensions) {
        reactorFactory = config -> {
            final var builder = RFC7950Reactors.defaultReactorBuilder(xpathFactory, config);
            for (var extension : extensions) {
                builder.addAllSupports(ModelProcessingPhase.FULL_DECLARATION, extension.configureBundle(config));
            }
            return builder.build();
        };

        // Make sure default reactor is available
        verifyNotNull(reactorFactory.apply(YangParserConfiguration.DEFAULT));
    }

    @Override
    public Collection<ImportResolutionMode> supportedImportResolutionModes() {
        return SUPPORTED_MODES;
    }

    @Override
    public YangParser createParser(final YangParserConfiguration configuration) {
        final var importMode = configuration.importResolutionMode();
        if (!SUPPORTED_MODES.contains(importMode)) {
            throw new IllegalArgumentException("Unsupported import resolution mode " + importMode);
        }
        return new DefaultYangParser(reactors.computeIfAbsent(configuration, reactorFactory).newBuild());
    }
}
