/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.parser.api.YangParser;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;

final class YangParserImpl implements YangParser {
    private final BuildAction buildAction;

    YangParserImpl(final BuildAction buildAction) {
        this.buildAction = requireNonNull(buildAction);
    }

    @Override
    public Collection<Class<? extends SchemaSourceRepresentation>> supportedSourceRepresentations() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<QName> supportedStatements() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public YangParser addSource(final SchemaSourceRepresentation source) throws IOException, YangSyntaxErrorException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public YangParser addLibSource(final SchemaSourceRepresentation source) throws IOException,
            YangSyntaxErrorException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public YangParser setSupportedFeatures(final Set<QName> supportedFeatures) {
        buildAction.setSupportedFeatures(supportedFeatures);
        return this;
    }

    @Override
    public YangParser setModulesWithSupportedDeviations(
            final Map<QNameModule, Set<QNameModule>> modulesDeviatedByModules) {
        buildAction.setModulesWithSupportedDeviations(modulesDeviatedByModules);
        return this;
    }

    @Override
    public List<DeclaredStatement<?>> buildDeclaredModel() throws YangParserException {
        return buildAction.build().getRootStatements();
    }

    @Override
    public SchemaContext buildSchemaContext() throws YangParserException {
        return buildAction.buildEffective();
    }
}
