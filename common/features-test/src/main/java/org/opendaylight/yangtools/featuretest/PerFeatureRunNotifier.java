/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.featuretest;

import java.net.URL;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;

import com.google.common.base.Preconditions;

public class PerFeatureRunNotifier extends RunNotifier {
    private final RunNotifier delegate;
    private final URL repoURL;
    private final String featureName;
    private  final String featureVersion;

    public PerFeatureRunNotifier(final URL repoURL, final String featureName, final String featureVersion,final RunNotifier delegate) {
        Preconditions.checkNotNull(repoURL);
        Preconditions.checkNotNull(featureName);
        Preconditions.checkNotNull(featureVersion);
        Preconditions.checkNotNull(delegate);
        this.delegate = delegate;
        this.repoURL = repoURL;
        this.featureName = featureName;
        this.featureVersion = featureVersion;
    }

    private Failure convertFailure(final Failure failure) {
        return new Failure(Util.convertDescription(repoURL,featureName,featureVersion,failure.getDescription()),failure.getException());
    }

    /**
     * @param listener
     * @see org.junit.runner.notification.RunNotifier#addListener(org.junit.runner.notification.RunListener)
     */
    @Override
    public void addListener(RunListener listener) {
        delegate.addListener(listener);
    }

    /**
     * @param listener
     * @see org.junit.runner.notification.RunNotifier#removeListener(org.junit.runner.notification.RunListener)
     */
    @Override
    public void removeListener(RunListener listener) {
        delegate.removeListener(listener);
    }

    /**
     * @return
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    /**
     * @param description
     * @see org.junit.runner.notification.RunNotifier#fireTestRunStarted(org.junit.runner.Description)
     */
    @Override
    public void fireTestRunStarted(Description description) {
        delegate.fireTestRunStarted(Util.convertDescription(repoURL,featureName,featureVersion,description));
    }

    /**
     * @param result
     * @see org.junit.runner.notification.RunNotifier#fireTestRunFinished(org.junit.runner.Result)
     */
    @Override
    public void fireTestRunFinished(Result result) {
        delegate.fireTestRunFinished(result);
    }

    /**
     * @param description
     * @throws StoppedByUserException
     * @see org.junit.runner.notification.RunNotifier#fireTestStarted(org.junit.runner.Description)
     */
    @Override
    public void fireTestStarted(Description description)
            throws StoppedByUserException {
        delegate.fireTestStarted(Util.convertDescription(repoURL,featureName,featureVersion,description));
    }

    /**
     * @param obj
     * @return
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    /**
     * @param failure
     * @see org.junit.runner.notification.RunNotifier#fireTestFailure(org.junit.runner.notification.Failure)
     */
    @Override
    public void fireTestFailure(Failure failure) {
        delegate.fireTestFailure(convertFailure(failure));
    }

    /**
     * @param failure
     * @see org.junit.runner.notification.RunNotifier#fireTestAssumptionFailed(org.junit.runner.notification.Failure)
     */
    @Override
    public void fireTestAssumptionFailed(Failure failure) {
        delegate.fireTestAssumptionFailed(convertFailure(failure));
    }

    /**
     * @param description
     * @see org.junit.runner.notification.RunNotifier#fireTestIgnored(org.junit.runner.Description)
     */
    @Override
    public void fireTestIgnored(Description description) {
        delegate.fireTestIgnored(Util.convertDescription(repoURL,featureName,featureVersion,description));
    }

    /**
     * @param description
     * @see org.junit.runner.notification.RunNotifier#fireTestFinished(org.junit.runner.Description)
     */
    @Override
    public void fireTestFinished(Description description) {
        delegate.fireTestFinished(Util.convertDescription(repoURL,featureName,featureVersion,description));
    }

    /**
     *
     * @see org.junit.runner.notification.RunNotifier#pleaseStop()
     */
    @Override
    public void pleaseStop() {
        delegate.pleaseStop();
    }

    /**
     * @param listener
     * @see org.junit.runner.notification.RunNotifier#addFirstListener(org.junit.runner.notification.RunListener)
     */
    @Override
    public void addFirstListener(RunListener listener) {
        delegate.addFirstListener(listener);
    }

    /**
     * @return
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return delegate.toString();
    }


}
