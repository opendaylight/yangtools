/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.Assert.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;

public class TestUtils {

    private TestUtils() {
    }

    static String loadTextFile(final File file) throws IOException {
        FileReader fileReader = new FileReader(file);
        BufferedReader bufReader = new BufferedReader(fileReader);

        String line = null;
        StringBuilder result = new StringBuilder();
        while ((line = bufReader.readLine()) != null) {
            result.append(line);
        }
        bufReader.close();
        return result.toString();
    }

    static String loadTextFile(final String relativePath) throws IOException, URISyntaxException {
        return loadTextFile(new File(TestUtils.class.getResource(relativePath).toURI()));
    }

    static JsonObject childObject(final JsonObject jsonObject, final String... names) {
        for (String name : names) {
            JsonObject childJsonObject = jsonObject.getAsJsonObject(name);
            if (childJsonObject != null) {
                return childJsonObject;
            }
        }
        return null;
    }

    static JsonPrimitive childPrimitive(final JsonObject jsonObject, final String... names) {
        for (String name : names) {
            JsonPrimitive childJsonPrimitive = jsonObject.getAsJsonPrimitive(name);
            if (childJsonPrimitive != null) {
                return childJsonPrimitive;
            }
        }
        return null;
    }

    static JsonArray childArray(final JsonObject jsonObject, final String... names) {
        for (String name : names) {
            JsonArray childJsonArray = jsonObject.getAsJsonArray(name);
            if (childJsonArray != null) {
                return childJsonArray;
            }
        }
        return null;
    }

    static JsonObject resolveCont1(String jsonOutput) {
        JsonParser parser = new JsonParser();
        JsonElement rootElement = parser.parse(jsonOutput);
        assertTrue(rootElement.isJsonObject());
        JsonObject rootObject = rootElement.getAsJsonObject();
        JsonObject cont1 = childObject(rootObject, "complexjson:cont1", "cont1");
        return cont1;
    }

}
