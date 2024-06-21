import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

import java.util.*;

public class DijkstraFX extends Application {
    private static final int INF = 999;

    private int[] dist;
    private int[][] cost;
    private int nodes;
    private int source;

    private ComboBox<Integer> sourceComboBox;
    private TextArea outputTextArea;
    private Pane graphPane;

    private Map<Integer, Circle> nodeCircles = new HashMap<>();
    private List<Line> shortestPathLines = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #f0f0f0;");

        VBox inputBox = new VBox(10);
        inputBox.setPadding(new Insets(10));
        inputBox.setMaxWidth(300);
        inputBox.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #ccc; -fx-border-width: 1px; -fx-border-radius: 5px;");

        Label nodesLabel = new Label("Cantidad de nodos:");
        nodesLabel.setStyle("-fx-font-weight: bold;");

        TextField nodesField = new TextField();
        nodesField.setPromptText("Ingrese la cantidad de nodos");
        nodesField.setStyle("-fx-pref-width: 150px;");

        Button submitNodesButton = new Button("Aceptar");
        submitNodesButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        submitNodesButton.setOnAction(e -> {
            nodes = Integer.parseInt(nodesField.getText());
            dist = new int[nodes + 1];
            cost = new int[nodes + 1][nodes + 1];
            createCostMatrix(root);
        });

        inputBox.getChildren().addAll(nodesLabel, nodesField, submitNodesButton);
        inputBox.setAlignment(Pos.CENTER);

        outputTextArea = new TextArea();
        outputTextArea.setEditable(false);
        outputTextArea.setWrapText(true);
        outputTextArea.setStyle("-fx-font-family: 'Courier New';");

        graphPane = new Pane();
        graphPane.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-width: 1px; -fx-border-radius: 5px;");
        graphPane.setMinSize(400, 400);
        graphPane.setMaxSize(400, 400);
        graphPane.setVisible(false);

        root.setLeft(inputBox);
        root.setCenter(outputTextArea);
        root.setRight(graphPane);

        Scene scene = new Scene(root, 900, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Dijkstra - Shortest Path Algorithm");
        primaryStage.show();
    }

    private void createCostMatrix(BorderPane root) {
        GridPane costGrid = new GridPane();
        costGrid.setAlignment(Pos.CENTER);
        costGrid.setPadding(new Insets(10));
        costGrid.setHgap(10);
        costGrid.setVgap(5);
        costGrid.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-border-width: 1px; -fx-border-radius: 5px;");

        for (int i = 1; i <= nodes; i++) {
            for (int j = 1; j <= nodes; j++) {
                TextField costField = new TextField();
                costField.setPromptText("Peso de " + i + " a " + j);
                costField.setStyle("-fx-pref-width: 80px;");
                costGrid.add(costField, j - 1, i - 1);
            }
        }

        Button calculateButton = new Button("Calcular");
        calculateButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        calculateButton.setOnAction(e -> {
            readCostMatrix(costGrid);
            createSourceSelection(root);
        });

        VBox controlBox = new VBox(10);
        controlBox.setAlignment(Pos.CENTER);
        controlBox.setPadding(new Insets(10));
        controlBox.getChildren().addAll(costGrid, calculateButton);

        root.setRight(controlBox);
    }

    private void readCostMatrix(GridPane costGrid) {
        for (int i = 1; i <= nodes; i++) {
            for (int j = 1; j <= nodes; j++) {
                TextField costField = (TextField) costGrid.getChildren().get((i - 1) * nodes + (j - 1));
                String value = costField.getText().trim();
                if (value.isEmpty()) {
                    cost[i][j] = INF;
                } else {
                    cost[i][j] = Integer.parseInt(value);
                }
            }
        }
    }

    private void createSourceSelection(BorderPane root) {
        VBox sourceBox = new VBox(10);
        sourceBox.setPadding(new Insets(10));
        sourceBox.setMaxWidth(200);
        sourceBox.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #ccc; -fx-border-width: 1px; -fx-border-radius: 5px;");

        Label sourceLabel = new Label("Seleccione el origen:");
        sourceLabel.setStyle("-fx-font-weight: bold;");

        sourceComboBox = new ComboBox<>();
        for (int i = 1; i <= nodes; i++) {
            sourceComboBox.getItems().add(i);
        }
        sourceComboBox.setStyle("-fx-pref-width: 150px;");

        Button submitSourceButton = new Button("Aceptar");
        submitSourceButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        submitSourceButton.setOnAction(e -> {
            source = sourceComboBox.getValue();
            runDijkstra();
        });

        sourceBox.getChildren().addAll(sourceLabel, sourceComboBox, submitSourceButton);

        root.setBottom(sourceBox);
    }

    private void runDijkstra() {
        if (source == 0 || nodes == 0) return;

        graphPane.getChildren().clear();
        nodeCircles.clear();
        shortestPathLines.clear();

        drawNodes();

        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(node -> node.distance));
        boolean[] visited = new boolean[nodes + 1];

        Arrays.fill(dist, INF);
        dist[source] = 0;

        priorityQueue.offer(new Node(source, 0));

        while (!priorityQueue.isEmpty()) {
            Node currentNode = priorityQueue.poll();
            int currentNodeIndex = currentNode.index;

            if (visited[currentNodeIndex]) continue;

            visited[currentNodeIndex] = true;

            for (int i = 1; i <= nodes; i++) {
                if (!visited[i] && cost[currentNodeIndex][i] != INF) {
                    int newDistance = dist[currentNodeIndex] + cost[currentNodeIndex][i];
                    if (newDistance < dist[i]) {
                        dist[i] = newDistance;
                        priorityQueue.offer(new Node(i, newDistance));
                        drawShortestPath(currentNodeIndex, i);
                    }
                }
            }
        }

        displayShortestPaths();
        graphPane.setVisible(true);
    }

    private void drawNodes() {
        double centerX = graphPane.getWidth() / 2;
        double centerY = graphPane.getHeight() / 2;
        double radius = Math.min(centerX, centerY) / 2;

        double angleStep = 360.0 / nodes;
        for (int i = 1; i <= nodes; i++) {
            double angle = i * angleStep;
            double x = centerX + radius * Math.cos(Math.toRadians(angle));
            double y = centerY + radius * Math.sin(Math.toRadians(angle));

            Circle circle = new Circle(x, y, 20, Color.LIGHTBLUE);
            circle.setStroke(Color.BLACK);
            circle.setStrokeWidth(2);
            nodeCircles.put(i, circle);
            graphPane.getChildren().add(circle);

            Label label = new Label(Integer.toString(i));
            label.setLayoutX(x - 5);
            label.setLayoutY(y - 15);
            graphPane.getChildren().add(label);
        }
    }

    private void drawShortestPath(int from, int to) {
        Circle fromCircle = nodeCircles.get(from);
        Circle toCircle = nodeCircles.get(to);

        Line line = new Line(fromCircle.getCenterX(), fromCircle.getCenterY(), toCircle.getCenterX(), toCircle.getCenterY());
        line.setStroke(Color.RED);
        line.setStrokeWidth(3);
        shortestPathLines.add(line);
        graphPane.getChildren().add(line);
    }

    private void displayShortestPaths() {
        StringBuilder sb = new StringBuilder();
        sb.append("El camino más corto desde el nodo ").append(source).append(" hacia los demás nodos es:\n\n");
        for (int i = 1; i <= nodes; i++) {
            if (i != source) {
                sb.append("Desde ").append(source).append(" hasta ").append(i);
                if (dist[i] == INF) {
                    sb.append("No hay camino disponible.\n");
                } else {
                    sb.append("Distancia mínima: ").append(dist[i]).append("\n");
                }
            }
            sb.append("\n");
        }

        outputTextArea.setText(sb.toString());
    }

    static class Node {
        int index;
        int distance;

        public Node(int index, int distance) {
            this.index = index;
            this.distance = distance;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "index=" + index +
                    ", distance=" + distance +
                    '}';
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
