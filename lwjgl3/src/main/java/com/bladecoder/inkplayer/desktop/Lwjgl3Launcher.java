package com.bladecoder.inkplayer.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.bladecoder.inkplayer.InkApp;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    private static boolean fullscreen = true;

    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.

        InkApp app = new InkApp();

        parseArgs(args, app);

        new Lwjgl3Application(app, getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("Blade Ink Template");
        configuration.useVsync(true);
        //// Limits FPS to the refresh rate of the currently active monitor.
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        //// If you remove the above line and set Vsync to false, you can get unlimited FPS, which can be
        //// useful for testing performance, but can also be very stressful to some hardware.
        //// You may also need to configure GPU drivers to fully disable Vsync; this can cause screen tearing.

        if(fullscreen) {
            configuration.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
        } else {
            configuration.setWindowedMode(1920/2, 1080/2);
            configuration.setResizable(true);
        }

        configuration.setWindowIcon("icon128.png", "icon64.png", "icon32.png", "icon16.png");
        return configuration;
    }

    private static void parseArgs(String[] args, InkApp app) {
        for (int i = 0; i < args.length; i++) {
            String s = args[i];
            if (s.equals("-p")) {
                if (i + 1 < args.length) {
                    i++;
                    app.setPlayMode(args[i]);
                }
            } else if (s.equals("-initPath")) {
                if (i + 1 < args.length) {
                    i++;
                    app.setInitPath(args[i]);
                }
            } else if (s.equals("-f")) {
                fullscreen = true;
            } else if (s.equals("-d")) {
                app.setDebugMode();
            } else if (s.equals("-r")) {
                app.setRestart();
            } else if (s.equals("-res")) {
                if (i + 1 < args.length) {
                    i++;
                    app.forceResolution(args[i]);
                }
            } else if (s.equals("-w")) {
                fullscreen = false;
            } else if (s.equals("-l")) {
                if (i + 1 < args.length) {
                    i++;
                    app.loadGameState(args[i]);
                }
            } else if (s.equals("-h")) {
                usage();
            } else {
                if (i == 0 && !s.startsWith("-"))
                    continue; // When embeded JRE the 0 parameter is the app
                // name
                System.out.println("Unrecognized parameter: " + s);
                usage();
            }
        }
    }

    private static void usage() {
        System.out.println("Usage:\n"
            + "-p record_name\tPlay previusly recorded games\n"
            + "-initPath path\tRun the specified path\n"
            + "-f\tSet fullscreen mode\n" + "-w\tSet windowed mode\n"
            + "-d\tShow debug messages\n"
            + "-res width\tForce the resolution width\n"
            + "-l game_state\tLoad the previusly saved game state\n"
            + "-aspect aspect_ratio\tSets the specified screen aspect (16:9, 9:16, 4:3, 16:10)\n");

        System.exit(0);
    }
}
