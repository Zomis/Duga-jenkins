package net.zomis.jenkins.duga;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.Extension;
import hudson.model.*;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

public class DugaJobProperty extends JobProperty<AbstractProject<?, ?>> {

    // public UsernamePasswordCredentials credentials;
    public final String remoteUrl;
    public final String apiKey;
    public final String roomIds;
    public final StandardUsernamePasswordCredentials credentials;

    @DataBoundConstructor
    public DugaJobProperty(String remoteUrl,
          String apiKey, String roomIds, StandardUsernamePasswordCredentials credentials) {
        this.remoteUrl = remoteUrl;
        this.apiKey = apiKey;
        this.roomIds = roomIds;
        this.credentials = credentials;
    }

    @Exported
    public String getApiKey() {
        return apiKey;
    }

    @Exported
    public String getRoomIds() {
        return roomIds;
    }

    @Exported
    public String getRemoteUrl() {
        return remoteUrl;
    }

    @Exported
    public StandardUsernamePasswordCredentials getCredentials() {
        return credentials;
    }

    @Extension
    public static final class DescriptorImpl extends JobPropertyDescriptor {

        // private UsernamePasswordCredentials credentials;

/*        private String remoteUrl;
        private String apiKey;
        // private StringCredentials apiKey;
        private String roomIds;

/*        public DescriptorImpl() {
            super(DugaJobProperty.class);
            load();
        }

/*        public FormValidation doCheckRemoteUrl(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.error("Please set a URL");
            }
            if (!value.startsWith("http")) {
                return FormValidation.warning("Please enter a valid URL");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckRoomIds(@QueryParameter String value) {
            if (value.isEmpty()) {
                return FormValidation.error("Please enter one or more room ids");
            }
            String[] rooms = value.split(",");
            for (String room : rooms) {
                if (!room.matches("\\d+")) {
                    return FormValidation.error(room + " is not a valid room id");
                }
            }
            return FormValidation.ok();
        }*/

        @Override
        public boolean isApplicable(Class<? extends Job> jobType) {
            return true;
        }

  /*      @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            req.bindJSON(this, formData);
            save();
            return super.configure(req, formData);
        }*/

        public ListBoxModel doFillCredentialsItems() {
            final ListBoxModel model = new ListBoxModel();

            DomainRequirement domain = new DomainRequirement();
            for (StandardUsernamePasswordCredentials credentials :
                    CredentialsProvider.lookupCredentials(StandardUsernamePasswordCredentials.class,
                    Jenkins.getInstance(), ACL.SYSTEM, domain)) {
                model.add(credentials.getId());
            }
            return model;
        }

        @Override
        public String getDisplayName() {
            return "Stack Exchange Chat";
        }

        @Override
        public JobProperty<?> newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            System.out.println("New instance: " + req);
            DugaJobProperty dugaJobProperty;

            if (req.hasParameter("hasDugaNotifier")) {
                System.out.println("New instance has parameter");
                System.out.println("New instance " + formData);
                JSONObject obj = formData.getJSONObject("hasDugaNotifier");
                System.out.println("Param: " + obj);
                String remoteURL = obj.getString("remoteUrl");
                String apiKey = obj.getString("apiKey");
                String roomIds = obj.getString("roomIds");
                System.out.println("values: " + remoteURL + ", " + apiKey + ", " + roomIds);

                String credentialsId = obj.getString("credentials");

                StandardUsernamePasswordCredentials credentials =
                    getCredentialsById(StandardUsernamePasswordCredentials.class, credentialsId);
                System.out.println("Credentials found: " + credentials + " with id " + credentialsId);

                dugaJobProperty = new DugaJobProperty(remoteURL, apiKey, roomIds, credentials);
//                dugaJobProperty = req.bindJSON(DugaJobProperty.class, obj);
            } else {
                dugaJobProperty = null;
            }
            System.out.println("New instance return " + dugaJobProperty);
            return dugaJobProperty;
        }

        private <T extends StandardCredentials> T getCredentialsById(Class<T> credentialsClass, String credentialsId) {
            DomainRequirement domain = new DomainRequirement();
            for (T credentials :
                    CredentialsProvider.lookupCredentials(credentialsClass,
                            Jenkins.getInstance(), ACL.SYSTEM, domain)) {
                if (credentials.getId().equals(credentialsId)) {
                    return credentials;
                }
            }
            return null;
        }
    }

    @Override
    public String toString() {
        return "DugaJobProperty{" +
                "remoteUrl='" + remoteUrl + '\'' +
                ", apiKey='" + apiKey + '\'' +
                ", roomIds='" + roomIds + '\'' +
                ", credentials=" + credentials +
                '}';
    }
}
