/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.parser.impl.util;

import java.io.InputStream;
import java.util.HashMap;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.util.repo.AdvancedSchemaSourceProvider;
import org.opendaylight.yangtools.yang.model.util.repo.SourceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;

public abstract class YangSourceContextResolver {

    enum ResolutionState {
        MISSING_SOURCE,
        MISSING_DEPENDENCY,
        OTHER_ERROR,
        EVERYTHING_OK,
    }

    private static final Logger LOG = LoggerFactory.getLogger(YangSourceContextResolver.class);
    private final HashMap<SourceIdentifier, YangSourceContextResolver.ResolutionState> alreadyProcessed = new HashMap<>();
    private final ImmutableSet.Builder<SourceIdentifier> missingSources = ImmutableSet.builder();
    private final ImmutableMultimap.Builder<SourceIdentifier, ModuleImport> missingDependencies =
            ImmutableMultimap.builder();
    private final ImmutableSet.Builder<SourceIdentifier> validSources = ImmutableSet.builder();
    private final AdvancedSchemaSourceProvider<InputStream> sourceProvider;

    public YangSourceContextResolver() {
        sourceProvider = null;
    }

    public YangSourceContextResolver(final AdvancedSchemaSourceProvider<InputStream> sourceProvicer) {
        super();
        this.sourceProvider = sourceProvicer;
    }

    public abstract YangSourceContext resolveContext();
    public abstract Optional<YangModelDependencyInfo> getDependencyInfo(SourceIdentifier identifier);

    public AdvancedSchemaSourceProvider<InputStream> getSourceProvider() {
        return sourceProvider;
    }

    public YangSourceContextResolver.ResolutionState resolveSource(final String name, final Optional<String> formattedRevision) {
        return resolveSource(new SourceIdentifier(name, formattedRevision));
    }

    public YangSourceContextResolver.ResolutionState resolveSource(final SourceIdentifier identifier) {

        if (alreadyProcessed.containsKey(identifier)) {
            return alreadyProcessed.get(identifier);
        }
        LOG.trace("Resolving source: {}", identifier);
        YangSourceContextResolver.ResolutionState potentialState = YangSourceContextResolver.ResolutionState.EVERYTHING_OK;
        try {
            Optional<YangModelDependencyInfo> potentialInfo = getDependencyInfo(identifier);
            if (potentialInfo.isPresent()) {
                YangModelDependencyInfo info = potentialInfo.get();
                checkValidSource(identifier, info);
                for (ModuleImport dependency : info.getDependencies()) {
                    LOG.trace("Source: {} Resolving dependency: {}", identifier, dependency);
                    YangSourceContextResolver.ResolutionState dependencyState = resolveDependency(dependency);
                    if (dependencyState != YangSourceContextResolver.ResolutionState.EVERYTHING_OK) {
                        potentialState = YangSourceContextResolver.ResolutionState.MISSING_DEPENDENCY;
                        missingDependencies.put(identifier, dependency);
                    }
                }
            } else {
                missingSources.add(identifier);
                return YangSourceContextResolver.ResolutionState.MISSING_SOURCE;
            }
        } catch (Exception e) {
            potentialState = YangSourceContextResolver.ResolutionState.OTHER_ERROR;
        }
        updateResolutionState(identifier, potentialState);
        return potentialState;
    }

    private boolean checkValidSource(final SourceIdentifier identifier, final YangModelDependencyInfo info) {
        if (!identifier.getName().equals(info.getName())) {
            LOG.warn("Incorrect model returned. Identifier name was: {}, source contained: {}",
                    identifier.getName(), info.getName());
            throw new IllegalStateException("Incorrect source was returned");
        }
        return true;
    }

    private void updateResolutionState(final SourceIdentifier identifier, final YangSourceContextResolver.ResolutionState potentialState) {
        alreadyProcessed.put(identifier, potentialState);
        switch (potentialState) {
        case MISSING_SOURCE:
            missingSources.add(identifier);
            break;
        case EVERYTHING_OK:
            validSources.add(identifier);
            break;
        default:
            break;
        }
    }

    private YangSourceContextResolver.ResolutionState resolveDependency(final ModuleImport dependency) {
        String name = dependency.getModuleName();
        Optional<String> formattedRevision = Optional
                .fromNullable(QName.formattedRevision(dependency.getRevision()));
        return resolveSource(new SourceIdentifier(name, formattedRevision));
    }

    protected YangSourceContext createSourceContext() {
        ImmutableSet<SourceIdentifier> missingSourcesSet = missingSources.build();
        ImmutableMultimap<SourceIdentifier, ModuleImport> missingDependenciesMap = missingDependencies.build();
        ImmutableSet<SourceIdentifier> validSourcesSet = validSources.build();
        return new YangSourceContext(validSourcesSet, missingSourcesSet, missingDependenciesMap, sourceProvider);
    }
}
