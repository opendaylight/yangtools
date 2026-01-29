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
import java.util.List;
import javax.xml.transform.TransformerException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationInText;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.api.source.YinTextSource;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureSet;
import org.opendaylight.yangtools.yang.model.spi.source.DefaultYinTextToDOMSourceTransformer;
import org.opendaylight.yangtools.yang.model.spi.source.SourceSyntaxException;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextToIRSourceTransformer;
import org.opendaylight.yangtools.yang.model.spi.source.YinDomSource;
import org.opendaylight.yangtools.yang.model.spi.source.YinXmlSource;
import org.opendaylight.yangtools.yang.parser.api.YangParser;
import org.opendaylight.yangtools.yang.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YinStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.source.YangIRStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;

@Deprecated(since = "14.0.21", forRemoval = true)
final class DefaultYangParser implements YangParser {
    @Deprecated
    static final @NonNull ImmutableSet<Class<? extends SourceRepresentation>> REPRESENTATIONS = ImmutableSet.of(
        // In order of preference
        YangIRSource.class,
        YangTextSource.class,
        YinDomSource.class,
        YinXmlSource.class,
        YinTextSource.class);

    private final YangTextToIRSourceTransformer textToIR;
    private final BuildAction buildAction;

    @Deprecated
    DefaultYangParser(final YangTextToIRSourceTransformer textToIR, final BuildAction buildAction) {
        this.textToIR = requireNonNull(textToIR);
        this.buildAction = requireNonNull(buildAction);
    }

    @Deprecated
    @Override
    public ImmutableSet<Class<? extends SourceRepresentation>> supportedSourceRepresentations() {
        return REPRESENTATIONS;
    }

    @Deprecated
    @Override
    public YangParser addSource(final SourceRepresentation source) throws YangSyntaxErrorException {
        buildAction.addSource(sourceToStatementStream(textToIR, source));
        return this;
    }

    @Deprecated
    @Override
    public YangParser addLibSource(final SourceRepresentation source) throws YangSyntaxErrorException {
        buildAction.addLibSource(sourceToStatementStream(textToIR, source));
        return this;
    }

    @Deprecated
    @Override
    public YangParser setSupportedFeatures(final FeatureSet supportedFeatures) {
        buildAction.setSupportedFeatures(supportedFeatures);
        return this;
    }

    @Deprecated
    @Override
    public YangParser setModulesWithSupportedDeviations(
            final SetMultimap<QNameModule, QNameModule> modulesDeviatedByModules) {
        buildAction.setModulesWithSupportedDeviations(modulesDeviatedByModules);
        return this;
    }

    @Deprecated
    @Override
    public List<DeclaredStatement<?>> buildDeclaredModel() throws YangParserException {
        try {
            return buildAction.build().getRootStatements();
        } catch (ReactorException e) {
            throw decodeReactorException(e);
        }
    }

    @Deprecated
    @Override
    public EffectiveModelContext buildEffectiveModel() throws YangParserException {
        try {
            return buildAction.buildEffective();
        } catch (ReactorException e) {
            throw decodeReactorException(e);
        }
    }

    @Deprecated
    static YangParserException decodeReactorException(final ReactorException reported) {
        // FIXME: map exception in some reasonable manner
        return new YangParserException("Failed to assemble sources", reported);
    }

    @Deprecated
    static StatementStreamSource sourceToStatementStream(final YangTextToIRSourceTransformer textToIR,
            final SourceRepresentation source) throws YangSyntaxErrorException {
        return switch (source) {
            case YangIRSource irSource -> new YangIRStatementStreamSource(irSource);
            case YangTextSource yangSource -> {
                final YangIRSource irSource;
                try {
                    irSource = textToIR.transformSource(yangSource);
                } catch (SourceSyntaxException e) {
                    throw newSyntaxError(source.sourceId(), e);
                }
                yield new YangIRStatementStreamSource(irSource);
            }
            case YinDomSource yinDom -> YinStatementStreamSource.create(yinDom);
            case YinTextSource yinText -> {
                final YinDomSource yinDom;
                try {
                    yinDom = new DefaultYinTextToDOMSourceTransformer().transformSource(yinText);
                } catch (SourceSyntaxException e) {
                    throw newSyntaxError(source.sourceId(), e);
                }
                yield YinStatementStreamSource.create(yinDom);
            }
            case YinXmlSource yinXml -> {
                try {
                    yield YinStatementStreamSource.create(yinXml);
                } catch (TransformerException e) {
                    throw new YangSyntaxErrorException(source.sourceId(), 0, 0,
                        "Failed to assemble in-memory representation", e);
                }
            }
            default -> throw new IllegalArgumentException("Unsupported source " + source);
        };
    }

    @NonNullByDefault
    private static YangSyntaxErrorException newSyntaxError(final SourceIdentifier sourceId,
        final SourceSyntaxException cause) {
        final var sourceRef = cause.sourceRef();
        if (sourceRef != null && sourceRef.declarationReference() instanceof DeclarationInText ref) {
            return new YangSyntaxErrorException(sourceId, ref.startLine(), ref.startColumn(), cause.getMessage(),
                cause);
        }
        return new YangSyntaxErrorException(sourceId, 0, 0, cause.getMessage(), cause);
    }
}
