/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.maven.spi.generator;

import com.google.common.annotations.Beta;

@Beta
public enum ImportResolutionMode {
    /**
     * Standard, RFC6020 and RFC7950 compliant mode. Imports are satisfied by exact revision match (if specified),
     * or by latest available revision.
     */
    REVISION_EXACT_OR_LATEST,
    /**
     * Semantic version based mode. Imports which specify a semantic version (via the OpenConfig extension) will
     * be satisfied by module which exports the latest compatible revision. Imports which do not specify semantic
     * version will be resolved just as they would be via {@link #REVISION_EXACT_OR_LATEST}.
     */
    SEMVER_LATEST,
}