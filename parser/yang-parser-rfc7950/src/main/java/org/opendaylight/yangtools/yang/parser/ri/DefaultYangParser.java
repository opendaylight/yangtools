/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.ri;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import java.io.IOException;
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
import org.opendaylight.yangtools.yang.model.api.source.YangSourceRepresentation;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.api.source.YinSourceRepresentation;
import org.opendaylight.yangtools.yang.model.api.source.YinTextSource;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureSet;
import org.opendaylight.yangtools.yang.model.spi.source.SourceSyntaxException;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextToIRSourceTransformer;
import org.opendaylight.yangtools.yang.model.spi.source.YinDomSource;
import org.opendaylight.yangtools.yang.model.spi.source.YinTextToDOMSourceTransformer;
import org.opendaylight.yangtools.yang.model.spi.source.YinXmlSource;
import org.opendaylight.yangtools.yang.parser.api.YangParser;
import org.opendaylight.yangtools.yang.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;

final class DefaultYangParser implements YangParser {
    static final @NonNull ImmutableSet<Class<? extends SourceRepresentation>> REPRESENTATIONS = ImmutableSet.of(
        // In order of preference
        YangIRSource.class,
        YangTextSource.class,
        YinDomSource.class,
        YinXmlSource.class,
        YinTextSource.class);

    private final @NonNull YangTextToIRSourceTransformer textToIR;
    private final @NonNull YinTextToDOMSourceTransformer textToDOM;
    private final @NonNull BuildAction buildAction;

    DefaultYangParser(final YangTextToIRSourceTransformer textToIR, final YinTextToDOMSourceTransformer textToDOM,
            final BuildAction buildAction) {
        this.textToIR = requireNonNull(textToIR);
        this.textToDOM = requireNonNull(textToDOM);
        this.buildAction = requireNonNull(buildAction);
    }

    @Override
    public ImmutableSet<Class<? extends SourceRepresentation>> supportedSourceRepresentations() {
        return REPRESENTATIONS;
    }

    @Override
    public YangParser addSource(final YangSourceRepresentation source) throws IOException, YangSyntaxErrorException {
        buildAction.addYangSource(sourceToYangIR(source));
        return this;
    }

    @Override
    public YangParser addSource(final YinSourceRepresentation source) throws IOException, YangSyntaxErrorException {
        buildAction.addYinSource(sourceToYinDOM(source));
        return this;
    }

    @Override
    public YangParser addLibSource(final YangSourceRepresentation source) throws YangSyntaxErrorException {
        buildAction.addLibYangSource(sourceToYangIR(source));
        return this;
    }

    @Override
    public YangParser addLibSource(final YinSourceRepresentation source) throws YangSyntaxErrorException {
        buildAction.addLibYinSource(sourceToYinDOM(source));
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

    YangIRSource sourceToYangIR(final YangSourceRepresentation source) throws YangSyntaxErrorException {
        return switch (source) {
            case YangIRSource irSource -> irSource;
            case YangTextSource yangSource -> {
                try {
                    yield textToIR.transformSource(yangSource);
                } catch (SourceSyntaxException e) {
                    throw newSyntaxError(source.sourceId(), e);
                }
            }
            default -> throw new IllegalArgumentException("Unsupported source " + source);
        };
    }

    YinDomSource sourceToYinDOM(final YinSourceRepresentation source) throws YangSyntaxErrorException {
        return sourceToYinDOM(textToDOM, source);
    }

    static YinDomSource sourceToYinDOM(final YinTextToDOMSourceTransformer textToDOM,
            final YinSourceRepresentation source) throws YangSyntaxErrorException {
        return switch (source) {
            case YinDomSource yinDom -> yinDom;
            case YinTextSource yinText -> {
                try {
                    yield textToDOM.transformSource(yinText);
                } catch (SourceSyntaxException e) {
                    throw newSyntaxError(source.sourceId(), e);
                }
            }
            case YinXmlSource yinXml -> {
                try {
                    yield YinDomSource.transform(yinXml);
                } catch (TransformerException e) {
                    final var locator = e.getLocator();
                    throw new YangSyntaxErrorException(source.sourceId(),
                        locator != null ? locator.getLineNumber() : 0,
                        locator != null ? locator.getColumnNumber() : 0,
                        "Failed to assemble in-memory representation", e);
                }
            }
            default -> throw new IllegalArgumentException("Unsupported source " + source);
        };
    }

    @NonNullByDefault
    static YangSyntaxErrorException newSyntaxError(final SourceIdentifier sourceId,
            final SourceSyntaxException cause) {
        final var sourceRef = cause.sourceRef();
        if (sourceRef != null && sourceRef.declarationReference() instanceof DeclarationInText ref) {
            return new YangSyntaxErrorException(sourceId, ref.startLine(), ref.startColumn(), cause.getMessage(),
                cause);
        }
        return new YangSyntaxErrorException(sourceId, 0, 0, cause.getMessage(), cause);
    }
}
