/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * A versioned binary encoding of {@link org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode}s to
 * {@link java.io.DataOutput} and from {@link java.io.DataInput}. Primary entry point for writeout is
 * {@link org.opendaylight.yangtools.yang.data.codec.binfmt.NormalizedNodeStreamVersion}, for example
 * {@code var nnOut = NormalizedNodeStreamVersion.current().newDataOutput(dataOutput);}. Reading from inputs is achieved
 * via {@code var nnIn = NormalizedNodeDataInput.newDataInput(dataInput);}, which reads a stream if the implementation
 * recognises the version used in the stream.
 */
@org.osgi.annotation.bundle.Export
package org.opendaylight.yangtools.yang.data.codec.binfmt;