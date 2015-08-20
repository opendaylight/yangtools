/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator;

import org.opendaylight.yangtools.sal.binding.model.api.CodeGenerator;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;

/**
 * 
 * Transformator of the data from the virtual form to JAVA programming language.
 * The result source code represent java class. For generation of the source
 * code is used the template written in XTEND language.
 * 
 */
public final class BuilderGenerator implements CodeGenerator {

    /**
     * Constant used as sufix for builder name.
     */
    public static final String BUILDER = "Builder";

    /**
     * Passes via list of implemented types in <code>type</code>.
     * 
     * @param type
     *            JAVA <code>Type</code>
     * @return boolean value which is true if any of implemented types is of the
     *         type <code>Augmentable</code>.
     */
    @Override
    public boolean isAcceptable(Type type) {
        if (type instanceof GeneratedType && !(type instanceof GeneratedTransferObject)) {
            for (Type t : ((GeneratedType) type).getImplements()) {
                // "rpc" and "grouping" elements do not implement Augmentable
                if (t.getFullyQualifiedName().equals(Augmentable.class.getName())) {
                    return true;
                } else if (t.getFullyQualifiedName().equals(Augmentation.class.getName())) {
                    return true;
                }

            }
        }
        return false;
    }

    /**
     * Generates JAVA source code for generated type <code>Type</code>. The code
     * is generated according to the template source code template which is
     * written in XTEND language.
     */
    @Override
    public String generate(Type type) {
        if (type instanceof GeneratedType && !(type instanceof GeneratedTransferObject)) {
            final GeneratedType genType = (GeneratedType) type;
            final BuilderTemplate template = new BuilderTemplate(genType);
            return template.generate();
        }
        return "";
    }

    @Override
    public String getUnitName(Type type) {
        return type.getName() + BUILDER;
    }

}
