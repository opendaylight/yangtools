/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.spi;

import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Interface implemented by CodeGenerator implementations which are integrated
 * with BuildContext. These will have the build context injected before any
 * attempt is made to generate files and should interact with the reactor solely
 * through it.
 *
 * @deprecated Use {@link FileGenerator} instead.
 */
@Deprecated
public interface BuildContextAware {
    /**
     * Set the build context to be used during the lifetime of this reactor.
     *
     * @param buildContext current build context reference.
     */
    void setBuildContext(BuildContext buildContext);
}
