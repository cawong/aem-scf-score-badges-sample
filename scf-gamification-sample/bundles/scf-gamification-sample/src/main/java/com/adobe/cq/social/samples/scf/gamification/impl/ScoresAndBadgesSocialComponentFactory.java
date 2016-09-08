/*************************************************************************
 * Copyright 2015 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the
 * terms of the Adobe license agreement accompanying it.  If you have received this file from a
 * source other than Adobe, then your use, modification, or distribution of it requires the prior
 * written permission of Adobe.
 **************************************************************************/
package com.adobe.cq.social.samples.scf.gamification.impl;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolverFactory;

import com.adobe.cq.social.badging.api.BadgingService;
import com.adobe.cq.social.samples.scf.gamification.api.ScoresAndBadgesSocialComponent;
import com.adobe.cq.social.scf.ClientUtilities;
import com.adobe.cq.social.scf.QueryRequestInfo;
import com.adobe.cq.social.scf.SocialCollectionComponentFactory;
import com.adobe.cq.social.scf.SocialComponent;
import com.adobe.cq.social.scf.core.AbstractSocialComponentFactory;
import com.adobe.cq.social.scoring.api.ScoringService;

@Service
@Component
public class ScoresAndBadgesSocialComponentFactory extends AbstractSocialComponentFactory implements
    SocialCollectionComponentFactory {

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY, policy = ReferencePolicy.STATIC)
    protected ResourceResolverFactory resourceResolverFactory;

    @Reference
    private BadgingService badging;

    @Reference
    private ScoringService scoring;

    @Override
    public SocialComponent getSocialComponent(Resource resource) {
        return new ScoresAndBadgesSocialComponentImpl(resource, getClientUtilities(resource.getResourceResolver()),
            resourceResolverFactory, badging, scoring);
    }

    @Override
    public SocialComponent getSocialComponent(Resource resource, SlingHttpServletRequest request) {
        return new ScoresAndBadgesSocialComponentImpl(resource, this.getClientUtilities(request),
            this.getQueryRequestInfo(request), resourceResolverFactory, badging, scoring);
    }

    @Override
    public String getSupportedResourceType() {
        return ScoresAndBadgesSocialComponent.RESOURCE_TYPE;
    }

    @Override
    public SocialComponent getSocialComponent(Resource resource, ClientUtilities clientUtils,
        QueryRequestInfo requestInfo) {
        return new ScoresAndBadgesSocialComponentImpl(resource, clientUtils, requestInfo, resourceResolverFactory,
            badging, scoring);
    }

}
