/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.util;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opendaylight.yangtools.yang.model.repo.api.MissingSchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource.Costs;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistry;
import org.opendaylight.yangtools.yang.model.util.repo.FilesystemSchemaCachingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FilesystemSchemaSourceCache<T extends SchemaSourceRepresentation> extends AbstractSchemaSourceCache<T> {

    private static final Logger LOG = LoggerFactory.getLogger(FilesystemSchemaSourceCache.class);

    // Init storage adapters
    private static final Map<Class<? extends SchemaSourceRepresentation>, StorageAdapter<? extends SchemaSourceRepresentation>> storageAdapters;
    static {
        storageAdapters = new HashMap<>(1);
        final StorageAdapter<? extends SchemaSourceRepresentation> value = new YangTextSchemaStorageAdapter();
        storageAdapters.put(value.getSupportedType(), value);
    }

    private final Class<T> representation;
    private final File storageDirectory;

    public FilesystemSchemaSourceCache(
            final SchemaSourceRegistry consumer, final Class<T> representation, final File storageDirectory) {
        super(consumer, representation, Costs.LOCAL_IO);
        this.representation = representation;
        this.storageDirectory = Preconditions.checkNotNull(storageDirectory);

        checkSupportedRepresentation(representation);

        if(storageDirectory.exists() == false) {
            Preconditions.checkArgument(storageDirectory.mkdirs(), "Unable to create cache directory at %s", storageDirectory);
        }
        Preconditions.checkArgument(storageDirectory.exists());
        Preconditions.checkArgument(storageDirectory.isDirectory());
        Preconditions.checkArgument(storageDirectory.canWrite());
        Preconditions.checkArgument(storageDirectory.canRead());

        init();
    }

    private static void checkSupportedRepresentation(final Class<? extends SchemaSourceRepresentation> representation) {
        for (final Class<? extends SchemaSourceRepresentation> supportedRepresentation : storageAdapters.keySet()) {
            if(supportedRepresentation.isAssignableFrom(representation)) {
                return;
            }
        }

       throw new IllegalArgumentException(String.format(
                "This cache does not support representation: %s, supported representations are: %s", representation, storageAdapters.keySet()));
    }

    private static final Pattern CACHED_FILE_PATTERN =
            Pattern.compile(
                    "(?<moduleName>[^@]+)" +
                    "(@(?<revision>" + FilesystemSchemaCachingProvider.REVISION_PATTERN + "))?");

    /**
     * Restore cache state
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
    public synchronized CheckedFuture<? extends T, SchemaSourceException> getSource(final SourceIdentifier sourceIdentifier) {
        final File file = FilesystemSchemaCachingProvider.sourceIdToFile(toLegacy(sourceIdentifier), storageDirectory);
        if(file.exists() && file.canRead()) {
            LOG.trace("Source {} found in cache as {}", sourceIdentifier, file);
            final SchemaSourceRepresentation restored = storageAdapters.get(representation).restore(sourceIdentifier, file);
            return Futures.immediateCheckedFuture(representation.cast(restored));
        }

        LOG.debug("Source {} not found in cache as {}", sourceIdentifier, file);
        return Futures.<T, SchemaSourceException>immediateFailedCheckedFuture(new MissingSchemaSourceException("Source not found"));
    }

    @Override
    protected synchronized void offer(final T source) {
        LOG.trace("Source {} offered to cache", source.getIdentifier());
        final File file = sourceIdToFile(source);
        if(file.exists()) {
            LOG.debug("Source {} already in cache as {}", source.getIdentifier(), file);
            return;
        }

        storeSource(file, source);
        register(source.getIdentifier());
        LOG.trace("Source {} stored in cache as {}", source.getIdentifier(), file);
    }

    private File sourceIdToFile(final T source) {
        // TODO Why there are 2 classes for source identifiers ?
        return FilesystemSchemaCachingProvider.sourceIdToFile(toLegacy(source.getIdentifier()), storageDirectory);
    }

    private void storeSource(final File file, final T schemaRepresentation) {
        storageAdapters.get(representation).store(file, schemaRepresentation);
    }

    private static org.opendaylight.yangtools.yang.model.util.repo.SourceIdentifier toLegacy(final SourceIdentifier identifier) {
        return new org.opendaylight.yangtools.yang.model.util.repo.SourceIdentifier(identifier.getName(), Optional.fromNullable(identifier.getRevision()));
    }

    private static abstract class StorageAdapter<T extends SchemaSourceRepresentation> {

        private final Class<T> supportedType;

        protected StorageAdapter(final Class<T> supportedType) {
            this.supportedType = supportedType;
        }

        void store(final File file, final SchemaSourceRepresentation schemaSourceRepresentation) {
            Preconditions.checkArgument(supportedType.isAssignableFrom(schemaSourceRepresentation.getClass()),
                    "Cannot store schema source %s, this adapter only supports %s", schemaSourceRepresentation, supportedType);

            storeAsType(file, supportedType.cast(schemaSourceRepresentation));

        }

        protected abstract void storeAsType(final File file, final T cast);

        public Class<T> getSupportedType() {
            return supportedType;
        }

        public T restore(final SourceIdentifier sourceIdentifier, final File cachedSource) {
            Preconditions.checkArgument(cachedSource.isFile());
            Preconditions.checkArgument(cachedSource.exists());
            Preconditions.checkArgument(cachedSource.canRead());
            return restoreAsType(sourceIdentifier, cachedSource);
        }

        protected abstract T restoreAsType(final SourceIdentifier sourceIdentifier, final File cachedSource);
    }

    private static final class YangTextSchemaStorageAdapter extends StorageAdapter<YangTextSchemaSource> {

        protected YangTextSchemaStorageAdapter() {
            super(YangTextSchemaSource.class);
        }

        @Override
        protected void storeAsType(final File file, final YangTextSchemaSource cast) {
            try {
                Files.copy(cast.openStream(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (final IOException e) {
                throw new IllegalStateException("Cannot store schema source " + cast.getIdentifier() + " to " + file, e);
            }
        }

        @Override
        public YangTextSchemaSource restoreAsType(final SourceIdentifier sourceIdentifier, final File cachedSource) {
            return new YangTextSchemaSource(sourceIdentifier) {

                @Override
                protected Objects.ToStringHelper addToStringAttributes(final Objects.ToStringHelper toStringHelper) {
                    return toStringHelper;
                }

                @Override
                public InputStream openStream() throws IOException {
                    return new FileInputStream(cachedSource);
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
            if(si.isPresent()) {
                LOG.trace("Restoring cached file {} as {}", file, si.get());
                cachedSchemas.add(si.get());
            } else {
                LOG.debug("Skipping cached file {}, cannot restore source identifier from filename: {}, does not match {}", file, fileName, CACHED_FILE_PATTERN);
            }
            return fileVisitResult;
        }

        private Optional<SourceIdentifier> getSourceIdentifier(final String fileName) {
            final Matcher matcher = CACHED_FILE_PATTERN.matcher(fileName);
            if(matcher.matches()) {
                final String moduleName = matcher.group("moduleName");
                final String revision = matcher.group("revision");
                return Optional.of(new SourceIdentifier(moduleName, Optional.fromNullable(revision)));
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
