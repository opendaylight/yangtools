package org.opendaylight.yangtools.restconf.client.api.event;

import java.util.Date;

import org.opendaylight.yangtools.yang.binding.Notification;

public interface EventReplay<T extends Notification> {

    Date getEventTime();
    Class<T> getEventType();
    T getEvent();
}
