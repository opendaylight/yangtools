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
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationInText;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;
import org.opendaylight.yangtools.yang.model.api.source.YangSourceRepresentation;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.api.source.YinSourceRepresentation;
import org.opendaylight.yangtools.yang.model.api.source.YinTextSource;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureSet;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorException;
import org.opendaylight.yangtools.yang.model.spi.source.SourceSyntaxException;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.model.spi.source.YinDOMSource;
import org.opendaylight.yangtools.yang.parser.api.YangParser;
import org.opendaylight.yangtools.yang.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction.Full;

final class DefaultYangParser implements YangParser {
    static final @NonNull ImmutableSet<Class<? extends SourceRepresentation>> REPRESENTATIONS = ImmutableSet.of(
        // In order of preference
        YangIRSource.class,
        YangTextSource.class,
        YinDOMSource.class,
        YinTextSource.class);

    @NonNullByDefault
    private final Full<YangTextSource, YinTextSource> buildAction;

    @NonNullByDefault
    DefaultYangParser(final Full<YangTextSource, YinTextSource> buildAction) {
        this.buildAction = requireNonNull(buildAction);
    }

    @Override
    public ImmutableSet<Class<? extends SourceRepresentation>> supportedSourceRepresentations() {
        return REPRESENTATIONS;
    }

    @Override
    public YangParser addSource(final YangSourceRepresentation source) throws IOException, YangSyntaxErrorException {
        try {
            switch (source) {
                case YangIRSource irSource -> buildAction.addSource(irSource);
                case YangTextSource yangSource -> {
                    buildAction.addSource(yangSource);
                }
                default -> throw new IllegalArgumentException("Unsupported YANG source " + source);
            }
        } catch (ExtractorException e) {
            throw newSyntaxError(source.sourceId(), e.sourceRef(), e);
        } catch (SourceSyntaxException e) {
            throw newSyntaxError(source.sourceId(), e.sourceRef(), e);
        }
        return this;
    }

    @Override
    public YangParser addSource(final YinSourceRepresentation source) throws IOException, YangSyntaxErrorException {
        try {
            switch (source) {
                case YinDOMSource yinDom -> buildAction.addSource(yinDom);
                case YinTextSource yinText -> {
                    buildAction.addSource(yinText);
                }
                default -> throw new IllegalArgumentException("Unsupported YIN source " + source);
            }
        } catch (ExtractorException e) {
            throw newSyntaxError(source.sourceId(), e.sourceRef(), e);
        } catch (SourceSyntaxException e) {
            throw newSyntaxError(source.sourceId(), e.sourceRef(), e);
        }
        return this;
    }

    @Override
    public YangParser addLibSource(final YangSourceRepresentation source) throws YangSyntaxErrorException {
        switch (source) {
            case YangIRSource irSource -> buildAction.addLibSource(irSource);
            case YangTextSource yangSource -> {
                try {
                    buildAction.addLibSource(yangSource);
                } catch (SourceSyntaxException e) {
                    throw newSyntaxError(source.sourceId(), e.sourceRef(), e);
                }
            }
            default -> throw new IllegalArgumentException("Unsupported YANG source " + source);
        }
        return this;
    }

    @Override
    public YangParser addLibSource(final YinSourceRepresentation source) throws YangSyntaxErrorException {
        try {
            switch (source) {
                case YinDOMSource yinDom -> buildAction.addLibSource(yinDom);
                case YinTextSource yinText -> {
                    buildAction.addLibSource(yinText);
                }
                default -> throw new IllegalArgumentException("Unsupported YIN source " + source);
            }
        } catch (SourceSyntaxException e) {
            throw newSyntaxError(source.sourceId(), e.sourceRef(), e);
        }
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
            return buildAction.buildDeclared().getRootStatements();
        } catch (ReactorException e) {
            throw decodeReactorException(e);
        } catch (ExtractorException e) {
            throw newSyntaxError(null, e.sourceRef(), e);
        }
    }

    @Override
    public EffectiveModelContext buildEffectiveModel() throws YangParserException {
        try {
            return buildAction.buildEffective();
        } catch (ExtractorException e) {
            throw newSyntaxError(null, e.sourceRef(), e);
        } catch (ReactorException e) {
            throw decodeReactorException(e);
        }
    }

    static YangParserException decodeReactorException(final ReactorException reported) {
        // FIXME: map exception in some reasonable manner
        return new YangParserException("Failed to assemble sources", reported);
    }

    @NonNullByDefault
    static YangSyntaxErrorException newSyntaxError(final @Nullable SourceIdentifier sourceId,
            final @Nullable StatementSourceReference sourceRef, final Exception cause) {
        if (sourceRef != null && sourceRef.declarationReference() instanceof DeclarationInText ref) {
            return new YangSyntaxErrorException(sourceId, ref.startLine(), ref.startColumn(), cause.getMessage(),
                cause);
        }
        return new YangSyntaxErrorException(sourceId, 0, 0, cause.getMessage(), cause);
    }
}
