/**
 * This file Copyright (c) 2015 Magnolia International
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
package info.magnolia.test.fixture;

import info.magnolia.cms.filters.OncePerRequestAbstractMgnlFilter;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SimpleContext;
import info.magnolia.module.cache.CacheModule;
import info.magnolia.module.cache.CachePolicyResult;
import info.magnolia.module.cache.commands.FlushCachesCommand;
import info.magnolia.module.cache.inject.CacheFactoryProvider;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.voting.Voter;
import info.magnolia.voting.voters.URIRegexVoter;

import java.io.IOException;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter for testing of {@link CacheModule}.
 */
public class CacheMonitorFilter extends OncePerRequestAbstractMgnlFilter {

    private static final Logger log = LoggerFactory.getLogger(CacheMonitorFilter.class);

    private static final String PARAMETER_ACTION = "mgnlCacheAction";
    private static final String ACTION_FLUSH = "flush";
    private static final String ACTION_RESULT = "result";
    private static final String ACTION_CREATE_PAGE = "createPage";
    private static final String ACTION_OVERVIEW = "overview";
    private static final String ACTION_STOP = "stop";

    private final CacheModule cacheModule;
    private final CacheFactoryProvider cacheFactoryProvider;

    private CachePolicyResult latestCachePolicyResult;

    @Inject
    public CacheMonitorFilter(CacheModule cacheModule, CacheFactoryProvider cacheFactoryProvider) {
        this.cacheModule = cacheModule;
        this.cacheFactoryProvider = cacheFactoryProvider;
        URIRegexVoter uriRegexVoter = new URIRegexVoter();
        uriRegexVoter.setPattern("/.magnolia/jcrprop*");
        this.setBypasses(new Voter[]{uriRegexVoter});
    }

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        final String action = request.getParameter(PARAMETER_ACTION);

        if (ACTION_FLUSH.equals(action)) {
            this.flush();
        } else if (ACTION_RESULT.equals(action)) {
            response.getWriter().append(this.getResult());
        } else if (ACTION_OVERVIEW.equals(action)) {
            response.getWriter().append(this.getOverview());
        } else if (ACTION_STOP.equals(action)) {
            this.stop();
        } else if (ACTION_CREATE_PAGE.equals(action)) {
            try {
                this.createPage();
            } catch (RepositoryException e) {
                log.error("Unable to create page: ", e);
            }
        } else {
            chain.doFilter(request, response);
            latestCachePolicyResult = CachePolicyResult.getCurrent();
        }
    }

    protected Node createPage() throws RepositoryException {
        Session session = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        Node node = session.getRootNode().getNode("demo-project");
        return node;
    }

    private void flush() {
        try {
            new FlushCachesCommand(cacheFactoryProvider).execute(new SimpleContext());
        } catch (Exception e) {
            log.error("Unable to flush cache", e);
        }
    }

    private String getOverview() {
        return String.valueOf(cacheModule.getCacheFactory().getCacheNames());
    }

    private String getResult() {
        return String.valueOf(latestCachePolicyResult);
    }

    private void stop() {
        cacheModule.stop(null);
    }
}
