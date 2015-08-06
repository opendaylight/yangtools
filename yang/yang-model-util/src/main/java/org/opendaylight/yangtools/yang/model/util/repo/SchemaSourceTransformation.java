/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.repo;

import com.google.common.annotations.Beta;

/**
 *
 * Schema Source Transformation which transforms from one schema source
 * representation to another.
 *
 * <p>
 * <b>Representation of Schema Source</b>
 * <p>
 * Schema source may be represented by
 * various Java Types, which depends on provider and/or consumer.
 * <p>
 * E.g example of possible representations:
 * <ul>
 * <li>{@link String}
 * <li>{@link java.io.InputStream}
 * <li>{@link com.google.common.io.ByteSource}
 * </ul>
 *
 * FIXME: <b>Beta:</b> Consider allowing transformations, which may
 * fail to produce Output, this will require introduction of
 * checked exception.
 *
 * @param <I> Input schema source representation
 * @param <O> Output schema source representation
 *
 * @deprecated Replaced with {@link org.opendaylight.yangtools.yang.model.repo.util.SchemaSourceTransformer}
 */
@Beta
@Deprecated
public interface SchemaSourceTransformation<I, O> {

    /**
     *
     * Transforms supplied schema source in format <code>I</code> to schema
     * source in format <code>O</code>.
     *
     * <ul>
     * <li>Its execution does not cause any observable side effects.
     * <li>If the contents of a,b are semantically same (e.g. contents of InputStream),
     * output representations MUST BE also semantically equals.
     * </ul>
     *
     * Implementations of transformation SHOULD NOT fail to
     * transform valid non-null input to output representation.
     *
     *
     * FIXME: <b>Beta:</b> Consider lowering condition for safe transformation
     * and introduce checked exception for cases when transformation may fail.
     *
     * @param input Not null input which should be transformed
     * @return Representation of input in <code>O</code> format.
     * @throws NullPointerException if input is null.
     *
     */
    @Beta
    O transform(I input);
}
