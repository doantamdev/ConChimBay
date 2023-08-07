package conchimbay;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.Scanner;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class ConChimBay extends JPanel implements KeyListener, Runnable {

    private int WIDTH ;
    private int HEIGHT;
    //vi tri bat dau theo trục X Y
    private final int INITIAL_X = 100;
    private final int INITIAL_Y = HEIGHT / 2;
    //trọng lực khi chim rơi xuống
    private final int GRAVITY = 1;
    //tăng tốc độ game sau khi qua cột thứ 10
    private int checkPipe = 0;
    private int gameSpeed = 5; 
    
    private Random random;
    
    ChuongNgaiVat pipe;   
    NguoiChoi player;
    Dan dan;
    MucTieu target;
    
    
    
    //diem
    private int scorePlayer1;
    private int scorePlayer2;
    private int highScore;

    
    //trạng thái mục tiêu
    private boolean targetActive;
    
    //trạng thái gane
    private boolean gameOver;   
    private boolean paused = false;


    public ConChimBay() {
        JFrame frame = new JFrame("Con Chim Bay - 2 người chơi");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        WIDTH = (int) screenSize.getWidth();
        HEIGHT = (int) screenSize.getHeight();
        frame.setSize(WIDTH/2+500, HEIGHT/2+300);
        //đặt hành động mặt định khi người chơi đóng ứng dụng
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //thay đổi kích thước cửa sổ
        frame.setResizable(false);
        frame.addKeyListener(this);
        //thêm vào khung
        frame.add(this);
        //hiển thị
        frame.setVisible(true);
        highScore = loadHighScore(); // Gọi phương thức để đọc điểm cao nhất từ tệp
        playBackgroundMusic();


        
        pipe = new ChuongNgaiVat();
        player = new NguoiChoi();
        dan = new Dan();
        target = new MucTieu();

        player.player1Y = INITIAL_Y;
        player.player2Y = INITIAL_Y;
        player.player1Velocity = 0;
        player.player2Velocity = 0;
        //vị trí đầu tiên của ống nằm ở chiều rộng
        pipe.pipeX = WIDTH;
        

        scorePlayer1 = 0;
        scorePlayer2 = 0;
        
        //random chiều cao ống
        random = new Random();
        pipe.pipeHeight = randomChieuCaoPipe();

        //dan
        //check người chơi bắn
        dan.player1BulletFired = false;
        dan.player2BulletFired = false;
        //trị trí đầu viên đạn
        dan.player1BulletX = 0;
        dan.player1BulletY = 0;
        dan.player2BulletX = 0;
        dan.player2BulletY = 0;
        dan.bulletSpeed = 10;
        dan.bulletWidth = 10;
        dan.bulletHeight = 10;
        //anh nguoi choi
          // Tải ảnh người chơi từ tệp "chim.jpg"
        player.playerImage1  = new ImageIcon(getClass().getClassLoader().getResource("anh/chim1.png")).getImage();
        player.playerImage2  = new ImageIcon(getClass().getClassLoader().getResource("anh/chim2.png")).getImage();
        target.imageTarget  = new ImageIcon(getClass().getClassLoader().getResource("anh/gold.png")).getImage();
        //muc tieu
        target.targetWidth = 30;
        target.targetHeight = 30;
        targetActive = false;
        
        // tạo ra run vẽ giao diện
        Thread thread = new Thread(this);
        thread.start();
        //hinh
        player.player1X=INITIAL_X;
        player.player2X=INITIAL_X+100;
    }
   
    //vẽ giao diện = Graphics
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
    //giao diện khung
    g.setColor(Color.CYAN);
    g.fillRect(0, 0, WIDTH, HEIGHT);
    
    
    int scoreDiff = scorePlayer2 - scorePlayer1;
    int scoreDiff2 = scorePlayer1 - scorePlayer2;
    if (scoreDiff >= 1) {
         g.setColor(Color.ORANGE);
        g.drawImage(player.playerImage2, INITIAL_X+100, player.player2Y, 40, 40, this);
        // Vẽ hình ảnh người chơi 1
        g.setColor(Color.RED);
        int player1Height = 40 + scoreDiff +10; // Tăng chiều cao của người chơi 1
        int player1Width = 40 + scoreDiff +10; // Tăng chiều rộng của người chơi 1
        //g.fillRect(INITIAL_X, player.player1Y, 40, player1Height);
         g.drawImage(player.playerImage1, INITIAL_X, player.player1Y, player1Width, player1Height, this);
    } else if (scoreDiff2 >= 1) {
         // Vẽ hình ảnh người chơi 1
            g.setColor(Color.RED);
            g.drawImage(player.playerImage1, INITIAL_X, player.player1Y, 40, 40, this);
           g.setColor(Color.ORANGE);
            int player2Height = 40 + scoreDiff2 +10; // Tăng chiều cao của người chơi 2
            int player2Width = 40 + scoreDiff2 +10; // Tăng chiều rộng của người chơi 2
            //g.fillRect(INITIAL_X + 100, player.player2Y, 40, player2Height);
            g.drawImage(player.playerImage2, INITIAL_X+100, player.player2Y, player2Width, player2Height, this);
        }
    else {
        // Vẽ hình ảnh người chơi 1
        g.setColor(Color.RED);
          g.drawImage(player.playerImage1, INITIAL_X, player.player1Y, 40, 40, this);

        // Vẽ hình ảnh người chơi 2
        g.setColor(Color.ORANGE);
        g.drawImage(player.playerImage2, INITIAL_X+100, player.player2Y, 40, 40, this);
    }
    //điểm
    g.setColor(Color.WHITE);
    g.drawString("Player 1 Score: " + scorePlayer1, 10, 20);
    g.drawString("Player 2 Score: " + scorePlayer2, 10, 40);
    //chướng ngại vật
    g.setColor(Color.GREEN);
    g.fillRect(pipe.pipeX, 0, pipe.PIPE_WIDTH, pipe.pipeHeight);
    g.fillRect(pipe.pipeX, pipe.pipeHeight + pipe.PIPE_GAP, pipe.PIPE_WIDTH, HEIGHT - pipe.pipeHeight - pipe.PIPE_GAP);

    // đạn nguoi1 choi 1
    g.setColor(Color.RED);
    if (dan.player1BulletFired) {
        g.fillRect(dan.player1BulletX, dan.player1BulletY, dan.bulletWidth, dan.bulletHeight);
    }
    // đạn nguoi1 choi 2
    g.setColor(Color.ORANGE);
    if (dan.player2BulletFired) {
        g.fillRect(dan.player2BulletX, dan.player2BulletY, dan.bulletWidth, dan.bulletHeight);
    }

    // Mục tiêu
    g.setColor(Color.YELLOW);
    if (targetActive) {
        //g.fillRect(target.targetX, target.targetY, target.targetWidth, target.targetHeight);
        g.drawImage(target.imageTarget, target.targetX, target.targetY, target.targetWidth+20, target.targetHeight+20, this);
    }

    if (gameOver) {
        g.setColor(Color.RED);
        if(scorePlayer1<scorePlayer2)
        {   
           g.setFont(new Font("Arial", Font.BOLD, 25)); 
           g.drawString("Người chơi 2 win", WIDTH / 2 -280, HEIGHT / 2 - 150);
           g.setFont(new Font("Arial", Font.BOLD, 20));
           g.drawString("Ấn space đế tiếp tục chơi nhé", WIDTH / 2 - 320, HEIGHT / 2 - 130);
        }
        else if(scorePlayer1>scorePlayer2){
             g.setFont(new Font("Arial", Font.BOLD, 25));
             g.drawString("Người chơi 1 win", WIDTH / 2 -280, HEIGHT / 2 - 150);
             g.setFont(new Font("Arial", Font.BOLD, 20));
              g.drawString("Ấn space đế tiếp tục chơi nhé", WIDTH / 2 - 320, HEIGHT / 2 - 130);
        }
    }
    // Vẽ điểm cao nhất
    g.setColor(Color.WHITE);
    g.drawString("High Score: " + highScore, 10, 60);

}

@Override
public void run() {
    //Trong luồng 2, sử dụng vòng lặp vô hạn để cập nhật vị trí của người chơi dựa trên sự kiện nhấn phím và các điều kiện khác.
    while (true) {
        if (gameOver) {
           saveHighScore();
        }
        if (scorePlayer1 > highScore) {
             highScore = scorePlayer1;
           }

        if (scorePlayer2 > highScore) {
              highScore = scorePlayer2;
            }

        if (!gameOver && !paused) {
            //tốc độ của người chơi 1 tăng theo giá trị gia tốc, giúp người chơi rơi xuống dưới màn hình theo đường cong hình parabol. 
            player.player1Velocity += GRAVITY;
            player.player2Velocity += GRAVITY;
            // cập nhật vị trí theo trục y của người chơi 1 trong trạng thái rơi tự do.
            player.player1Y += player.player1Velocity;
            player.player2Y += player.player2Velocity;
            //cập nhật vị trí của ống nước (pipe) trong trò chơi. 
            // ống nước sẽ giảm đi 5 đơn vị. Điều này đồng nghĩa với việc ống nước di chuyển sang trái trên màn hình.
            if (checkPipe >= 2) {
                    gameSpeed = 10; 
                    pipe.pipeX -= gameSpeed;
                }
            pipe.pipeX -= gameSpeed;
            
              // Kiểm tra chênh lệch điểm giữa hai người chơi
            int scoreDiff = Math.abs(scorePlayer1 - scorePlayer2);
            if (scoreDiff >= 1) {
                // Người chơi có điểm thấp hơn bay nhanh hơn
                if (scorePlayer1 < scorePlayer2) {
                    player.player1Velocity += GRAVITY * (scoreDiff / 5);
                } else {
                    player.player2Velocity += GRAVITY * (scoreDiff / 5);
                }
            }
            
            //vượt quá chiều cao thì thua
            if (player.player1Y > HEIGHT || player.player2Y > HEIGHT) {
                // Game over
                gameOver = true;
            }
            //kiểm tra xem ống nước đã vượt qua khung hiển thị của màn hình hay chưa
            if (pipe.pipeX + pipe.PIPE_WIDTH < 0) {
                 checkPipe++; 
                // thiết lập lại vị trí ban đầu của ống 
                pipe.pipeX = WIDTH;
                pipe.pipeHeight = randomChieuCaoPipe();
                scorePlayer1++;
                scorePlayer2++;
            }

            if (checkVaCham()) {
                // Game over
                gameOver = true;
                
            }

            if (targetActive && checkDanTrungMucTieu()) {
                // Mục tiêu bị đạn trúng
                targetActive = false;
                // Tăng điểm cho người chơi
                 if (dan.player1BulletFired) {
                    scorePlayer1++;
                }
                else if (dan.player2BulletFired) {
                    scorePlayer2++;
                }

            }
            
               // Cập nhật vị trí mục tiêu
        if (!targetActive) {
            // Mục tiêu không active, sinh ra mục tiêu mới
            target.targetX = WIDTH;
            target.targetY = random.nextInt(HEIGHT - target.targetHeight);
            targetActive = true;
        } else {
            // Di chuyển mục tiêu
                       target.targetX -= 5;
            if (target.targetX + target.targetWidth < 0) {
                // Mục tiêu ra khỏi màn hình, không active
                targetActive = false;
            }
        }
        
            // Cập nhật vị trí đạn
        if (dan.player1BulletFired) {
            dan.player1BulletX += dan.bulletSpeed;
            // kiểm tra đạn ra khỏi màn hình và set lai đạn
            if (dan.player1BulletX > WIDTH) {
                dan.player1BulletFired = false;
            }
        }

        if (dan.player2BulletFired) {
            dan.player2BulletX += dan.bulletSpeed;
            if (dan.player2BulletX > WIDTH) {
                dan.player2BulletFired = false;
            }
        }
            
    }

        repaint();

        // Giảm tốc độ rơi trò chơi
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
}

private int randomChieuCaoPipe() {
    return random.nextInt(HEIGHT / 2) + HEIGHT / 4;
}

private boolean checkVaCham() {
    // tạo ra một đối tượng hình chữ nhật đại diện cho vùng bao quanh người chơi trong trò chơi.
    Rectangle player1Rect = new Rectangle(INITIAL_X, player.player1Y, 40, 40);
    Rectangle player2Rect = new Rectangle(INITIAL_X + 100, player.player2Y, 40, 40);
    //tạo ra một đối tượng hình chữ nhật đại diện cho vùng bao quanh ống nước
    Rectangle pipeTopRect = new Rectangle(pipe.pipeX, 0, pipe.PIPE_WIDTH, pipe.pipeHeight);
    //Tạo ra một đối tượng hình chữ nhật đại diện cho vùng bao quanh ống nước phía dưới trong trò chơi.
    Rectangle pipeBottomRect = new Rectangle(pipe.pipeX, pipe.pipeHeight + pipe.PIPE_GAP, pipe.PIPE_WIDTH, HEIGHT - pipe.pipeHeight - pipe.PIPE_GAP);
    //intersects() là một phương thức trong lớp java.awt.Rectangle, được sử dụng để kiểm tra xem hai hình chữ nhật có giao nhau hay không.
    if (player1Rect.intersects(pipeTopRect) || player1Rect.intersects(pipeBottomRect)) {
        scorePlayer1 -= 10; // Trừ điểm cho người chơi 1
        return true;
    }

    if (player2Rect.intersects(pipeTopRect) || player2Rect.intersects(pipeBottomRect)) {
        scorePlayer2 -= 10; // Trừ điểm cho người chơi 2
        return true;
    }
     // Kiểm tra vượt qua màn hình
    if (player.player1Y < 0 || player.player1Y + 40 > HEIGHT) {
         scorePlayer1 -= 10;
        return true; // Người chơi bay vượt qua màn hình, trò chơi kết thúc
    }
    if (player.player2Y < 0 || player.player2Y + 40 > HEIGHT){
         scorePlayer2 -= 10;
        return true;
    }
     return false; // Không có va chạm
}

private boolean checkDanTrungMucTieu() {
    //Đối tượng này được sử dụng để đại diện cho hình chữ nhật bao quanh viên đạn của người chơi 1.
    Rectangle player1BulletRect = new Rectangle(dan.player1BulletX, dan.player1BulletY, dan.bulletWidth, dan.bulletHeight);
    Rectangle player2BulletRect = new Rectangle(dan.player2BulletX, dan.player2BulletY, dan.bulletWidth, dan.bulletHeight);
    //Đối tượng này được sử dụng để đại diện cho hình chữ nhật bao quanh mục tiêu trong trò chơi
    Rectangle targetRect = new Rectangle(target.targetX, target.targetY, target.targetWidth, target.targetHeight);
    // Đạn không trúng mục tiêu
    return player1BulletRect.intersects(targetRect) || player2BulletRect.intersects(targetRect); 
}



private void saveHighScore() {
    try {
        FileWriter writer = new FileWriter("high_score.txt");
        writer.write(String.valueOf(highScore));
        writer.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
}

private int loadHighScore() {
    int highScore = 0;
    File file = new File("high_scores.txt");
    
    // Kiểm tra nếu tệp không tồn tại, thì tạo mới
    if (!file.exists()) {
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    try (Scanner scanner = new Scanner(file)) {
        if (scanner.hasNextInt()) {
            highScore = scanner.nextInt();
        }
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    }
    
    return highScore;
}

private void playBackgroundMusic() {
    try {
        URL musicFileUrl = getClass().getClassLoader().getResource("anh/song2.wav");
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(musicFileUrl);
        Clip clip = AudioSystem.getClip();
        clip.open(audioInputStream);
        clip.loop(Clip.LOOP_CONTINUOUSLY); 
    } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
        e.printStackTrace();
    }
}



@Override
public void keyTyped(KeyEvent e) {
    // Không sử dụng
}

@Override
public void keyPressed(KeyEvent e) {
    int key = e.getKeyCode();    
     if (key == KeyEvent.VK_UP) {
         //nhảy lên với một độ cao xác định bởi JUMP_HEIGHT.
            player.player2Velocity = pipe.JUMP_HEIGHT;
        }
     
    if (key == KeyEvent.VK_SPACE) {
        if (gameOver) {
            // Khởi động lại trò chơi
            gameOver = false;
            scorePlayer1 = 0;
            scorePlayer2 = 0;
            player.player1Y = INITIAL_Y;
            player.player2Y = INITIAL_Y;
            player.player1Velocity = 0;
            player.player2Velocity = 0;
            checkPipe=0;
            pipe.pipeX = WIDTH;
            pipe.pipeHeight = randomChieuCaoPipe();
        } else {
            player.player1Velocity = pipe.JUMP_HEIGHT;
        }

    } else if (key == KeyEvent.VK_Q) {
        if (!dan.player1BulletFired) {
            // Người chơi 1 bắn đạn
            dan.player1BulletFired = true;
            // đạn người chơi 1 sẽ được đặt tại vị trí bắt đầu ban đầu của người chơi cộng thêm 40.
            dan.player1BulletX = INITIAL_X + 40;
            //vị trí ban đầu của viên đạn người chơi 1 sẽ được đặt tại vị trí hiện tại của người chơi cộng thêm 15
            //để đảm bảo rằng viên đạn bắn ra từ vị trí phù hợp trên người chơi
            dan.player1BulletY = player.player1Y + 15;
        }
    } else if (key == KeyEvent.VK_ENTER) {
        if (!dan.player2BulletFired) {
            // Người chơi 2 bắn đạn
            dan.player2BulletFired = true;
            dan.player2BulletX = INITIAL_X + 140;
            dan.player2BulletY = player.player2Y + 15;
        }
    }else if (key == KeyEvent.VK_S) {
        // Tạm dừng hoặc trở lại trò chơi
        paused = !paused;
    }
    
}

@Override
public void keyReleased(KeyEvent e) {
    // Không sử dụng
}

public static void main(String[] args) {
    //Trong trò chơi, sử dụng một luồng chính để vẽ giao diện và xử lý sự kiện người dùng
    //để tạo luồng người chơi 2 và bắt đầu nó.
    SwingUtilities.invokeLater(() -> {
        new ConChimBay();
    });
   }
}


