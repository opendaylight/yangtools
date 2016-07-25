/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.base.MoreObjects;
import com.google.common.testing.EqualsTester;
import java.util.Objects;
import org.junit.Test;

public class EvenMoreObjectsTest {

    @Test
    public void test() {
        new EqualsTester()
                .addEqualityGroup(new Thing("hello", 123), new Thing("hello", 123))
                .addEqualityGroup(new Thing("hoi", 123), new Thing("hoi", 123))
                .addEqualityGroup(new Thing("hoi", null))
                .addEqualityGroup(new Thing(null, null))
                .testEquals();
    }

    static class Thing {

        String name;
        Integer age;

        @Override
        public boolean equals(Object obj) {
            return EvenMoreObjects.equalsHelper(this, obj,
                (one, another) -> Objects.equals(one.name, another.name) && Objects.equals(one.age, another.age));
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("name", name).add("age", age).toString();
        }

        Thing(String name, Integer age) {
            super();
            this.name = name;
            this.age = age;
        }

    }

}
