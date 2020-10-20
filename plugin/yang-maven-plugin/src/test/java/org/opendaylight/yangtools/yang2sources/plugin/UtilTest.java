/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class UtilTest {

    @Test
    public void getClassPathTest() {
        final MavenProject project = mock(MavenProject.class);
        final File file = mock(File.class);
        final File file2 = mock(File.class);
        final Artifact artifact = mock(Artifact.class);
        final Artifact artifact2 = mock(Artifact.class);

        final Set<Artifact> artifacts = new HashSet<>();
        artifacts.add(artifact);
        artifacts.add(artifact2);

        when(project.getArtifacts()).thenReturn(artifacts);
        when(artifact.getFile()).thenReturn(file);
        when(file.isFile()).thenReturn(true);
        when(file.getName()).thenReturn("iamjar.jar");
        when(artifact2.getFile()).thenReturn(file2);
        when(file2.isDirectory()).thenReturn(true);

        final List<File> files = Util.getClassPath(project);
        assertEquals(2, files.size());
        assertTrue(files.contains(file) && files.contains(file2));
    }

    @Test
    public void checkClasspathTest() throws Exception {
        final MavenProject project = mock(MavenProject.class);
        final Plugin plugin = mock(Plugin.class);
        final RepositorySystem repoSystem = mock(RepositorySystem.class);
        final ArtifactRepository localRepo = mock(ArtifactRepository.class);
        final ArtifactResolutionResult artifactResolResult = mock(ArtifactResolutionResult.class);
        final Artifact artifact = mock(Artifact.class);
        final Dependency dep = mock(Dependency.class);

        final List<ArtifactRepository> remoteRepos = new ArrayList<>();
        remoteRepos.add(localRepo);

        final Set<Artifact> artifacts = new HashSet<>();
        artifacts.add(artifact);

        final List<Dependency> listDepcy = new ArrayList<>();
        listDepcy.add(dep);

        when(project.getPlugin(anyString())).thenReturn(plugin);
        when(plugin.getDependencies()).thenReturn(listDepcy);
        when(artifact.getArtifactId()).thenReturn("artifactId");
        when(artifact.getGroupId()).thenReturn("groupId");
        when(artifact.getVersion()).thenReturn("SNAPSHOT");
        when(repoSystem.createDependencyArtifact(dep)).thenReturn(artifact);
        when(repoSystem.resolve(any(ArtifactResolutionRequest.class))).thenReturn(artifactResolResult);
        when(artifactResolResult.getArtifacts()).thenReturn(artifacts);
        when(project.getDependencyArtifacts()).thenReturn(artifacts);

        Util.checkClasspath(project, repoSystem, localRepo, remoteRepos);
        assertEquals(1, artifacts.size());
        assertEquals(1, remoteRepos.size());
        assertEquals(1, listDepcy.size());
    }

    @Test
    public void contextHolderTest() throws Exception {
        final EffectiveModelContext context = YangParserTestUtils.parseYangResources(getClass(), "/test.yang",
            "/test2.yang");
        final Set<Module> yangModules = new HashSet<>();
        final ContextHolder cxH = new ContextHolder(context, yangModules, ImmutableSet.of());
        assertEquals(context, cxH.getContext());
        assertEquals(yangModules, cxH.getYangModules());
    }
}
