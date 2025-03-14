/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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
module org.opendaylight.yangtools.yang.data.codec.binfmt {
    exports org.opendaylight.yangtools.yang.data.codec.binfmt;

    requires transitive com.google.common;
    requires transitive org.opendaylight.yangtools.yang.common;
    requires transitive org.opendaylight.yangtools.yang.model.api;
    requires transitive org.opendaylight.yangtools.yang.data.api;
    requires transitive org.opendaylight.yangtools.yang.data.tree.api;
    requires transitive org.opendaylight.yangtools.yang.data.tree.spi;
    requires java.xml;
    requires org.opendaylight.yangtools.yang.data.impl;
    requires org.slf4j;

    // Annotations
    requires static org.eclipse.jdt.annotation;
    requires static org.osgi.annotation.bundle;
}
