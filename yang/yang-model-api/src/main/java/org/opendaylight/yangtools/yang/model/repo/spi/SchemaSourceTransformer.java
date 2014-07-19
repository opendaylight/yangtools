/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.model.repo.spi;

import com.google.common.util.concurrent.CheckedFuture;

import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;

/**
 * An schema source representation transformation service. An instance can create
 * some output schema source representation based on some input source representation.
 *
 * @param <I> Input {@link SchemaSourceRepresentation}
 * @param <O> Output {@link SchemaSourceRepresentation}
 */
public interface SchemaSourceTransformer<I extends SchemaSourceRepresentation, O extends SchemaSourceRepresentation> {
    /**
     * Return the {@link SchemaSourceRepresentation} which this transformer
     * accepts on its input.
     *
     * @return The input source representation type.
     */
    Class<I> getInputRepresentation();

    /**
     * Return the {@link SchemeSourceRepresentation} which this transformer
     * produces on its output.
     *
     * @return The output source representation type.
     */
    Class<O> getOutputRepresentation();

    /**
     * Transform a schema source representation from its input form to
     * the transformers output form.
     *
     * @param source Schema source in its source representation
     * @return A future which produces the output schema source representation.
     */
    CheckedFuture<O, SchemaSourceTransformationException> transformSchemaSource(I source);

    /**
     * Return the relative cost of performing the transformation. When in doubt,
     * return 1.
     *
     * @return Relative cost.
     */
    int getCost();
}
