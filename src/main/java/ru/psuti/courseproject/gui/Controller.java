package ru.psuti.courseproject.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import ru.psuti.courseproject.core.generator.GammaDistributionGenerator;
import ru.psuti.courseproject.core.generator.Generator;
import ru.psuti.courseproject.core.generator.LogNormalDistributionGenerator;
import ru.psuti.courseproject.core.generator.NormalDistributionGenerator;
import ru.psuti.courseproject.core.generator.UniformDistributionGenerator;
import ru.psuti.courseproject.core.pojo.CalculatedDataObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Controller {
    private static final int NUMBER_OF_BARS = 20;

    private static final String GAMMA_DISTRIBUTION = "Гамма-распределение";
    private static final String NORMAL_DISTRIBUTION = "Нормальное распределение";
    private static final String LOGNORMAL_DISTRIBUTION = "Лог - Нормальное распределение";
    //СЮДА ДОБАВИТЬ НАЗВАНИЕ СВОЕГО РАСПРЕДЛЕНИЯ
    private static final String EXPONENTIAL_DISTRIBUTION = "Экспоненциальное распределение";
    private static final String UNIFORM_DISTRIBUTION = "Равномерное распределение";

    @FXML
    private Button okButton;
    @FXML
    private TableView<CalculatedDataObject> dataTable;
    @FXML
    private TableColumn<CalculatedDataObject, String> statColumn;
    @FXML
    private TableColumn<CalculatedDataObject, String> theoColumn;
    @FXML
    private TableColumn<CalculatedDataObject, String> labelColumn;
    @FXML
    private TextField sampleSizeTextField;
    @FXML
    private TextField firstParamTextField;
    @FXML
    private TextField secondParamTextField;
    @FXML
    private BarChart<?, ?> histogramChart;
    @FXML
    private ComboBox<String> distributionComboBox;

    private int sampleSize;
    private double firstParam;
    private double secondParam;

    @FXML
    public void initialize() {
        setupLayout();
        showParamFields(false);
        handleComboBoxOnAction();
        handelOkButtonOnAction();
        handleSampleSizeTextFieldOnAction();
    }

    private void setupLayout() {
        histogramChart.setBarGap(0);
        histogramChart.setCategoryGap(0);

        sampleSizeTextField.setDisable(true);
        okButton.setDisable(true);

        distributionComboBox.getItems().addAll(
                GAMMA_DISTRIBUTION,
                NORMAL_DISTRIBUTION,
                LOGNORMAL_DISTRIBUTION,
                // ПОТОМ ДОБАВИТЬ НАЗВАНИЕ СВОЕГО РАСПРЕДЕЛЕНИЯ СЮДА
                EXPONENTIAL_DISTRIBUTION,
                UNIFORM_DISTRIBUTION
        );
    }

    private void showParamFields(boolean visible) {
        firstParamTextField.setVisible(visible);
        secondParamTextField.setVisible(visible);
    }

    private void handleComboBoxOnAction() {
        distributionComboBox.setOnAction(event -> {
            if (sampleSizeTextField.getText() != null || sampleSizeTextField.getText().length() > 0) {
                sampleSizeTextField.setDisable(false);
                okButton.setDisable(false);
            }
            showParamFields(false);
            switch (distributionComboBox.getValue()) {
                case GAMMA_DISTRIBUTION:
                    resetParamField();
                    // Важно! Для текстового поля следует задать подсказку, что это за параметр, чтобы не городить
                    // кучу меток.
                    firstParamTextField.setVisible(true);
                    firstParamTextField.setPromptText("Форма");
                    secondParamTextField.setVisible(true);
                    secondParamTextField.setPromptText("Размер");
                    break;
                case NORMAL_DISTRIBUTION:
                    break;
                case LOGNORMAL_DISTRIBUTION:
                    break;
                case UNIFORM_DISTRIBUTION:
                    break;
                default:
                    break;
            }
        });
    }


    private void handelOkButtonOnAction() {
        okButton.setOnAction(event -> showHistogram());
    }

    private void handleSampleSizeTextFieldOnAction() {
        sampleSizeTextField.setOnAction(event -> showHistogram());
    }

    private void resetParamField() {
        firstParamTextField.setText("");
        secondParamTextField.setText("");
        firstParam = 0.0;
        secondParam = 0.0;
    }

    private void updateTable(Generator generator) {

        ObservableList<CalculatedDataObject> calculatedDataObjects =
                FXCollections.observableArrayList(generator.getCalculatedData());
        dataTable.setItems(FXCollections.observableArrayList(calculatedDataObjects));
        labelColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getParamName()));
        statColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                String.format("%.3f", cellData.getValue().getParamValueStat()))
        );
        theoColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getParamValueTheo() == null ? "" :
                        String.format("%.3f", cellData.getValue().getParamValueTheo())
        ));
    }

    private void setupHistogram(Generator generator) {
        Map<Integer, Integer> histogramData = getHistogramData(generator);

        XYChart.Series chartSeries = new XYChart.Series();
        for (Integer key : histogramData.keySet()) {
            chartSeries.getData().addAll(new XYChart.Data<>(String.valueOf(key + 1), histogramData.get(key)));
        }
        histogramChart.getData().clear();
        histogramChart.getData().addAll(chartSeries);
    }

    private void showHistogram() {
        // Валидатор текстового поля "Объем выборки"
        if (sampleSizeTextField.getText().matches("^\\d+$")) {
            sampleSize = Integer.parseInt(sampleSizeTextField.getText());
        } else {
            return;
        }

        // Для корректной работы приложения необходимо следить за видимостью полей. Если требуется один параметр, то
        // следует установить setVisible(true) у firstParamTextField.

        // Валидатор текстового поля первого параметра
        if (firstParamTextField.isVisible() && isFirstParamValid()) {
            firstParam = Double.valueOf(firstParamTextField.getText());
        }

        // Вадидатор текстового поля второго параметра
        if (secondParamTextField.isVisible() && isSecondParamsValid()) {
            secondParam = Double.valueOf(secondParamTextField.getText());
        }

        if (distributionComboBox.getValue() != null) {
            switch (distributionComboBox.getValue()) {
                case GAMMA_DISTRIBUTION:
                    if (firstParam <= 0) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Параметр формы меньше или равен 0");
                        alert.showAndWait();
                        return;
                    }
                    if (secondParam <= 0) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Параметр размера меньше или равен 0");
                        alert.showAndWait();
                        return;
                    }
                    GammaDistributionGenerator gammaDistributionGenerator = new GammaDistributionGenerator(
                            firstParam, secondParam);
                    setupHistogram(gammaDistributionGenerator);
                    updateTable(gammaDistributionGenerator);
                    break;
                //И СЮДА
                case NORMAL_DISTRIBUTION:
                    NormalDistributionGenerator normalDistributionGenerator = new NormalDistributionGenerator();
                    setupHistogram(normalDistributionGenerator);
                    updateTable(normalDistributionGenerator);
                    break;
                case LOGNORMAL_DISTRIBUTION:
                    LogNormalDistributionGenerator logNormalDistributionGenerator = new LogNormalDistributionGenerator();
                    setupHistogram(logNormalDistributionGenerator);
                    updateTable(logNormalDistributionGenerator);
                    break;
                case UNIFORM_DISTRIBUTION:
                    UniformDistributionGenerator uniformDistributionGenerator = new UniformDistributionGenerator();
                    setupHistogram(uniformDistributionGenerator);
                    updateTable(uniformDistributionGenerator);
                    break;
                default:
                    break;
            }
        }
    }

    private Map<Integer, Integer> getHistogramData(Generator generator) {
        Map<Integer, Integer> histogramData = new HashMap<>(NUMBER_OF_BARS);
        List<Double> data = generator.getGeneratedRandomValues(sampleSize);

        double boundInc = (data.get(data.size() - 1) - data.get(0)) / NUMBER_OF_BARS;
        double lowerBound = data.get(0);
        double upperBound = lowerBound + boundInc;
        int index = 0;
        int count = 0;
        int sum = 0;

        for (double d : data) {
            if (d >= lowerBound && d < upperBound) {
                count++;
            } else {
                lowerBound += boundInc;
                upperBound += boundInc;
                histogramData.put(index, count);
                sum += count;
                count = 0;
                index++;
                count++;
            }
        }

        sum += count;

        return sum == sampleSize ? histogramData : new HashMap<>();
    }

    private boolean isSecondParamsValid() {
        return secondParamTextField.getText().matches("^[0-9]+(\\.[0-9]+)?$");
    }

    private boolean isFirstParamValid() {
        return firstParamTextField.getText().matches("^[0-9]+(\\.[0-9]+)?$");
    }
}
