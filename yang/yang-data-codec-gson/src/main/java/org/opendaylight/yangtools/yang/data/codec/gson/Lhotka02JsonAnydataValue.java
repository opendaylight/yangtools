/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.common.annotations.Beta;
import com.google.gson.stream.JsonReader;
import java.io.IOException;

/**
 * A string-based representation of anydata encoded into JSON according draft-lhotka-netmod-yang-json-02. Note this
 * implies how data values are encoded.
 *
 * @author Robert Varga
 */
@Beta
public final class Lhotka02JsonAnydataValue extends JsonAnydataValue<Lhotka02JsonAnydataValue> {

    private Lhotka02JsonAnydataValue(final String str) {
        super(str);
    }

    static Lhotka02JsonAnydataValue create(final JsonReader in) throws IOException {
        return create(in, Lhotka02JsonAnydataValue::new);
    }

    @Override
    public Class<Lhotka02JsonAnydataValue> getValueType() {
        return Lhotka02JsonAnydataValue.class;
    }
}
