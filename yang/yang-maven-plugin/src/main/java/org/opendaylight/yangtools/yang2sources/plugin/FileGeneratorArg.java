/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Identifiable;

public final class FileGeneratorArg implements Identifiable<String> {
    @Parameter
    private final Map<String, String> configuration = new HashMap<>();

    @Parameter(required = true)
    private String identifier;

    public FileGeneratorArg() {

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
}
