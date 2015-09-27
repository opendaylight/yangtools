/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import com.google.common.base.Preconditions;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;

class StatementIdentifier {

    private final QName name;
    private final String argument;

    StatementIdentifier(@Nonnull final QName name, @Nullable final String argument) {
        this.name = Preconditions.checkNotNull(name);
        this.argument = argument;
    }

    @Nonnull QName getName() {
        return name;
    }

    @Nullable String getArgument() {
        return argument;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + Objects.hashCode(argument);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        StatementIdentifier other = (StatementIdentifier) obj;
        if (!name.equals(other.name)) {
            return false;
        }
        return Objects.equals(argument, other.argument);
    }

    @Override
    public String toString() {
        return "StatementIdentifier [name=" + name + ", argument=" + argument + "]";
    }
}
