/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.api;

import com.google.common.annotations.Beta;
import java.io.IOException;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.spi.source.SchemaSourceRepresentation;

/**
 * A service capable of transforming a {@link YangLibModuleSet} to an {@link EffectiveModelContext}.
 */
@Beta
public interface YangLibResolver {
    /**
     * Return enumeration of concrete types of {@link SchemaSourceRepresentation} this resolver supports. Users can use
     * this information prepare the source they have to a representation which will be accepted by this resolver.
     *
     * @return Enumeration of supported schema source representations.
     */
    @NonNull Collection<Class<? extends SchemaSourceRepresentation>> supportedSourceRepresentations();

    /**
     * Build the effective view of a combined view of effective statements.
     *
     * @return Effective module statements indexed by their QNameModule.
     * @throws IOException if a module source cannot be read
     * @throws YangSyntaxErrorException when a syntactic error is encountered
     * @throws NullPointerException if {@code moduleSet} is {@code null}
     * @throws IllegalArgumentException if {@code moduleSet} references an unsupported
     *                                  {@link SchemaSourceRepresentation}
     */
    @NonNull EffectiveModelContext resolveModuleSet(YangLibModuleSet moduleSet) throws IOException, YangParserException;
}
