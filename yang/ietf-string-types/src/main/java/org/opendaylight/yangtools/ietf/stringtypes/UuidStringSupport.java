/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.ietf.stringtypes;

import com.google.common.annotations.Beta;
import java.util.UUID;
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.AbstractDerivedStringSupport;

@Beta
@NonNullByDefault
@ThreadSafe
public final class UuidStringSupport extends AbstractDerivedStringSupport<UuidString> {

    private static final UuidStringSupport INSTANCE = new UuidStringSupport();

    private UuidStringSupport() {
        super(UuidString.class);
    }

    public static UuidStringSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public UuidString fromString(final String str) {
        // FIXME: we should be able to do something better here
        return UuidString.valueOf(UUID.fromString(str));
    }
}
