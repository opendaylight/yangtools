/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.ri;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Collection;
import java.util.ServiceLoader;
import org.eclipse.jdt.annotation.NonNull;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;
import org.opendaylight.yangtools.yang.model.api.source.SourceSyntaxException;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.api.source.YinTextSource;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureSet;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextToIRSourceTransformer;
import org.opendaylight.yangtools.yang.model.spi.source.YinDOMSource;
import org.opendaylight.yangtools.yang.model.spi.source.YinTextToDOMSourceTransformer;
import org.opendaylight.yangtools.yang.parser.api.YangLibModuleSet;
import org.opendaylight.yangtools.yang.parser.api.YangLibResolver;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.ParserExtension;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParserFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * Reference {@link YangLibResolver} implementation.
 */
@Component
@MetaInfServices
public final class DefaultYangLibResolver implements YangLibResolver {
    private final @NonNull YangTextToIRSourceTransformer textToIR;
    private final @NonNull YinTextToDOMSourceTransformer textToDOM;
    private final @NonNull CrossSourceStatementReactor reactor;

    /**
     * Default constructor for {@link ServiceLoader} instantiation.
     */
    public DefaultYangLibResolver() {
        this(
            ServiceLoader.load(YangXPathParserFactory.class).findFirst()
                .orElseThrow(() -> new IllegalStateException("No YangXPathParserFactory found")),
            ServiceLoader.load(YangTextToIRSourceTransformer.class).findFirst()
                .orElseThrow(() -> new IllegalStateException("No YangTextToIRSourceTransformer found")),
            ServiceLoader.load(YinTextToDOMSourceTransformer.class).findFirst()
                .orElseThrow(() -> new IllegalStateException("No YinTextToDOMSourceTransformer found")),
            ServiceLoader.load(ParserExtension.class).stream().map(ServiceLoader.Provider::get).toList());
    }

    @Activate
    public DefaultYangLibResolver(@Reference final YangXPathParserFactory xpathFactory,
            @Reference final YangTextToIRSourceTransformer textToIR,
            @Reference final YinTextToDOMSourceTransformer textToDOM,
            @Reference(policyOption = ReferencePolicyOption.GREEDY) final Collection<ParserExtension> extensions) {
        this.textToIR = requireNonNull(textToIR);
        this.textToDOM = requireNonNull(textToDOM);
        final var builder = RFC7950Reactors.defaultReactorBuilder(xpathFactory, YangParserConfiguration.DEFAULT);
        for (var extension : extensions) {
            builder.addAllSupports(ModelProcessingPhase.FULL_DECLARATION,
                extension.configureBundle(YangParserConfiguration.DEFAULT));
        }
        reactor = builder.build();
    }

    @Override
    public Collection<Class<? extends SourceRepresentation>> supportedSourceRepresentations() {
        return DefaultYangParser.REPRESENTATIONS;
    }

    @Override
    public EffectiveModelContext resolveModuleSet(final YangLibModuleSet moduleSet)
            throws IOException, YangParserException {
        final var act = reactor.newBuild(textToIR, textToDOM);
        final var features = ImmutableSet.<QName>builder();

        for (var module : moduleSet.modules().values()) {
            final var namespace = QNameModule.ofRevision(module.namespace(), module.identifier().revision());
            for (var feat : module.features()) {
                features.add(feat.bindTo(namespace));
            }

            final var source = module.source();
            try {
                switch (source) {
                    case YangIRSource yangIR -> act.addSource(yangIR);
                    case YangTextSource yangText -> act.addSource(yangText);
                    case YinDOMSource yinDOM -> act.addSource(yinDOM);
                    case YinTextSource yinText -> act.addSource(yinText);
                    default -> throw new IllegalArgumentException("Unsupported source " + source);
                }
            } catch (SourceSyntaxException e) {
                throw DefaultYangParser.newSyntaxError(source.sourceId(), e.sourceRef(), e);
            }
        }

        for (var module : moduleSet.importOnlyModules().values()) {
            final var source = module.source();
            switch (source) {
                case YangIRSource yangIR -> act.addLibSource(yangIR);
                case YangTextSource yangText -> act.addLibSource(yangText);
                case YinDOMSource yinDOM -> act.addLibSource(yinDOM);
                case YinTextSource yinText -> act.addLibSource(yinText);
                default -> throw new IllegalArgumentException("Unsupported source " + source);
            }
        }

        try {
            return act.setSupportedFeatures(FeatureSet.of(features.build())).buildEffective();
        } catch (ReactorException e) {
            throw DefaultYangParser.decodeException(e);
        } catch (SourceSyntaxException e) {
            throw DefaultYangParser.newSyntaxError(null, e.sourceRef(), e);
        }
    }
}
