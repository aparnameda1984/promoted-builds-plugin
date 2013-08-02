package hudson.plugins.promoted_builds;

import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.DescribableList;

import java.util.List;
import java.util.Map;

import org.jvnet.hudson.test.HudsonTestCase;

/**
 * Some utilities to work with Hudson.
 * 
 * @author Bob Foster
 */
public class PromotionTestCase extends HudsonTestCase {

    protected void addBuilder(FreeStyleProject up, Builder builder) throws Exception {
      DescribableList<Builder, Descriptor<Builder>> buildersList = up.getBuildersList();
      buildersList.add(builder);
      up.setBuilders(buildersList);
    }
    
    protected void replacePublishers(FreeStyleProject p, List<Recorder> recorders) throws Exception {
      DescribableList<Publisher, Descriptor<Publisher>> publishersList = p.getPublishersList();
      Map<Descriptor<Publisher>, Publisher> map = publishersList.toMap();
      for (Descriptor<Publisher> descriptor : map.keySet()) {
        p.removePublisher(descriptor);
      }
      for (Recorder recorder : recorders) {
        p.addPublisher(recorder);
      }
    }

    public void testDummy() throws Exception {
        // Do nothing. This class is just used as super class
    }
}
