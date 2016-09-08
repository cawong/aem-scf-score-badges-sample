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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.social.badging.api.BadgingService;
import com.adobe.cq.social.badging.api.UserBadge;
import com.adobe.cq.social.samples.scf.gamification.api.ScoresAndBadgesSocialComponent;
import com.adobe.cq.social.scf.ClientUtilities;
import com.adobe.cq.social.scf.QueryRequestInfo;
import com.adobe.cq.social.scf.User;
import com.adobe.cq.social.scf.core.BaseSocialComponent;
import com.adobe.cq.social.scoring.api.ScoringService;
import com.adobe.granite.security.user.UserProperties;
import com.adobe.granite.security.user.UserPropertiesManager;

public class ScoresAndBadgesSocialComponentImpl extends BaseSocialComponent implements ScoresAndBadgesSocialComponent {

    private static final Logger LOG = LoggerFactory.getLogger(ScoresAndBadgesSocialComponentImpl.class);

    private static final String UTILITY_READER = "communities-utility-reader";

    final ValueMap props;
    final BadgingService badging;
    final ScoringService scoring;
    final ResourceResolver resolver;
    final ResourceResolverFactory resourceResolverFactory;

    private String userid;

    public ScoresAndBadgesSocialComponentImpl(Resource resource, ClientUtilities clientUtils,
        ResourceResolverFactory resourceResolverFactory, BadgingService badging, ScoringService scoring) {
        super(resource, clientUtils);

        props = resource.adaptTo(ValueMap.class);
        this.badging = badging;
        this.scoring = scoring;
        this.resolver = resource.getResourceResolver();
        this.resourceResolverFactory = resourceResolverFactory;

        getUserProps(clientUtils);

    }

    public ScoresAndBadgesSocialComponentImpl(Resource resource, ClientUtilities clientUtils,
        QueryRequestInfo requestInfo, ResourceResolverFactory resourceResolverFactory, BadgingService badging,
        ScoringService scoring) {
        super(resource, clientUtils);

        props = resource.adaptTo(ValueMap.class);
        this.badging = badging;
        this.scoring = scoring;
        this.resolver = resource.getResourceResolver();
        this.resourceResolverFactory = resourceResolverFactory;

        getUserProps(clientUtils);

    }

    private void getUserProps(ClientUtilities clientUtils) {
        String authId = null;
        Resource propertyNode = null;
        UserProperties userProperties = null;

        UserPropertiesManager upm = resolver.adaptTo(UserPropertiesManager.class);

        final String authPath = clientUtils.getRequest().getRequestPathInfo().getSuffix();

        if (StringUtils.startsWith(authPath, User.SOCIAL_AUTHORS_PREFIX)) {
            authId = authPath.substring(User.SOCIAL_AUTHORS_PREFIX.length());
        } else if (!StringUtils.isEmpty(authPath)) {
            // pass in user profile path
            propertyNode = resolver.resolve(authPath);
        }

        try {
            if (upm != null) {
                if (!StringUtils.isEmpty(authId)) {
                    userProperties = upm.getUserProperties(authId, "profile");
                } else if (propertyNode != null) {
                    userProperties = upm.getUserProperties(propertyNode.adaptTo(Node.class));
                } else {
                    userProperties = null;
                }
            } else {
                userProperties = null;
            }
        } catch (RepositoryException e) {
            LOG.error("Can't obtain user properties for {}", authId != null ? authId : propertyNode.getPath());
        }

        if (userProperties != null) {
            userid = userProperties.getAuthorizableID();
        }
    }

    @Override
    public List<String> getBadges() {
        List<String> badges = new ArrayList<String>();

        try {
            // get all of the user's badges
            final List<UserBadge> userBadges =
                badging.getBadges(resource.getResourceResolver(), userid, null, null, BadgingService.ALL_BADGES);

            for (final UserBadge badge : userBadges) {
                badges.add(badge.getImagePath());
            }
        } catch (RepositoryException e) {
            LOG.error("Error calling BadgingService.getBadges() for userid {}", userid, e);
        }

        return badges;
    }

    @Override
    public Long getScore() {
        long score;

        // read the component's properties to get the rule location
        final String ruleLocationProp = clientUtils.getRequest().getResource().getValueMap().get("ruleLocation", "");

        final Resource ruleLocation = resolver.getResource(ruleLocationProp);
        if (ruleLocation == null) {
            LOG.error("Can't read rule location {}", ruleLocationProp);
            return 0L;
        }

        final Resource ruleResource;

        ResourceResolver serviceUserResolver = null;
        try {
            // the rules have restricted read permission, so get a service user to read the resource
            serviceUserResolver =
                resourceResolverFactory.getServiceResourceResolver(Collections.singletonMap(
                    ResourceResolverFactory.SUBSERVICE, (Object) UTILITY_READER));

            // read the component's properties to get the scoring rule
            final String ruleProp = clientUtils.getRequest().getResource().getValueMap().get("scoringRule", "");
            ruleResource = serviceUserResolver.getResource(ruleProp);
            if (ruleResource == null) {
                LOG.error("Can't read rule resource {}", ruleProp);
                return 0L;
            }

            // get the score from the scoring service
            score = scoring.getScore(resolver, userid, ruleLocation, ruleResource);
        } catch (LoginException e) {
            LOG.error("Can't get service user.");
            return 0L;
        } catch (RepositoryException e) {
            LOG.error("Error calling ScoringService.getScore() for userid {}", resolver.getUserID(), e);
            return 0L;
        } finally {
            if (serviceUserResolver != null && serviceUserResolver.isLive()) {
                serviceUserResolver.close();
            }
        }

        return score;
    }

}
