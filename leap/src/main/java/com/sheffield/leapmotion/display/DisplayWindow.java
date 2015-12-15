package com.sheffield.leapmotion.display;

import com.leapmotion.leap.*;
import com.leapmotion.leap.Frame;
import com.sheffield.instrumenter.App;
import com.sheffield.instrumenter.Properties;

import javax.swing.*;
import java.awt.*;

public class DisplayWindow extends JFrame {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private HandCanvas hand;

	protected class HandCanvas extends JPanel {

		private Frame currentFrame;

		public HandCanvas() {
			setSize(600, 600);
//			Canvas c = new Canvas();
//			c.setPreferredSize(new Dimension(getWidth() / 4, getHeight() / 4));
//			c.setLocation(3 * getWidth() / 4, 3 * getHeight() / 4);
//			add(c);
		}

		public void setFrame(Frame frame) {
			currentFrame = frame;
			repaint();
			revalidate();
			// paintComponent(getGraphics());
		}

		@Override
		public void paintComponent(Graphics g) {

			// BufferedImage bufferedImage = new BufferedImage(getWidth(),
			// getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
			// Graphics2D g2d = bufferedImage.createGraphics();

			super.paintComponent(g);

			Graphics2D g2d = (Graphics2D) g;

			final float RADIUS = 4;
			final float SCALE = 2;
            //App.out.println("- Frame Received!");
			if (currentFrame != null && currentFrame.isValid()) {

				for (Hand h : currentFrame.hands()) {
					// String data = HandFactory.handToString(h);
					// h = HandFactory.createHand(data, currentFrame);
					if (!h.isValid()) {
						continue;
					}
                    Pointable leftMost = h.fingers().leftmost();
                    Pointable rightMost = h.fingers().rightmost();
                    Pointable frontMost = h.fingers().frontmost();
					for (Finger f : h.fingers()) {
						if (!f.isValid()) {
							continue;
						}

                        if (f == frontMost){
                            g2d.setColor(Color.ORANGE);
                        } else if (f == leftMost) {
                            g2d.setColor(Color.BLUE);
                        } else if (f == rightMost){
                            g2d.setColor(Color.RED);
                        } else if (f.isExtended()) {
							g2d.setColor(Color.BLACK);
						}  else {
                            g2d.setColor(Color.GRAY);
                        }

						for (Bone.Type t : Bone.Type.values()) {
							Bone b = f.bone(t);
							if (!b.isValid()) {
								continue;
							}
							if (b.isValid()) {
								// Vector prevVect =
								// handTransform.transformPoint(b.prevJoint());
								// Vector nextVect =
								// handTransform.transformPoint(b.nextJoint());

								Vector prevVect = b.prevJoint();
								Vector nextVect = b.nextJoint();

								// prevVect = prevVect.times(SCALE);
								// prevVect = nextVect.times(SCALE);

								float prevX = getWidth() / 4 + prevVect.getX();
								float prevY = getHeight() / 4 - prevVect.getY();
								float nextX = getWidth() / 4 + nextVect.getX();
								float nextY = getHeight() / 4 - nextVect.getY();
								g2d.drawLine((int) prevX, (int) prevY, (int) nextX, (int) nextY);
								g2d.fillOval((int) (prevX - RADIUS), (int) (prevY - RADIUS), (int) RADIUS * 2,
										(int) RADIUS * 2);

								prevX = 3 * getWidth() / 4 + prevVect.getX();
								prevY = getHeight() / 4 - prevVect.getZ();
								nextX = 3 * getWidth() / 4 + nextVect.getX();
								nextY = getHeight() / 4 - nextVect.getZ();
								g2d.drawLine((int) prevX, (int) prevY, (int) nextX, (int) nextY);
								g2d.fillOval((int) (prevX - RADIUS), (int) (prevY - RADIUS), (int) RADIUS * 2,
										(int) RADIUS * 2);

								prevX = getWidth() / 4 - prevVect.getZ();
								prevY = 3 * getHeight() / 4 - prevVect.getY();
								nextX = getWidth() / 4 - nextVect.getZ();
								nextY = 3 * getHeight() / 4 - nextVect.getY();
								g2d.drawLine((int) prevX, (int) prevY, (int) nextX, (int) nextY);
								g2d.fillOval((int) (prevX - RADIUS), (int) (prevY - RADIUS), (int) RADIUS * 2,
										(int) RADIUS * 2);
								g2d.drawLine((int) prevX, (int) prevY, (int) nextX, (int) nextY);
								g2d.fillOval((int) (prevX - RADIUS), (int) (prevY - RADIUS), (int) RADIUS * 2,
										(int) RADIUS * 2);
							}
						}
					}
				}
			}

			// Graphics2D g2dComponent = (Graphics2D) g;
			// g2dComponent.drawImage(bufferedImage, null, 0, 0);

			// super.paintComponent(g2d);
		}

	}

	public DisplayWindow() {
		super("Leapmotion Display");
		// setLayout(new FlowLayout());
		App.out.println("- Display Initialized");
		hand = new HandCanvas();
		hand.setDoubleBuffered(true);
		add(hand);

		if (!Properties.SEQUENCE) {
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}

		setVisible(true);
		setSize(600, 600);
		// setAlwaysOnTop(true);
	}

	public void setFrame(Frame f) {
		if (f.isValid()) {
			hand.setFrame(f);
		}
	}

}
