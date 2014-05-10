/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.sal.binding.generator.util;

import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;

/**
 * Implementation of the SourceCodeGenerator interface that does nothing.
 *
 * @author Thomas Pantelis
 */
public class NullSourceCodeGenerator implements SourceCodeGenerator {

    @Override
    public void appendField( CtField field, String value ) {
    }

    @Override
    public void appendMethod( CtMethod method, String code ) {
    }

    @Override
    public void outputGeneratedSource( CtClass ctClass ) {
    }
}
