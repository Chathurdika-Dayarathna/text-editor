package lk.ijse.dep10.editor.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditorSceneController {

    public TextArea txtEditor;
    public MenuItem mnAbout;
    public TextField txtFind;
    public TextField txtReplace;
    public CheckBox chkMatchCase;
    public Button btnUp;
    public Button btnDown;
    public Button btnReplace;
    public Button btnReplaceAll;
    public Label lblResults;
    private ArrayList<Integer> startLocation = new ArrayList<>();
    private ArrayList<Integer> endLocation = new ArrayList<>();
    private int moving;
    private boolean isDown=true;
    public void initialize(){
        txtFind.textProperty().addListener(event -> {
            findResultCount();
        });
        txtEditor.textProperty().addListener(event -> {
            findResultCount();
        });
    }
    public void findResultCount(){
        String findText = txtFind.getText();
        if (findText.isEmpty()) {
            lblResults.setText("0 Results");
            return;
        }
        Pattern pattern;
        try {
            if(chkMatchCase.isSelected()) pattern = Pattern.compile(findText);
            else pattern = Pattern.compile(findText, Pattern.CASE_INSENSITIVE);
        } catch (RuntimeException e) {
            return;
        }
        Matcher matcher = pattern.matcher(" "+txtEditor.getText()+" ");
        int count = 0;
        startLocation = new ArrayList<>();
        endLocation = new ArrayList<>();
        moving=0;
        while (matcher.find()) {
            count++;
            int start = matcher.start();
            int end = matcher.end();
            startLocation.add(start-1);
            endLocation.add(end-1);

        }
        if (!startLocation.isEmpty()) {
            txtEditor.selectRange(startLocation.get(moving), endLocation.get(moving));
            moving++;
            lblResults.setText("1/"+count + " Results");
        }else lblResults.setText("0 Results");
    }
    @FXML
    void mnAboutOnAction(ActionEvent event) throws IOException {
        Stage aboutWindow = new Stage();
        URL fxmlFile=this.getClass().getResource("/view/AboutScene.fxml");
        FXMLLoader fxmlLoader=new FXMLLoader(fxmlFile);
        AnchorPane root=fxmlLoader.load();
        Scene scene=new Scene(root);
        aboutWindow.setScene(scene);
        aboutWindow.initModality(Modality.APPLICATION_MODAL);
        aboutWindow.setTitle("About CD Text Editor");
        aboutWindow.show();
        aboutWindow.centerOnScreen();
        aboutWindow.setResizable(false);
    }
    @FXML
    void mnCloseOnAction(ActionEvent event) {
            int currenTextLength=txtEditor.getLength();
            if(textLength!=currenTextLength) {
                Alert confirmMsg = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to close the file without saving ", ButtonType.YES, ButtonType.NO);
                Optional<ButtonType> optButton = confirmMsg.showAndWait();
                if (optButton.isEmpty() || optButton.get() == ButtonType.YES) {
                    Platform.exit();
                }
                if ( optButton.get() == ButtonType.NO) return;
            }else Platform.exit();
    }
    @FXML
    void mnNewOnAction(ActionEvent event) {
        if(!txtEditor.getText().isBlank()|(save)){
            Alert confirmMsg = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want a new editor ", ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> optButton = confirmMsg.showAndWait();
            if (optButton.isEmpty() || optButton.get() == ButtonType.NO) return;
        }
        txtEditor.setText("");
        Stage stage = (Stage) txtEditor.getScene().getWindow();
        stage.setTitle("untitled");
        save=false;
    }
    public void txtEditorOnAction(KeyEvent keyEvent) {
        if(!save) {
            Stage stage = (Stage) txtEditor.getScene().getWindow();
            stage.setTitle("*untitled");
            return;
        }
    }
    @FXML
    void mnOpenOnAction(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open a text file");
        File file = fileChooser.showOpenDialog(txtEditor.getScene().getWindow());
        if (file == null) return;
        FileInputStream fis = new FileInputStream(file);
        byte[] bytes = fis.readAllBytes();
        fis.close();
        txtEditor.setText(new String(bytes));
        Stage stage = (Stage) txtEditor.getScene().getWindow();
        stage.setTitle(file.getName());
    }
    @FXML
    void mnSaveOnAction(ActionEvent event) throws IOException {
        textLength=txtEditor.getLength();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save a text file");
        File file = fileChooser.showSaveDialog(txtEditor.getScene().getWindow());
        if (file == null) return;
        FileOutputStream fos = new FileOutputStream(file, false);
        String text = txtEditor.getText();
        byte[] bytes = text.getBytes();
        fos.write(bytes);
        fos.close();
        Stage stage = (Stage) txtEditor.getScene().getWindow();
        stage.setTitle(file.getName());
        save=true;
    }
    boolean save;
    int textLength;
    public void rootOnDragOver(DragEvent dragEvent) {
        dragEvent.acceptTransferModes(TransferMode.ANY);

        if (dragEvent.getDragboard().hasFiles()) {
            File draggedFile = dragEvent.getDragboard().getFiles().get(0);
            String fileName = draggedFile.getName();

            Stage stage = (Stage) txtEditor.getScene().getWindow();
            stage.setTitle(fileName);
        }
    }
    public void rootOnDragDropped(DragEvent dragEvent) throws IOException {
        File droppedFile = dragEvent.getDragboard().getFiles().get(0);
        FileInputStream fis = new FileInputStream(droppedFile);
        byte[] bytes=fis.readAllBytes();
        fis.close();
        txtEditor.setText(new String(bytes));
        txtFind.clear();
        txtReplace.clear();
    }
    public void chkMatchCaseOnAction(ActionEvent actionEvent) {
        findResultCount();
    }
    public void btnUpOnAction(ActionEvent actionEvent) {
        if(isDown) moving--;
        isDown=false;
        if (moving > 0) {
            moving--;
            txtEditor.selectRange(startLocation.get(moving), endLocation.get(moving));
            lblResults.setText(moving+"/"+ startLocation.size() + " Results");
        }else moving= startLocation.size();
    }
    public void btnDownOnAction(ActionEvent actionEvent) {
        btnUp.setDisable(false);
        if(!isDown) moving++;
        isDown = true;
        if (moving< startLocation.size()) {
            txtEditor.selectRange(startLocation.get(moving), endLocation.get(moving));
            lblResults.setText(moving+1+"/"+ startLocation.size() + " Results");
            moving++;
        }else moving=0;
    }
    public void btnReplaceOnAction(ActionEvent actionEvent) {
        if (startLocation.isEmpty())return;
        txtEditor.replaceText(txtEditor.selectionProperty().getValue(),txtReplace.getText());
        findResultCount();
        moving=0;
        if (startLocation.isEmpty())return;
        txtEditor.selectRange(startLocation.get(moving), endLocation.get(moving));
        moving++;
        lblResults.setText(moving+"/"+ startLocation.size() + " Results");
        btnUp.setDisable(true);
        isDown=false;
    }
    public void btnReplaceAllOnAction(ActionEvent actionEvent) {
        while(!startLocation.isEmpty()){
            txtEditor.replaceText(startLocation.get(0),endLocation.get(0),txtReplace.getText());
        }
    }
}
