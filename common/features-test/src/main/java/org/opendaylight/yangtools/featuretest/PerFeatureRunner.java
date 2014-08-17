package org.opendaylight.yangtools.featuretest;

import static org.opendaylight.yangtools.featuretest.Constants.ORG_OPENDAYLIGHT_FEATURETEST_FEATURENAME_PROP;
import static org.opendaylight.yangtools.featuretest.Constants.ORG_OPENDAYLIGHT_FEATURETEST_FEATUREVERSION_PROP;
import static org.opendaylight.yangtools.featuretest.Constants.ORG_OPENDAYLIGHT_FEATURETEST_URI_PROP;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Collection;

import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.ops4j.pax.exam.junit.PaxExam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class PerFeatureRunner extends Runner {
    private static final Logger LOG = LoggerFactory.getLogger(PerFeatureRunner.class);
    PaxExam delegate;
    URL repoURL;
    private String featureName;
    private String featureVersion;

    public PerFeatureRunner(URL repoURL, String featureName, String featureVersion, Class<?> testClass) throws InitializationError {
        System.setProperty(ORG_OPENDAYLIGHT_FEATURETEST_URI_PROP, repoURL.toString());
        System.setProperty(ORG_OPENDAYLIGHT_FEATURETEST_FEATURENAME_PROP, featureName);
        System.setProperty(ORG_OPENDAYLIGHT_FEATURETEST_FEATUREVERSION_PROP,featureVersion);
        this.delegate = new PaxExam(testClass);
        this.featureName = featureName;
        this.featureVersion = featureVersion;
        this.repoURL = repoURL;
    }

    @Override
    public Description getDescription() {
        return Util.convertDescription(repoURL,featureName,featureVersion,delegate.getDescription());
    }

    @Override
    public void run(RunNotifier notifier) {
        LOG.info("About to run test for feature: {} {}", featureName, featureVersion);
        System.setProperty(ORG_OPENDAYLIGHT_FEATURETEST_URI_PROP, repoURL.toString());
        System.setProperty(ORG_OPENDAYLIGHT_FEATURETEST_FEATURENAME_PROP, featureName);
        System.setProperty(ORG_OPENDAYLIGHT_FEATURETEST_FEATUREVERSION_PROP,featureVersion);
        delegate.run(new PerFeatureRunNotifier(repoURL,featureName, featureVersion,notifier));
    }

    /**
     * @return
     * @see org.junit.runner.Runner#testCount()
     */
    public int testCount() {
        return delegate.testCount();
    }

    /**
     * @param filter
     * @throws NoTestsRemainException
     * @see org.ops4j.pax.exam.junit.PaxExam#filter(org.junit.runner.manipulation.Filter)
     */
    public void filter(Filter filter) throws NoTestsRemainException {
        delegate.filter(filter);
    }

    /**
     * @param sorter
     * @see org.ops4j.pax.exam.junit.PaxExam#sort(org.junit.runner.manipulation.Sorter)
     */
    public void sort(Sorter sorter) {
        delegate.sort(sorter);
    }

    /**
     * @return
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return delegate.toString();
    }

    /**
     * @return the repoURL
     */
    public URL getRepoURL() {
        return repoURL;
    }

    /**
     * @return the featureName
     */
    public String getFeatureName() {
        return featureName;
    }

    /**
     * @return the featureVersion
     */
    public String getFeatureVersion() {
        return featureVersion;
    }



}
