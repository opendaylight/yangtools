/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

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
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class YangToSourcesMojoTest {
    @Mock
    private MavenProject project;
    @Mock
    private Plugin plugin;
    @Mock
    private RepositorySystem repoSystem;
    @Mock
    private ArtifactRepository localRepo;
    @Mock
    private ArtifactResolutionResult artifactResolResult;
    @Mock
    private Artifact artifact;
    @Mock
    private Dependency dep;

    @Test
    public void checkClasspathTest() {
        final var artifacts = Set.of(artifact);

        doReturn(plugin).when(project).getPlugin(anyString());
        doReturn(List.of(dep)).when(plugin).getDependencies();
        doReturn("artifactId").when(artifact).getArtifactId();
        doReturn("groupId").when(artifact).getGroupId();
        doReturn("SNAPSHOT").when(artifact).getVersion();
        doReturn(artifact).when(repoSystem).createDependencyArtifact(dep);
        doReturn(artifactResolResult).when(repoSystem).resolve(any(ArtifactResolutionRequest.class));
        doReturn(artifacts).when(artifactResolResult).getArtifacts();
        doReturn(artifacts).when(project).getDependencyArtifacts();

        YangToSourcesMojo.checkClasspath(project, repoSystem, localRepo, List.of(localRepo));
    }
}
