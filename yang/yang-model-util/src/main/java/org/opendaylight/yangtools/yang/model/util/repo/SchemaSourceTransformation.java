/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.model.util.repo;

/**
 *
 * Schema Source Transformation which transforms from one schema source
 * representation to another.
 *
 * @param <I> Input schema source representation
 * @param <O> Output schema source representation
 */
public interface SchemaSourceTransformation<I, O> {

    /**
     *
     * Transforms supplied schema source in format <code>I</code> to schema
     * source in format <code>O</code>.
     *
     * <ul>
     * <li>Its execution does not cause any observable side effects.
     * <li>If the contents of a,b are semantically same (e.g. contens of InputStream),
     * output representations MUST BE also semantically equals.
     * </ul>
     *
     * @param input Not null input which should be transformed
     * @return Representation of input in <code>O</code> format.
     */
    O transform(I input);
}
