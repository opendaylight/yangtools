/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class SampleNormalizedNodeSerializable implements Serializable {
    private static final long serialVersionUID = 1L;

    private transient NormalizedNodeStreamVersion version;
    private NormalizedNode<?, ?> input;

    public SampleNormalizedNodeSerializable(final NormalizedNodeStreamVersion version,
            final NormalizedNode<?, ?> input) {
        this.version = requireNonNull(version);
        this.input = input;
    }

    public NormalizedNode<?, ?> getInput() {
        return input;
    }

    private void readObject(final ObjectInputStream stream) throws IOException {
        final NormalizedNodeDataInput in = NormalizedNodeDataInput.newDataInput(stream);
        this.input = in.readNormalizedNode();
        this.version = in.getVersion();
    }

    private void writeObject(final ObjectOutputStream stream) throws IOException {
        version.newDataOutput(stream).writeNormalizedNode(input);
    }
}
