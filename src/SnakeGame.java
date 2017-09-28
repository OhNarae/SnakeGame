import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.*;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

public class SnakeGame {

	public static void main(String[] args) {
		GameFrame gameFrame = new GameFrame();
		gameFrame.Init();
	}
}

class GameFrame extends JFrame implements KeyListener, Runnable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int TIME_INTERVAL = 500;

	int FRAME_WIDTH; // ������ ����
	int FRAME_HEIGHT; // ������ ����

	Thread th;

	Image buffImage;
	Graphics buffg;

	ImageIcon[] imgSnake;
	ImageIcon imgWall;
	ImageIcon imgStar;

	int keyEvent = KeyEvent.VK_UP; // ���� Ű���� ����
//	int starNum = 0;

	ArrayList<SnakePiece> snake = new ArrayList<SnakePiece>();
	Star stars;

	int timeCount = 0;

	enum GameStatus {
		CONTINUE, DONE, FAIL, SUCCESS
	}

	GameStatus gameStatus = GameStatus.CONTINUE;

	public boolean InitVariable() {
		// �̹������� �����Ѵ�.
		imgSnake = new ImageIcon[3];
		for (int i = 0; i < imgSnake.length; ++i) {
			imgSnake[i] = new ImageIcon(GameFrame.class.getClassLoader().getResource("Snake_" + i + ".png"), "");
		}

		imgWall = new ImageIcon(GameFrame.class.getClassLoader().getResource("Wall.png"), "");
		imgStar = new ImageIcon(GameFrame.class.getClassLoader().getResource("Star.png"), "");

		// ȭ���� ũ�⸦ �����ش�. ���� ������ ����� ���߾� �ش�.
		FRAME_WIDTH = imgWall.getIconWidth() * 30;
		FRAME_HEIGHT = imgWall.getIconWidth() * 30;

		// ���� �ʹ� ��ġ�� �������ش�.
		stars = new Star(imgWall.getIconWidth(), 2 * imgWall.getIconHeight(), FRAME_WIDTH - 3 * imgWall.getIconWidth(),
				FRAME_HEIGHT - 3 * imgWall.getIconHeight());

		return true;
	}

	public boolean InitScreen() {
		setTitle("�� �Դ� ��");
		setSize(FRAME_WIDTH, FRAME_HEIGHT);

		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension screen = tk.getScreenSize();
		setLocation((int) (screen.getWidth() / 2 - FRAME_WIDTH / 2), (int) (screen.getHeight() / 2 - FRAME_HEIGHT / 2));

		setResizable(false);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		addKeyListener(this);

		return true;
	}

	public boolean Init() {

		if (!InitVariable()) {
			return false;
		}

		if (!InitScreen()) {
			return false;
		}

		th = new Thread(this);
		th.start();

		return true;
	}

	// Ű���� ���ۿ� ���� ������ üũ�ϰ� �� ���°����� �����Ų��.
	public boolean DealWithKeyInput() {
		SnakePiece piece = null;
		SnakePiece prePiece = null;

		Point nextHeadPoint = new Point();
		if (!snake.isEmpty()) {
			nextHeadPoint.x = snake.get(0).pos.x;
			nextHeadPoint.y = snake.get(0).pos.y;
			// �Ӹ��� ���� ��ǥ�� �����Ѵ�.			
			switch (keyEvent) {
			case KeyEvent.VK_UP:
				nextHeadPoint.y -= imgSnake[0].getIconHeight();
				break;
			case KeyEvent.VK_DOWN:
				nextHeadPoint.y += imgSnake[0].getIconHeight();
				break;
			case KeyEvent.VK_RIGHT:
				nextHeadPoint.x += imgSnake[0].getIconWidth();
				break;
			case KeyEvent.VK_LEFT:
				nextHeadPoint.x -= imgSnake[0].getIconWidth();
				break;
			}

			// ���� �ε������� �ʾҳ�?
			if (nextHeadPoint.x < imgWall.getIconWidth() || nextHeadPoint.x > (FRAME_WIDTH - 2 * imgWall.getIconWidth())
					|| nextHeadPoint.y < 2 * imgWall.getIconHeight()
					|| nextHeadPoint.y > (FRAME_HEIGHT - 2 * imgWall.getIconHeight())) {
				gameStatus = GameStatus.FAIL;
				return false;
			}

			// �ڱ���� �ε������� �ʾҳ�?
			for (int i = snake.size() - 1; i > 0; i--) {
				piece = snake.get(i);
				if (Crash(nextHeadPoint, piece.pos, imgSnake[0].getIconWidth(), imgSnake[1].getIconWidth(),
						imgSnake[0].getIconHeight(), imgSnake[1].getIconHeight())) {
					gameStatus = GameStatus.FAIL;
					return false;
				}
			}

			// ���� ���� �� �ִ°�?
			if (Crash(nextHeadPoint, stars.pos, imgSnake[1].getIconWidth(), imgSnake[1].getIconHeight(),
					imgStar.getIconWidth(), imgStar.getIconHeight())) {
				// ���� ���̸� �߰��Ѵ�.
				snake.add(new SnakePiece(0, 0));

				// ���� �ִ� ���� �������� ���ο� ���� �����.
				stars.replace();

				// ���� ���� ���ڰ� �þ��.
				if (stars.addAndCheck()) // 10���� �� �Ծ ������ ���
				{
					gameStatus = GameStatus.DONE;
					stars.setCompleteTimeCount(timeCount);
					return false;
				}
			}
		}

		// �� �� �����̱�
		for (int i = snake.size() - 1; i >= 0; i--) {
			piece = snake.get(i);

			if (0 == i) {
				piece.pos.x = nextHeadPoint.x;
				piece.pos.y = nextHeadPoint.y;
			} else {
				prePiece = snake.get(i - 1);

				piece.pos.x = prePiece.pos.x;
				piece.pos.y = prePiece.pos.y;
			}
		}

		return true;
	}

	public boolean Crash(Point p1, Point p2, int w1, int h1, int w2, int h2) {

		if (Math.abs((p1.x + w1 / 2) - (p2.x + w2 / 2)) < (w2 / 2 + w1 / 2)
				&& Math.abs((p1.y + h1 / 2) - (p2.y + h2 / 2)) < (h2 / 2 + h1 / 2)) {
			return true;
		}
		return false;
	}

	public void paint(Graphics g) {
		buffImage = createImage(FRAME_WIDTH, FRAME_HEIGHT);
		buffg = buffImage.getGraphics();

		// ȭ�� ���۸� �����.
		buffg.clearRect(0, 0, FRAME_WIDTH, FRAME_HEIGHT);

		// ���� �׸���.
		int wallWidth = imgWall.getIconWidth();
		int wallHeight = imgWall.getIconHeight();

		int xCount = FRAME_WIDTH / wallWidth;
		int yCount = FRAME_HEIGHT / wallHeight;
		// �� ���� ��
		for (int j = 0; j < xCount; j++)
			buffg.drawImage(imgWall.getImage(), wallWidth * j, wallHeight, this);
		// ������ ���� ��
		for (int j = 0; j < yCount; j++)
			buffg.drawImage(imgWall.getImage(), wallWidth * (xCount - 1), wallHeight * j, this);
		// �Ʒ� ���� ��
		for (int j = 0; j < xCount; j++)
			buffg.drawImage(imgWall.getImage(), wallWidth * j, wallHeight * (yCount - 1), this);
		// ���� ���� ��
		for (int j = 0; j < xCount; j++)
			buffg.drawImage(imgWall.getImage(), 0, wallHeight * j, this);

		//�ʱ� ȭ��: ���� ���� �����鼭 ���� �����ϰ� ���� ��ġ�� ȭ��ȿ��
		switch (timeCount) {
		case 1:
		case 2:
		case 3:
			buffg.clearRect(FRAME_WIDTH - 5 * imgWall.getIconWidth(), FRAME_HEIGHT - imgWall.getIconHeight(),
					imgWall.getIconWidth() * timeCount, imgWall.getIconHeight());
			break;
		case 4:
		case 5:
		case 6:
			buffg.clearRect(FRAME_WIDTH - 5 * imgWall.getIconWidth(), FRAME_HEIGHT - imgWall.getIconHeight(),
					imgWall.getIconWidth() * 3, imgWall.getIconHeight());
			// ���� �ʹ� ��ġ�� �������ش�.
			snake.add(new SnakePiece(FRAME_WIDTH - 4 * imgWall.getIconWidth(), FRAME_HEIGHT - imgWall.getIconHeight()));
			break;
		case 7:
		case 8:
		case 9:
			buffg.clearRect(FRAME_WIDTH - 5 * imgWall.getIconWidth(), FRAME_HEIGHT - imgWall.getIconHeight(),
					imgWall.getIconWidth() * (10 - timeCount), imgWall.getIconHeight());
			break;
		}

		if(gameStatus == GameStatus.CONTINUE)
		{
			// ���� �׸���.
			buffg.drawImage(imgStar.getImage(), stars.pos.x, stars.pos.y, this);
		}
		else if(gameStatus == GameStatus.DONE)
		{
			buffg.clearRect(3 * imgWall.getIconWidth(), imgWall.getIconHeight(),
					imgWall.getIconWidth(), imgWall.getIconHeight());
		}	

		// ���� �׸���.
		int snakeLength = snake.size();
		for (int i = 0; i < snakeLength; i++) {
			SnakePiece piece = (SnakePiece) snake.get(i);
			if (0 == i)
				buffg.drawImage(imgSnake[2].getImage(), piece.pos.x, piece.pos.y, this);
			else if (snakeLength - 1 == i)
				buffg.drawImage(imgSnake[0].getImage(), piece.pos.x, piece.pos.y, this);
			else
				buffg.drawImage(imgSnake[1].getImage(), piece.pos.x, piece.pos.y, this);
		}

		// �������� �׸���.
		buffg.drawString("STAR : " + stars.getStarNum(), 50, 70);
		if (!snake.isEmpty())
			buffg.drawString("�� �Ӹ� x : " + snake.get(0).pos.x + ", y : " + snake.get(0).pos.y, 50, 90);

		switch (gameStatus) {
		case SUCCESS:
			buffg.drawString("SUCCESS!! *(^0^)*", FRAME_WIDTH / 2, FRAME_HEIGHT / 2);
			break;
		case FAIL:
			buffg.drawString("FAIL! -��-)/", FRAME_WIDTH / 2, FRAME_HEIGHT / 2);
			break;
		case CONTINUE:
			break;
		default:
		}

		g.drawImage(buffImage, 0, 0, this);
	}

	@Override
	public void run() {
		try {
			while (true) {
				timeCount++;

				// ���� ��Ȳ�� üũ�ϰ� ����� ��Ȳ�� ���Ͽ� �����Ѵ�.
				DealWithKeyInput();

				// ����� ��Ȳ�� �׷��ش�.
				repaint();

				if (gameStatus == GameStatus.SUCCESS || gameStatus == GameStatus.FAIL)
					break;
				// 20ms �������� �ݺ��Ѵ�.
				Thread.sleep(TIME_INTERVAL);
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		int nowKey = e.getKeyCode();

		if (nowKey == KeyEvent.VK_DOWN && keyEvent == KeyEvent.VK_UP)
			return;

		keyEvent = e.getKeyCode();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}
}

class SnakePiece {
	Point pos;

	SnakePiece(int x, int y) {
		pos = new Point(x, y);
	}
}

class Star {
	Point pos;

	final int startX; // ���� ���� �� �ִ� ������ �׸�� ǥ���� �� ���۵Ǵ� ���� x��ǥ.
	final int startY; // ���� ���� �� �ִ� ������ �׸�� ǥ���� �� ���۵Ǵ� ���� y��ǥ.
	final int width; // ���� ���� �� �ִ� ������ �׸�� ǥ���� �� �׸��� ���� ����
	final int height; // ���� ���� �� �ִ� ������ �׸�� ǥ���� �� �׸��� ���� ����
	
	final int MAX_STAR_NUM = 3; 
	int successNum; //���� ���� �� (������ ����)
	int completeTimeCount; //�ִ� ���� ������ �Դ� ���� �����Ͽ����� Ÿ��ī��Ʈ
	
	Star(int startX, int startY, int width, int height) {
		pos = new Point(0, 0);

		this.startX = startX;
		this.startY = startY;
		this.width = width;
		this.height = height;

		replace();
	}

	public void replace() {
		pos.x = (int) (Math.random() * width) + startX;
		pos.y = (int) (Math.random() * height) + startY;
	}
	
	public boolean addAndCheck() {
		successNum++;
		if(successNum < MAX_STAR_NUM)
			return false;
		
		return true;
	}
	
	public int getStarNum() {
		return successNum;
	}
	
	public boolean setCompleteTimeCount(int timeCount)
	{
		completeTimeCount = timeCount;
		return true;
	}
	
	public int getCompleteTimeCount()
	{
		return completeTimeCount;
	}
}
