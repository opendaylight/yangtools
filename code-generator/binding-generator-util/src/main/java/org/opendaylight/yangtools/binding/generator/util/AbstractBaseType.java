/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.util;

import org.opendaylight.yangtools.sal.binding.model.api.Type;

/**
 * It is used only as ancestor for other <code>Type</code>s
 * 
 */
public class AbstractBaseType implements Type {

    /**
     * Name of the package to which this <code>Type</code> belongs.
     */
    private final String packageName;

    /**
     * Name of this <code>Type</code>.
     */
    private final String name;

    @Override
    public String getPackageName() {
        return packageName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFullyQualifiedName() {
        if (packageName.isEmpty()) {
            return name;
        } else {
            return packageName + "." + name;
        }
    }

    /**
     * Constructs the instance of this class with the concrete package name type
     * name.
     * 
     * @param pkName
     *            string with the package name to which this <code>Type</code>
     *            belongs
     * @param name
     *            string with the name for this <code>Type</code>
     */
    protected AbstractBaseType(String pkName, String name) {
        this.packageName = pkName;
        this.name = name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Type other = (Type) obj;
        if (name == null) {
            if (other.getName() != null)
                return false;
        } else if (!name.equals(other.getName()))
            return false;
        if (packageName == null) {
            if (other.getPackageName() != null)
                return false;
        } else if (!packageName.equals(other.getPackageName()))
            return false;
        return true;
    }

    @Override
    public String toString() {
        if (packageName.isEmpty()) {
            return "Type (" + name + ")";
        }
        return "Type (" + packageName + "." + name + ")";
    }
}
