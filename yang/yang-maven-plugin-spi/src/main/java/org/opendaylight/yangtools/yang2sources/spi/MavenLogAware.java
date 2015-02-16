/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.spi;

import org.apache.maven.plugin.logging.Log;

/**
 * Bridge compatibility class for plugins using the maven logger functionality.
 *
 * @deprecated Use slf4j logging directly.
 */
@Deprecated
public interface MavenLogAware {
    /**
     * Utilize maven logging if necessary
     *
     * @param log maven log instance
     */
    void setLog(Log log);
}
