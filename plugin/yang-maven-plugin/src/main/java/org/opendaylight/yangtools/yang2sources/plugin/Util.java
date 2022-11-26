/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static org.opendaylight.yangtools.yang2sources.plugin.YangToSourcesProcessor.LOG_PREFIX;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.ModuleLike;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class Util {

    /**
     * It isn't desirable to create instances of this class.
     */
    private Util() {
    }

    private static final Logger LOG = LoggerFactory.getLogger(Util.class);

    static List<File> getClassPath(final MavenProject project) {
        final List<File> dependencies = new ArrayList<>();
        for (Artifact element : project.getArtifacts()) {
            File asFile = element.getFile();
            if (isJar(asFile) || asFile.isDirectory()) {
                dependencies.add(asFile);
            }
        }
        return dependencies;
    }

    /**
     * Read current project dependencies and check if it don't grab incorrect
     * artifacts versions which could be in conflict with plugin dependencies.
     *
     * @param project
     *            current project
     * @param repoSystem
     *            repository system
     * @param localRepo
     *            local repository
     * @param remoteRepos
     *            remote repositories
     */
    static void checkClasspath(final MavenProject project, final RepositorySystem repoSystem,
            final ArtifactRepository localRepo, final List<ArtifactRepository> remoteRepos) {
        Plugin plugin = project.getPlugin(YangToSourcesMojo.PLUGIN_NAME);
        if (plugin == null) {
            LOG.warn("{} {} not found, dependencies version check skipped", LOG_PREFIX, YangToSourcesMojo.PLUGIN_NAME);
        } else {
            Map<Artifact, Collection<Artifact>> pluginDependencies = new HashMap<>();
            getPluginTransitiveDependencies(plugin, pluginDependencies, repoSystem, localRepo, remoteRepos);

            Set<Artifact> projectDependencies = project.getDependencyArtifacts();
            for (Map.Entry<Artifact, Collection<Artifact>> entry : pluginDependencies.entrySet()) {
                checkArtifact(entry.getKey(), projectDependencies);
                for (Artifact dependency : entry.getValue()) {
                    checkArtifact(dependency, projectDependencies);
                }
            }
        }
    }

    /**
     * Read transitive dependencies of given plugin and store them in map.
     *
     * @param plugin
     *            plugin to read
     * @param map
     *            map, where founded transitive dependencies will be stored
     * @param repoSystem
     *            repository system
     * @param localRepository
     *            local repository
     * @param remoteRepos
     *            list of remote repositories
     */
    private static void getPluginTransitiveDependencies(final Plugin plugin,
            final Map<Artifact, Collection<Artifact>> map, final RepositorySystem repoSystem,
            final ArtifactRepository localRepository, final List<ArtifactRepository> remoteRepos) {

        List<Dependency> pluginDependencies = plugin.getDependencies();
        for (Dependency dep : pluginDependencies) {
            Artifact artifact = repoSystem.createDependencyArtifact(dep);

            ArtifactResolutionRequest request = new ArtifactResolutionRequest();
            request.setArtifact(artifact);
            request.setResolveTransitively(true);
            request.setLocalRepository(localRepository);
            request.setRemoteRepositories(remoteRepos);

            ArtifactResolutionResult result = repoSystem.resolve(request);
            Set<Artifact> pluginDependencyDependencies = result.getArtifacts();
            map.put(artifact, pluginDependencyDependencies);
        }
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
    private static void checkArtifact(final Artifact artifact, final Collection<Artifact> dependencies) {
        for (org.apache.maven.artifact.Artifact d : dependencies) {
            if (artifact.getGroupId().equals(d.getGroupId()) && artifact.getArtifactId().equals(d.getArtifactId())
                && !artifact.getVersion().equals(d.getVersion())) {
                LOG.warn("{} Dependency resolution conflict:", LOG_PREFIX);
                LOG.warn("{} '{}' dependency [{}] has different version than one declared in current project [{}]"
                    + ". It is recommended to fix this problem because it may cause compilation errors.",
                    LOG_PREFIX, YangToSourcesMojo.PLUGIN_NAME, artifact, d);
            }
        }
    }

    private static boolean isJar(final File element) {
        return element.isFile() && element.getName().endsWith(".jar");
    }

    static SourceIdentifier moduleToIdentifier(final ModuleLike module) {
        return new SourceIdentifier(Unqualified.of(module.getName()), module.getRevision().orElse(null));
    }
}
