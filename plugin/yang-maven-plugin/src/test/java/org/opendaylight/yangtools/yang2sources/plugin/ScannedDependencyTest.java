/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.ImmutableSet;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.spi.FileSystemProvider;
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
    @Mock
    private File file;
    @Mock
    private File file2;
    @Mock
    private Path path;
    @Mock
    private Path path2;
    @Mock
    private FileSystem filesystem;
    @Mock
    private FileSystemProvider provider;
    @Mock
    private BasicFileAttributes attr;
    @Mock
    private BasicFileAttributes attr2;
    @Mock
    private Artifact artifact;
    @Mock
    private Artifact artifact2;

    @Test
    void getClassPathTest() throws IOException {
        doReturn(provider).when(filesystem).provider();

        doReturn(Set.of(artifact, artifact2)).when(project).getArtifacts();
        doReturn(file).when(artifact).getFile();
        doReturn(path).when(file).toPath();
        doReturn(Path.of("iamjar.jar")).when(path).getFileName();
        doReturn(filesystem).when(path).getFileSystem();
        doReturn(attr).when(provider).readAttributesIfExists(eq(path), eq(BasicFileAttributes.class),
            eq(new LinkOption[0]));
        doReturn(true).when(attr).isRegularFile();

        doReturn(file2).when(artifact2).getFile();
        doReturn(path2).when(file2).toPath();
        doReturn(filesystem).when(path2).getFileSystem();
        doReturn(attr2).when(provider).readAttributesIfExists(eq(path2), eq(BasicFileAttributes.class),
            eq(new LinkOption[0]));
        doReturn(true).when(attr2).isDirectory();

        final var files = ScannedDependency.getClassPath(project);
        assertEquals(2, files.size());
        assertTrue(files.contains(path) && files.contains(path2));
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

    private void prepareProject(final MavenProject prj) throws Exception {
        final var manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        final var testFile2 = Path.of(ScannedDependencyTest.class.getResource("/").toURI()).resolve("test.jar");
        try (var target = new JarOutputStream(Files.newOutputStream(testFile2), manifest)) {
            addSourceFileToTargetJar(Path.of(ScannedDependencyTest.class.getResource("/tests/META-INF").toURI()),
                target);
        }

        doReturn(Path.of(ScannedDependencyTest.class.getResource("/tests").toURI()).toFile()).when(artifact).getFile();

        doReturn(testFile2.toFile()).when(artifact2).getFile();
        doReturn(ImmutableSet.of(artifact, artifact2)).when(prj).getArtifacts();
    }

    private static void addSourceFileToTargetJar(final Path source, final JarOutputStream target) throws IOException {
        if (Files.isDirectory(source)) {
            if (source.getNameCount() != 0) {
                final var entry = new JarEntry(source.toString() + "/");
                entry.setLastModifiedTime(Files.getLastModifiedTime(source));
                target.putNextEntry(entry);
                target.closeEntry();
            }

            try (var files = Files.newDirectoryStream(source)) {
                files.forEach(file -> assertDoesNotThrow(() -> addSourceFileToTargetJar(file, target)));
            }
            return;
        }

        final var entry = new JarEntry(source.toString());
        entry.setLastModifiedTime(Files.getLastModifiedTime(source));
        target.putNextEntry(entry);

        try (var in = new BufferedInputStream(Files.newInputStream(source))) {
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
