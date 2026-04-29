package com.cleanmate;

import com.cleanmate.presentation.nav.Route;
import com.cleanmate.presentation.nav.ViewRouter;
import javafx.application.Application;
import javafx.stage.Stage;

public class CleanMateApp extends Application {

    @Override
    public void start(Stage stage) {
        stage.setResizable(true);
        stage.setMaximized(true);

        com.cleanmate.db.DatabaseManager.init();
        com.cleanmate.service.ServiceLocator.init();
        ViewRouter.init(stage);
        ViewRouter.get().navigate(Route.LOGIN);

        stage.setOnCloseRequest(e -> com.cleanmate.db.DatabaseManager.close());

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
