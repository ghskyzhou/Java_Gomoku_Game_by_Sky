import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.scene.control.*;
import javafx.scene.shape.*;
import javafx.util.Duration;

public class Main extends Application {

    private Image stoneBlack, stoneWhite;
    private Pane gamePane;
    private ImageView backgroundBoardView;
    private Rectangle highlightBox;
    private Text WhoseTurnText, BlackTimeText, WhiteTimeText;

    private boolean isBlackTurn = true, gameOverBool = false;
    private boolean AIMode = false;
    private int[][] gameBoard = new int[20][20]; // 0: None; 1: White; 2: Black
    private final double gridSize = 30;
    private int BlackTimer = 600, WhiteTimer = 600;

    private Timeline BlackTimeline = new Timeline(
        new KeyFrame(Duration.seconds(1), event -> {
            BlackTimer--;
            BlackTimeText.setText("BLACK: " + BlackTimer + "\n");
            if(BlackTimer <= 0) {
                Platform.runLater(() -> {
                    gameOver("White");
                });
            }
        })
    );

    private Timeline WhiteTimeline = new Timeline(
        new KeyFrame(Duration.seconds(1), event -> {
            WhiteTimer--;
            WhiteTimeText.setText("WHITE: " + WhiteTimer + "\n");

            if(WhiteTimer <= 0) {
                Platform.runLater(() -> {
                    gameOver("Black");
                });
            }
        })
    );

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        primaryStage.setTitle("Java Gomoku");

        Image backgroundBoard = new Image("file:./img/Board.png");
        backgroundBoardView = new ImageView(backgroundBoard);
        backgroundBoardView.setFitWidth(570);
        backgroundBoardView.setFitHeight(570);

        stoneBlack = new Image("file:./img/Stones_black.png");
        stoneWhite = new Image("file:./img/Stones_white.png");

        highlightBox = new Rectangle(30, 30, Color.BLACK);
        highlightBox.setOpacity(0.3);
        highlightBox.setManaged(false);

        gamePane = new Pane();
        gamePane.getChildren().add(backgroundBoardView);
        gamePane.setOnMouseClicked(event -> mouseClick(event));
        gamePane.setOnMouseMoved(event -> placeHighlight(event));
        gamePane.setOnMouseEntered(event -> {
            if(!gamePane.getChildren().contains(highlightBox)) {
                gamePane.getChildren().add(highlightBox);
            }
        });
        gamePane.setOnMouseExited(event -> {
            gamePane.getChildren().remove(highlightBox);
        });

        TextFlow titleText = new TextFlow();
        titleText.setPrefHeight(300);
        Text titleGomuku = new Text("Gomoku\n");
        Text titleGame = new Text("Game\n");
        titleGomuku.setFont(Font.font("Verdana", FontWeight.BOLD, 40));
        titleGomuku.setFill(Color.BROWN);
        titleGame.setFont(Font.font("Verdana", FontWeight.BOLD, 40));
        titleGame.setFill(Color.BROWN);
        titleText.getChildren().addAll(titleGomuku, titleGame);

        Text authorText = new Text("\nMade by Skyzhou\n\n");
        authorText.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
        titleText.getChildren().addAll(authorText);

        WhoseTurnText = new Text("BLACK's Turn");
        WhoseTurnText.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        WhoseTurnText.setFill(Color.RED);
        titleText.getChildren().addAll(WhoseTurnText);

        Text TimerTitle = new Text("\n\nTimer(s)\n");
        TimerTitle.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        BlackTimeText = new Text("BLACK: " + BlackTimer + "\n");
        WhiteTimeText = new Text("WHITE: " + WhiteTimer + "\n");
        BlackTimeText.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
        WhiteTimeText.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
        titleText.getChildren().addAll(TimerTitle, BlackTimeText, WhiteTimeText);

        BlackTimeline.setCycleCount(Timeline.INDEFINITE);
        WhiteTimeline.setCycleCount(Timeline.INDEFINITE);

        Button startButton = new Button("New Game (PVP)");
        startButton.setStyle("-fx-font-size: 12px;");
        startButton.setPrefSize(120, 40);
        startButton.setOnMouseClicked(event -> startNewGame());

        Button startAIButton = new Button("New Game (AI)");
        startAIButton.setStyle("-fx-font-size: 12px;");
        startAIButton.setPrefSize(120, 40);
        startAIButton.setOnMouseClicked(event -> startNewAIGame());

        Text justSpace = new Text("\n");

        Button exitButton = new Button("Exit");
        exitButton.setStyle("-fx-font-size: 16px;");
        exitButton.setPrefSize(120, 40);
        exitButton.setOnMouseClicked(event -> Platform.exit());

        VBox menuLayout = new VBox(10);
        menuLayout.getChildren().addAll(titleText, startButton, startAIButton, justSpace, exitButton);
        menuLayout.setAlignment(Pos.CENTER);

        HBox allGameLayout = new HBox(10);
        allGameLayout.getChildren().addAll(gamePane, menuLayout);

        Scene gameScene = new Scene(allGameLayout, 800, 570);
        primaryStage.setScene(gameScene);
        primaryStage.getIcons().add(new Image("file:./img/Happy.jpg"));
        primaryStage.show();
    }

    private void mouseClick(javafx.scene.input.MouseEvent event) {
        //System.out.println(event.getX() + " " + event.getY());
        double x = event.getX();
        double y = event.getY();
        int gridX = (int) (x / gridSize);
        int gridY = (int) (y / gridSize);
        placeStone(gridX, gridY);
    }

    private void placeStone(int gridX, int gridY) {
        if(gameOverBool) return;

        if(gameBoard[gridX][gridY] != 0) return;
        if(isBlackTurn) gameBoard[gridX][gridY] = 2;
        else gameBoard[gridX][gridY] = 1;
        double centerX = gridX * gridSize + gridSize / 2;
        double centerY = gridY * gridSize + gridSize / 2;

        ImageView stoneView = new ImageView(isBlackTurn ? stoneBlack : stoneWhite);
        stoneView.setFitWidth(gridSize);
        stoneView.setFitHeight(gridSize);
        stoneView.setX(centerX - stoneView.getFitWidth() / 2);
        stoneView.setY(centerY - stoneView.getFitHeight() / 2);
        //System.out.println(stoneView.getX() + " " + stoneView.getY());

        gamePane.getChildren().add(stoneView);

        checkWin();

        if(gameOverBool) return;
        isBlackTurn = !isBlackTurn;
        if(isBlackTurn) WhoseTurnText.setText("BLACK's Turn");
        else WhoseTurnText.setText("WHITE's Turn");

        if(isBlackTurn) {
            BlackTimeline.play();
            WhiteTimeline.pause();
        }
        else {
            BlackTimeline.pause();
            WhiteTimeline.play();
        }

        if(AIMode && !isBlackTurn) {
            AIPlaceStone();
        }
    }

    private void AIPlaceStone() {
        // 0: None; 1: White; 2: Black
        int blackWeight = 0, AIX = 0, AIY = 0;
        // 12 single with 2 blank
        // 20 double with 0 blank
        for(int x = 0; x < 19; x++) {
            for(int y = 0; y < 19; y++) {
                if(gameBoard[x][y] == 2) { //DEFENSE
                    // X-axis
                    int cnt = 1;
                    for(int i = 1; i <= 4; i++) {
                        if(gameBoard[x+i][y] == 2) cnt++;
                        else break;
                    }
                    //System.out.println(x+cnt);
                    if(x > 0 && x+cnt <= 18) {
                        if(gameBoard[x-1][y] == 0 && gameBoard[x+cnt][y] == 0) {
                            if(cnt*10 + 2 > blackWeight) {
                                blackWeight = cnt*10 + 2;
                                AIX = x-1; AIY = y;
                            }
                        }
                        else if(gameBoard[x-1][y] == 0) {
                            if(cnt*10 + 2 > blackWeight) {
                                blackWeight = cnt*10 + 1;
                                AIX = x-1; AIY = y;
                            }
                        }
                        else if(gameBoard[x+cnt][y] == 0) {
                            if(cnt*10 + 2 > blackWeight) {
                                blackWeight = cnt*10 + 1;
                                AIX = x+cnt; AIY = y;
                            }
                        }
                    }
                    else if(x == 0) {
                        if(gameBoard[x+cnt][y] == 0) {
                            if(cnt*10 + 2 > blackWeight) {
                                blackWeight = cnt*10 + 1;
                                AIX = x+cnt; AIY = y;
                            }
                        }
                    }
                    else if(x+cnt >= 19) {
                        if(gameBoard[x-1][y] == 0) {
                            if(cnt*10 + 2 > blackWeight) {
                                blackWeight = cnt*10 + 1;
                                AIX = x-1; AIY = y;
                            }
                        }
                    }

                    // Y-axis
                    cnt = 1;
                    for(int i = 1; i <= 4; i++) {
                        if(gameBoard[x][y+i] == 2) cnt++;
                        else break;
                    }
                    //System.out.println(x+cnt);
                    if(y > 0 && y+cnt <= 18) {
                        if(gameBoard[x][y-1] == 0 && gameBoard[x][y+cnt] == 0) {
                            if(cnt*10 + 2 > blackWeight) {
                                blackWeight = cnt*10 + 2;
                                AIX = x; AIY = y-1;
                            }
                        }
                        else if(gameBoard[x][y-1] == 0) {
                            if(cnt*10 + 2 > blackWeight) {
                                blackWeight = cnt*10 + 1;
                                AIX = x; AIY = y-1;
                            }
                        }
                        else if(gameBoard[x][y+cnt] == 0) {
                            if(cnt*10 + 2 > blackWeight) {
                                blackWeight = cnt*10 + 1;
                                AIX = x; AIY = y+cnt;
                            }
                        }
                    }
                    else if(y == 0) {
                        if(gameBoard[x][y+cnt] == 0) {
                            if(cnt*10 + 2 > blackWeight) {
                                blackWeight = cnt*10 + 1;
                                AIX = x; AIY = y+cnt;
                            }
                        }
                    }
                    else if(y+cnt >= 19) {
                        if(gameBoard[x][y-1] == 0) {
                            if(cnt*10 + 2 > blackWeight) {
                                blackWeight = cnt*10 + 1;
                                AIX = x; AIY = y-1;
                            }
                        }
                    }

                    // Diagonal left_top -> right_bottom
                    cnt = 1;
                    for(int i = 1; i <= 4; i++) {
                        if(gameBoard[x+i][y+i] == 2) cnt++;
                        else break;
                    }
                    //System.out.println(x+cnt);
                    if(x > 0 && y > 0 && x+cnt <= 18 && y+cnt <= 18) {
                        if(gameBoard[x-1][y-1] == 0 && gameBoard[x+cnt][y+cnt] == 0) {
                            if(cnt*10 + 2 > blackWeight) {
                                blackWeight = cnt*10 + 2;
                                AIX = x-1; AIY = y-1;
                            }
                        }
                        else if(gameBoard[x-1][y-1] == 0) {
                            if(cnt*10 + 2 > blackWeight) {
                                blackWeight = cnt*10 + 1;
                                AIX = x-1; AIY = y-1;
                            }
                        }
                        else if(gameBoard[x+cnt][y+cnt] == 0) {
                            if(cnt*10 + 2 > blackWeight) {
                                blackWeight = cnt*10 + 1;
                                AIX = x+cnt; AIY = y+cnt;
                            }
                        }
                    }
                    else if((y == 0 || x == 0) && (x+cnt <= 18 && y+cnt <= 18)) {
                        if(gameBoard[x+cnt][y+cnt] == 0) {
                            if(cnt*10 + 2 > blackWeight) {
                                blackWeight = cnt*10 + 1;
                                AIX = x+cnt; AIY = y+cnt;
                            }
                        }
                    }
                    else if((x+cnt >= 19 || y+cnt >= 19) && (x-1 >= 0 && y-1 >= 0)) {
                        if(gameBoard[x-1][y-1] == 0) {
                            if(cnt*10 + 2 > blackWeight) {
                                blackWeight = cnt*10 + 1;
                                AIX = x-1; AIY = y-1;
                            }
                        }
                    }

                    // Diagonal right_top -> left_bottom
                    cnt = 1;
                    for(int i = 1; i <= 4; i++) {
                        if(x-i <= 0) break;
                        if(gameBoard[x-i][y+i] == 2) cnt++;
                        else break;
                    }
                    //System.out.println(x+cnt);
                    if(x-cnt >= 0 && y-1 >= 0 && x+1 <= 18 && y+cnt <= 18) {
                        if(gameBoard[x+1][y-1] == 0 && gameBoard[x-cnt][y+cnt] == 0) {
                            if(cnt*10 + 2 > blackWeight) {
                                blackWeight = cnt*10 + 2;
                                AIX = x+1; AIY = y-1;
                            }
                        }
                        else if(gameBoard[x+1][y-1] == 0) {
                            if(cnt*10 + 2 > blackWeight) {
                                blackWeight = cnt*10 + 1;
                                AIX = x+1; AIY = y-1;
                            }
                        }
                        else if(gameBoard[x-cnt][y+cnt] == 0) {
                            if(cnt*10 + 2 > blackWeight) {
                                blackWeight = cnt*10 + 1;
                                AIX = x-cnt; AIY = y+cnt;
                            }
                        }
                    }
                    else if((x == 18 || y == 0) && (x-cnt >= 0 && y+cnt <= 18)) {
                        if(gameBoard[x-cnt][y+cnt] == 0) {
                            if(cnt*10 + 2 > blackWeight) {
                                blackWeight = cnt*10 + 1;
                                AIX = x-cnt; AIY = y+cnt;
                            }
                        }
                    }
                    else if((x-cnt < 0 || y+cnt >= 19) && (x+1 <= 18 && y-1 >= 0)) {
                        if(gameBoard[x+1][y-1] == 0) {
                            if(cnt*10 + 2 > blackWeight) {
                                blackWeight = cnt*10 + 1;
                                AIX = x+1; AIY = y-1;
                            }
                        }
                    }
                }
                else if(gameBoard[x][y] == 1) { //ATTACK
                    // X-axis
                    int cnt = 1;
                    for(int i = 1; i <= 4; i++) {
                        if(gameBoard[x+i][y] == 1) cnt++;
                        else break;
                    }
                    if(cnt == 4) {
                        if(x+cnt <= 18 && gameBoard[x+cnt][y] == 0) {
                            blackWeight = 114514;
                            AIX = x+cnt; AIY = y;
                        }
                        else if(x-1 >= 0 && gameBoard[x-1][y] == 0) {
                            blackWeight = 114514;
                            AIX = x-1; AIY = y;
                        }
                    }
                    else if(cnt == 3 && blackWeight < 40) {
                        if(x+cnt <= 18 && x-1 >= 0 && gameBoard[x-1][y] == 0 && gameBoard[x+cnt][y] == 0) {
                            blackWeight = 114514;
                            AIX = x-1; AIY = y;
                        }
                    }

                    // Y-axis
                    cnt = 1;
                    for(int i = 1; i <= 4; i++) {
                        if(gameBoard[x][y+i] == 1) cnt++;
                        else break;
                    }
                    if(cnt == 4) {
                        if(y+cnt <= 18 && gameBoard[x][y+cnt] == 0) {
                            blackWeight = 114514;
                            AIX = x; AIY = y+cnt;
                        }
                        else if(y-1 >= 0 && gameBoard[x][y-1] == 0) {
                            blackWeight = 114514;
                            AIX = x; AIY = y-1;
                        }
                    }
                    else if(cnt == 3 && blackWeight < 40) {
                        if(y+cnt <= 18 && y-1 >= 0 && gameBoard[x][y-1] == 0 && gameBoard[x][y+cnt] == 0) {
                            blackWeight = 114514;
                            AIX = x; AIY = y-1;
                        }
                    }

                    // Diagonal left_top -> right_bottom
                    cnt = 1;
                    for(int i = 1; i <= 4; i++) {
                        if(gameBoard[x+i][y+i] == 1) cnt++;
                        else break;
                    }
                    if(cnt == 4) {
                        if(x+cnt <= 18 && y+cnt <= 18 && gameBoard[x+cnt][y+cnt] == 0) {
                            blackWeight = 114514;
                            AIX = x+cnt; AIY = y+cnt;
                        }
                        else if(x-1 >= 0 && y-1 >= 0 && gameBoard[x-1][y-1] == 0) {
                            blackWeight = 114514;
                            AIX = x-1; AIY = y-1;
                        }
                    }
                    else if(cnt == 3 && blackWeight < 40) {
                        if(x+cnt <= 18 && x-1 >= 0 && y+cnt <= 18 && y-1 >= 0 && gameBoard[x-1][y-1] == 0 && gameBoard[x+cnt][y+cnt] == 0) {
                            blackWeight = 114514;
                            AIX = x-1; AIY = y-1;
                        }
                    }

                    // Diagonal right_top -> left_bottom
                    cnt = 1;
                    for(int i = 1; i <= 4; i++) {
                        if(x-i <= 0) break;
                        if(gameBoard[x-i][y+i] == 1) cnt++;
                        else break;
                    }
                    if(cnt == 4) {
                        if(x-cnt >= 0 && y+cnt <= 18 && gameBoard[x-cnt][y+cnt] == 0) {
                            blackWeight = 114514;
                            AIX = x-cnt; AIY = y+cnt;
                        }
                        else if(x+1 <= 18 && y-1 >= 0 && gameBoard[x+1][y-1] == 0) {
                            blackWeight = 114514;
                            AIX = x+1; AIY = y-1;
                        }
                    }
                    else if(cnt == 3 && blackWeight < 40) {
                        if(x-cnt >= 0 && x+1 <= 18 && y+cnt <= 18 && y-1 >= 0 && gameBoard[x+1][y-1] == 0 && gameBoard[x-cnt][y+cnt] == 0) {
                            blackWeight = 114514;
                            AIX = x+1; AIY = y-1;
                        }
                    }
                }
            }
        }
        System.out.println("Weight:" + blackWeight);
        placeStone(AIX, AIY);
    }

    private void placeHighlight(javafx.scene.input.MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        double gridSize = 30;
        int gridX = (int) (x / gridSize);
        int gridY = (int) (y / gridSize);
        double centerX = gridX * gridSize + gridSize / 2;
        double centerY = gridY * gridSize + gridSize / 2;
        highlightBox.setX(centerX - highlightBox.getWidth() / 2);
        highlightBox.setY(centerY - highlightBox.getHeight() / 2);
    }

    private void checkWin(){
        for(int x = 0; x < 19; x++){
            for(int y = 0; y < 19; y++){
                if(gameBoard[x][y] == 1)
                {
                    if((gameBoard[x][y] == gameBoard[x+1][y] && gameBoard[x][y] == gameBoard[x+2][y] && gameBoard[x][y] == gameBoard[x+3][y] && gameBoard[x][y] == gameBoard[x+4][y]) ||
                            (gameBoard[x][y] == gameBoard[x][y+1] && gameBoard[x][y] == gameBoard[x][y+2] && gameBoard[x][y] == gameBoard[x][y+3] && gameBoard[x][y] == gameBoard[x][y+4]) ||
                            (gameBoard[x][y] == gameBoard[x+1][y+1] && gameBoard[x][y] == gameBoard[x+2][y+2] && gameBoard[x][y] == gameBoard[x+3][y+3] && gameBoard[x][y] == gameBoard[x+4][y+4])){
                        gameOver("White");
                    } else if(y >= 4 && x <= 15){
                        if(gameBoard[x][y] == gameBoard[x+1][y-1] && gameBoard[x][y] == gameBoard[x+2][y-2] && gameBoard[x][y] == gameBoard[x+3][y-3] && gameBoard[x][y] == gameBoard[x+4][y-4]){
                            gameOver("White");
                        }
                    }
                } else if(gameBoard[x][y] == 2){
                    if((gameBoard[x][y] == gameBoard[x+1][y] && gameBoard[x][y] == gameBoard[x+2][y] && gameBoard[x][y] == gameBoard[x+3][y] && gameBoard[x][y] == gameBoard[x+4][y]) ||
                            (gameBoard[x][y] == gameBoard[x][y+1] && gameBoard[x][y] == gameBoard[x][y+2] && gameBoard[x][y] == gameBoard[x][y+3] && gameBoard[x][y] == gameBoard[x][y+4]) ||
                            (gameBoard[x][y] == gameBoard[x+1][y+1] && gameBoard[x][y] == gameBoard[x+2][y+2] && gameBoard[x][y] == gameBoard[x+3][y+3] && gameBoard[x][y] == gameBoard[x+4][y+4])){
                        gameOver("Black");
                    } else if(y >= 4 && x <= 15){
                        if(gameBoard[x][y] == gameBoard[x+1][y-1] && gameBoard[x][y] == gameBoard[x+2][y-2] && gameBoard[x][y] == gameBoard[x+3][y-3] && gameBoard[x][y] == gameBoard[x+4][y-4]){
                            gameOver("Black");
                        }
                    }
                }
            }
        }
    }

    private void startNewGame() {
        for(int x = 0; x < 19; x++)
            for(int y = 0; y < 19; y++) gameBoard[x][y] = 0;
        isBlackTurn = true;
        WhoseTurnText.setText("BLACK's Turn");
        gamePane.getChildren().clear();
        gamePane.getChildren().add(backgroundBoardView);
        BlackTimeline.pause();
        WhiteTimeline.pause();
        BlackTimer = 600;
        WhiteTimer = 600;
        BlackTimeText.setText("BLACK: " + BlackTimer + "\n");
        WhiteTimeText.setText("WHITE: " + WhiteTimer + "\n");
        gameOverBool = false;
        AIMode = false;
    }

    private void startNewAIGame() {
        startNewGame();
        AIMode = true;
    }

    private void gameOver(String winOne) {
        BlackTimeline.pause();
        WhiteTimeline.pause();
        Image alartImage = new Image("file:./img/Lika.gif");
        ImageView alartImageView = new ImageView(alartImage);
        alartImageView.setFitWidth(100);
        alartImageView.setPreserveRatio(true);
        Alert alertOver = new Alert(Alert.AlertType.INFORMATION);
        alertOver.setTitle("Game Over");
        alertOver.setHeaderText(winOne + " wins the game!");
        alertOver.setContentText("Have fun and good luck!");
        alertOver.setGraphic(alartImageView);
        Stage alertStage = (Stage) alertOver.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image("file:./img/Ciallo.jpg"));
        alertOver.showAndWait();
        WhoseTurnText.setText("GAME OVER!");
        gameOverBool = true;
    }
}