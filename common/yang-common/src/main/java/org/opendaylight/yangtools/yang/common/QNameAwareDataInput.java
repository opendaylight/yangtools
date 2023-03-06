/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import com.google.common.annotations.Beta;
import java.io.DataInput;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A {@link DataInput} which has an understanding of {@link QName}'s semantics.
 */
@Beta
public interface QNameAwareDataInput extends DataInput {
    /**
     * Read a {@link QName} from the stream.
     *
     * @return A QName
     * @throws IOException if an I/O error occurs.
     */
    @NonNull QName readQName() throws IOException;
}