/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.concepts.WritableObject;

public final class FileGeneratorArg implements Identifiable<String>, WritableObject {
    @Parameter
    private final Map<String, String> configuration = new HashMap<>();

    @Parameter(required = true)
    private String identifier;

    public FileGeneratorArg() {
        // Default constructor
    }

    public FileGeneratorArg(final String identifier) {
        this.identifier = requireNonNull(identifier);
    }

    public FileGeneratorArg(final String identifier, final Map<String, String> configuration) {
        this(identifier);
        this.configuration.putAll(configuration);
    }

    @Override
    public String getIdentifier() {
        return verifyNotNull(identifier);
    }

    public @NonNull Map<String, String> getConfiguration() {
        return Collections.unmodifiableMap(configuration);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", identifier).add("configuration", configuration).toString();
    }

    public static @NonNull FileGeneratorArg readFrom(final DataInput in) throws IOException {
        final var identifier = in.readUTF();
        final var size = in.readInt();
        final var configuration = Maps.<String, String>newHashMapWithExpectedSize(size);
        for (int i = 0; i < size; ++i) {
            configuration.put(in.readUTF(), in.readUTF());
        }
        return new FileGeneratorArg(identifier, configuration);
    }

    @Override
    public void writeTo(final DataOutput out) throws IOException {
        out.writeUTF(identifier);
        out.writeInt(configuration.size());
        for (String key : configuration.keySet().stream().sorted().toList()) {
            out.writeUTF(key);
            out.writeUTF(configuration.getOrDefault(key, ""));
        }
    }
}
