/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.featuretest;


import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.karaf.features.internal.model.Feature;
import org.apache.karaf.features.internal.model.Features;
import org.apache.karaf.tooling.url.CustomBundleURLStreamHandlerFactory;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class PerRepoTestRunner extends ParentRunner<PerFeatureRunner> {
    private static final String REPO_RECURSE = "repo.recurse";
    private static final Logger LOG = LoggerFactory.getLogger(PerRepoTestRunner.class);
    private static final String FEATURES_FILENAME = "features.xml";
    protected final List<PerFeatureRunner> children = new ArrayList<PerFeatureRunner>();


    public PerRepoTestRunner(final Class<?> testClass) throws InitializationError {
        super(testClass);
        try {
            URL.setURLStreamHandlerFactory(new CustomBundleURLStreamHandlerFactory());
            URL repoURL = getClass().getClassLoader().getResource(FEATURES_FILENAME);
            boolean recursive = Boolean.getBoolean(REPO_RECURSE);
            LOG.info("Creating test runners for repoURL {} recursive {}",repoURL,recursive);
            children.addAll(runnersFromRepoURL(repoURL,testClass,recursive));
        } catch (Exception e) {
            throw new InitializationError(e);
        }
    }

    protected List<PerFeatureRunner> runnersFromRepoURL(final URL repoURL,final Class<?> testClass,boolean recursive) throws JAXBException, IOException, InitializationError {
        if(recursive) {
            return recursiveRunnersFromRepoURL(repoURL,testClass);
        } else {
            return runnersFromRepoURL(repoURL,testClass);
        }
    }

    protected List<PerFeatureRunner> runnersFromRepoURL(final URL repoURL,final Class<?> testClass) throws JAXBException, IOException, InitializationError {
        List<PerFeatureRunner> runners = new ArrayList<PerFeatureRunner>();
        Features features = getFeatures(repoURL);
        runners.addAll(runnersFromFeatures(repoURL,features,testClass));
        return runners;
    }

    protected List<PerFeatureRunner> recursiveRunnersFromRepoURL(final URL repoURL,final Class<?> testClass) throws JAXBException, IOException, InitializationError {
        List<PerFeatureRunner> runners = new ArrayList<PerFeatureRunner>();
        Features features = getFeatures(repoURL);
        runners.addAll(runnersFromRepoURL(repoURL,testClass));
        for(String repoString: features.getRepository()) {
            URL subRepoURL = new URL(repoString);
            runners.addAll(recursiveRunnersFromRepoURL(subRepoURL,testClass));
        }
        return runners;
    }

    protected List<PerFeatureRunner> runnersFromFeatures(final URL repoURL, final Features features,final Class<?> testClass) throws InitializationError {
        List<PerFeatureRunner> runners = new ArrayList<PerFeatureRunner>();
        List<Feature> featureList = new ArrayList<Feature>();
        featureList = features.getFeature();
        for(Feature f : featureList) {
            runners.add(new PerFeatureRunner(repoURL, f.getName(), f.getVersion(),testClass));
        }
        return runners;
    }

    /**
     * @param repoURL
     * @return
     * @throws JAXBException
     * @throws IOException
     */
    protected Features getFeatures(final URL repoURL) throws JAXBException,
            IOException {
        JAXBContext context;
        context = JAXBContext.newInstance(Features.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Object obj = unmarshaller.unmarshal(repoURL.openStream());
        Features features =(Features)  obj;
        return features;
    }

    @Override
    protected List<PerFeatureRunner> getChildren() {
        return children;
    }

    @Override
    protected Description describeChild(final PerFeatureRunner child) {
        return child.getDescription();
    }

    @Override
    protected void runChild(PerFeatureRunner child, RunNotifier notifier) {
        LOG.info("About to run test for {}",child.repoURL);
        child.run(notifier);
    }

    /* (non-Javadoc)
     * @see org.junit.runner.Runner#testCount()
     */
    @Override
    public int testCount() {
        return super.testCount()*children.size();
    }



}
