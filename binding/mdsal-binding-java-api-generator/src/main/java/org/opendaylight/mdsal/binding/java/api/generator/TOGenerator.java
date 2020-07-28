/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import org.opendaylight.mdsal.binding.model.api.CodeGenerator;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.Type;

/**
 * Transformator of the data from the virtual form to JAVA source code. The result source code represents JAVA class.
 * For generating of the source code is used the template written in XTEND language.
 */
public final class TOGenerator implements CodeGenerator {
    /**
     * Generates JAVA source code for generated type <code>Type</code>. The code is generated according to the template
     * source code template which is written in XTEND language.
     */
    @Override
    public String generate(final Type type) {
        if (type instanceof GeneratedTransferObject) {
            final GeneratedTransferObject genTO = (GeneratedTransferObject) type;
            if (genTO.isUnionType()) {
                final UnionTemplate template = new UnionTemplate(genTO);
                return template.generate();
            } else if (genTO.isUnionTypeBuilder()) {
                final UnionBuilderTemplate template = new UnionBuilderTemplate(genTO);
                return template.generate();
            } else if (genTO.isTypedef()) {
                final ClassTemplate template = new ClassTemplate(genTO);
                return template.generate();
            } else {
                final ListKeyTemplate template = new ListKeyTemplate(genTO);
                return template.generate();
            }
        }
        return "";
    }

    @Override
    public boolean isAcceptable(final Type type) {
        return type instanceof GeneratedTransferObject;
    }

    @Override
    public String getUnitName(final Type type) {
        return type.getName();
    }
}
