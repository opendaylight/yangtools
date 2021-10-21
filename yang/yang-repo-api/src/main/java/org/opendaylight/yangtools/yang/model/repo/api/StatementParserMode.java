/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import com.google.common.annotations.Beta;

@Beta
public enum StatementParserMode {
    /**
     * Default mode of statement parser.
     */
    DEFAULT_MODE,
    /**
     * Semantic version mode of statement parser. If it is enabled, module imports are processed on the basis of
     * semantic versions.
     */
    @Deprecated(since = "7.0.11", forRemoval = true)
    SEMVER_MODE
}