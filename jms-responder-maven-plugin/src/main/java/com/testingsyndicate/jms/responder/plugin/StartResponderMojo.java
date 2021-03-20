package com.testingsyndicate.jms.responder.plugin;

import com.testingsyndicate.jms.responder.ResponderServer;
import com.testingsyndicate.jms.responder.model.config.FileConfig;
import java.io.File;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "start", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class StartResponderMojo extends AbstractResponderMojo {

  @Parameter(property = "configFile", defaultValue = "src/test/resources/responder.yaml")
  private File configFile;

  @Parameter(property = "skip", defaultValue = "false")
  private boolean skip;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (skip) {
      return;
    }
    getLog().info(String.format("Loading responder config %s", configFile));
    try {
      FileConfig config = FileConfig.fromFile(configFile);
      ResponderServer server = ResponderServer.fromConfig(config);
      getLog().info("Starting responder");
      server.start();
      getPluginContext().put(SERVER_KEY, server);
    } catch (Exception e) {
      getLog().error("Execution failed", e);
      throw new MojoExecutionException("Responder start failed", e);
    }
  }
}
