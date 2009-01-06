/**
 * This file Copyright (c) 2003-2009 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.test;

import info.magnolia.cms.beans.config.RenderableDefinition;
import info.magnolia.cms.beans.config.RenderingModel;
import info.magnolia.cms.core.Content;

import java.util.Random;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ParagraphModelForTests implements RenderingModel {
    private static int executionCount;
    public ParagraphModelForTests(Content content, RenderableDefinition renderable, RenderingModel parent) {
    }

    public RenderingModel getParent() {
        return null;
    }

    public Content getContent() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public RenderableDefinition getDefinition() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String execute() {
        executionCount++;
        return "foobar";
    }

    public String getCount() {
        return "This class was executed " + executionCount + " times.";
    }

    public int getRandomInt() {
        return new Random().nextInt(100);
    }

}
