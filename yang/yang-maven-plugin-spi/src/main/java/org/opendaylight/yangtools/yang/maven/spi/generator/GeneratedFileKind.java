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
 * Basic type of a generated file.
 *
 * @author Robert Varga
 */
@Beta
public enum GeneratedFileKind {
    /**
     * A generated source file. This file should be part of main compilation unit.
     */
    SOURCE,
    /**
     * A generated resource file. This file should be part of artifact's resources.
     */
    RESOURCE,
    /**
     * A generated test source file. This file should be part of test sources.
     */
    TEST_SOURCE,
    /**
     * A generated test resource file. This file should be part of test resources.
     */
    TEST_RESOURCE,
}
