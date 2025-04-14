import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class CalculatorApp extends Application {

    private TextField display = new TextField();
    private long longPressStart = 0;

    @Override
    public void start(Stage primaryStage) {
        display.setEditable(false);
        display.setFont(Font.font("Arial", 28));
        display.setAlignment(Pos.CENTER_RIGHT);
        display.setPrefHeight(60);
        display.setStyle("-fx-background-color: white; -fx-border-color: #ccc;");

        GridPane buttonsGrid = createButtons();

        VBox root = new VBox(10, display, buttonsGrid);
        root.setPadding(new Insets(20));
        Scene scene = new Scene(root, 320, 450);

        // Keyboard support
        scene.setOnKeyPressed(event -> {
            String key = event.getText();
            if (key.matches("[0-9]") || "+-*/.%".contains(key)) {
                appendToDisplay(key);
            } else if (event.getCode() == KeyCode.ENTER) {
                evaluate();
            } else if (event.getCode() == KeyCode.BACK_SPACE) {
                removeLastCharacter();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                clearDisplay();
            }
        });

        primaryStage.setTitle("Smart % Calculator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private GridPane createButtons() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        String[][] buttons = {
            {"7", "8", "9", "/"},
            {"4", "5", "6", "*"},
            {"1", "2", "3", "-"},
            {"0", "%", "C", "+"},
            {".", "=", "", ""}
        };

        for (int i = 0; i < buttons.length; i++) {
            for (int j = 0; j < buttons[i].length; j++) {
                String text = buttons[i][j];
                if (!text.isEmpty()) {
                    Button btn = new Button(text);
                    btn.setPrefSize(60, 60);
                    btn.setStyle("-fx-font-size: 18px; -fx-background-radius: 20px;");

                    if ("=C".contains(text)) {
                        btn.setStyle("-fx-font-size: 18px; -fx-background-color: #ff9500; -fx-text-fill: white; -fx-background-radius: 20px;");
                    }

                    if (text.equals("C")) {
                        btn.setOnMousePressed(e -> longPressStart = System.currentTimeMillis());
                        btn.setOnMouseReleased(e -> {
                            long duration = System.currentTimeMillis() - longPressStart;
                            if (duration >= 500) {
                                clearDisplay(); // long press
                            } else {
                                removeLastCharacter(); // short press
                            }
                        });
                    } else {
                        btn.setOnAction(e -> handleInput(text));
                    }

                    grid.add(btn, j, i);
                }
            }
        }

        return grid;
    }

    private void appendToDisplay(String value) {
        display.appendText(value);
    }

    private void removeLastCharacter() {
        String text = display.getText();
        if (!text.isEmpty()) {
            display.setText(text.substring(0, text.length() - 1));
        }
    }

    private void clearDisplay() {
        display.clear();
    }

    private void handleInput(String value) {
        switch (value) {
            case "=":
                evaluate();
                break;
            default:
                appendToDisplay(value);
                break;
        }
    }

    private void evaluate() {
        try {
            String input = display.getText().replaceAll("\\s+", "");

            if (input.contains("%%%")) {
                display.setText("Invalid Input");
                return;
            }

            // a% % b → a percent of b
            if (input.contains("%%")) {
                String[] parts = input.split("%%");
                double percent = Double.parseDouble(parts[0].replace("%", ""));
                double base = Double.parseDouble(parts[1]);
                double result = (percent / 100.0) * base;
                display.setText(String.valueOf(result));
                return;
            }

            // a % b → what percent is a of b
            if (input.contains("%")) {
                String[] parts = input.split("%");
                double num = Double.parseDouble(parts[0]);
                double total = Double.parseDouble(parts[1]);
                double result = (num / total) * 100;
                display.setText(String.format("%.2f%%", result));
                return;
            }

            // Basic arithmetic fallback
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("JavaScript");
            Object result = engine.eval(input);
            display.setText(result.toString());

        } catch (Exception e) {
            display.setText("Error");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
