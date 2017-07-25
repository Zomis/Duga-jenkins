package net.zomis.jenkins.duga;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.gistlabs.mechanize.document.json.JsonDocument;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import jenkins.tasks.SimpleBuildStep;
import net.zomis.duga.chat.*;
import org.jenkinsci.Symbol;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Extension
public class DugaTaskListener extends RunListener<Run> {

    private final Map<String, StackExchangeChatBot> bots = new HashMap<String, StackExchangeChatBot>();

    public DugaTaskListener() {
        System.out.println("Create DugaTaskListener");
    }

    public ChatBot getBot(StandardUsernamePasswordCredentials credentials) {
        String id = credentials.getId();
        if (bots.get(id) != null) {
            return bots.get(id);
        }
        BotConfiguration botConfig = new BotConfiguration();
        botConfig.setBotEmail(credentials.getUsername());
        botConfig.setBotPassword(credentials.getPassword().getPlainText());
        botConfig.setRootUrl("http://stackexchange.com");
        botConfig.setChatUrl("http://chat.stackexchange.com");
        StackExchangeChatBot bot = new StackExchangeChatBot(botConfig);
        bot.start();
        bots.put(id, bot);
        return bot;
    }

    @Override
    public void onCompleted(Run run, TaskListener listener) {
        ExtensionList.lookup(DugaTaskListener.class);
        post(run, listener, "Completed with status " + run.getResult());
    }

    private void post(Run run, final TaskListener listener, String message) {
        System.out.println(message + " " + run);
        final DugaJobProperty config = (DugaJobProperty) run.getParent().getProperty(DugaJobProperty.class);
        if (config == null) {
            return;
        }

        ChatBot bot = getBot(config.getCredentials());

        // [JOB-NAME] BUILD STARTED/COMPLETED/FAILED
        String job = String.format("**\\[[%s](%s)\\]**", run.getParent().getName(), run.getParent().getAbsoluteUrl());
        String build = String.format("**[build %d](%s)**", run.getNumber(), run.getParent().getAbsoluteUrl()
            + run.getNumber() + '/');

        final String string = String.format("%s %s %s", job, build, message);
        System.out.println(string);
        listener.getLogger().println("[DUGA] Posting message: " + string);

        String rooms = config.getRoomIds();
        for (String room : rooms.split(",")) {
            bot.postAsync(new ChatMessage(new BotRoom(bot, room), string, new Consumer<JsonDocument>() {
                @Override
                public void accept(JsonDocument jsonDocument) {
                    listener.getLogger().println("[DUGA] Message posted: " + string);
                }
            }));
        }
    }

    @Override
    public void onStarted(Run run, TaskListener listener) {
        post(run, listener, "Started");
    }

}
