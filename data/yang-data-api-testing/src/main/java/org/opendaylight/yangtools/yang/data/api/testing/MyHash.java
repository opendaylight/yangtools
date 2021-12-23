/*
 * Copyright (c) 2021 Vratko Polak and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.testing;

/**
 * Just a place to store a utility function.
 */
public final class MyHash {

    /**
     * Utility classes should have constructors hidden.
     */
    private MyHash() {
    }

    /**
     * Turn int into another int in a fast and somewhat pseudorandom way.
     *
     * <p>Adapted from https://stackoverflow.com/a/9625053
     *
     * <p>This method was written by Doug Lea with assistance from members of JCP
     * JSR-166 Expert Group and released to the public domain, as explained at
     * http://creativecommons.org/licenses/publicdomain
     *
     * <p>As of 2010/06/11, this method is identical to the (package private) hash
     * method in OpenJDK 7's java.util.HashMap class.
     */
    public static int myHash(int iii) {
        iii ^= (iii >>> 20) ^ (iii >>> 12);
        return iii ^ (iii >>> 7) ^ (iii >>> 4);
    }

}
