/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.plugin.generator.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Lifecycle policy of a generated file. This governs how existing files interact with newly-generated bodies.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public enum GeneratedFileLifecycle {
    /**
     * Generated file should be transient and is not expected to be further modified.
     */
    TRANSIENT,
    /**
     * Generated file should be persistent and potentially customized. It should not be overwritten if it already
     * exists.
     */
    PERSISTENT,
}
