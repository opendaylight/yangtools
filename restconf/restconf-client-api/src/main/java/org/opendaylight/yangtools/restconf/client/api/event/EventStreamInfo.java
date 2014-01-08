package org.opendaylight.yangtools.restconf.client.api.event;

import java.util.Date;

import org.opendaylight.yangtools.concepts.Identifiable;

public interface EventStreamInfo extends Identifiable<String> {

    /**
     * 
     * @return name of event stream.
     */
    @Override
    public String getIdentifier();

    public String getDescription();

    boolean isReplaySupported();

    Date getReplayLogCreationTime();
}
