/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.fs;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.util.concurrent.FluentFutures.immediateFailedFluentFuture;
import static org.opendaylight.yangtools.util.concurrent.FluentFutures.immediateFluentFuture;

import com.google.common.util.concurrent.FluentFuture;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.repo.api.MissingSchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.spi.AbstractSchemaSourceCache;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource.Costs;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistry;
import org.opendaylight.yangtools.yang.model.spi.source.FileYangTextSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cache implementation that stores schemas in form of files under provided folder.
 */
public final class FilesystemSchemaSourceCache<T extends SourceRepresentation> extends AbstractSchemaSourceCache<T> {
    private static final Logger LOG = LoggerFactory.getLogger(FilesystemSchemaSourceCache.class);

    // Init storage adapters
    private static final Map<Class<? extends SourceRepresentation>, StorageAdapter<? extends SourceRepresentation>>
        STORAGE_ADAPTERS = Collections.singletonMap(YangTextSource.class, new YangTextStorageAdapter());

    private static final Pattern CACHED_FILE_PATTERN =
            Pattern.compile("(?<moduleName>[^@]+)" + "(@(?<revision>" + Revision.STRING_FORMAT_PATTERN + "))?");

    private final Class<T> representation;
    private final Path storageDirectory;

    @Deprecated(since = "14.0.7")
    public FilesystemSchemaSourceCache(final SchemaSourceRegistry consumer, final Class<T> representation,
            final File storageDirectory) {
        this(consumer, representation, storageDirectory.toPath());
    }

    public FilesystemSchemaSourceCache(final SchemaSourceRegistry consumer, final Class<T> representation,
            final Path storageDirectory) {
        super(consumer, representation, Costs.LOCAL_IO);
        this.representation = representation;
        this.storageDirectory = requireNonNull(storageDirectory);

        checkSupportedRepresentation(representation);
        try {
            Files.createDirectories(storageDirectory);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot establist storage at " + storageDirectory, e);
        }
        checkArgument(Files.isReadable(storageDirectory));
        checkArgument(Files.isWritable(storageDirectory));
        init();
    }

    private static void checkSupportedRepresentation(final Class<? extends SourceRepresentation> representation) {
        for (final var supportedRepresentation : STORAGE_ADAPTERS.keySet()) {
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
            Files.walkFileTree(storageDirectory, fileVisitor);
        } catch (final IOException e) {
            LOG.warn("Unable to restore cache from {}. Starting with an empty cache", storageDirectory, e);
            return;
        }

        fileVisitor.getCachedSchemas().stream().forEach(this::register);
    }

    @Override
    public synchronized FluentFuture<? extends T> getSource(final SourceIdentifier sourceIdentifier) {
        final var file = sourceIdToFile(sourceIdentifier, storageDirectory);
        if (Files.exists(file) && Files.isReadable(file)) {
            LOG.trace("Source {} found in cache as {}", sourceIdentifier, file);
            final var restored = STORAGE_ADAPTERS.get(representation).restore(sourceIdentifier, file);
            return immediateFluentFuture(representation.cast(restored));
        }

        LOG.debug("Source {} not found in cache as {}", sourceIdentifier, file);
        return immediateFailedFluentFuture(new MissingSchemaSourceException(sourceIdentifier, "Source not found"));
    }

    @Override
    protected synchronized void offer(final T source) {
        LOG.trace("Source {} offered to cache", source.sourceId());
        final var file = sourceIdToFile(source);
        if (Files.exists(file)) {
            LOG.debug("Source {} already in cache as {}", source.sourceId(), file);
            return;
        }

        storeSource(file, source);
        register(source.sourceId());
        LOG.trace("Source {} stored in cache as {}", source.sourceId(), file);
    }

    private Path sourceIdToFile(final T source) {
        return sourceIdToFile(source.sourceId(), storageDirectory);
    }

    static Path sourceIdToFile(final SourceIdentifier identifier, final Path storageDirectory) {
        final var rev = identifier.revision();
        return rev != null ? storageDirectory.resolve(identifier.toYangFilename())
            // FIXME: this does not look right
            : findFileWithNewestRev(identifier, storageDirectory);
    }

    private static Path findFileWithNewestRev(final SourceIdentifier identifier, final Path storageDirectory) {
        final var files = Arrays.stream(storageDirectory.toFile()
            .listFiles(new FilenameFilter() {
                final Pattern pat = Pattern.compile(Pattern.quote(identifier.name().getLocalName())
                    + "(\\.yang|@\\d\\d\\d\\d-\\d\\d-\\d\\d.yang)");

                @Override
                public boolean accept(final File dir, final String name) {
                    return pat.matcher(name).matches();
                }
            }))
            .map(File::toPath)
            .toArray(Path[]::new);
        if (files.length == 0) {
            return storageDirectory.resolve(identifier.toYangFilename());
        }
        if (files.length == 1) {
            return files[0];
        }

        Path file = null;
        final var map = new TreeMap<Optional<Revision>, Path>(Revision::compare);
        for (var sorted : files) {
            final var fileName = sorted.getFileName().toString();
            final var match = Revision.STRING_FORMAT_PATTERN.matcher(fileName);
            if (match.find()) {
                final var revStr = match.group();
                Revision rev;
                try {
                    rev = Revision.of(revStr);
                } catch (final DateTimeParseException e) {
                    LOG.info("Unable to parse date from yang file name {}, falling back to not-present", fileName, e);
                    rev = null;
                }

                map.put(Optional.ofNullable(rev), sorted);
            } else {
                map.put(Optional.empty(), sorted);
            }
        }
        file = map.lastEntry().getValue();

        return file;
    }

    private void storeSource(final Path file, final T schemaRepresentation) {
        STORAGE_ADAPTERS.get(representation).store(file, schemaRepresentation);
    }

    private abstract static class StorageAdapter<T extends SourceRepresentation> {
        private final Class<T> supportedType;

        protected StorageAdapter(final Class<T> supportedType) {
            this.supportedType = supportedType;
        }

        void store(final Path file, final SourceRepresentation schemaSourceRepresentation) {
            checkArgument(supportedType.isAssignableFrom(schemaSourceRepresentation.getClass()),
                    "Cannot store schema source %s, this adapter only supports %s", schemaSourceRepresentation,
                    supportedType);

            storeAsType(file, supportedType.cast(schemaSourceRepresentation));
        }

        // FIXME: use java.nio.filePath
        protected abstract void storeAsType(Path file, T cast);

        T restore(final SourceIdentifier sourceIdentifier, final Path cachedSource) {
            checkArgument(Files.isRegularFile(cachedSource));
            checkArgument(Files.isReadable(cachedSource));
            return restoreAsType(sourceIdentifier, cachedSource);
        }

        abstract T restoreAsType(SourceIdentifier sourceIdentifier, Path cachedSource);
    }

    private static final class YangTextStorageAdapter extends StorageAdapter<YangTextSource> {
        protected YangTextStorageAdapter() {
            super(YangTextSource.class);
        }

        @Override
        protected void storeAsType(final Path file, final YangTextSource cast) {
            try (var castStream = cast.asByteSource(StandardCharsets.UTF_8).openStream()) {
                Files.copy(castStream, file, StandardCopyOption.REPLACE_EXISTING);
            } catch (final IOException e) {
                throw new IllegalStateException("Cannot store schema source " + cast.sourceId() + " to " + file, e);
            }
        }

        @Override
        YangTextSource restoreAsType(final SourceIdentifier sourceIdentifier, final Path cachedSource) {
            return new FileYangTextSource(sourceIdentifier, cachedSource, StandardCharsets.UTF_8);
        }
    }

    private static final class CachedModulesFileVisitor extends SimpleFileVisitor<Path> {
        private final ArrayList<SourceIdentifier> cachedSchemas = new ArrayList<>();

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            final var fileVisitResult = super.visitFile(file, attrs);
            String fileName = file.toFile().getName();
            fileName = com.google.common.io.Files.getNameWithoutExtension(fileName);

            final Optional<SourceIdentifier> si = getSourceIdentifier(fileName);
            if (si.isPresent()) {
                LOG.trace("Restoring cached file {} as {}", file, si.orElseThrow());
                cachedSchemas.add(si.orElseThrow());
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
                return Optional.of(new SourceIdentifier(moduleName, revision));
            }
            return Optional.empty();
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
