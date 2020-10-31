/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.WritableObject;

@NonNullByDefault
final class BuildConfiguration implements Immutable, WritableObject {
    private final boolean inspectDependencies;

//    /**
//     * Classes implementing {@link BasicCodeGenerator} interface. An instance will be
//     * created out of every class using default constructor. Method {@link
//     * BasicCodeGenerator#generateSources(EffectiveModelContext, File, Set)} will be called on every instance.
//     */
//    @Parameter(required = false)
//    private CodeGeneratorArg[] codeGenerators;
//
//    /**
//     * {@link FileGenerator} instances resolved via ServiceLoader can hold additional configuration, which details
//     * how they are executed.
//     */
//    @Parameter(required = false)
//    private FileGeneratorArg[] fileGenerators;
//
//    /**
//     * Source directory that will be recursively searched for yang files (ending
//     * with .yang suffix).
//     */
//    @Parameter(required = false)
//    // defaults to ${basedir}/src/main/yang
//    private String yangFilesRootDir;
//
//    @Parameter(required = false)
//    private String[] excludeFiles;
//
    BuildConfiguration(final boolean inspectDependencies) {
        this.inspectDependencies = inspectDependencies;
    }

    static BuildConfiguration readFrom(final DataInput in) throws IOException {
        return new BuildConfiguration(in.readBoolean());
    }

    @Override
    public void writeTo(final DataOutput out) throws IOException {
        out.writeBoolean(inspectDependencies);
    }
}
