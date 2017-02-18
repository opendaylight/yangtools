/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.plugin.generator.api;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Type of generated file. Four most common kinds are captured in {@link #RESOURCE}, {@link #SOURCE},
 * {@value #TEST_RESOURCE} and {@link #TEST_SOURCE}, but others may be externally defined.
 *
 * <p>
 * Users of {@link FileGenerator} are expected to provide sensible mapping of {@link GeneratedFileType} to their
 * output structures. Notably they must handle pre-defined types, allow end users to specify mapping of custom types.
 * They need to deal with runtime mismatches involving between FileGenerators and user's expectations, for example by
 * issuing warnings when a mismatch is detected.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public final class GeneratedFileType {
    /**
     * A generated resource file. This file should be part of artifact's resources.
     */
    public static final GeneratedFileType RESOURCE = new GeneratedFileType("resource");

    /**
     * A generated source file. This file should be part of main compilation unit.
     */
    public static final GeneratedFileType SOURCE = new GeneratedFileType("source");

    /**
     * A generated test resource file. This file should be part of test resources.
     */
    public static final GeneratedFileType TEST_RESOURCE = new GeneratedFileType("test-resource");

    /**
     * A generated test source file. This file should be part of test sources.
     */
    public static final GeneratedFileType TEST_SOURCE = new GeneratedFileType("test-source");

    private static final ImmutableMap<String, GeneratedFileType> WELL_KNOWN = ImmutableMap.of(
        RESOURCE.name(), RESOURCE, TEST_RESOURCE.name(), TEST_RESOURCE,
        SOURCE.name(), SOURCE, TEST_SOURCE.name(), TEST_SOURCE);

    private final String name;

    private GeneratedFileType(final String name) {
        this.name = requireNonNull(name);
    }

    public static GeneratedFileType of(final String name) {
        checkArgument(!name.isEmpty());
        final @Nullable GeneratedFileType wellKnown = WELL_KNOWN.get(name);
        return wellKnown != null ? wellKnown : new GeneratedFileType(name);
    }

    public String name() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof GeneratedFileType && name.equals(((GeneratedFileType) obj).name);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("name", name).toString();
    }
}
