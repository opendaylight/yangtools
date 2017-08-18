/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.repo.util;

import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.either;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Collections2;
import com.google.common.io.Files;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistration;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistry;

public class FilesystemSchemaSourceCacheTest {

    @Mock
    private SchemaSourceRegistry registry;
    @Mock
    private SchemaSourceRegistration<?> registration;
    private File storageDir;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.storageDir = Files.createTempDir();
        doReturn(this.registration).when(this.registry).registerSchemaSource(any(SchemaSourceProvider.class),
            any(PotentialSchemaSource.class));
    }

    @Test
    public void testCacheAndRestore() throws Exception {
        final FilesystemSchemaSourceCache<YangTextSchemaSource> cache
                = new FilesystemSchemaSourceCache<>(this.registry, YangTextSchemaSource.class, this.storageDir);

        final String content = "content1";
        final YangTextSchemaSource source = new TestingYangSource("test", "2012-12-12", content);
        cache.offer(source);

        final String content2 = "content2";
        final YangTextSchemaSource source2 = new TestingYangSource("test2", null, content);
        cache.offer(source2);

        final List<File> storedFiles = getFilesFromCache();
        assertEquals(2, storedFiles.size());
        final Collection<String> fileNames = filesToFilenamesWithoutRevision(storedFiles);

        assertThat(fileNames, both(hasItem("test2")).and(hasItem("test@2012-12-12")));

        assertThat(Files.asCharSource(storedFiles.get(0), StandardCharsets.UTF_8).read(),
            either(containsString(content)).or(containsString(content2)));
        assertThat(Files.asCharSource(storedFiles.get(1), StandardCharsets.UTF_8).read(),
            either(containsString(content)).or(containsString(content2)));

        verify(this.registry, times(2)).registerSchemaSource(any(SchemaSourceProvider.class),
            any(PotentialSchemaSource.class));

        // Create new cache from stored sources
        new FilesystemSchemaSourceCache<>(this.registry, YangTextSchemaSource.class, this.storageDir);

        verify(this.registry, times(4)).registerSchemaSource(any(SchemaSourceProvider.class),
            any(PotentialSchemaSource.class));

        final List<File> storedFilesAfterNewCache = getFilesFromCache();
        assertEquals(2, storedFilesAfterNewCache.size());
    }

    private static Collection<String> filesToFilenamesWithoutRevision(final List<File> storedFiles) {
        return Collections2.transform(storedFiles, input -> Files.getNameWithoutExtension(input.getName()));
    }

    @Test
    public void testCacheDuplicate() throws Exception {
        final FilesystemSchemaSourceCache<YangTextSchemaSource> cache
                = new FilesystemSchemaSourceCache<>(this.registry, YangTextSchemaSource.class, this.storageDir);

        final String content = "content1";
        final YangTextSchemaSource source = new TestingYangSource("test", null, content);
        // Double offer
        cache.offer(source);
        cache.offer(source);

        final List<File> storedFiles = getFilesFromCache();
        assertEquals(1, storedFiles.size());
        verify(this.registry).registerSchemaSource(any(SchemaSourceProvider.class), any(PotentialSchemaSource.class));
    }

    @Test
    public void testCacheMultipleRevisions() throws Exception {
        final FilesystemSchemaSourceCache<YangTextSchemaSource> cache
                = new FilesystemSchemaSourceCache<>(this.registry, YangTextSchemaSource.class, this.storageDir);

        final String content = "content1";
        final YangTextSchemaSource source = new TestingYangSource("test", null, content);
        final YangTextSchemaSource source2 = new TestingYangSource("test", "2012-12-12", content);
        final YangTextSchemaSource source3 = new TestingYangSource("test", "2013-12-12", content);
        // Double offer
        cache.offer(source);
        cache.offer(source2);
        cache.offer(source3);

        final List<File> storedFiles = getFilesFromCache();
        assertEquals(3, storedFiles.size());

        assertThat(filesToFilenamesWithoutRevision(storedFiles), both(hasItem("test"))
            .and(hasItem("test@2012-12-12")).and(hasItem("test@2013-12-12")));

        verify(this.registry, times(3)).registerSchemaSource(any(SchemaSourceProvider.class),
            any(PotentialSchemaSource.class));
    }

    @Test
    public void sourceIdToFileEmptyRevWithEmptyDir() {
        final SourceIdentifier sourceIdentifier = RevisionSourceIdentifier.create("test", "");
        final File sourceIdToFile = FilesystemSchemaSourceCache.sourceIdToFile(sourceIdentifier, this.storageDir);
        final FilesystemSchemaSourceCache<YangTextSchemaSource> cache = new FilesystemSchemaSourceCache<>(this.registry,
                YangTextSchemaSource.class, sourceIdToFile);
        Assert.assertNotNull(cache);
        final List<File> storedFiles = Arrays.asList(sourceIdToFile.listFiles());
        assertEquals(0, storedFiles.size());
    }

    @Test
    public void sourceIdToFileEmptyRevWithOneItemInDir() {
        final FilesystemSchemaSourceCache<YangTextSchemaSource> cache = new FilesystemSchemaSourceCache<>(this.registry,
                YangTextSchemaSource.class, this.storageDir);
        final String content = "content1";
        final YangTextSchemaSource source = new TestingYangSource("test", "2013-12-12", content);
        cache.offer(source);

        final SourceIdentifier sourceIdentifier = RevisionSourceIdentifier.create("test", "");
        final File sourceIdToFile = FilesystemSchemaSourceCache.sourceIdToFile(sourceIdentifier,
                this.storageDir);
        Assert.assertNotNull(sourceIdToFile);
        final List<File> storedFiles = Arrays.asList(this.storageDir.listFiles());
        assertEquals(1, storedFiles.size());
    }

    @Test
    public void sourceIdToFileEmptyRevWithMoreItemsInDir() {
        final FilesystemSchemaSourceCache<YangTextSchemaSource> cache = new FilesystemSchemaSourceCache<>(this.registry,
                YangTextSchemaSource.class, this.storageDir);
        final String content = "content1";
        final YangTextSchemaSource source = new TestingYangSource("test", "2012-12-12", content);
        final YangTextSchemaSource source2 = new TestingYangSource("test", "2013-12-12", content);
        cache.offer(source);
        cache.offer(source2);

        final SourceIdentifier sourceIdentifier = RevisionSourceIdentifier.create("test", "");
        final File sourceIdToFile = FilesystemSchemaSourceCache.sourceIdToFile(sourceIdentifier, this.storageDir);
        Assert.assertNotNull(sourceIdToFile);
        final List<File> storedFiles = Arrays.asList(this.storageDir.listFiles());
        assertEquals(2, storedFiles.size());
    }

    @Test
    public void test() throws Exception {

        final FilesystemSchemaSourceCache<YangTextSchemaSource> cache = new FilesystemSchemaSourceCache<>(this.registry,
                YangTextSchemaSource.class, this.storageDir);
        final String content = "content1";
        final YangTextSchemaSource source = new TestingYangSource("test", "2013-12-12", content);
        cache.offer(source);
        final SourceIdentifier sourceIdentifier = RevisionSourceIdentifier.create("test", "2013-12-12");
        final CheckedFuture<? extends YangTextSchemaSource, SchemaSourceException> checked = cache
                .getSource(sourceIdentifier);
        Assert.assertNotNull(checked);
        final YangTextSchemaSource checkedGet = checked.checkedGet();
        Assert.assertEquals(sourceIdentifier, checkedGet.getIdentifier());
        Assert.assertTrue(checked.isDone());
    }

    @Test(expected = ExecutionException.class)
    public void test1() throws Exception {

        final FilesystemSchemaSourceCache<YangTextSchemaSource> cache = new FilesystemSchemaSourceCache<>(this.registry,
                YangTextSchemaSource.class, this.storageDir);
        final String content = "content1";
        final YangTextSchemaSource source = new TestingYangSource("test", "2013-12-12", content);
        cache.offer(source);
        final SourceIdentifier sourceIdentifier = RevisionSourceIdentifier.create("test1", "2012-12-12");
        final ListenableFuture<? extends YangTextSchemaSource> checked = cache.getSource(sourceIdentifier);
        Assert.assertNotNull(checked);
        checked.get();
    }

    private List<File> getFilesFromCache() {
        return Arrays.asList(this.storageDir.listFiles());
    }

    private class TestingYangSource extends YangTextSchemaSource {

        private final String content;

        protected TestingYangSource(final String name, final String revision, final String content) {
            super(RevisionSourceIdentifier.create(name, Optional.ofNullable(revision)));
            this.content = content;
        }

        @Override
        protected MoreObjects.ToStringHelper addToStringAttributes(final MoreObjects.ToStringHelper toStringHelper) {
            return toStringHelper;
        }

        @Override
        public InputStream openStream() throws IOException {
            return new ByteArrayInputStream(this.content.getBytes(StandardCharsets.UTF_8));
        }
    }
}
