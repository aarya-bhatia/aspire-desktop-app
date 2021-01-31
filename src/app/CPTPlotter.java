package app;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class CPTPlotter extends Application implements EventHandler<ActionEvent> {

    private static final int CHART_WIDTH = 700;
    private static final int CHART_HEIGHT = 500;

    File selectedFile;

    List<Chart> plots = new LinkedList<>();
    List<Chart> barPlots = new LinkedList<>();

    Chart currentChart;
    BorderPane plotPane;

    Button openButton, startButton, backButton, nextButton;
    Label fileNameLabel;
    Label plotNumber;
    FileChooser fc;

    TextArea infoBox;
    BorderPane infoPane;

    public enum ChartTypes {
        LINE, BAR
    }

    ComboBox<ChartTypes> chartTypeComboBox;
    ChartTypes chartType = ChartTypes.LINE;

    String infoMessage = "No new notifications";

    int size = -1;
    int currentPlotIndex = 0;

    private void createPlots() throws IOException {
        if (selectedFile != null) {
            HashMap<String, String[]> data = CPTFileReader.read(selectedFile);

            int total = 0;

            for (Map.Entry<String, String[]> entry : data.entrySet()) {

                String key = entry.getKey();

                List<Axis> ax = CPTFileReader.score(data, key, this);
                Collections.sort(ax);

                Chart linePlot = createLinePlot(key, ax);

                Chart barPlot = createBarPlot(key, ax);

                plots.add(linePlot);
                barPlots.add(barPlot);

                saveImageAsPng(entry.getKey(), linePlot);
                saveImageAsPng(entry.getKey(), barPlot);

                total++;
            }

            size = total;
            currentPlotIndex = 0;
        }
    }

    private Chart createBarPlot(String name, List<Axis> ax) {
        CategoryAxis X = new CategoryAxis();
        X.setLabel("Category");

        NumberAxis Y = new NumberAxis();
        Y.setLabel("Score");

        XYChart.Series<String, Number> series = createSeries(name, ax, X, Y);

        BarChart<String, Number> barChart = new BarChart<>(X, Y);

        barChart.getData().add(series);

        return barChart;
    }

    private Chart createLinePlot(String name, List<Axis> ax) {
        CategoryAxis X = new CategoryAxis();
        X.setLabel("Category");

        NumberAxis Y = new NumberAxis();
        Y.setLabel("Score");

        XYChart.Series<String, Number> series = createSeries(name, ax, X, Y);

        LineChart<String, Number> lineChart = new LineChart<>(X, Y);

        lineChart.getData().add(series);

        return lineChart;
    }

    private XYChart.Series<String, Number> createSeries(String name, List<Axis> ax, CategoryAxis x, NumberAxis y) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        series.setName(name);

        for (Axis a : ax) {
            series.getData().add(new XYChart.Data<>(a.getName(), a.getScore()));
        }

        return series;
    }

    private ToolBar createToolbar() {
        ToolBar toolBar = new ToolBar();
        toolBar.getItems().add(openButton);
        toolBar.getItems().add(new Separator());

        toolBar.getItems().add(fileNameLabel);
        toolBar.getItems().add(new Separator());

        toolBar.getItems().add(startButton);
        toolBar.getItems().add(new Separator());

        toolBar.getItems().add(chartTypeComboBox);
        toolBar.getItems().add(new Separator());

        toolBar.getItems().add(nextButton);
        toolBar.getItems().add(new Separator());

        toolBar.getItems().add(backButton);
        toolBar.getItems().add(new Separator());

        toolBar.getItems().add(plotNumber);
        toolBar.getItems().add(new Separator());

        return toolBar;
    }

    private Scene createScene() {
        VBox vbox = new VBox();

        vbox.getChildren().add(createToolbar());

        if (size > 0) {
            currentChart = plots.get(currentPlotIndex);
            plotPane = new BorderPane();
            plotPane.setCenter(currentChart);
            vbox.getChildren().add(new Separator(Orientation.HORIZONTAL));
            vbox.getChildren().add(plotPane);
            plotNumber.setText(1  + " of " + size);
        }
        else {
            Region spacer = new Region();
            spacer.setPrefHeight(40);
            VBox.setVgrow(spacer, Priority.ALWAYS);
            vbox.getChildren().add(spacer);
        }

        vbox.getChildren().add(new Separator(Orientation.HORIZONTAL));

        infoBox = new TextArea();
        infoBox.setWrapText(true);
        infoBox.setEditable(false);
        infoBox.setText(infoMessage);

        infoPane = new BorderPane(infoBox);

        vbox.getChildren().add(infoPane);

        return new Scene(vbox, CHART_WIDTH, CHART_HEIGHT);
    }

    private void initGUI() {
        backButton = new Button("Previous");
        nextButton = new Button("Next");
        openButton = new Button("Select File");
        startButton = new Button("Start");
        fileNameLabel = new Label("No File Selected.");
        plotNumber = new Label("0 of 0");

        chartTypeComboBox = new ComboBox<>();

        for(ChartTypes type: ChartTypes.values()) {
            chartTypeComboBox.getItems().add(type);
        }

        chartTypeComboBox.getSelectionModel().selectFirst();

        fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        openButton.setOnAction(this);
        startButton.setOnAction(this);
        nextButton.setOnAction(this);
        backButton.setOnAction(this);

        chartTypeComboBox.setOnAction(this);
    }

    public void updateInfoMessage(String nextLine) {
        if (nextLine == null || nextLine.length() == 0) {
            return;
        }
        if (!nextLine.startsWith("\n")) {
            nextLine = '\n' + nextLine;
        }
        infoBox.appendText(nextLine);
        infoMessage = infoBox.getText();
    }

    private void updatePlot(int change) {
        if (currentChart == null || size <= 0) {
            updateInfoMessage("No Charts available.");
            return;
        }
        currentPlotIndex = (currentPlotIndex + change) % size;
        currentChart = chartType == ChartTypes.LINE ? plots.get(currentPlotIndex) : barPlots.get(currentPlotIndex);
        plotPane.setCenter(currentChart);
        plotNumber.setText((1 + currentPlotIndex) + " of " + size);
    }

    @Override
    public void start(Stage stage) {
        initGUI();

        Scene scene = createScene();

        stage.setTitle("Plotter");
        stage.setScene(scene);
        stage.show();
    }

    private void saveImageAsPng(String studentName, Chart chart) throws IOException {
        Scene scene = new Scene(new BorderPane(chart), 600, 400);

        WritableImage image = scene.snapshot(null);
        BufferedImage buffImage = SwingFXUtils.fromFXImage(image, null);

        File file;

        if(chart instanceof LineChart) {
            file = getDestinationFile(studentName, ChartTypes.LINE);
        }
        else {
            file = getDestinationFile(studentName, ChartTypes.BAR);
        }

        if (file.exists() || file.mkdirs()) {
            ImageIO.write(buffImage, "PNG", file);
            String message = "File saved in location: " + file.getAbsolutePath();
            updateInfoMessage(message);
        }
    }

    private static File getDestinationFile(String studentName, ChartTypes chartType) {
        File baseDir = new File(System.getProperty("user.home"));
        File documentsDir = new File(baseDir, "Documents");
        File chartsDir = new File(documentsDir, "Charts");
        File subDirectory;
        if(chartType.equals(ChartTypes.LINE)) {
           subDirectory = new File(chartsDir, "Line");
        }
        else if(chartType.equals(ChartTypes.BAR)) {
            subDirectory = new File(chartsDir, "Bar");
        }
        else {
            subDirectory = chartsDir;
        }

        return new File(subDirectory, String.format("%s_%s.png", studentName, System.currentTimeMillis()));
    }

    public static void showAlert(String str, Alert.AlertType alertType) {
        Alert message = new Alert(alertType);
        message.setContentText(str);
        message.show();
    }

    public static void main(String[] args) {
        try {
            launch(args);
        }
        catch (Exception e) {
            showAlert(e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @Override
    public void handle(ActionEvent event) {
        if(event.getSource() == openButton) {
            Window window = openButton.getScene().getWindow();
            Stage stage = (Stage) window;
            selectedFile = fc.showOpenDialog(stage);
            fileNameLabel.setText(selectedFile.getAbsolutePath());
            updateInfoMessage("File selected: " + selectedFile.getAbsolutePath());
        }
        else if(event.getSource() == startButton) {
            if (selectedFile != null) {
                try {
                    updateInfoMessage("Creating Plots...");
                    createPlots();

                    Window window = startButton.getScene().getWindow();
                    Stage stage = (Stage) window;
                    stage.setScene(createScene());
                } catch (IOException ex) {
                    showAlert(ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
        }
        else if(event.getSource() == nextButton) {
                updatePlot(1);
        }
        else if(event.getSource() == backButton) {
                updatePlot(-1);
        }
        else if(event.getSource() == chartTypeComboBox) {
            chartType = chartTypeComboBox.getSelectionModel().getSelectedItem();
            updatePlot(0);
        }
    }
}
