/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.SetMultimap;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.transform.TransformerException;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.parser.api.YangParser;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.api.YinDomSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.api.YinTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.api.YinXmlSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.ASTSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YinStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YinTextToDomTransformer;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
import org.xml.sax.SAXException;

final class YangParserImpl implements YangParser {
    private static final Collection<Class<? extends SchemaSourceRepresentation>> REPRESENTATIONS = ImmutableList.of(
        ASTSchemaSource.class, YangTextSchemaSource.class, YinDomSchemaSource.class, YinXmlSchemaSource.class,
        YinTextSchemaSource.class);

    private final BuildAction buildAction;

    YangParserImpl(final BuildAction buildAction) {
        this.buildAction = requireNonNull(buildAction);
    }

    @Override
    public Collection<Class<? extends SchemaSourceRepresentation>> supportedSourceRepresentations() {
        return REPRESENTATIONS;
    }

    @Override
    public Set<QName> supportedStatements() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public YangParser addSource(final SchemaSourceRepresentation source) throws IOException, YangSyntaxErrorException {
        buildAction.addSources(sourceToStatementStream(source));
        return this;
    }

    @Override
    public YangParser addLibSource(final SchemaSourceRepresentation source) throws IOException,
            YangSyntaxErrorException {
        buildAction.addLibSources(sourceToStatementStream(source));
        return this;
    }

    @Override
    public YangParser setSupportedFeatures(final Set<QName> supportedFeatures) {
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
    public Map<QNameModule, ModuleEffectiveStatement> buildEffectiveModel() throws YangParserException {
        final List<EffectiveStatement<?, ?>> effectiveStatements;
        try {
            effectiveStatements = buildAction.buildEffective().getRootEffectiveStatements();
        } catch (ReactorException e) {
            throw decodeReactorException(e);
        }

        return effectiveStatements.stream()
                .filter(ModuleEffectiveStatement.class::isInstance).map(ModuleEffectiveStatement.class::cast)
                .collect(toImmutableMap(ModuleEffectiveStatement::localQNameModule, identity()));
    }

    @Override
    public SchemaContext buildSchemaContext() throws YangParserException {
        try {
            return buildAction.buildEffective();
        } catch (ReactorException e) {
            throw decodeReactorException(e);
        }
    }

    private static YangParserException decodeReactorException(final ReactorException reported) {
        // FIXME: map exception in some reasonable manner
        return new YangParserException("Failed to assemble sources", reported);
    }

    private static StatementStreamSource sourceToStatementStream(final SchemaSourceRepresentation source)
            throws IOException, YangSyntaxErrorException {
        requireNonNull(source);
        if (source instanceof ASTSchemaSource) {
            return YangStatementStreamSource.create((ASTSchemaSource) source);
        } else if (source instanceof YangTextSchemaSource) {
            return YangStatementStreamSource.create((YangTextSchemaSource) source);
        } else if (source instanceof YinDomSchemaSource) {
            return YinStatementStreamSource.create((YinDomSchemaSource) source);
        } else if (source instanceof YinTextSchemaSource) {
            try {
                return YinStatementStreamSource.create(YinTextToDomTransformer.transformSource(
                    (YinTextSchemaSource) source));
            } catch (SAXException e) {
                throw new YangSyntaxErrorException(source.getIdentifier(), 0, 0, "Failed to parse XML text", e);
            }
        } else if (source instanceof YinXmlSchemaSource) {
            try {
                return YinStatementStreamSource.create((YinXmlSchemaSource) source);
            } catch (TransformerException e) {
                throw new YangSyntaxErrorException(source.getIdentifier(), 0, 0,
                    "Failed to assemble in-memory representation", e);
            }
        } else {
            throw new IllegalArgumentException("Unsupported source " + source);
        }
    }
}
