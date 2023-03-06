/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import com.google.common.annotations.Beta;
import java.io.DataOutput;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNull;

@Beta
public interface QNameAwareDataOutput extends DataOutput {
    /**
     * Write a {@link QName} into the stream.
     *
     * @param qname A QName
     * @throws  IOException if an I/O error occurs.
     */
    void writeQName(@NonNull QName qname) throws IOException;
}