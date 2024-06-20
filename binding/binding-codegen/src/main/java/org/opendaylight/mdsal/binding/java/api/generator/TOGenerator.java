/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.binding.model.api.CodeGenerator;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.Type;

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
        if (type instanceof GeneratedTransferObject genTO) {
            if (genTO.isUnionType()) {
                return new UnionTemplate(genTO).generate();
            } else if (genTO.isTypedef()) {
                return new ClassTemplate(genTO).generate();
            } else {
                final var featureDataRoot = BindingTypes.extractYangFeatureDataRoot(genTO);
                return featureDataRoot == null ? new ListKeyTemplate(genTO).generate()
                    : new FeatureTemplate(genTO, featureDataRoot).generate();
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
