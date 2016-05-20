/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opendaylight.yangtools.xsd.regex;

import java.util.Arrays;
import java.util.EmptyStackException;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * A specialized minimal implementation of Stack<Integer>, which operations on simple ints.
 */
@NotThreadSafe
final class IntStack {
    private static final int DEFAULT_SIZE = 10;
    private static final int INCREMENT = 10;
    private int[] items;
    private int count = 0;

    IntStack() {
        this(DEFAULT_SIZE);
    }

    IntStack(final int size) {
        items = new int[size];
    }

    void push(final int item) {
        if (count == items.length) {
            items = Arrays.copyOf(items, items.length + INCREMENT);
        }

        items[count++] = item;
    }

    int pop() {
        if (count == 0) {
            throw new EmptyStackException();
        }

        return items[--count];
    }
}
