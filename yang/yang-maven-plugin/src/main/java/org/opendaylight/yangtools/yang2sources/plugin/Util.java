/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static org.opendaylight.yangtools.yang.common.YangConstants.RFC6020_YANG_FILE_EXTENSION;
import static org.opendaylight.yangtools.yang2sources.plugin.YangToSourcesProcessor.LOG_PREFIX;
import static org.opendaylight.yangtools.yang2sources.plugin.YangToSourcesProcessor.META_INF_YANG_STRING;
import static org.opendaylight.yangtools.yang2sources.plugin.YangToSourcesProcessor.META_INF_YANG_STRING_JAR;

import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class Util {

    /**
     * It isn't desirable to create instances of this class.
     */
    private Util() {
    }

    static final String YANG_SUFFIX = "yang";

    private static final Logger LOG = LoggerFactory.getLogger(Util.class);

    static Collection<File> listFiles(final File root, final Collection<File> excludedFiles)
            throws FileNotFoundException {
        if (!root.exists()) {
            LOG.warn("{} YANG source directory {} not found. No code will be generated.", LOG_PREFIX, root);

            return Collections.emptyList();
        }
        Collection<File> result = new ArrayList<>();
        Collection<File> yangFiles = FileUtils.listFiles(root, new String[] { YANG_SUFFIX }, true);
        for (File f : yangFiles) {
            if (excludedFiles.contains(f)) {
                LOG.info("{} {} file excluded {}", LOG_PREFIX, Util.YANG_SUFFIX.toUpperCase(), f);
            } else {
                result.add(f);
            }
        }

        return result;
    }

    static List<File> getClassPath(final MavenProject project) {
        List<File> dependencies = new ArrayList<>();
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
            if (artifact.getGroupId().equals(d.getGroupId()) && artifact.getArtifactId().equals(d.getArtifactId())) {
                if (!(artifact.getVersion().equals(d.getVersion()))) {
                    LOG.warn("{} Dependency resolution conflict:", LOG_PREFIX);
                    LOG.warn("{} '{}' dependency [{}] has different version than one declared in current project [{}]"
                            + ". It is recommended to fix this problem because it may cause compilation errors.",
                            LOG_PREFIX, YangToSourcesMojo.PLUGIN_NAME, artifact, d);
                }
            }
        }
    }

    private static boolean isJar(final File element) {
        return element.isFile() && element.getName().endsWith(".jar");
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    static List<YangTextSchemaSource> findYangFilesInDependenciesAsStream(final MavenProject project)
            throws MojoFailureException {
        try {
            final List<File> filesOnCp = Util.getClassPath(project);
            LOG.info("{} Searching for yang files in following dependencies: {}", LOG_PREFIX, filesOnCp);

            final List<YangTextSchemaSource> yangsFromDependencies = new ArrayList<>();
            for (File file : filesOnCp) {
                final List<String> foundFilesForReporting = new ArrayList<>();
                // is it jar file or directory?
                if (file.isDirectory()) {
                    //FIXME: code duplicate
                    File yangDir = new File(file, META_INF_YANG_STRING);
                    if (yangDir.exists() && yangDir.isDirectory()) {
                        File[] yangFiles = yangDir.listFiles(
                            (dir, name) -> name.endsWith(RFC6020_YANG_FILE_EXTENSION) && new File(dir, name).isFile());
                        for (final File yangFile : yangFiles) {
                            foundFilesForReporting.add(yangFile.getName());
                            yangsFromDependencies.add(YangTextSchemaSource.forFile(yangFile));
                        }
                    }
                } else {
                    try (ZipFile zip = new ZipFile(file)) {
                        final Enumeration<? extends ZipEntry> entries = zip.entries();
                        while (entries.hasMoreElements()) {
                            final ZipEntry entry = entries.nextElement();
                            final String entryName = entry.getName();

                            if (entryName.startsWith(META_INF_YANG_STRING_JAR) && !entry.isDirectory()
                                    && entryName.endsWith(RFC6020_YANG_FILE_EXTENSION)) {
                                foundFilesForReporting.add(entryName);

                                yangsFromDependencies.add(YangTextSchemaSource.delegateForByteSource(
                                    entryName.substring(entryName.lastIndexOf('/') + 1),
                                    ByteSource.wrap(ByteStreams.toByteArray(zip.getInputStream(entry)))));
                            }
                        }
                    }
                }
                if (foundFilesForReporting.size() > 0) {
                    LOG.info("{} Found {} yang files in {}: {}", LOG_PREFIX, foundFilesForReporting.size(), file,
                        foundFilesForReporting);
                }

            }

            return yangsFromDependencies;
        } catch (Exception e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }

    /**
     * Find all dependencies which contains yang sources.
     * Returns collection of YANG files and Zip files which contains YANG files.
     *
     */
    //  FIXME: Rename to what class is actually doing.
    @SuppressWarnings("checkstyle:illegalCatch")
    static Collection<File> findYangFilesInDependencies(final MavenProject project) throws MojoFailureException {
        final List<File> yangsFilesFromDependencies = new ArrayList<>();

        final List<File> filesOnCp;
        try {
            filesOnCp = Util.getClassPath(project);
        } catch (Exception e) {
            throw new MojoFailureException("Failed to scan for YANG files in dependencies", e);
        }
        LOG.info("{} Searching for yang files in following dependencies: {}", LOG_PREFIX, filesOnCp);

        for (File file : filesOnCp) {
            try {
                // is it jar file or directory?
                if (file.isDirectory()) {
                    //FIXME: code duplicate
                    File yangDir = new File(file, META_INF_YANG_STRING);
                    if (yangDir.exists() && yangDir.isDirectory()) {
                        File[] yangFiles = yangDir.listFiles(
                            (dir, name) -> name.endsWith(RFC6020_YANG_FILE_EXTENSION) && new File(dir, name).isFile());

                        yangsFilesFromDependencies.addAll(Arrays.asList(yangFiles));
                    }
                } else {
                    try (ZipFile zip = new ZipFile(file)) {

                        final Enumeration<? extends ZipEntry> entries = zip.entries();
                        while (entries.hasMoreElements()) {
                            ZipEntry entry = entries.nextElement();
                            String entryName = entry.getName();

                            if (entryName.startsWith(META_INF_YANG_STRING_JAR)
                                    && !entry.isDirectory() && entryName.endsWith(RFC6020_YANG_FILE_EXTENSION)) {
                                LOG.debug("{} Found a YANG file in {}: {}", LOG_PREFIX, file, entryName);
                                yangsFilesFromDependencies.add(file);
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new MojoFailureException("Failed to scan for YANG files in dependency: " + file.toString(), e);
            }
        }
        return yangsFilesFromDependencies;
    }

    static SourceIdentifier moduleToIdentifier(final Module module) {
        final QNameModule mod = module.getQNameModule();
        final Date rev = mod.getRevision();
        final com.google.common.base.Optional<String> optRev;
        if (SimpleDateFormatUtil.DEFAULT_DATE_REV.equals(rev)) {
            optRev = com.google.common.base.Optional.absent();
        } else {
            optRev = com.google.common.base.Optional.of(mod.getFormattedRevision());
        }

        return RevisionSourceIdentifier.create(module.getName(), optRev);
    }
}
