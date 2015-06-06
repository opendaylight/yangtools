/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.parser.impl.util;

import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.concurrent.NotThreadSafe;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.util.repo.AdvancedSchemaSourceProvider;
import org.opendaylight.yangtools.yang.model.util.repo.SchemaSourceProvider;
import org.opendaylight.yangtools.yang.model.util.repo.SourceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Resolution task for YANG Source Context
 *
 * {@link YangSourceContextResolver} and its subclasses are responsible for
 * resolving {@link YangSourceContext} based on provided
 * {@link SchemaSourceProvider} and set of modules to process.
 *
 *
 * <h3>Implementation notes</h3>
 *
 * In order to customize resolution of {@link YangSourceContext} implementators
 * of this class are required to implement following methods:
 * <ul>
 * <li>{@link #getDependencyInfo(SourceIdentifier)} - Retrieval of dependency
 * information</li>
 * <li>{@link #resolveContext()} - Main resolution algorithm
 * <li>
 * </ul>
 *
 * This abstract class provides utility methods for implementators which may be
 * used in {@link #resolveContext()} to create {@link YangSourceContext}:
 * <ul>
 * <li>{@link #resolveSource(SourceIdentifier)} and
 * {@link #resolveSource(String, Optional)} - Tries to resolve state for
 * supplied model identifier and updates internal state. If state was not
 * already resolved for identifier it invokes
 * {@link #getDependencyInfo(SourceIdentifier)} for particular identifier. This
 * method is recursively invoked for all dependencies.</li>
 * <li>{@link #createSourceContext()} - Creates {@link YangSourceContext} based
 * on previous invocations of {@link #resolveSource(SourceIdentifier)} methods.</li>
 * </ul>
 *
 */
@Deprecated
@NotThreadSafe
public abstract class YangSourceContextResolver {

    /**
     *
     * State of source code resolution
     *
     */
    public enum ResolutionState {
        /**
         *
         * Source was missing during source resolution
         *
         */
        MISSING_SOURCE,
        /**
         *
         * One or multiple of dependencies of source are missing
         *
         */
        MISSING_DEPENDENCY,
        /**
         * Other error ocurred during resolution
         *
         */
        OTHER_ERROR,
        /**
         * Source, its dependencies and its transient dependencies
         * are resolved.
         *
         */
        EVERYTHING_OK,
    }

    private static final Logger LOG = LoggerFactory.getLogger(YangSourceContextResolver.class);
    private final Map<SourceIdentifier, YangSourceContextResolver.ResolutionState> alreadyProcessed = new HashMap<>();
    private final ImmutableSet.Builder<SourceIdentifier> missingSources = ImmutableSet.builder();
    private final ImmutableMultimap.Builder<SourceIdentifier, ModuleImport> missingDependencies = ImmutableMultimap
            .builder();
    private final ImmutableSet.Builder<SourceIdentifier> validSources = ImmutableSet.builder();
    private final AdvancedSchemaSourceProvider<InputStream> sourceProvider;

    public YangSourceContextResolver(final AdvancedSchemaSourceProvider<InputStream> sourceProvider) {
        this.sourceProvider = checkNotNull(sourceProvider, "Missing sourceProvider");
    }

    /**
     * Resolves {@link YangSourceContext}
     *
     * Implementators of this method should invoke
     * {@link #resolveSource(SourceIdentifier)} for sources which should be
     * present in {@link YangSourceContext} and {@link #createSourceContext()}
     * to create resulting {@link YangSourceContext} which will contain state
     * derived by callbacks to {@link #getDependencyInfo(SourceIdentifier)}.
     *
     * @return Resolved {@link YangSourceContext}.
     */
    public abstract YangSourceContext resolveContext();

    /**
     * Returns dependency information for provided identifier
     *
     * Implementations are required to:
     * <ul>
     * <li>return {@link Optional#absent()} If source code for source is not
     * present</li>
     * <li>return same dependency information for multiple invocations of this
     * method for same source identifier.</li>
     * <li>return latest available revision if {@link SourceIdentifier} does not
     * specify revision. If no revision is available {@link Optional#absent()}
     * MUST be returned.</li>
     * </ul>
     *
     *
     * Internal state of this object (and resulting {@link YangSourceContext}
     * will be updated as following:
     * <ul>
     * <li>If {@link Optional#absent()} is returned:
     * <ul>
     * <li>source will be marked as {@link ResolutionState#MISSING_SOURCE} and
     * source identifier will be contained in -
     * {@link YangSourceContext#getMissingSources()}</li>
     * <li>All sources which imported or included this source will be present in
     * {@link YangSourceContext#getMissingDependencies()}</li>
     * </ul>
     * </li></ul>
     *
     *
     * @param identifier
     *            Source identifier
     * @return Dependency Information for {@link SourceIdentifier},
     *         {@link Optional#absent()} if no source is present.
     */
    abstract Optional<YangModelDependencyInfo> getDependencyInfo(SourceIdentifier identifier);

    /**
     * Return Source provider against which YANG source context was computed
     *
     * @return Source provider against which YANG source context was computed or null, if source provider
     *   is not associated with computation.
     */
    public AdvancedSchemaSourceProvider<InputStream> getSourceProvider() {
        return sourceProvider;
    }

    /**
     *
     * Resolves resolution state for provided name and formated revision
     *
     * This method is shorthand for {@link #resolveSource(SourceIdentifier)}
     * with argument <code>new SourceIdentifier(name, formattedRevision)</code>
     *
     * @see #resolveSource(SourceIdentifier)
     * @param name
     *            Name of YANG model
     * @param formattedRevision
     *            revision of YANG model
     * @return Resolution context of YANG Source
     */
    public final YangSourceContextResolver.ResolutionState resolveSource(final String name,
            final Optional<String> formattedRevision) {
        return resolveSource(new SourceIdentifier(name, formattedRevision));
    }

    /**
     * Resolves state of source and updates internal state accordingly.
     *
     * <p>
     * Resolves state of source and updates internal state based on resolution.
     * This method tries to get module dependency info via user implementation
     * of {@link #getDependencyInfo(SourceIdentifier)} and then is recursively
     * called for each announced dependency in
     * {@link YangModelDependencyInfo#getDependencies()}.
     *
     * <p>
     * Resolution state of resolveSource is internally cached and is used in
     * subsequent resolution of dependent modules and in creation of
     * YANGSourceContext via {@link #createSourceContext()}.
     *
     * <p>
     * Possible resolution state for sources are:
     * <ul>
     * <li>{@link ResolutionState#EVERYTHING_OK} - If sources for module and its
     * dependencies are available</li>
     * <li>{@link ResolutionState#MISSING_DEPENDENCY} - If dependency of source
     * is missing (call to {@link #getDependencyInfo(SourceIdentifier)} for
     * imported / included model returned returned {@link Optional#absent()}.</li>
     * <li>{@link ResolutionState#MISSING_SOURCE} - If source is missing. (call
     * of {@link #getDependencyInfo(SourceIdentifier)} returned
     * {@link Optional#absent()}.</li>
     * <li>{@link ResolutionState#OTHER_ERROR} - If other runtime error
     * prevented resolution of informations.</li>
     * </ul>
     *
     * Note: Multiple invocations of this method returns cached result, since
     * {@link #getDependencyInfo(SourceIdentifier)} contract requires
     * implementors to return same information during life of this object.
     *
     *
     * @param identifier
     *            Source Identifier
     * @return Returns resolution state for source.
     */
    public final YangSourceContextResolver.ResolutionState resolveSource(final SourceIdentifier identifier) {

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

    private static boolean checkValidSource(final SourceIdentifier identifier, final YangModelDependencyInfo info) {
        if (!identifier.getName().equals(info.getName())) {
            LOG.warn("Incorrect model returned. Identifier name was: {}, source contained: {}", identifier.getName(),
                    info.getName());
            throw new IllegalStateException("Incorrect source was returned");
        }
        return true;
    }

    private void updateResolutionState(final SourceIdentifier identifier,
            final YangSourceContextResolver.ResolutionState potentialState) {
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
        Optional<String> formattedRevision = Optional.fromNullable(QName.formattedRevision(dependency.getRevision()));
        return resolveSource(new SourceIdentifier(name, formattedRevision));
    }

    protected YangSourceContext createSourceContext() {
        ImmutableSet<SourceIdentifier> missingSourcesSet = missingSources.build();
        ImmutableMultimap<SourceIdentifier, ModuleImport> missingDependenciesMap = missingDependencies.build();
        ImmutableSet<SourceIdentifier> validSourcesSet = validSources.build();
        return new YangSourceContext(validSourcesSet, missingSourcesSet, missingDependenciesMap, sourceProvider);
    }
}
