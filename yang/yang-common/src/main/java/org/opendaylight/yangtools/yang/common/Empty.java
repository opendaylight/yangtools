/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import com.google.common.annotations.Beta;
import java.io.Serializable;

@Beta
public final class Empty implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Empty INSTANCE = new Empty();

    private Empty() {

    }

    public static Empty getInstance() {
        return INSTANCE;
    }

    @Override
    public String toString() {
        return "empty";
    }

    @SuppressWarnings("static-method")
    private Object readResolve() {
        return INSTANCE;
    }
}
