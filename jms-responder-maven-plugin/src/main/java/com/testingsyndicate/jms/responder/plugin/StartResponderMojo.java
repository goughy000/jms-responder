package com.testingsyndicate.jms.responder.plugin;

import com.testingsyndicate.jms.responder.ResponderServer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Mojo(name = "start", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class StartResponderMojo extends AbstractResponderMojo {

    @Parameter(property = "configFile", defaultValue = "src/test/resources/responder.yaml")
    private File configFile;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info(String.format("Loading responder config %s", configFile));
        try (InputStream is = new FileInputStream(configFile)) {
            ResponderServer server = ResponderServer.fromConfig(is);
            getLog().info("Starting responder");
            server.start();
            getPluginContext().put(SERVER_KEY, server);
        } catch (Exception e) {
            getLog().error("Execution failed", e);
            throw new MojoExecutionException("Responder start failed", e);
        }
    }
}
