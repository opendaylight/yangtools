/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.plugin.generator.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Abstract base class for {@link GeneratedFile}s. This class is suitable for binary files. For text files use
 * {@link AbstractGeneratedTextFile}.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public abstract class AbstractGeneratedFile implements GeneratedFile {
    private final GeneratedFileLifecycle lifecycle;

    protected AbstractGeneratedFile(final GeneratedFileLifecycle lifecycle) {
        this.lifecycle = requireNonNull(lifecycle);
    }

    /**
     * Return the lifecycle governing this file.
     *
     * @return Governing lifecycle
     */
    @Override
    public final GeneratedFileLifecycle getLifecycle() {
        return lifecycle;
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("lifecycle", lifecycle);
    }
}
