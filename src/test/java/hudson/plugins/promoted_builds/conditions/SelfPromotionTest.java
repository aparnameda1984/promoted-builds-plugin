package hudson.plugins.promoted_builds.conditions;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.plugins.promoted_builds.JobPropertyImpl;
import hudson.plugins.promoted_builds.PromotedBuildAction;
import hudson.plugins.promoted_builds.Promotion;
import hudson.plugins.promoted_builds.PromotionProcess;
import hudson.plugins.promoted_builds.PromotionTestCase;

/**
 * @author Kohsuke Kawaguchi
 */
public class SelfPromotionTest extends PromotionTestCase {
    public void testBasic() throws Exception {
        FreeStyleProject p = createFreeStyleProject();

        // promote if the downstream passes
        JobPropertyImpl promotion = new JobPropertyImpl(p);
        p.addProperty(promotion);

        PromotionProcess promo1 = promotion.addProcess("promo1");
        promo1.conditions.add(new SelfPromotionCondition(false));

        PromotionProcess promo2 = promotion.addProcess("promo2");
        promo2.conditions.add(new SelfPromotionCondition(false));

        // ensure that the data survives the roundtrip
        configRoundtrip(p);

        // rebind
        promotion = p.getProperty(JobPropertyImpl.class);
        promo1 = promotion.getItem("promo1");
        promo2 = promotion.getItem("promo2");

        FreeStyleBuild b = assertBuildStatusSuccess(p.scheduleBuild2(0));
        // internally, the promotion is still an asynchronous process. It just happens
        // right away after the build is complete.
        Thread.sleep(1000);

        // verify that both promotions happened
        Promotion pb = promo1.getBuilds().get(0);
        assertSame(pb.getTarget(),b);

        pb = promo2.getBuilds().get(0);
        assertSame(pb.getTarget(),b);

        PromotedBuildAction badge = (PromotedBuildAction) b.getBadgeActions().get(0);
        assertTrue(badge.contains(promo1));
        assertTrue(badge.contains(promo2));
    }

    public void testUnstable() throws Exception {
        FreeStyleProject p = createFreeStyleProject();

        // promote if the downstream passes
        JobPropertyImpl promotion = new JobPropertyImpl(p);
        p.addProperty(promotion);

        PromotionProcess promo1 = promotion.addProcess("promo1");
        promo1.conditions.add(new SelfPromotionCondition(false));

        PromotionProcess promo2 = promotion.addProcess("promo2");
        promo2.conditions.add(new SelfPromotionCondition(true));

        // ensure that the data survives the roundtrip
        configRoundtrip(p);

        // rebind
        promotion = p.getProperty(JobPropertyImpl.class);
        promo1 = promotion.getItem("promo1");
        promo2 = promotion.getItem("promo2");

        addBuilder(p, unstableBuilder());
        FreeStyleBuild b = assertBuildStatus(Result.UNSTABLE, p.scheduleBuild2(0).get());
        // internally, the promotion is still an asynchronous process. It just happens
        // right away after the build is complete.
        Thread.sleep(1000);

        // verify that only one promotions happened
        assertTrue(promo1.getBuilds().isEmpty());

        Promotion pb = promo2.getBuilds().get(0);
        assertSame(pb.getTarget(),b);

        PromotedBuildAction badge = (PromotedBuildAction) b.getBadgeActions().get(0);
        assertFalse(badge.contains(promo1));
        assertTrue(badge.contains(promo2));
    }


    public void testFailure() throws Exception {
        FreeStyleProject p = createFreeStyleProject();

        // promote if the downstream passes
        JobPropertyImpl promotion = new JobPropertyImpl(p);
        p.addProperty(promotion);

        PromotionProcess promo1 = promotion.addProcess("promo1");
        promo1.conditions.add(new SelfPromotionCondition(false));

        PromotionProcess promo2 = promotion.addProcess("promo2");
        promo2.conditions.add(new SelfPromotionCondition(true));

        // ensure that the data survives the roundtrip
        configRoundtrip(p);

        // rebind
        promotion = p.getProperty(JobPropertyImpl.class);
        promo1 = promotion.getItem("promo1");
        promo2 = promotion.getItem("promo2");

        addBuilder(p, failureBuilder());
        FreeStyleBuild b = assertBuildStatus(Result.FAILURE, p.scheduleBuild2(0).get());

        // internally, the promotion is still an asynchronous process. It just happens
        // right away after the build is complete.
        Thread.sleep(1000);

        // verify that neither promotions happened
        assertTrue("promo1 did not occur", promo1.getBuilds().isEmpty());
        assertTrue("promo2 did not occur", promo2.getBuilds().isEmpty());

        PromotedBuildAction badge = (PromotedBuildAction) b.getBadgeActions().get(0);
        assertFalse(badge.contains(promo1));
        assertFalse(badge.contains(promo2));
    }

    private FixedResultBuilder successfulBuilder() {
        return new FixedResultBuilder(Result.SUCCESS);
    }

    private FixedResultBuilder failureBuilder() {
        return new FixedResultBuilder(Result.FAILURE);
    }

    private FixedResultBuilder unstableBuilder() {
        return new FixedResultBuilder(Result.UNSTABLE);
    }

}
