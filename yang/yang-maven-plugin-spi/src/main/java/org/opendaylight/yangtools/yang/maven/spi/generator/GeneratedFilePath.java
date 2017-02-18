/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.maven.spi.generator;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import org.opendaylight.yangtools.concepts.Identifier;

/**
 * Identifier of a generated file, based on the file's relative path, guaranteeing uniqueness within the scope
 * of a single {@link FileGenerator} and {@link GeneratedFileKind}.
 *
 * @author Robert Varga
 */
@Beta
public final class GeneratedFilePath implements Identifier {
    private static final long serialVersionUID = 1L;

    private final List<String> components;

    private GeneratedFilePath(final List<String> components) {
        this.components = ImmutableList.copyOf(components);
    }

    public static GeneratedFilePath create(final String... components) {
        return create(Arrays.asList(components));
    }

    public static GeneratedFilePath create(final List<String> components) {
        Preconditions.checkArgument(!components.isEmpty(), "Need at least one component");
        return new GeneratedFilePath(components);
    }

    public List<String> getComponents() {
        return components;
    }

    @Override
    public int hashCode() {
        return components.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof GeneratedFilePath && components.equals(((GeneratedFilePath) obj).components);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("components", components).toString();
    }
}
