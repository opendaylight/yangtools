/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.tailf.common.model.api;

import java.net.URI;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;

/**
 * Constants associated with tailf-common.yang.
 *
 * @author Robert Varga
 */
@NonNullByDefault
public final class TailFCommonConstants {
    private static final URI MODULE_NAMESPACE = URI.create("http://tail-f.com/yang/common");

    /**
     * Runtime tailf-common identity.
     */
    public static final QNameModule MODULE = QNameModule.create(MODULE_NAMESPACE, Revision.of("2017-09-28")).intern();

    private TailFCommonConstants() {
        throw new UnsupportedOperationException();
    }
}
