/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.repo;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.regex.Pattern;
import org.opendaylight.yangtools.yang.model.repo.util.FilesystemSchemaSourceCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filesystem-based schema caching source provider
 *
 * This schema source provider caches all YANG modules loaded from backing
 * schema source providers (registered via
 * {@link #createInstanceFor(SchemaSourceProvider)} to supplied folder.
 *
 * @param <I>
 *            Input format in which schema source is represented.
 *
 * @deprecated Replaced with {@link org.opendaylight.yangtools.yang.model.repo.util.FilesystemSchemaSourceCache}
 *
 */
@Deprecated
public final class FilesystemSchemaCachingProvider<I> extends AbstractCachingSchemaSourceProvider<I, InputStream> {
    private static final Logger LOG = LoggerFactory.getLogger(FilesystemSchemaCachingProvider.class);
    public static final Pattern REVISION_PATTERN = Pattern.compile("\\d\\d\\d\\d-\\d\\d-\\d\\d");

    private final File storageDirectory;
    private final SchemaSourceTransformation<I, String> transformationFunction;

    /**
     *
     * Construct filesystem caching schema source provider.
     *
     *
     * @param delegate
     *            Default delegate to lookup for missed entries in cache.
     * @param directory
     *            Directory where YANG files should be cached.
     * @param transformationFunction
     *            Transformation function which translates from input in format
     *            <code>I</code> to InputStream.
     * @throws IllegalArgumentException
     *             If supplied directory does not exists or is not directory.
     */
    public FilesystemSchemaCachingProvider(final AdvancedSchemaSourceProvider<I> delegate, final File directory,
            final SchemaSourceTransformation<I, String> transformationFunction) {
        super(delegate);
        Preconditions.checkNotNull(directory, "directory must not be null.");
        Preconditions.checkArgument(directory.exists(), "directory must be directory.");
        Preconditions.checkArgument(directory.isDirectory(), "directory must be directory.");
        this.storageDirectory = directory;
        this.transformationFunction = Preconditions.checkNotNull(transformationFunction,
                "transformationFunction must not be null.");
    }

    /**
     *
     * Construct filesystem caching schema source provider.
     *
     *
     * @param delegate
     *            Default delegate to lookup for missed entries in cache.
     * @param directory
     *            Directory where YANG files should be cached.
     * @param transformationFunction
     *            Transformation function which translates from input in format
     *            <code>I</code> to InputStream.
     * @throws IllegalArgumentException
     *             If supplied directory does not exists or is not directory.
     * @deprecated Use
     *             {@link #FilesystemSchemaCachingProvider(AdvancedSchemaSourceProvider, File, SchemaSourceTransformation)}
     *             with
     *             {@link SchemaSourceProviders#schemaSourceTransformationFrom(Function)}
     *             instead.
     */
    @Deprecated
    public FilesystemSchemaCachingProvider(final AdvancedSchemaSourceProvider<I> delegate, final File directory,
            final Function<I, String> transformationFunction) {
        super(delegate);
        Preconditions.checkNotNull(directory, "directory must not be null.");
        Preconditions.checkArgument(directory.exists(), "directory must be directory.");
        Preconditions.checkArgument(directory.isDirectory(), "directory must be directory.");
        this.storageDirectory = directory;
        this.transformationFunction = SchemaSourceProviders.schemaSourceTransformationFrom(transformationFunction);
    }

    @Override
    protected synchronized Optional<InputStream> cacheSchemaSource(final SourceIdentifier identifier,
            final Optional<I> source) {
        File schemaFile = toFile(identifier);
        try {
            if (source.isPresent() && schemaFile.createNewFile()) {
                try (FileOutputStream outStream = new FileOutputStream(schemaFile);
                        OutputStreamWriter writer = new OutputStreamWriter(outStream);) {
                    writer.write(transformToString(source.get()));
                    writer.flush();
                } catch (IOException e) {
                    LOG.warn("Could not chache source for {}. Source: ",identifier,source.get(),e);
                }
            }
        } catch (IOException e) {
            LOG.warn("Could not create cache file for {}. File: ",identifier,schemaFile,e);
        }
        return transformToStream(source);
    }

    private Optional<InputStream> transformToStream(final Optional<I> source) {
        if (source.isPresent()) {
            return Optional.<InputStream> of(new ByteArrayInputStream(transformToString(source.get()).getBytes(
                    Charsets.UTF_8)));
        }
        return Optional.absent();
    }

    private String transformToString(final I input) {
        return transformationFunction.transform(input);
    }

    @Override
    protected Optional<InputStream> getCachedSchemaSource(final SourceIdentifier identifier) {
        File inputFile = toFile(identifier);
        try {
            if (inputFile.exists() && inputFile.canRead()) {
                InputStream stream = new FileInputStream(inputFile);
                return Optional.of(stream);
            }
        } catch (FileNotFoundException e) {
            return Optional.absent();
        }
        return Optional.absent();
    }

    private File toFile(final SourceIdentifier identifier) {
        return sourceIdToFile(identifier, storageDirectory);
    }

    public static File sourceIdToFile(final SourceIdentifier identifier, final File storageDirectory) {
        org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier newIdentifier =
                org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier.create(identifier.getName(), Optional.fromNullable(identifier.getRevision()));
        return FilesystemSchemaSourceCache.sourceIdToFile(newIdentifier, storageDirectory);
    }

    public static FilesystemSchemaCachingProvider<String> createFromStringSourceProvider(
            final SchemaSourceProvider<String> liveProvider, final File directory) {
        Preconditions.checkNotNull(liveProvider);
        Preconditions.checkNotNull(directory);
        directory.mkdirs();
        return new FilesystemSchemaCachingProvider<String>(
                SchemaSourceProviders.toAdvancedSchemaSourceProvider(liveProvider),//
                directory, //
                SchemaSourceProviders.<String>identityTransformation());
    }
}
