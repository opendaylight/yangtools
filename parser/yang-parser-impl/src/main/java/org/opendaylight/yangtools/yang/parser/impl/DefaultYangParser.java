/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import java.io.IOException;
import java.util.List;
import javax.xml.transform.TransformerException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.api.source.YinTextSource;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureSet;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.model.spi.source.YinDomSource;
import org.opendaylight.yangtools.yang.model.spi.source.YinXmlSource;
import org.opendaylight.yangtools.yang.parser.api.YangParser;
import org.opendaylight.yangtools.yang.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YinStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YinTextToDomTransformer;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
import org.xml.sax.SAXException;

final class DefaultYangParser implements YangParser {
    static final @NonNull ImmutableSet<Class<? extends SourceRepresentation>> REPRESENTATIONS = ImmutableSet.of(
        // In order of preference
        YangIRSource.class,
        YangTextSource.class,
        YinDomSource.class,
        YinXmlSource.class,
        YinTextSource.class);

    private final BuildAction buildAction;

    DefaultYangParser(final BuildAction buildAction) {
        this.buildAction = requireNonNull(buildAction);
    }

    @Override
    public ImmutableSet<Class<? extends SourceRepresentation>> supportedSourceRepresentations() {
        return REPRESENTATIONS;
    }

    @Override
    public YangParser addSource(final SourceRepresentation source) throws IOException, YangSyntaxErrorException {
        buildAction.addSource(sourceToStatementStream(source));
        return this;
    }

    @Override
    public YangParser addLibSource(final SourceRepresentation source) throws IOException, YangSyntaxErrorException {
        buildAction.addLibSource(sourceToStatementStream(source));
        return this;
    }

    @Override
    public YangParser setSupportedFeatures(final FeatureSet supportedFeatures) {
        buildAction.setSupportedFeatures(supportedFeatures);
        return this;
    }

    @Override
    public YangParser setModulesWithSupportedDeviations(
            final SetMultimap<QNameModule, QNameModule> modulesDeviatedByModules) {
        buildAction.setModulesWithSupportedDeviations(modulesDeviatedByModules);
        return this;
    }

    @Override
    public List<DeclaredStatement<?>> buildDeclaredModel() throws YangParserException {
        try {
            return buildAction.build().getRootStatements();
        } catch (ReactorException e) {
            throw decodeReactorException(e);
        }
    }

    @Override
    public EffectiveModelContext buildEffectiveModel() throws YangParserException {
        try {
            return buildAction.buildEffective();
        } catch (ReactorException e) {
            throw decodeReactorException(e);
        }
    }

    static YangParserException decodeReactorException(final ReactorException reported) {
        // FIXME: map exception in some reasonable manner
        return new YangParserException("Failed to assemble sources", reported);
    }

    static StatementStreamSource sourceToStatementStream(final SourceRepresentation source)
            throws IOException, YangSyntaxErrorException {
        requireNonNull(source);
        if (source instanceof YangIRSource irSource) {
            return YangStatementStreamSource.create(irSource);
        } else if (source instanceof YangTextSource yangSource) {
            return YangStatementStreamSource.create(yangSource);
        } else if (source instanceof YinDomSource yinDom) {
            return YinStatementStreamSource.create(yinDom);
        } else if (source instanceof YinTextSource yinText) {
            try {
                return YinStatementStreamSource.create(YinTextToDomTransformer.transformSource(yinText));
            } catch (SAXException e) {
                throw new YangSyntaxErrorException(source.sourceId(), 0, 0, "Failed to parse XML text", e);
            }
        } else if (source instanceof YinXmlSource yinXml) {
            try {
                return YinStatementStreamSource.create(yinXml);
            } catch (TransformerException e) {
                throw new YangSyntaxErrorException(source.sourceId(), 0, 0,
                    "Failed to assemble in-memory representation", e);
            }
        } else {
            throw new IllegalArgumentException("Unsupported source " + source);
        }
    }
}
