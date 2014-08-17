package org.opendaylight.yangtools.featuretest;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Collection;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;

public class PerFeatureRunNotifier extends RunNotifier {
    private RunNotifier delegate;
    private URL repoURL;
    private String featureName;
    private  String featureVersion;

    public PerFeatureRunNotifier(URL repoURL, String featureName, String featureVersion,RunNotifier delegate) {
        this.delegate = delegate;
        this.repoURL = repoURL;
        this.featureName = featureName;
        this.featureVersion = featureVersion;
    }

    private Failure convertFailure(Failure failure) {
        return new Failure(Util.convertDescription(repoURL,featureName,featureVersion,failure.getDescription()),failure.getException());
    }

    /**
     * @param listener
     * @see org.junit.runner.notification.RunNotifier#addListener(org.junit.runner.notification.RunListener)
     */
    public void addListener(RunListener listener) {
        delegate.addListener(listener);
    }

    /**
     * @param listener
     * @see org.junit.runner.notification.RunNotifier#removeListener(org.junit.runner.notification.RunListener)
     */
    public void removeListener(RunListener listener) {
        delegate.removeListener(listener);
    }

    /**
     * @return
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return delegate.hashCode();
    }

    /**
     * @param description
     * @see org.junit.runner.notification.RunNotifier#fireTestRunStarted(org.junit.runner.Description)
     */
    public void fireTestRunStarted(Description description) {
        delegate.fireTestRunStarted(Util.convertDescription(repoURL,featureName,featureVersion,description));
    }

    /**
     * @param result
     * @see org.junit.runner.notification.RunNotifier#fireTestRunFinished(org.junit.runner.Result)
     */
    public void fireTestRunFinished(Result result) {
        delegate.fireTestRunFinished(result);
    }

    /**
     * @param description
     * @throws StoppedByUserException
     * @see org.junit.runner.notification.RunNotifier#fireTestStarted(org.junit.runner.Description)
     */
    public void fireTestStarted(Description description)
            throws StoppedByUserException {
        delegate.fireTestStarted(Util.convertDescription(repoURL,featureName,featureVersion,description));
    }

    /**
     * @param obj
     * @return
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    /**
     * @param failure
     * @see org.junit.runner.notification.RunNotifier#fireTestFailure(org.junit.runner.notification.Failure)
     */
    public void fireTestFailure(Failure failure) {
        delegate.fireTestFailure(convertFailure(failure));
    }

    /**
     * @param failure
     * @see org.junit.runner.notification.RunNotifier#fireTestAssumptionFailed(org.junit.runner.notification.Failure)
     */
    public void fireTestAssumptionFailed(Failure failure) {
        delegate.fireTestAssumptionFailed(convertFailure(failure));
    }

    /**
     * @param description
     * @see org.junit.runner.notification.RunNotifier#fireTestIgnored(org.junit.runner.Description)
     */
    public void fireTestIgnored(Description description) {
        delegate.fireTestIgnored(Util.convertDescription(repoURL,featureName,featureVersion,description));
    }

    /**
     * @param description
     * @see org.junit.runner.notification.RunNotifier#fireTestFinished(org.junit.runner.Description)
     */
    public void fireTestFinished(Description description) {
        delegate.fireTestFinished(Util.convertDescription(repoURL,featureName,featureVersion,description));
    }

    /**
     *
     * @see org.junit.runner.notification.RunNotifier#pleaseStop()
     */
    public void pleaseStop() {
        delegate.pleaseStop();
    }

    /**
     * @param listener
     * @see org.junit.runner.notification.RunNotifier#addFirstListener(org.junit.runner.notification.RunListener)
     */
    public void addFirstListener(RunListener listener) {
        delegate.addFirstListener(listener);
    }

    /**
     * @return
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return delegate.toString();
    }


}
