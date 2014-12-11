/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import java.io.IOException;

/**
 * Common interface definition for Json writers
 */

public interface JSONWriter {

    public void setIndent(String indent);

    public void beginArray() throws IOException;

    public void endArray() throws IOException;

    public void beginObject() throws IOException;

    public void endObject() throws IOException;

    public void name(String name) throws IOException;

    public void value(String value) throws IOException;

    public void nullValue() throws IOException;

    public void value(boolean value) throws IOException;

    public void value(double value) throws IOException;

    public void value(long value) throws IOException;

    public void value(Number value) throws IOException;

    public void flush() throws IOException;

    public void close() throws IOException;
}
