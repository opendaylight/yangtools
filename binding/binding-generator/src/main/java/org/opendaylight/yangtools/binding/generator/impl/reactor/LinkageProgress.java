/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

/**
 * Enumeration of progress indicators.
 */
enum LinkageProgress {
    /**
     * No progress made and there is work to do.
     */
    NONE,
    /**
     * Some progress made, but there is more work to do.
     */
    SOME,
    /**
     * There is no more work to do.
     */
    DONE;
}
