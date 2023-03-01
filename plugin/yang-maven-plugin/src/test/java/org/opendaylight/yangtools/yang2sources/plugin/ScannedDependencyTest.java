/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableSet;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScannedDependencyTest {
    @Mock
    private MavenProject project;

    @Test
    void getClassPathTest() {
        final var file = mock(File.class);
        final var file2 = mock(File.class);
        final var artifact = mock(Artifact.class);
        final var artifact2 = mock(Artifact.class);

        doReturn(Set.of(artifact, artifact2)).when(project).getArtifacts();
        doReturn(file).when(artifact).getFile();
        doReturn(true).when(file).isFile();
        doReturn("iamjar.jar").when(file).getName();
        doReturn(file2).when(artifact2).getFile();
        doReturn(true).when(file2).isDirectory();

        final var files = ScannedDependency.getClassPath(project);
        assertEquals(2, files.size());
        assertTrue(files.contains(file) && files.contains(file2));
    }

    @Test
    void findYangFilesInDependenciesAsStream() throws Exception {
        prepareProject(project);

        final var yangzip = ScannedDependency.scanDependencies(project);
        assertNotNull(yangzip);
        assertEquals(2, yangzip.size());
    }

    @Test
    void findYangFilesInDependencies() throws Exception {
        prepareProject(project);

        final var files = ScannedDependency.scanDependencies(project);
        assertNotNull(files);
        assertEquals(2, files.size());
    }

    private static void prepareProject(final MavenProject project) throws Exception {
        final var manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        final var testFile2 = new File(ScannedDependencyTest.class.getResource("/").getPath(), "test.jar");
        try (var target = new JarOutputStream(new FileOutputStream(testFile2), manifest)) {
            addSourceFileToTargetJar(new File(ScannedDependencyTest.class.getResource("/tests/META-INF").getPath()),
                target);
        }

        final var artifact = mock(Artifact.class);
        doReturn(new File(ScannedDependencyTest.class.getResource("/tests").toURI())).when(artifact).getFile();

        final var artifact2 = mock(Artifact.class);
        doReturn(testFile2).when(artifact2).getFile();
        doReturn(ImmutableSet.of(artifact, artifact2)).when(project).getArtifacts();
    }

    private static void addSourceFileToTargetJar(final File source, final JarOutputStream target) throws IOException {
        if (source.isDirectory()) {
            String name = source.getPath().replace("\\", "/");
            if (!name.isEmpty()) {
                if (!name.endsWith("/")) {
                    name += "/";
                }
                final var entry = new JarEntry(name);
                entry.setTime(source.lastModified());
                target.putNextEntry(entry);
                target.closeEntry();
            }
            for (var nestedFile : source.listFiles()) {
                addSourceFileToTargetJar(nestedFile, target);
            }
            return;
        }

        final var entry = new JarEntry(source.getPath().replace("\\", "/"));
        entry.setTime(source.lastModified());
        target.putNextEntry(entry);

        try (var in = new BufferedInputStream(new FileInputStream(source))) {
            final byte[] buffer = new byte[1024];
            while (true) {
                final int count = in.read(buffer);
                if (count == -1) {
                    break;
                }
                target.write(buffer, 0, count);
            }
            target.closeEntry();
        }
    }
}
