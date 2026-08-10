/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.benchmark.binding.runtime;

import java.util.List;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.alarms.types.rev181121.OpenconfigAlarmTypesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.ethernet.rev220420.OpenconfigIfEthernetData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev210406.OpenconfigInterfacesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.lldp.rev181121.OpenconfigLldpData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.lldp.types.rev181121.OpenconfigLldpTypesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.openconfig.ext.rev200616.OpenconfigExtensionsData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.openconfig.types.rev190416.OpenconfigTypesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.platform.port.rev211001.OpenconfigPlatformPortData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.platform.rev220610.OpenconfigPlatformData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.platform.transceiver.rev210729.OpenconfigPlatformTransceiverData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.platform.types.rev220327.OpenconfigPlatformTypesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.terminal.device.rev210729.OpenconfigTerminalDeviceData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.transport.line.common.rev190603.OpenconfigTransportLineCommonData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.transport.types.rev210729.OpenconfigTransportTypesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.types.yang.rev210714.OpenconfigYangTypesData;

final class OpenConfig240119 {
    private OpenConfig240119() {
        // hidden on purpose
    }

    static List<Class<?>> classes() {
        return List.of(
            OpenconfigAlarmTypesData.class,
            OpenconfigIfEthernetData.class,
            OpenconfigInterfacesData.class,
            OpenconfigYangTypesData.class,
            OpenconfigTypesData.class,
            OpenconfigExtensionsData.class,
            OpenconfigLldpData.class,
            OpenconfigLldpTypesData.class,
            OpenconfigPlatformPortData.class,
            OpenconfigPlatformData.class,
            OpenconfigPlatformTypesData.class,
            OpenconfigPlatformTransceiverData.class,
            OpenconfigTransportTypesData.class,
            OpenconfigTransportLineCommonData.class,
            OpenconfigTerminalDeviceData.class);
    }
}
