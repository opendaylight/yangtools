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
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.maven.RepositoryUtils;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
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
        for (var element : project.getArtifacts()) {
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
            final RepositorySystemSession repoSession) {
        final var plugin = project.getPlugin(YangToSourcesMojo.PLUGIN_NAME);
        if (plugin == null) {
            LOG.warn("{} {} not found, dependencies version check skipped", LOG_PREFIX, YangToSourcesMojo.PLUGIN_NAME);
            return;
        }

        final var remoteRepos = RepositoryUtils.toRepos(project.getRemoteArtifactRepositories());
        final var pluginDeps = new HashMap<Artifact, Set<Artifact>>();
        for (var mavenDep : plugin.getDependencies()) {
            final var aetherDep = RepositoryUtils.toDependency(mavenDep, repoSession.getArtifactTypeRegistry());
            final var collectRequest = new CollectRequest();
            collectRequest.setRoot(aetherDep);
            collectRequest.setRepositories(remoteRepos);

            final var request = new DependencyRequest(collectRequest, null);
            final DependencyResult result;
            try {
                result = repoSystem.resolveDependencies(repoSession, request);
            } catch (DependencyResolutionException e) {
                throw new IllegalStateException(e);
            }

            pluginDeps.put(aetherDep.getArtifact(),
                result.getArtifactResults().stream().map(ArtifactResult::getArtifact).collect(Collectors.toSet()));
        }

        final var projectDependencies = RepositoryUtils.toArtifacts(project.getDependencyArtifacts());
        for (var entry : pluginDeps.entrySet()) {
            checkArtifact(entry.getKey(), projectDependencies);
            for (var dependency : entry.getValue()) {
                checkArtifact(dependency, projectDependencies);
            }
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
        for (var d : dependencies) {
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
