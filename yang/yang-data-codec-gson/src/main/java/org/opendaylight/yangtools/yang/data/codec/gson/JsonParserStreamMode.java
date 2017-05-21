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
 * Set of known parser modes.
 *
 * @author Robert Varga
 *
 * @param <T> Anydata value representation type
 */
@Beta
public abstract class JsonParserStreamMode<T extends JsonAnydataValue<T>> {
    public static final class Lhotka02 extends JsonParserStreamMode<Lhotka02JsonAnydataValue> {
        Lhotka02() {
            // Hidden on purpose
        }

        @Override
        Lhotka02JsonAnydataValue createAnydataValue(final JsonReader in) throws IOException {
            return Lhotka02JsonAnydataValue.create(in);
        }
    }

    // FIXME: BUG-8083: define a mode for RFC7891
    public static final Lhotka02 DRAFT_LHOTKA_NETMOD_YANG_JSON_O2 = new Lhotka02();

    JsonParserStreamMode() {
        // Hidden on purpose
    }

    abstract T createAnydataValue(final JsonReader in) throws IOException;
}
