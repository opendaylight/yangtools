/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.maven.spi.generator;

import com.google.common.annotations.Beta;
import com.google.common.collect.Table;
import java.util.Set;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Interface implemented by plugins which can generate files from a {@link SchemaContext}.
 *
 * @author Robert Varga
 */
@Beta
public interface FileGenerator extends Identifiable<String> {
    /**
     * {@inheritDoc}
     *
     * This identifier must be a simple string without any whitespace.
     */
    @Override
    String getIdentifier();

    /**
     * Generate files from a {@link SchemaContext}, being aware the that specific modules are local to the current
     * project being processed.
     *
     * Implementations of this interface must not interact directly with project directory directly, but rather supply
     * the files generated as a set of {@link GeneratedFile}s. The caller of this method will use these to integrate
     * with build management to ensure proper dependency tracking is performed.
     *
     * @param context SchemaContext to examine
     * @param localModules Modules local to the
     * @return The set of generated files.
     */
    Table<GeneratedFileKind, GeneratedFilePath, GeneratedFile> generateFiles(SchemaContext context,
           Set<QNameModule> localModules);
}
