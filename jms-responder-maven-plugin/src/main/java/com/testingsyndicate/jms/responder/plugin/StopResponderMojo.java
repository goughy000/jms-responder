package com.testingsyndicate.jms.responder.plugin;

import com.testingsyndicate.jms.responder.ResponderServer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "stop", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class StopResponderMojo extends AbstractResponderMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            getLog().info("Stopping responder");
            ResponderServer server = (ResponderServer) getPluginContext().get(SERVER_KEY);
            if (null == server) {
                getLog().warn("No responder to stop!");
            } else {
                server.close();
            }
        } catch (Exception ex) {
            throw new MojoExecutionException("Failed to stop responder", ex);
        } finally {
            getPluginContext().remove(SERVER_KEY);
        }
    }
}
