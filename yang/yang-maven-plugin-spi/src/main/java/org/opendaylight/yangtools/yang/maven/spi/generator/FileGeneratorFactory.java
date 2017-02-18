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
 * A {@link java.util.ServiceLoader} factory for instantiating {@link FileGenerator} instances.
 *
 * @author Robert Varga
 */
@Beta
public interface FileGeneratorFactory {
    /**
     * Create a new {@link FileGenerator}.
     *
     * @return a new FileGenerator.
     */
    FileGenerator newFileGenerator();
}
