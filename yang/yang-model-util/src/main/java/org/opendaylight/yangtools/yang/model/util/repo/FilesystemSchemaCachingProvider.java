/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.model.util.repo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringBufferInputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class FilesystemSchemaCachingProvider<I> extends AbstractCachingSchemaSourceProvider<I, InputStream> {
    private static final Logger LOG = LoggerFactory.getLogger(FilesystemSchemaCachingProvider.class);

    private final File storageDirectory;
    private final Function<I, String> transformationFunction;

    public FilesystemSchemaCachingProvider(AdvancedSchemaSourceProvider<I> delegate, File directory,
            Function<I, String> transformationFunction) {
        super(delegate);
        this.storageDirectory = directory;
        this.transformationFunction = transformationFunction;
    }

    @Override
    protected synchronized Optional<InputStream> cacheSchemaSource(SourceIdentifier identifier, Optional<I> source) {
        File schemaFile = toFile(identifier);
        try {
            if (source.isPresent() && schemaFile.createNewFile()) {
                try (FileOutputStream outStream = new FileOutputStream(schemaFile);
                        OutputStreamWriter writer = new OutputStreamWriter(outStream);) {
                    writer.write(transformToString(source.get()));
                    writer.flush();
                } catch (IOException e) {

                }
            }
        } catch (IOException e) {

        }
        return transformToStream(source);
    }

    @SuppressWarnings("deprecation")
    private Optional<InputStream> transformToStream(Optional<I> source) {
        if (source.isPresent()) {
            return Optional.<InputStream> of(new StringBufferInputStream(transformToString(source.get())));
        }
        return Optional.absent();
    }

    private String transformToString(I input) {
        return transformationFunction.apply(input);
    }

    @Override
    protected Optional<InputStream> getCachedSchemaSource(SourceIdentifier identifier) {
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

    private File toFile(SourceIdentifier identifier) {
        File file = null;
        String rev = identifier.getRevision();
        if (rev == null || rev.isEmpty()) {
            file = findFileWithNewestRev(identifier);
        } else {
            file = new File(storageDirectory, identifier.toYangFilename());
        }
        return file;
    }

    private File findFileWithNewestRev(final SourceIdentifier identifier) {
        File[] files = storageDirectory.listFiles(new FilenameFilter() {
            final String regex = identifier.getName() + "(\\.yang|@\\d\\d\\d\\d-\\d\\d-\\d\\d.yang)";
            @Override
            public boolean accept(File dir, String name) {
                if (name.matches(regex)) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        if (files.length == 0) {
            return new File(storageDirectory, identifier.toYangFilename());
        }
        if (files.length == 1) {
            return files[0];
        }

        File file = null;
        Pattern p = Pattern.compile("\\d\\d\\d\\d-\\d\\d-\\d\\d");
        TreeMap<Date, File> map = new TreeMap<>();
        for (File sorted : files) {
            String fileName = sorted.getName();
            Matcher m = p.matcher(fileName);
            if (m.find()) {
                String revStr = m.group();
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date d = df.parse(revStr);
                    map.put(d, sorted);
                } catch (ParseException e) {
                    LOG.info("Unable to parse date from yang file name");
                    map.put(new Date(0L), sorted);
                }

            } else {
                map.put(new Date(0L), sorted);
            }
        }
        file = map.lastEntry().getValue();

        return file;
    }

    private static final Function<String, String> NOOP_TRANSFORMATION = new Function<String, String>() {
        @Override
        public String apply(String input) {
            return input;
        }
    };

    public static FilesystemSchemaCachingProvider<String> createFromStringSourceProvider(
            SchemaSourceProvider<String> liveProvider, File directory) {
        Preconditions.checkNotNull(liveProvider);
        Preconditions.checkNotNull(directory);
        directory.mkdirs();
        return new FilesystemSchemaCachingProvider<String>(
                SchemaSourceProviders.toAdvancedSchemaSourceProvider(liveProvider),//
                directory, //
                NOOP_TRANSFORMATION);
    }
}
