/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.featuretest;

public final class Constants {

    private Constants() {
        // Noop constructor
    }

    /*
     * Named of System Properties we need to set in PerFeatureRunner and read in SingleFeatureTest
     */
    public static final String ORG_OPENDAYLIGHT_FEATURETEST_URI_PROP = "org.opendaylight.featuretest.uri";
    public static final String ORG_OPENDAYLIGHT_FEATURETEST_FEATURENAME_PROP = "org.opendaylight.featuretest.featurename";
    public static final String ORG_OPENDAYLIGHT_FEATURETEST_FEATUREVERSION_PROP = "org.opendaylight.featuretest.featureversion";
}
