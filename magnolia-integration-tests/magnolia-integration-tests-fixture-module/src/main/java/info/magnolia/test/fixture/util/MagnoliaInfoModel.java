/**
 * This file Copyright (c) 2010-2016 Magnolia International
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
package info.magnolia.test.fixture.util;

import info.magnolia.cms.pddescriptor.ProductDescriptorExtractor;
import info.magnolia.objectfactory.Components;
import info.magnolia.rendering.model.RenderingModel;
import info.magnolia.rendering.model.RenderingModelImpl;
import info.magnolia.rendering.template.RenderableDefinition;

import javax.jcr.Node;

/**
 * A simple model used in tests which exposes Magnolia version info.
 */
public class MagnoliaInfoModel extends RenderingModelImpl<RenderableDefinition> {
    public MagnoliaInfoModel(Node content, RenderableDefinition renderable, RenderingModel parent) {
        super(content, renderable, parent);
    }

    public String getMagnoliaVersion() {
        final ProductDescriptorExtractor productDescriptorExtractor = Components.getComponent(ProductDescriptorExtractor.class);
        final StringBuilder info = new StringBuilder();
        info.append(productDescriptorExtractor.get(ProductDescriptorExtractor.EDITION)).append(" ");
        info.append(productDescriptorExtractor.get(ProductDescriptorExtractor.VERSION_NUMBER)).append(" ");
        info.append("(").append(productDescriptorExtractor.get(ProductDescriptorExtractor.BUILD_NUMBER)).append(")");
        return info.toString();
    }
}
