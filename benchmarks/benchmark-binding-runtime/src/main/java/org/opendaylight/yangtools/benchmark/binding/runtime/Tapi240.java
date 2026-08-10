/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.benchmark.binding.runtime;

import java.util.List;
import org.opendaylight.yang.gen.v1.urn.onf.otcc.yang.tapi.common.rev221121.TapiCommonData;
import org.opendaylight.yang.gen.v1.urn.onf.otcc.yang.tapi.connectivity.rev221121.TapiConnectivityData;
import org.opendaylight.yang.gen.v1.urn.onf.otcc.yang.tapi.digital.otn.rev221121.TapiDigitalOtnData;
import org.opendaylight.yang.gen.v1.urn.onf.otcc.yang.tapi.dsr.rev221121.TapiDsrData;
import org.opendaylight.yang.gen.v1.urn.onf.otcc.yang.tapi.equipment.rev221121.TapiEquipmentData;
import org.opendaylight.yang.gen.v1.urn.onf.otcc.yang.tapi.eth.rev221121.TapiEthData;
import org.opendaylight.yang.gen.v1.urn.onf.otcc.yang.tapi.notification.rev221121.TapiNotificationData;
import org.opendaylight.yang.gen.v1.urn.onf.otcc.yang.tapi.oam.rev221121.TapiOamData;
import org.opendaylight.yang.gen.v1.urn.onf.otcc.yang.tapi.path.computation.rev221121.TapiPathComputationData;
import org.opendaylight.yang.gen.v1.urn.onf.otcc.yang.tapi.photonic.media.rev221121.TapiPhotonicMediaData;
import org.opendaylight.yang.gen.v1.urn.onf.otcc.yang.tapi.streaming.rev221121.TapiStreamingData;
import org.opendaylight.yang.gen.v1.urn.onf.otcc.yang.tapi.topology.rev221121.TapiTopologyData;
import org.opendaylight.yang.gen.v1.urn.onf.otcc.yang.tapi.virtual.network.rev221121.TapiVirtualNetworkData;

final class Tapi240 {
    private Tapi240() {
        // hidden on purpose
    }

    static List<Class<?>> classes() {
        return List.of(
            TapiCommonData.class,
            TapiConnectivityData.class,
            TapiDigitalOtnData.class,
            TapiDsrData.class,
            TapiEquipmentData.class,
            TapiEthData.class,
            TapiNotificationData.class,
            TapiOamData.class,
            TapiPathComputationData.class,
            TapiPhotonicMediaData.class,
            TapiStreamingData.class,
            TapiTopologyData.class,
            TapiVirtualNetworkData.class);
    }
}