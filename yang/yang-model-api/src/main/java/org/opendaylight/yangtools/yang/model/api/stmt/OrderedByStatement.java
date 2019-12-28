/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static com.google.common.base.Verify.verifyNotNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

public interface OrderedByStatement extends DeclaredStatement<OrderedByStatement.Ordering> {
    @NonNullByDefault
    public enum Ordering {
        SYSTEM("system"),
        USER("user");

        private String argumentString;

        Ordering(final String argumentString) {
            this.argumentString = argumentString;
        }

        public String getArgumentString() {
            return argumentString;
        }

        public static Ordering forArgumentString(final String argumentString) {
            switch (argumentString) {
                case "system":
                    return SYSTEM;
                case "user":
                    return USER;
                default:
                    throw new IllegalArgumentException("Invalid ordering string '" + argumentString + "'");
            }
        }
    }

    default @NonNull Ordering getValue() {
        // FIXME: YANGTOOLS-908: verifyNotNull() should not be needed here
        return verifyNotNull(argument());
    }
}
