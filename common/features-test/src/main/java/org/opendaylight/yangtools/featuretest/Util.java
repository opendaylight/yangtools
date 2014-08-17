package org.opendaylight.yangtools.featuretest;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Collection;

import org.junit.runner.Description;

public class Util {

    /*
     * Convert a Description to a Description that includes information about repoURL, featureName, and featureVersion
     *
     * This is done so that when a test fails, we can get information about which repoURL, featureName, and featureVersion
     * can come back with the Failure.
     */

    public static Description convertDescription(URL repoURL, String featureName, String featureVersion,Description description) {
        String delegateDisplayName = description.getDisplayName();
        delegateDisplayName = delegateDisplayName + "[repoUrl: " + repoURL+ ", Feature: " + featureName + " " +featureVersion + "]";
        Collection<Annotation> annotations = description.getAnnotations();
        Annotation[] annotationArray = annotations.toArray(new Annotation[annotations.size()]);
        Description newDescription = Description.createSuiteDescription(delegateDisplayName,annotationArray);
        return newDescription;
    }
}
