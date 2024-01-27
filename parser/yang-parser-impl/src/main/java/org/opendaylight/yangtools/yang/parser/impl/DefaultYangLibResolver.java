/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Collection;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureSet;
import org.opendaylight.yangtools.yang.parser.api.YangLibModuleSet;
import org.opendaylight.yangtools.yang.parser.api.YangLibResolver;
import org.opendaylight.yangtools.yang.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParserFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Reference {@link YangLibResolver} implementation.
 */
@Singleton
@Component
@MetaInfServices
public final class DefaultYangLibResolver implements YangLibResolver {
    private final CrossSourceStatementReactor reactor;

    public DefaultYangLibResolver() {
        reactor = DefaultReactors.defaultReactorBuilder().build();
    }

    @Inject
    @Activate
    public DefaultYangLibResolver(@Reference final YangXPathParserFactory xpathFactory) {
        reactor = DefaultReactors.defaultReactorBuilder(xpathFactory).build();
    }

    @Override
    public Collection<Class<? extends SourceRepresentation>> supportedSourceRepresentations() {
        return DefaultYangParser.REPRESENTATIONS;
    }

    @Override
    public EffectiveModelContext resolveModuleSet(final YangLibModuleSet moduleSet)
            throws IOException, YangParserException {
        final var act = reactor.newBuild();
        final var features = ImmutableSet.<QName>builder();

        for (var module : moduleSet.modules().values()) {
            final var namespace = QNameModule.ofRevision(module.namespace(), module.identifier().revision());
            for (var feat : module.features()) {
                features.add(feat.bindTo(namespace));
            }

            act.addSource(DefaultYangParser.sourceToStatementStream(module.source()));
        }

        for (var module : moduleSet.importOnlyModules().values()) {
            act.addLibSource(DefaultYangParser.sourceToStatementStream(module.source()));
        }

        try {
            return act.setSupportedFeatures(FeatureSet.of(features.build())).buildEffective();
        } catch (ReactorException e) {
            throw DefaultYangParser.decodeReactorException(e);
        }
    }
}
