/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common.jackson;

import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.opendaylight.yangtools.yang.common.YangVersion;

public class MockObjectContainer {

    private Decimal64 decimal64;
    private Empty empty;
    private Revision revision;
    private Uint8 uint8;
    private Uint16 uint16;
    private Uint32 uint32;
    private Uint64 uint64;
    private YangVersion yangVersion;

    public Decimal64 getDecimal64() {
        return this.decimal64;
    }

    public void setDecimal64(final Decimal64 decimal64) {
        this.decimal64 = decimal64;
    }

    public Empty getEmpty() {
        return this.empty;
    }

    public void setEmpty(final Empty empty) {
        this.empty = empty;
    }

    public Revision getRevision() {
        return this.revision;
    }

    public void setRevision(final Revision revision) {
        this.revision = revision;
    }

    public Uint8 getUint8() {
        return this.uint8;
    }

    public void setUint8(final Uint8 uint8) {
        this.uint8 = uint8;
    }

    public Uint16 getUint16() {
        return this.uint16;
    }

    public void setUint16(final Uint16 uint16) {
        this.uint16 = uint16;
    }

    public Uint32 getUint32() {
        return this.uint32;
    }

    public void setUint32(final Uint32 uint32) {
        this.uint32 = uint32;
    }

    public Uint64 getUint64() {
        return this.uint64;
    }

    public void setUint64(final Uint64 uint64) {
        this.uint64 = uint64;
    }

    public YangVersion getYangVersion() {
        return this.yangVersion;
    }

    public void setYangVersion(final YangVersion yangVersion) {
        this.yangVersion = yangVersion;
    }
}
