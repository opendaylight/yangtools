/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static org.opendaylight.yangtools.yang2sources.plugin.YangToSourcesProcessor.LOG_PREFIX;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.maven.RepositoryUtils;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.opendaylight.yangtools.plugin.generator.api.FileGenerator;
import org.opendaylight.yangtools.plugin.generator.api.ModuleResourceResolver;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Generate sources from yang files using user provided set of
 * {@link FileGeneratorArg}s. Steps of this process:
 * <ol>
 *   <li>List yang files from {@link #yangFilesRootDir}</li>
 *   <li>Process yang files using Yang Parser</li>
 *   <li>For each {@link FileGeneratorArg} from {@link #fileGenerators}:
 *     <ol>
 *       <li>Instantiate using default constructor</li>
 *       <li>Call {@link FileGenerator#generateFiles(EffectiveModelContext, Set, ModuleResourceResolver)}</li>
 *     </ol>
 *   </li>
 * </ol>
 */
@Mojo(name = "generate-sources", defaultPhase = LifecyclePhase.GENERATE_SOURCES,
    requiresDependencyResolution = ResolutionScope.COMPILE, requiresProject = true, threadSafe = true)
public final class YangToSourcesMojo extends AbstractMojo {
    public static final String PLUGIN_NAME = "org.opendaylight.yangtools:yang-maven-plugin";

    private static final Logger LOG = LoggerFactory.getLogger(YangToSourcesMojo.class);
    private static final DependencyFilter DEP_FILTER = DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE);

    /**
     * {@link FileGenerator} instances resolved via ServiceLoader can hold additional configuration, which details
     * how they are executed.
     */
    @Parameter(required = false)
    private FileGeneratorArg[] fileGenerators;

    /**
     * Source directory that will be recursively searched for yang files (ending
     * with .yang suffix).
     */
    @Parameter(required = false)
    // defaults to ${basedir}/src/main/yang
    private String yangFilesRootDir;

    @Parameter(required = false)
    private String[] excludeFiles;

    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;

    @Parameter(property = "inspectDependencies")
    private boolean inspectDependencies;

    // When set to "true", then the execution of the plugin is disabled
    @Parameter(property = "yang.skip", defaultValue = "false")
    private boolean yangSkip;

    @Component
    private BuildContext buildContext;

    @Component
    private RepositorySystem repoSystem;

    @Parameter(readonly = true, defaultValue = "${repositorySystemSession}")
    private RepositorySystemSession repoSession;

    public YangToSourcesMojo() {

    }

    @Override
    @SuppressFBWarnings(value = "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", justification = "yangFilesRootDir")
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (yangSkip) {
            LOG.info("{} Skipping YANG code generation because property yang.skip is true",
                YangToSourcesProcessor.LOG_PREFIX);
            return;
        }

        checkClasspath(project, repoSystem, repoSession);
        // defaults to ${basedir}/src/main/yang
        File yangFilesRootFile = processYangFilesRootDir(yangFilesRootDir, project.getBasedir());
        Collection<File> excludedFiles = processExcludeFiles(excludeFiles, yangFilesRootFile);

        new YangToSourcesProcessor(buildContext, yangFilesRootFile, excludedFiles, arrayToList(fileGenerators), project,
            inspectDependencies).execute();
    }

    /**
     * Read current project dependencies and check if it don't grab incorrect
     * artifacts versions which could be in conflict with plugin dependencies.
     *
     * @param project current project
     * @param repoSystem repository system
     */
    @VisibleForTesting
    static void checkClasspath(final MavenProject project, final RepositorySystem repoSystem,
            final RepositorySystemSession repoSession) {
        final var plugin = project.getPlugin(YangToSourcesMojo.PLUGIN_NAME);
        if (plugin == null) {
            LOG.warn("{} {} not found, dependencies version check skipped", LOG_PREFIX, YangToSourcesMojo.PLUGIN_NAME);
            return;
        }

        final var projectDependencies = project.getDependencyArtifacts();
        for (var entry : getPluginTransitiveDependencies(plugin, repoSystem, repoSession,
                RepositoryUtils.toRepos(project.getRemoteArtifactRepositories())).entrySet()) {
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
     * @param remoteRepos list of remote repositories
     * @return a Map of transitive dependencies
     */
    private static Map<Artifact, Set<Artifact>> getPluginTransitiveDependencies(final Plugin plugin,
            final RepositorySystem repoSystem, final RepositorySystemSession repoSession,
            final List<RemoteRepository> remoteRepos) {
        final var ret = new HashMap<Artifact, Set<Artifact>>();

        for (var dep : plugin.getDependencies()) {
            final var aetherDep = RepositoryUtils.toDependency(dep, repoSession.getArtifactTypeRegistry());
            final var collectRequest = new CollectRequest();
            collectRequest.setRoot(aetherDep);
            collectRequest.setRepositories(remoteRepos);

            final var request = new DependencyRequest(collectRequest, DEP_FILTER);
            final DependencyResult result;
            try {
                result = repoSystem.resolveDependencies(repoSession, request);
            } catch (DependencyResolutionException e) {
                throw new IllegalStateException(e);
            }

            ret.put(aetherDep.getArtifact(),
                result.getArtifactResults().stream().map(ArtifactResult::getArtifact).collect(Collectors.toSet()));
        }
        return ret;
    }

    /**
     * Check artifact against collection of dependencies. If collection contains artifact with same groupId and
     * artifactId, but different version, logs a warning.
     *
     * @param artifact artifact to check
     * @param dependencies collection of dependencies
     */
    private static void checkArtifact(final Artifact artifact,
            final Set<org.apache.maven.artifact.Artifact> dependencies) {
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


    private static <T> List<T> arrayToList(final T[] array) {
        return array == null ? ImmutableList.of() : Arrays.asList(array);
    }

    private static File processYangFilesRootDir(final String yangFilesRootDir, final File baseDir) {
        File yangFilesRootFile;
        if (yangFilesRootDir == null) {
            yangFilesRootFile = new File(baseDir, "src" + File.separator + "main" + File.separator + "yang");
        } else {
            File file = new File(yangFilesRootDir);
            if (file.isAbsolute()) {
                yangFilesRootFile = file;
            } else {
                yangFilesRootFile = new File(baseDir, file.getPath());
            }
        }
        return yangFilesRootFile;
    }

    private static Collection<File> processExcludeFiles(final String[] excludeFiles, final File baseDir) {
        if (excludeFiles == null) {
            return ImmutableList.of();
        }

        return Collections2.transform(Arrays.asList(excludeFiles), f -> new File(baseDir, f));
    }
}
