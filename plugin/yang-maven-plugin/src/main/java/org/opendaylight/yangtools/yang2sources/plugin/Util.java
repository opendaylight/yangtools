/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static org.opendaylight.yangtools.yang2sources.plugin.YangToSourcesProcessor.LOG_PREFIX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.ModuleLike;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class Util {
    private static final Logger LOG = LoggerFactory.getLogger(Util.class);

    private Util() {
        // Hidden on purpose
    }

    static @NonNull SourceIdentifier moduleToIdentifier(final ModuleLike module) {
        return new SourceIdentifier(Unqualified.of(module.getName()), module.getRevision().orElse(null));
    }

    /**
     * Read current project dependencies and check if it don't grab incorrect
     * artifacts versions which could be in conflict with plugin dependencies.
     *
     * @param project current project
     * @param repoSystem repository system
     * @param localRepo local repository
     * @param remoteRepos remote repositories
     */
    static void checkClasspath(final MavenProject project, final RepositorySystem repoSystem,
            final ArtifactRepository localRepo, final List<ArtifactRepository> remoteRepos) {
        final var plugin = project.getPlugin(YangToSourcesMojo.PLUGIN_NAME);
        if (plugin == null) {
            LOG.warn("{} {} not found, dependencies version check skipped", LOG_PREFIX, YangToSourcesMojo.PLUGIN_NAME);
            return;
        }

        final var projectDependencies = project.getDependencyArtifacts();
        for (var entry : getPluginTransitiveDependencies(plugin, repoSystem, localRepo, remoteRepos).entrySet()) {
            checkArtifact(entry.getKey(), projectDependencies);
            for (var dependency : entry.getValue()) {
                checkArtifact(dependency, projectDependencies);
            }
        }
    }

    /**
     * Read transitive dependencies of given plugin and store them in map.
     *
     * @param plugin plugin to read
     * @param repoSystem repository system
     * @param localRepository local repository
     * @param remoteRepos list of remote repositories
     * @return a Map of transitive dependencies
     */
    private static Map<Artifact, Set<Artifact>> getPluginTransitiveDependencies(final Plugin plugin,
            final RepositorySystem repoSystem, final ArtifactRepository localRepository,
            final List<ArtifactRepository> remoteRepos) {
        final var ret = new HashMap<Artifact, Set<Artifact>>();
        for (var dep : plugin.getDependencies()) {
            final var artifact = repoSystem.createDependencyArtifact(dep);

            final var request = new ArtifactResolutionRequest();
            request.setArtifact(artifact);
            request.setResolveTransitively(true);
            request.setLocalRepository(localRepository);
            request.setRemoteRepositories(remoteRepos);

            ret.put(artifact, repoSystem.resolve(request).getArtifacts());
        }
        return ret;
    }

    /**
     * Check artifact against collection of dependencies. If collection contains
     * artifact with same groupId and artifactId, but different version, logs a
     * warning.
     *
     * @param artifact
     *            artifact to check
     * @param dependencies
     *            collection of dependencies
     */
    private static void checkArtifact(final Artifact artifact, final Set<Artifact> dependencies) {
        for (var dep : dependencies) {
            if (artifact.getGroupId().equals(dep.getGroupId()) && artifact.getArtifactId().equals(dep.getArtifactId())
                && !artifact.getVersion().equals(dep.getVersion())) {
                LOG.warn("{} Dependency resolution conflict:", LOG_PREFIX);
                LOG.warn("{} '{}' dependency [{}] has different version than one declared in current project [{}]"
                    + ". It is recommended to fix this problem because it may cause compilation errors.",
                    LOG_PREFIX, YangToSourcesMojo.PLUGIN_NAME, artifact, dep);
            }
        }
    }
}
