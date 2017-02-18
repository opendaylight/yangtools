/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.maven.spi.generator;

import com.google.common.annotations.Beta;

/**
 * Lifecycle policy of a generated file. This governs
 *
 * @author Robert Varga
 */
@Beta
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
