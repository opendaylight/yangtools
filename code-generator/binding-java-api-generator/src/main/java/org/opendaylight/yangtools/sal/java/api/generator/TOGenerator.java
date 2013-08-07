/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator;

import org.opendaylight.yangtools.sal.binding.model.api.CodeGenerator;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.sal.binding.model.api.Type;

public final class TOGenerator implements CodeGenerator {

    @Override
    public String generate(Type type) {
        if (type instanceof GeneratedTransferObject) {
            final GeneratedTransferObject genTO = (GeneratedTransferObject) type;
            final ClassTemplate template = new ClassTemplate(genTO);
            return template.generate();
        }
        return "";
    }

    @Override
    public boolean isAcceptable(Type type) {
        return type instanceof GeneratedTransferObject;
    }

    @Override
    public String getUnitName(Type type) {
        return type.getName();
    }

}
