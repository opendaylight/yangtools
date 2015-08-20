/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.union.rev150121;

/**
 * The purpose of generated class in src/main/java for Union types is to create new instances of unions from a string representation.
 * In some cases it is very difficult to automate it since there can be unions such as (uint32 - uint16), or (string - uint32).
 * 
 * The reason behind putting it under src/main/java is:
 * This class is generated in form of a stub and needs to be finished by the user. This class is generated only once to prevent
 * loss of user code.
 * 
 */
public class UnionTestTypeBuilder {

    public static UnionTestType getDefaultInstance(java.lang.String defaultValue) {
        if (defaultValue.length() > 8) {
            return new UnionTestType(new LowestLevel1(defaultValue));
        } else {
            return new UnionTestType(new LowestLevel2(defaultValue));
        }
    }

}
