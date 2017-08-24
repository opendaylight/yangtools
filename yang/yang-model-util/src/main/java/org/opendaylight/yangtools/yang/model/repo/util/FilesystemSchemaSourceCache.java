/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.util;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.repo.api.MissingSchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource.Costs;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cache implementation that stores schemas in form of files under provided folder.
 */
public final class FilesystemSchemaSourceCache<T extends SchemaSourceRepresentation>
        extends AbstractSchemaSourceCache<T> {

    private static final Logger LOG = LoggerFactory.getLogger(FilesystemSchemaSourceCache.class);

    // Init storage adapters
    private static final Map<Class<? extends SchemaSourceRepresentation>,
            StorageAdapter<? extends SchemaSourceRepresentation>> STORAGE_ADAPTERS = Collections.singletonMap(
                    YangTextSchemaSource.class, new YangTextSchemaStorageAdapter());

    private static final Pattern CACHED_FILE_PATTERN =
            Pattern.compile("(?<moduleName>[^@]+)" + "(@(?<revision>" + SourceIdentifier.REVISION_PATTERN + "))?");

    private final Class<T> representation;
    private final File storageDirectory;

    public FilesystemSchemaSourceCache(
            final SchemaSourceRegistry consumer, final Class<T> representation, final File storageDirectory) {
        super(consumer, representation, Costs.LOCAL_IO);
        this.representation = representation;
        this.storageDirectory = Preconditions.checkNotNull(storageDirectory);

        checkSupportedRepresentation(representation);

        if (!storageDirectory.exists()) {
            Preconditions.checkArgument(storageDirectory.mkdirs(), "Unable to create cache directory at %s",
                    storageDirectory);
        }
        Preconditions.checkArgument(storageDirectory.exists());
        Preconditions.checkArgument(storageDirectory.isDirectory());
        Preconditions.checkArgument(storageDirectory.canWrite());
        Preconditions.checkArgument(storageDirectory.canRead());

        init();
    }

    private static void checkSupportedRepresentation(final Class<? extends SchemaSourceRepresentation> representation) {
        for (final Class<? extends SchemaSourceRepresentation> supportedRepresentation : STORAGE_ADAPTERS.keySet()) {
            if (supportedRepresentation.isAssignableFrom(representation)) {
                return;
            }
        }

        throw new IllegalArgumentException(String.format(
                   "This cache does not support representation: %s, supported representations are: %s",
                   representation, STORAGE_ADAPTERS.keySet()));
    }

    /**
     * Restore cache state.
     */
    private void init() {

        final CachedModulesFileVisitor fileVisitor = new CachedModulesFileVisitor();
        try {
            Files.walkFileTree(storageDirectory.toPath(), fileVisitor);
        } catch (final IOException e) {
            LOG.warn("Unable to restore cache from {}. Starting with empty cache", storageDirectory);
            return;
        }

        for (final SourceIdentifier cachedSchema : fileVisitor.getCachedSchemas()) {
            register(cachedSchema);
        }
    }

    @Override
    public synchronized ListenableFuture<? extends T> getSource(
            final SourceIdentifier sourceIdentifier) {
        final File file = sourceIdToFile(sourceIdentifier, storageDirectory);
        if (file.exists() && file.canRead()) {
            LOG.trace("Source {} found in cache as {}", sourceIdentifier, file);
            final SchemaSourceRepresentation restored = STORAGE_ADAPTERS.get(representation).restore(sourceIdentifier,
                    file);
            return Futures.immediateFuture(representation.cast(restored));
        }

        LOG.debug("Source {} not found in cache as {}", sourceIdentifier, file);
        return Futures.immediateFailedFuture(new MissingSchemaSourceException("Source not found", sourceIdentifier));
    }

    @Override
    protected synchronized void offer(final T source) {
        LOG.trace("Source {} offered to cache", source.getIdentifier());
        final File file = sourceIdToFile(source);
        if (file.exists()) {
            LOG.debug("Source {} already in cache as {}", source.getIdentifier(), file);
            return;
        }

        storeSource(file, source);
        register(source.getIdentifier());
        LOG.trace("Source {} stored in cache as {}", source.getIdentifier(), file);
    }

    private File sourceIdToFile(final T source) {
        return sourceIdToFile(source.getIdentifier(), storageDirectory);
    }

    static File sourceIdToFile(final SourceIdentifier identifier, final File storageDirectory) {
        final String rev = identifier.getRevision();
        final File file;
        if (Strings.isNullOrEmpty(rev)) {
            file = findFileWithNewestRev(identifier, storageDirectory);
        } else {
            file = new File(storageDirectory, identifier.toYangFilename());
        }
        return file;
    }

    private static File findFileWithNewestRev(final SourceIdentifier identifier, final File storageDirectory) {
        File[] files = storageDirectory.listFiles(new FilenameFilter() {
            final Pattern pat = Pattern.compile(Pattern.quote(identifier.getName())
                    + "(\\.yang|@\\d\\d\\d\\d-\\d\\d-\\d\\d.yang)");

            @Override
            public boolean accept(final File dir, final String name) {
                return pat.matcher(name).matches();
            }
        });

        if (files.length == 0) {
            return new File(storageDirectory, identifier.toYangFilename());
        }
        if (files.length == 1) {
            return files[0];
        }

        File file = null;
        TreeMap<Date, File> map = new TreeMap<>();
        for (File sorted : files) {
            String fileName = sorted.getName();
            Matcher match = SourceIdentifier.REVISION_PATTERN.matcher(fileName);
            if (match.find()) {
                String revStr = match.group();
                /*
                 * FIXME: Consider using string for comparison.
                 * String is comparable, pattern check tested format
                 * so comparing as ASCII string should be sufficient
                 */
                DateFormat df = SimpleDateFormatUtil.getRevisionFormat();
                try {
                    Date date = df.parse(revStr);
                    map.put(date, sorted);
                } catch (final ParseException e) {
                    LOG.info("Unable to parse date from yang file name {}", fileName);
                    map.put(new Date(0L), sorted);
                }

            } else {
                map.put(new Date(0L), sorted);
            }
        }
        file = map.lastEntry().getValue();

        return file;
    }

    private void storeSource(final File file, final T schemaRepresentation) {
        STORAGE_ADAPTERS.get(representation).store(file, schemaRepresentation);
    }

    private abstract static class StorageAdapter<T extends SchemaSourceRepresentation> {

        private final Class<T> supportedType;

        protected StorageAdapter(final Class<T> supportedType) {
            this.supportedType = supportedType;
        }

        void store(final File file, final SchemaSourceRepresentation schemaSourceRepresentation) {
            Preconditions.checkArgument(supportedType.isAssignableFrom(schemaSourceRepresentation.getClass()),
                    "Cannot store schema source %s, this adapter only supports %s", schemaSourceRepresentation,
                    supportedType);

            storeAsType(file, supportedType.cast(schemaSourceRepresentation));
        }

        protected abstract void storeAsType(File file, T cast);

        public T restore(final SourceIdentifier sourceIdentifier, final File cachedSource) {
            Preconditions.checkArgument(cachedSource.isFile());
            Preconditions.checkArgument(cachedSource.exists());
            Preconditions.checkArgument(cachedSource.canRead());
            return restoreAsType(sourceIdentifier, cachedSource);
        }

        protected abstract T restoreAsType(SourceIdentifier sourceIdentifier, File cachedSource);
    }

    private static final class YangTextSchemaStorageAdapter extends StorageAdapter<YangTextSchemaSource> {

        protected YangTextSchemaStorageAdapter() {
            super(YangTextSchemaSource.class);
        }

        @Override
        protected void storeAsType(final File file, final YangTextSchemaSource cast) {
            try (InputStream castStream = cast.openStream()) {
                Files.copy(castStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (final IOException e) {
                throw new IllegalStateException("Cannot store schema source " + cast.getIdentifier() + " to " + file,
                        e);
            }
        }

        @Override
        public YangTextSchemaSource restoreAsType(final SourceIdentifier sourceIdentifier, final File cachedSource) {
            return new YangTextSchemaSource(sourceIdentifier) {

                @Override
                protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
                    return toStringHelper;
                }

                @Override
                public InputStream openStream() throws IOException {
                    return Files.newInputStream(cachedSource.toPath());
                }
            };
        }
    }

    private static final class CachedModulesFileVisitor extends SimpleFileVisitor<Path> {
        private final List<SourceIdentifier> cachedSchemas = Lists.newArrayList();

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            final FileVisitResult fileVisitResult = super.visitFile(file, attrs);
            String fileName = file.toFile().getName();
            fileName = com.google.common.io.Files.getNameWithoutExtension(fileName);

            final Optional<SourceIdentifier> si = getSourceIdentifier(fileName);
            if (si.isPresent()) {
                LOG.trace("Restoring cached file {} as {}", file, si.get());
                cachedSchemas.add(si.get());
            } else {
                LOG.debug("Skipping cached file {}, cannot restore source identifier from filename: {},"
                        + " does not match {}", file, fileName, CACHED_FILE_PATTERN);
            }
            return fileVisitResult;
        }

        private static Optional<SourceIdentifier> getSourceIdentifier(final String fileName) {
            final Matcher matcher = CACHED_FILE_PATTERN.matcher(fileName);
            if (matcher.matches()) {
                final String moduleName = matcher.group("moduleName");
                final String revision = matcher.group("revision");
                return Optional.of(RevisionSourceIdentifier.create(moduleName, Optional.fromNullable(revision)));
            }
            return Optional.absent();
        }

        @Override
        public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
            LOG.warn("Unable to restore cached file {}. Ignoring", file, exc);
            return FileVisitResult.CONTINUE;
        }

        public List<SourceIdentifier> getCachedSchemas() {
            return cachedSchemas;
        }
    }
}
