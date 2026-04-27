package com.cleanmate.presentation.nav;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.logging.Logger;

public final class ViewRouter {

    private static final Logger LOG = Logger.getLogger(ViewRouter.class.getName());
    private static ViewRouter INSTANCE;

    private final Stage stage;
    private final Deque<Route> history = new ArrayDeque<>();

    private ViewRouter(Stage stage) {
        this.stage = stage;
    }

    public static void init(Stage stage) {
        INSTANCE = new ViewRouter(stage);
    }

    public static ViewRouter get() {
        if (INSTANCE == null) {
            throw new IllegalStateException("ViewRouter not initialized — call ViewRouter.init(stage) first.");
        }
        return INSTANCE;
    }

    public Stage getStage() { return stage; }

    public void navigate(Route route) {
        navigate(route, true);
    }

    public void back() {
        if (history.size() >= 2) {
            history.pop();
            Route prev = history.peek();
            navigate(prev, false);
        } else {
            LOG.fine("Back ignored — history empty");
        }
    }

    public void reload() {
        if (!history.isEmpty()) {
            Route current = history.pop();
            navigate(current, true);
        }
    }

    private void navigate(Route route, boolean pushHistory) {
        try {
            LOG.info("Navigate -> " + route);
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    getClass().getResource(route.fxml),
                    "Missing FXML resource: " + route.fxml));
            loader.setResources(LanguageManager.getBundle());
            Parent root = loader.load();

            Scene scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                scene.getStylesheets().add(Objects.requireNonNull(
                        getClass().getResource("/css/styles.css")).toExternalForm());
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }
            stage.setTitle(LanguageManager.getBundle().getString(route.titleKey));

            if (pushHistory) history.push(route);
        } catch (IOException e) {
            throw new RuntimeException("Failed to navigate to " + route, e);
        }
    }
}
