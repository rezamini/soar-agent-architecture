package com.soar.agent.architecture;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import com.soar.agent.architecture.world.PanelUI;

/**
 * The start of the application/simulator/agent
 *
 */
@SpringBootApplication(scanBasePackages = "com.soar.agent.architecture")
@Component
public class AppMain {

    @Autowired
    private PanelUI panelUI;

    @PostConstruct
    private void init(){
        panelUI.initUI();
    }
    public static void main(String[] args) throws IOException {
        // depending on system the scale might be different,
        // if this is not set on some sytems the UI icons or images might be blury and
        // upscaled.
        System.setProperty("sun.java2d.uiScale", "1.0");

        // SpringApplication.run(AppMain.class, args);
        SpringApplicationBuilder builder = new SpringApplicationBuilder(AppMain.class);

        builder.headless(false);

        ConfigurableApplicationContext context = builder.run(args);

        // panelUI.initUI();

        /* Alternative way of starting spring boot */

        // ConfigurableApplicationContext ctx = new
        // SpringApplicationBuilder(AppMain.class)
        // .headless(true).run(args);
        // EventQueue.invokeLater(() -> {

        // AppMain app = ctx.getBean(AppMain.class);
        // app.panelUI.setVisible(true);
        // });

    }

}
