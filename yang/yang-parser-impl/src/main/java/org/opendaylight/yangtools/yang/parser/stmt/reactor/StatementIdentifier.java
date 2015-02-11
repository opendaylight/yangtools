/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;

class StatementIdentifier {

    private final @Nonnull QName name;
    private final @Nullable String argument;

    public StatementIdentifier(QName name, String argument) {
        super();
        this.name = name;
        this.argument = argument;
    }

    public QName getName() {
        return name;
    }

    public String getArgument() {
        return argument;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result +  name.hashCode();
        result = prime * result + ((argument == null) ? 0 : argument.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
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
        if (argument == null) {
            if (other.argument != null) {
                return false;
            }
        } else if (!argument.equals(other.argument)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "StatementIdentifier [name=" + name + ", argument=" + argument + "]";
    }


}
