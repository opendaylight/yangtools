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
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.concepts.Variant;
import org.opendaylight.yangtools.yang.common.AbstractCanonicalValueSupport;
import org.opendaylight.yangtools.yang.common.CanonicalValueSupport;
import org.opendaylight.yangtools.yang.common.CanonicalValueViolation;

@Beta
@MetaInfServices(value = CanonicalValueSupport.class)
@NonNullByDefault
@ThreadSafe
public final class UuidStringSupport extends AbstractCanonicalValueSupport<UuidString> {
    private static final UuidStringSupport INSTANCE = new UuidStringSupport();

    public UuidStringSupport() {
        super(UuidString.class);
    }

    public static UuidStringSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public Variant<UuidString, CanonicalValueViolation> fromString(final String str) {
        // TODO: we should be able to do something better here
        return Variant.ofFirst(UuidString.valueOf(UUID.fromString(str)));
    }
}
