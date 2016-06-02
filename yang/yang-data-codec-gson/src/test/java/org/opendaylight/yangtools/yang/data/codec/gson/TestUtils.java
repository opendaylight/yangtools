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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;

public class TestUtils {

    private TestUtils() {
    }

    static SchemaContext loadModules(final String resourceDirectory) throws IOException, URISyntaxException,
            ReactorException {
        URI path = StreamToNormalizedNodeTest.class.getResource(resourceDirectory).toURI();
        final File testDir = new File(path);
        final String[] fileList = testDir.list();
        final List<File> testFiles = new ArrayList<>();
        if (fileList == null) {
            throw new FileNotFoundException(resourceDirectory);
        }
        for (String fileName : fileList) {
            if (!new File(testDir, fileName).isDirectory()) {
                testFiles.add(new File(testDir, fileName));
            }
        }
        return parseYangSources(testFiles);
    }

    public static SchemaContext parseYangSources(StatementStreamSource... sources)
            throws SourceException, ReactorException {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR
                .newBuild();
        reactor.addSources(sources);

        return reactor.buildEffective();
    }

    public static SchemaContext parseYangSources(File... files) throws SourceException, ReactorException, FileNotFoundException {

        StatementStreamSource[] sources = new StatementStreamSource[files.length];

        for (int i = 0; i<files.length; i++) {
            sources[i] = new YangStatementSourceImpl(new FileInputStream(files[i]));
        }

        return parseYangSources(sources);
    }

    public static SchemaContext parseYangSources(Collection<File> files) throws SourceException, ReactorException, FileNotFoundException {
        return parseYangSources(files.toArray(new File[files.size()]));
    }


    public static SchemaContext parseYangStreams(List<InputStream> streams)
            throws SourceException, ReactorException {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR
                .newBuild();
        return reactor.buildEffective(streams);
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
