/*************************************************************************
 * Copyright 2015 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the
 * terms of the Adobe license agreement accompanying it.  If you have received this file from a
 * source other than Adobe, then your use, modification, or distribution of it requires the prior
 * written permission of Adobe.
 **************************************************************************/
package com.adobe.cq.social.samples.scf.gamification.api;

import java.util.List;

import com.adobe.cq.social.scf.SocialComponent;

/**
 * SocialComponents are logical representations of resources that represent the business view of a resource. They are
 * also used to convert a resource into JSON.
 */
public interface ScoresAndBadgesSocialComponent extends SocialComponent {
    public static final String RESOURCE_TYPE = "social/samples/components/gamification";

    /**
     * @return a list of badges for the owner of this resource
     */
    List<String> getBadges();

    /**
     * Return a single score for the owner given a location and scoring rule. At the component location, e.g.
     * /content/acme/en/projects/jcr:content/content/taskbox, set the following 2 properties. ruleLocation - the
     * location where the scoring rule was configured scoringRule - the scoring rule that was configured at the above
     * ruleLocation
     * @return return the owner's score for the given location and scoring rule
     */
    Long getScore();
}
