/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static org.opendaylight.yangtools.yang.common.YangConstants.RFC6020_YANG_FILE_EXTENSION;

import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.maven.plugin.MojoExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ZipSignificantDependency extends SignificantDependency {
    private static final Logger LOG = LoggerFactory.getLogger(ZipSignificantDependency.class);

    private final Collection<ZipEntry> entries;
    private final ZipFile zip;

    private ZipSignificantDependency(final File file, final ZipFile zip, final Collection<ZipEntry> entries) {
        super(file);
        this.zip = Preconditions.checkNotNull(zip);
        this.entries = ImmutableList.copyOf(entries);
    }

    static Optional<ZipSignificantDependency> create(final File file) throws MojoExecutionException {
        final ZipFile zip;
        try {
            zip = new ZipFile(file);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to open " + file, e);
        }

        final Collection<ZipEntry> matching = new ArrayList<>();
        final Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            final ZipEntry entry = entries.nextElement();
            final String entryName = entry.getName();

            if (entryName.startsWith(YangToSourcesProcessor.META_INF_YANG_STRING_JAR) && !entry.isDirectory()
                    && entryName.endsWith(RFC6020_YANG_FILE_EXTENSION)) {
                LOG.debug("Found a YANG file in {}: {}", file, entryName);
                matching.add(entry);
            }
        }

        if (matching.isEmpty()) {
            closeZip(zip);
            return Optional.empty();
        }

        return Optional.of(new ZipSignificantDependency(file, zip, matching));
    }

    @Override
    Collection<ByteSource> asSources() {
        return Collections2.transform(entries, e -> new ByteSource() {
            @Override
            public InputStream openStream() throws IOException {
                return zip.getInputStream(e);
            }
        });
    }

    @Override
    public void close() {
        closeZip(zip);
    }

    private static void closeZip(final ZipFile zip) {
        try {
            zip.close();
        } catch (IOException e) {
            LOG.warn("Failed to close {}", zip, e);
        }
    }
}
