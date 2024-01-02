/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.fs;

import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.either;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Collections2;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistry;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextSource;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class FilesystemSchemaSourceCacheTest {
    @Mock
    public SchemaSourceRegistry registry;
    @Mock
    public Registration registration;

    public File storageDir;

    @BeforeEach
    public void setUp() throws Exception {
        storageDir = Files.createTempDirectory(null).toFile();
        doReturn(registration).when(registry).registerSchemaSource(any(SchemaSourceProvider.class),
            any(PotentialSchemaSource.class));
    }

    @Test
    public void testCacheAndRestore() throws Exception {
        final FilesystemSchemaSourceCache<YangTextSource> cache = new FilesystemSchemaSourceCache<>(registry,
            YangTextSource.class, storageDir);

        final String content = "content1";
        final YangTextSource source = new TestingYangSource("test", "2012-12-12", content);
        cache.offer(source);

        final String content2 = "content2";
        final YangTextSource source2 = new TestingYangSource("test2", null, content);
        cache.offer(source2);

        final List<File> storedFiles = getFilesFromCache();
        assertEquals(2, storedFiles.size());
        final Collection<String> fileNames = filesToFilenamesWithoutRevision(storedFiles);

        assertThat(fileNames, both(hasItem("test2")).and(hasItem("test@2012-12-12")));

        assertThat(Files.readString(storedFiles.get(0).toPath()),
            either(containsString(content)).or(containsString(content2)));
        assertThat(Files.readString(storedFiles.get(1).toPath()),
            either(containsString(content)).or(containsString(content2)));

        verify(registry, times(2)).registerSchemaSource(any(SchemaSourceProvider.class),
            any(PotentialSchemaSource.class));

        // Create new cache from stored sources
        new FilesystemSchemaSourceCache<>(registry, YangTextSource.class, storageDir);

        verify(registry, times(4)).registerSchemaSource(any(SchemaSourceProvider.class),
            any(PotentialSchemaSource.class));

        final List<File> storedFilesAfterNewCache = getFilesFromCache();
        assertEquals(2, storedFilesAfterNewCache.size());
    }

    private static Collection<String> filesToFilenamesWithoutRevision(final List<File> storedFiles) {
        return Collections2.transform(storedFiles, input -> {
            final String fileName = input.getName();
            final int dotIndex = fileName.lastIndexOf('.');
            return dotIndex == -1 ? fileName : fileName.substring(0, dotIndex);
        });
    }

    @Test
    public void testCacheDuplicate() throws Exception {
        final FilesystemSchemaSourceCache<YangTextSource> cache = new FilesystemSchemaSourceCache<>(registry,
            YangTextSource.class, storageDir);

        final String content = "content1";
        final YangTextSource source = new TestingYangSource("test", null, content);
        // Double offer
        cache.offer(source);
        cache.offer(source);

        final List<File> storedFiles = getFilesFromCache();
        assertEquals(1, storedFiles.size());
        verify(registry).registerSchemaSource(any(SchemaSourceProvider.class), any(PotentialSchemaSource.class));
    }

    @Test
    public void testCacheMultipleRevisions() throws Exception {
        final FilesystemSchemaSourceCache<YangTextSource> cache = new FilesystemSchemaSourceCache<>(registry,
            YangTextSource.class, storageDir);

        final String content = "content1";
        final YangTextSource source = new TestingYangSource("test", null, content);
        final YangTextSource source2 = new TestingYangSource("test", "2012-12-12", content);
        final YangTextSource source3 = new TestingYangSource("test", "2013-12-12", content);
        // Double offer
        cache.offer(source);
        cache.offer(source2);
        cache.offer(source3);

        final List<File> storedFiles = getFilesFromCache();
        assertEquals(3, storedFiles.size());

        assertThat(filesToFilenamesWithoutRevision(storedFiles), both(hasItem("test"))
            .and(hasItem("test@2012-12-12")).and(hasItem("test@2013-12-12")));

        verify(registry, times(3)).registerSchemaSource(any(SchemaSourceProvider.class),
            any(PotentialSchemaSource.class));
    }

    @Test
    public void sourceIdToFileEmptyRevWithEmptyDir() {
        final SourceIdentifier sourceIdentifier = new SourceIdentifier("test");
        final File sourceIdToFile = FilesystemSchemaSourceCache.sourceIdToFile(sourceIdentifier, storageDir);
        final FilesystemSchemaSourceCache<YangTextSource> cache = new FilesystemSchemaSourceCache<>(registry,
                YangTextSource.class, sourceIdToFile);
        assertNotNull(cache);
        final List<File> storedFiles = Arrays.asList(sourceIdToFile.listFiles());
        assertEquals(0, storedFiles.size());
    }

    @Test
    public void sourceIdToFileEmptyRevWithOneItemInDir() {
        final FilesystemSchemaSourceCache<YangTextSource> cache = new FilesystemSchemaSourceCache<>(registry,
                YangTextSource.class, storageDir);
        final String content = "content1";
        final YangTextSource source = new TestingYangSource("test", "2013-12-12", content);
        cache.offer(source);

        final SourceIdentifier sourceIdentifier = new SourceIdentifier("test");
        final File sourceIdToFile = FilesystemSchemaSourceCache.sourceIdToFile(sourceIdentifier,
                storageDir);
        assertNotNull(sourceIdToFile);
        final List<File> storedFiles = Arrays.asList(storageDir.listFiles());
        assertEquals(1, storedFiles.size());
    }

    @Test
    public void sourceIdToFileEmptyRevWithMoreItemsInDir() {
        final FilesystemSchemaSourceCache<YangTextSource> cache = new FilesystemSchemaSourceCache<>(registry,
                YangTextSource.class, storageDir);
        final String content = "content1";
        final YangTextSource source = new TestingYangSource("test", "2012-12-12", content);
        final YangTextSource source2 = new TestingYangSource("test", "2013-12-12", content);
        cache.offer(source);
        cache.offer(source2);

        final SourceIdentifier sourceIdentifier = new SourceIdentifier("test");
        final File sourceIdToFile = FilesystemSchemaSourceCache.sourceIdToFile(sourceIdentifier, storageDir);
        assertNotNull(sourceIdToFile);
        final List<File> storedFiles = Arrays.asList(storageDir.listFiles());
        assertEquals(2, storedFiles.size());
    }

    @Test
    public void test() throws Exception {
        final FilesystemSchemaSourceCache<YangTextSource> cache = new FilesystemSchemaSourceCache<>(registry,
                YangTextSource.class, storageDir);
        final String content = "content1";
        final YangTextSource source = new TestingYangSource("test", "2013-12-12", content);
        cache.offer(source);
        final SourceIdentifier sourceIdentifier = new SourceIdentifier("test", "2013-12-12");
        final ListenableFuture<? extends YangTextSource> checked = cache.getSource(sourceIdentifier);
        assertNotNull(checked);
        assertTrue(checked.isDone());
        final YangTextSource checkedGet = checked.get();
        assertEquals(sourceIdentifier, checkedGet.sourceId());
    }

    @Test
    public void test1() throws Exception {
        final FilesystemSchemaSourceCache<YangTextSource> cache = new FilesystemSchemaSourceCache<>(registry,
                YangTextSource.class, storageDir);
        final String content = "content1";
        final YangTextSource source = new TestingYangSource("test", "2013-12-12", content);
        cache.offer(source);
        final SourceIdentifier sourceIdentifier = new SourceIdentifier("test1", "2012-12-12");
        final ListenableFuture<? extends YangTextSource> checked = cache.getSource(sourceIdentifier);
        assertNotNull(checked);
        assertThrows(ExecutionException.class, () -> checked.get());
    }

    private List<File> getFilesFromCache() {
        return Arrays.asList(storageDir.listFiles());
    }

    private static class TestingYangSource extends YangTextSource {
        private final String content;

        TestingYangSource(final String name, final String revision, final String content) {
            super(new SourceIdentifier(name, revision));
            this.content = content;
        }

        @Override
        protected MoreObjects.ToStringHelper addToStringAttributes(final MoreObjects.ToStringHelper toStringHelper) {
            return toStringHelper;
        }

        @Override
        public Reader openStream() throws IOException {
            return new StringReader(content);
        }

        @Override
        public String symbolicName() {
            return null;
        }
    }
}
