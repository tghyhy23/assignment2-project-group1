package com.group01.asm2.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import com.group01.asm2.utils.ScrollUtils;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class ExploreController implements Initializable {

    @FXML
    private ScrollPane exploreScrollPane;

    @FXML
    private VBox exploreRoot;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        ScrollUtils.makeSmooth(exploreScrollPane);
    }
}