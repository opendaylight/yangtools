/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import com.google.common.base.Splitter;
import java.math.BigDecimal;
import org.opendaylight.yangtools.yang.model.api.stmt.UnresolvedNumber;

/**
 * Utility class for manipulating YANG base and extended types implementation.
 */
public final class TypeUtils {
    public static final Splitter PIPE_SPLITTER = Splitter.on('|').trimResults();
    public static final Splitter TWO_DOTS_SPLITTER = Splitter.on("..").trimResults();

    // these objects are to compare whether range has MAX or MIN value
    // none of these values should appear as Yang number according to spec so they are safe to use
    private static final BigDecimal YANG_MIN_NUM = BigDecimal.valueOf(-Double.MAX_VALUE);
    private static final BigDecimal YANG_MAX_NUM = BigDecimal.valueOf(Double.MAX_VALUE);

    private TypeUtils() {
    }

    private static BigDecimal yangConstraintToBigDecimal(final Number number) {
        if (UnresolvedNumber.max().equals(number)) {
            return YANG_MAX_NUM;
        }
        if (UnresolvedNumber.min().equals(number)) {
            return YANG_MIN_NUM;
        }

        return new BigDecimal(number.toString());
    }

    public static int compareNumbers(final Number n1, final Number n2) {
        final BigDecimal num1 = yangConstraintToBigDecimal(n1);
        final BigDecimal num2 = yangConstraintToBigDecimal(n2);
        return new BigDecimal(num1.toString()).compareTo(new BigDecimal(num2.toString()));
    }

}
