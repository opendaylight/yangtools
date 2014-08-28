package org.opendaylight.yangtools.features.plugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.karaf.features.internal.model.Bundle;
import org.apache.karaf.features.internal.model.ConfigFile;
import org.apache.karaf.features.internal.model.Feature;
import org.apache.karaf.features.internal.model.Features;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.ops4j.pax.url.mvn.Parser;

import com.google.common.base.Preconditions;

/**
 * Goal which touches a timestamp file.
 *
 * @goal touch
 *
 * @phase process-sources
 */
public class MyMojo
    extends AbstractMojo
{

    /**
     * Location of the file.
     * @parameter expression="${project.build.resources[0].directory}/features.xml"
     * @required
     */
    private File featureFile;

    public void execute() throws MojoExecutionException {
        File pomFile = new File("pom.xml");
        FileInputStream stream;
        try {
            logFeature();
            stream = new FileInputStream(pomFile);
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(stream);
            for(Dependency d: model.getDependencies()) {
                getLog().info("Dependency " + dependencyToMvnURL(d));
            }
        } catch (Exception e) {
            getLog().error(e);
        }
    }

    protected Features getFeatures() throws JAXBException, IOException {
        final URL repoURL = featureFile.toURI().toURL();
        JAXBContext context;
        context = JAXBContext.newInstance(Features.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Object obj = unmarshaller.unmarshal(repoURL.openStream());
        Features features =(Features)  obj;
        return features;
     }

    protected void logFeature() throws JAXBException, IOException {
        Features features = getFeatures();
        for(Feature f : features.getFeature()) {
            getLog().info(f.getName() + " " + f.getVersion());
            for(Bundle b : f.getBundle()) {
               getLog().info(b.getLocation());
            }
            for(ConfigFile cf : f.getConfigfile()) {
                getLog().info(cf.getLocation());
            }
        }
        for(String r: features.getRepository()) {
            getLog().info(r);
            Parser url = new Parser(r);

        }
    }

    protected String  dependencyToMvnURL(Dependency dependency) {
        Preconditions.checkNotNull(dependency,"dependency must not be null");
        Preconditions.checkNotNull(dependency.getGroupId(),"dependency.getGroupId() must not be null");
        Preconditions.checkNotNull(dependency.getArtifactId(),"dependency.getArtifactId() must not be null , groupId: {} ",dependency.getGroupId());
        Preconditions.checkNotNull(dependency.getVersion(),"dependency.getVersion() must not be null, groupId: {} artifactId {}", dependency.getGroupId(), dependency.getArtifactId());
        String urlString = "mvn:" + dependency.getGroupId() +  "/" + dependency.getVersion();
        if(dependency.getType() != null) {
            urlString = urlString + "/" + dependency.getType();
            if(dependency.getClassifier() != null) {
                urlString = urlString + "/" + dependency.getClassifier();
            }
        }
        return urlString;
    }

    protected Dependency MvnURLtoDependency(String url) throws MalformedURLException {
        Parser parser = new Parser(url);
        Dependency dependency = new Dependency();
        dependency.setGroupId(parser.getGroup());
        dependency.setArtifactId(parser.getArtifact());
        dependency.setVersion(parser.getVersion());
        dependency.setType(parser.getType());
        dependency.setClassifier(parser.getClassifier());
        return dependency;
    }

    protected boolean matches(Dependency dependency ,Parser mvnURL) {
        Preconditions.checkNotNull(dependency,"dependency must not be null");
        Preconditions.checkNotNull(dependency.getGroupId(),"dependency.getGroupId() must not be null");
        Preconditions.checkNotNull(dependency.getArtifactId(),"dependency.getArtifactId() must not be null , groupId: {} ",dependency.getGroupId());
        Preconditions.checkNotNull(mvnURL,"mvnURL must not be null");
        Preconditions.checkNotNull(mvnURL.getGroup(),"mvnURL.getGroup() must not be null");
        Preconditions.checkNotNull(mvnURL.getArtifact(),"mvnURL.getArtifactId() must not be null , groupId: {} ",mvnURL.getGroup());
        boolean matches = false;
        return matches;
    }
}
