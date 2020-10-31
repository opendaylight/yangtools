/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.StreamCorruptedException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.WritableObject;

/**
 * Build information used to compare inputs that went it a particular build with what is available in the next build.
 */
@NonNullByDefault
abstract class BuildInfo implements Immutable, WritableObject {
    private static final String BUILD_INFO_FILE = "yang-maven-plugin.buildinfo";
    private static final String HEADER = "yang-maven-plugin:build-info";

    static PersistentBuildInfo readFrom(final DataInput in) throws IOException {
        final String hdr = in.readUTF();
        if (!HEADER.equals(hdr)) {
            throw new StreamCorruptedException("Unexpected header \"" + hdr + "\"");
        }
        final String hashName = in.readUTF();
        if (!HashedFile.HASH_FUNCTION_NAME.equals(hashName)) {
            throw new InvalidObjectException("Unsupported hash function \"" + hashName + "\"");
        }

        return new PersistentBuildInfo(in);
    }

    static PersistentBuildInfo from(final String parentDir) throws IOException {
        return readFrom(new DataInputStream(Files.newInputStream(new File(parentDir, BUILD_INFO_FILE).toPath())));
    }

    abstract BuildConfiguration buildConfiguration() throws IOException;

    abstract ImmutableList<HashedFile> inputFiles() throws IOException;

    abstract ImmutableMultimap<String, HashedFile> outputFiles() throws IOException;

    abstract ImmutableBuildInfo toImmutable() throws IOException;

    @Override
    public void writeTo(final DataOutput out) throws IOException {
        out.writeUTF(HEADER);
        out.writeUTF(HashedFile.HASH_FUNCTION_NAME);
        buildConfiguration().writeTo(out);

        final ImmutableList<HashedFile> inputFiles = inputFiles();
        out.writeInt(inputFiles.size());
        for (HashedFile file : inputFiles) {
            file.writeTo(out);
        }

        final ImmutableMap<String, Collection<HashedFile>> outputFiles = outputFiles().asMap();
        out.writeInt(outputFiles.size());
        for (Entry<String, Collection<HashedFile>> entry : outputFiles.entrySet()) {
            out.writeUTF(entry.getKey());
            final Collection<HashedFile> files = entry.getValue();
            out.writeInt(files.size());
            for (HashedFile file : files) {
                file.writeTo(out);
            }
        }
    }
}
