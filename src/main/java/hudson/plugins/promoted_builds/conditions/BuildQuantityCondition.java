package hudson.plugins.promoted_builds.conditions;

import hudson.Extension;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.listeners.RunListener;
import hudson.plugins.promoted_builds.PromotionBadge;
import hudson.plugins.promoted_builds.PromotionCondition;
import hudson.plugins.promoted_builds.PromotionConditionDescriptor;
import hudson.plugins.promoted_builds.PromotionProcess;
import hudson.plugins.promoted_builds.JobPropertyImpl;
import hudson.util.FormValidation;
import hudson.util.RunList;

import java.io.IOException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
/**
 * @author Juan Pablo Proverbio
 * 
 *
 */
public class BuildQuantityCondition extends PromotionCondition{
	
	private final String successQuantity;

    @DataBoundConstructor
    public BuildQuantityCondition(String successQuantity) {
        this.successQuantity = successQuantity;
    }

    
    public String getSuccessQuantity() {
		return successQuantity;
		
	}
    
    @Override
    public PromotionBadge isMet(PromotionProcess promotionProcess, AbstractBuild<?, ?> build) {
    	Result r = build.getResult();
        RunList<?> jobs = build.getProject().getBuilds();
    	int quantity = Integer.parseInt(successQuantity);
        boolean runPromote = true;
        if((jobs.size()) < quantity){
        	runPromote = false;
        }else {
        	for(int a = 0; a <jobs.size(); a++) {
       			if(a < quantity) {
       				if(!jobs.get(a).getResult().equals(Result.SUCCESS)){
       				  runPromote = false;
        			  break;
        		   }
    			}
       		} 
        }

       if (runPromote && r != Result.UNSTABLE) {
    	   return new BuildQuantityBadge();
       }
       return null;
    }

    /**
     * {@link RunListener} to pick up completions of a build.
     *
     * <p>
     * This is a single instance that receives all the events everywhere in the system.
     * @author Juan Pablo Proverbio
     */
    @Extension
    public static final class RunListenerImpl extends RunListener<AbstractBuild<?,?>> {
        public RunListenerImpl() {
            super((Class)AbstractBuild.class);
        }

        @Override
        public void onCompleted(AbstractBuild<?,?> build, TaskListener listener) {
            JobPropertyImpl jp = build.getProject().getProperty(JobPropertyImpl.class);
            if(jp!=null) {
                for (PromotionProcess p : jp.getItems()) {
                    for (PromotionCondition cond : p.conditions) {
                        if (cond instanceof BuildQuantityCondition) {
                            try {
                                p.considerPromotion2(build);
                                break; // move on to the next process
                            } catch (IOException e) {
                            	e.printStackTrace(listener.error("Failed to promote a build"));
                            }
                        }
                    }
                }
            }
        }
    }

    @Extension
    public static final class DescriptorImpl extends PromotionConditionDescriptor {
    	/**
         * <p>
         * Validate the value of the textbox before It be passed to isMet() method.
         * @author Juan Pablo Proverbio
         */
    	public FormValidation doCheckSuccessQuantity(@QueryParameter String successQuantity) {
        	if (successQuantity.length()==0)
                return FormValidation.error(hudson.plugins.promoted_builds.Messages.BuildQuantityCondition_descriptor_cannotBeNull());
            try {
            	int quantity = Integer.parseInt(successQuantity);
            	if(quantity <= 0)
            		return FormValidation.error(hudson.plugins.promoted_builds.Messages.BuildQuantityCondition_descriptor_greatherZero());
            	return FormValidation.ok();
            }catch (NumberFormatException numberFormatEx){
            	return FormValidation.error(hudson.plugins.promoted_builds.Messages.BuildQuantityCondition_descriptor_isNotInteger());
            }
        }
        
    	public boolean isApplicable(AbstractProject<?,?> item) {
            return true;
        }

        public String getDisplayName() {
            return Messages.BuildQuantityCondition_DisplayName();
        }
    }
    
    //private static Logger log = Logger.getLogger(BuildQuantityCondition.class.getName());
}
