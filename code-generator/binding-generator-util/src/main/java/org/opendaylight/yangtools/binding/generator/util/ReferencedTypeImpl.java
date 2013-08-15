/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.util;

/**
 * 
 * Wraps combination of <code>packageName</code> and <code>name</code> to the
 * object representation
 * 
 */
public final class ReferencedTypeImpl extends AbstractBaseType {

    /**
     * Creates instance of this class with concrete package name and type name
     * 
     * @param packageName
     *            string with the package name
     * @param name
     *            string with the name for referenced type
     */
    public ReferencedTypeImpl(String packageName, String name) {
        super(packageName, name);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ReferencedTypeImpl [packageName=");
        builder.append(getPackageName());
        builder.append(", name=");
        builder.append(getName());
        builder.append("]");
        return builder.toString();
    }
}
