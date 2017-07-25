package net.zomis.jenkins.duga;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import net.zomis.duga.chat.ChatBot;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.IOException;

public class DugaStep extends Builder implements SimpleBuildStep {

    private String botId;
    private String room;
    private String message;

    @DataBoundConstructor
    public DugaStep(String botId, String room, String message) {
        this.botId = botId;
        this.room = room;
        this.message = message;
    }

    public String getBotId() {
        return botId;
    }

    public String getMessage() {
        return message;
    }

    public String getRoom() {
        return room;
    }

    @DataBoundSetter
    public void setBotId(String botId) {
        this.botId = botId;
    }

    @DataBoundSetter
    public void setRoom(String room) {
        this.room = room;
    }

    @DataBoundSetter
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath workspace, @Nonnull Launcher launcher, @Nonnull TaskListener listener) throws InterruptedException, IOException {
        StandardUsernamePasswordCredentials creds = getCredentialsForId(botId);
        if (creds == null) {
            listener.getLogger().println("ERROR: No bot credentials found for id " + botId);
            return;
        }
        ExtensionList<DugaTaskListener> duga = ExtensionList.lookup(DugaTaskListener.class);
        ChatBot bot = duga.get(0).getBot(creds);
        bot.room(room).message(message).post();
    }

    private StandardUsernamePasswordCredentials getCredentialsForId(String botId) {
        DomainRequirement domain = new DomainRequirement();
        for (StandardUsernamePasswordCredentials credentials :
                CredentialsProvider.lookupCredentials(StandardUsernamePasswordCredentials.class,
                        Jenkins.getInstance(), ACL.SYSTEM, domain)) {
            if (credentials.getId().equals(botId)) {
                return credentials;
            }
        }
        return null;
    }

    @Extension
    @Symbol("duga")
    public static class DugaStepDescriptor extends BuildStepDescriptor<Builder> {

        @Override
        public String getDisplayName() {
            return "Duga post to Stack Exchange chat";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }


}
