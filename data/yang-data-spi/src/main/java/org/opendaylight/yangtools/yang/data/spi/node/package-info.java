/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Various bits and pieces useful when dealing with
 * {@link org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode}s. Most visible here are convenience methods
 * for instantiating immutable implementations, which come in two flavors
 * <ul>
 *   <li>direct instantiations via {@link ImmutableNodes} methods</li>
 *   <li>{@link org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode.Builder}s exposed via the
 *       {@link org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode.BuilderFactory} available from
 *       {@link ImmutableNodes#builderFactory()}</li>
 * </ul>
 *
 * <p>
 * Another bit of convenience are {@link InterningLeafNodeBuilder} and {@link InterningLeafSetNodeBuilder}, which
 * indirect instantiations through an {@link com.google.common.collect.Interner} -- this is useful when the set of
 * allowed values has been determined to have a sufficiently-low cardinality, in which case these will reuse value
 * nodes.
 */
package org.opendaylight.yangtools.yang.data.spi.node;
