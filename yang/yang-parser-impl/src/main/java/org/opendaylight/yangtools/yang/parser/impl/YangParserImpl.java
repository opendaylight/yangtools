/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.SetMultimap;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.parser.api.YangParser;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;

@Deprecated(since = "7.0.0", forRemoval = true)
final class YangParserImpl implements YangParser {
    private final DefaultYangParser delegate;

    YangParserImpl(final DefaultYangParser delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public @NonNull Set<QName> supportedStatements() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @NonNull Collection<Class<? extends SchemaSourceRepresentation>> supportedSourceRepresentations() {
        return delegate.supportedSourceRepresentations();
    }

    @Override
    public @NonNull YangParser addSource(final SchemaSourceRepresentation source) throws IOException,
            YangSyntaxErrorException {
        try {
            delegate.addSource(source);
        } catch (org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException e) {
            throw mapException(e);
        }
        return this;
    }

    @Override
    public @NonNull YangParser addLibSource(final SchemaSourceRepresentation source) throws IOException,
            YangSyntaxErrorException {
        try {
            delegate.addLibSource(source);
        } catch (org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException e) {
            throw mapException(e);
        }
        return this;
    }

    @Override
    public @NonNull YangParser setSupportedFeatures(final Set<QName> supportedFeatures) {
        delegate.setSupportedFeatures(supportedFeatures);
        return this;
    }

    @Override
    public @NonNull YangParser setModulesWithSupportedDeviations(
            final SetMultimap<QNameModule, QNameModule> modulesDeviatedByModules) {
        delegate.setModulesWithSupportedDeviations(modulesDeviatedByModules);
        return this;
    }

    @Override
    public @NonNull List<DeclaredStatement<?>> buildDeclaredModel() throws YangParserException {
        try {
            return delegate.buildDeclaredModel();
        } catch (org.opendaylight.yangtools.yang.parser.api.YangParserException e) {
            throw mapException(e);
        }
    }

    @Override
    public @NonNull EffectiveModelContext buildEffectiveModel() throws YangParserException {
        try {
            return delegate.buildEffectiveModel();
        } catch (org.opendaylight.yangtools.yang.parser.api.YangParserException e) {
            throw mapException(e);
        }
    }

    private static YangParserException mapException(
            final org.opendaylight.yangtools.yang.parser.api.YangParserException e) {
        return new YangParserException(e.getMessage(), e);
    }

    private static YangSyntaxErrorException mapException(
            final org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException e) {
        return new YangSyntaxErrorException(e.getSource().orElse(null), e.getLine(), e.getCharPositionInLine(),
            e.getMessage(), e);
    }
}
