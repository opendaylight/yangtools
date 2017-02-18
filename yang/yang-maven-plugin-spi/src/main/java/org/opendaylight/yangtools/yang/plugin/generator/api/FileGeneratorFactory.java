/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.plugin.generator.api;

import com.google.common.annotations.Beta;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Identifiable;

/**
 * A {@link java.util.ServiceLoader} factory for instantiating {@link FileGenerator} instances.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public interface FileGeneratorFactory extends Identifiable<String> {
    /**
     * {@inheritDoc}
     *
     * <p>
     * This identifier must be a simple string without any whitespace.
     */
    @Override
    String getIdentifier();

    /**
     * Create a new {@link FileGenerator}.
     *
     * @return a new FileGenerator.
     */
    FileGenerator newFileGenerator(Map<String, String> configuration);
}
