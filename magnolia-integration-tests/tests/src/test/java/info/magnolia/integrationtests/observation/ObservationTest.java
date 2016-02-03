/**
 * This file Copyright (c) 2015-2016 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.integrationtests.observation;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import info.magnolia.testframework.AbstractMagnoliaIntegrationTest;
import info.magnolia.testframework.htmlunit.AbstractMagnoliaHtmlUnitTest;

import org.junit.After;
import org.junit.Test;

/**
 * Tests for Observation module.
 */
public class ObservationTest extends AbstractMagnoliaHtmlUnitTest {
    private static final String WORKSPACE = "website";
    private static final String EVENTS = "/commandEventListenerTestNode/events/";
    private static final String OBSERVATION_RESULTS = "/commandEventListenerTestNode/observationResults/";

    @After
    public void tearDown() throws Exception {
        //FINALLY - delete created properties
        openPage(AbstractMagnoliaIntegrationTest.Instance.PUBLIC.getURL("/.magnolia/jcrprop/?workspace=" + WORKSPACE + "&path=" + EVENTS + "event1/event1" + "&delete=true"), AbstractMagnoliaIntegrationTest.User.superuser, false, false);
        openPage(AbstractMagnoliaIntegrationTest.Instance.PUBLIC.getURL("/.magnolia/jcrprop/?workspace=" + WORKSPACE + "&path=" + EVENTS + "event2/event2" + "&delete=true"), AbstractMagnoliaIntegrationTest.User.superuser, false, false);
        openPage(AbstractMagnoliaIntegrationTest.Instance.PUBLIC.getURL("/.magnolia/jcrprop/?workspace=" + WORKSPACE + "&path=" + OBSERVATION_RESULTS + "event1" + "&delete=true"), AbstractMagnoliaIntegrationTest.User.superuser, false, false);
        openPage(AbstractMagnoliaIntegrationTest.Instance.PUBLIC.getURL("/.magnolia/jcrprop/?workspace=" + WORKSPACE + "&path=" + OBSERVATION_RESULTS + "event2" + "&delete=true"), AbstractMagnoliaIntegrationTest.User.superuser, false, false);
    }

    @Test
    public void observationEventCalledTwiceAndContextReleaseCalledAfterEachEvent() throws Exception {
        //WHEN
        openPage(AbstractMagnoliaIntegrationTest.Instance.PUBLIC.getURL("/.magnolia/jcrprop/?workspace=" + WORKSPACE + "&path=" + EVENTS + "event1/event1" + "&value=start"), AbstractMagnoliaIntegrationTest.User.superuser, false, false);

        //THEN
        String contents = openPage(AbstractMagnoliaIntegrationTest.Instance.PUBLIC.getURL("/.magnolia/jcrprop/?workspace=" + WORKSPACE + "&path=" + OBSERVATION_RESULTS + "event1"), AbstractMagnoliaIntegrationTest.User.superuser, false, false).getWebResponse().getContentAsString();
        assertThat(contents, equalTo("end"));

        //WHEN
        openPage(AbstractMagnoliaIntegrationTest.Instance.PUBLIC.getURL("/.magnolia/jcrprop/?workspace=" + WORKSPACE + "&path=" + EVENTS + "event2/event2" + "&value=start"), AbstractMagnoliaIntegrationTest.User.superuser, false, false);

        //THEN
        contents = openPage(AbstractMagnoliaIntegrationTest.Instance.PUBLIC.getURL("/.magnolia/jcrprop/?workspace=" + WORKSPACE + "&path=" + OBSERVATION_RESULTS + "event2"), AbstractMagnoliaIntegrationTest.User.superuser, false, false).getWebResponse().getContentAsString();
        assertThat(contents, equalTo("end"));
    }
}
