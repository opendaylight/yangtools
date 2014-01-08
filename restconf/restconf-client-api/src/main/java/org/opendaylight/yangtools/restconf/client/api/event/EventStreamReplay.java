package org.opendaylight.yangtools.restconf.client.api.event;

import java.util.Date;

import com.google.common.base.Optional;

public interface EventStreamReplay extends Iterable<EventReplay<?>> {

    String getStreamIdentifier();
    Optional<Date> getStartTime();
    Optional<Date> getEndTime();
}
