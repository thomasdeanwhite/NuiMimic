package com.sheffield.leapmotion.display;

import com.leapmotion.leap.*;
import com.leapmotion.leap.Frame;
import com.sheffield.leapmotion.App;

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

		public void drawBasis (Graphics2D g, Hand h, Vector offset){
			Matrix basis = h.basis();

			Vector v = h.palmPosition().plus(offset);

			final float LINE_SCALE = 50f;

			g.setColor(Color.BLUE);

			//App.out.println(basis.getXBasis());

			g.drawLine((int)v.getX(), (int)v.getY(), (int)(v.getX() + (basis.getXBasis().getX()*LINE_SCALE)), (int)((v.getY() + basis.getXBasis().getY()*LINE_SCALE)));

			g.setColor(Color.GREEN);

			g.drawLine((int)v.getX(), (int)v.getZ(), (int)(v.getX() + (basis.getXBasis().getX()*LINE_SCALE)), (int)((v.getZ() + basis.getXBasis().getZ()*LINE_SCALE)));
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

					drawBasis(g2d, h, new Vector( getWidth() / 4 ,  getHeight() / 4 , 0f));

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

								Vector origin = h.palmPosition();

								float prevX = getWidth() / 4 + prevVect.getX() - origin.getX();
								float prevY = getHeight() / 4 - prevVect.getY() + origin.getY();
								float nextX = getWidth() / 4 + nextVect.getX() - origin.getX();
								float nextY = getHeight() / 4 - nextVect.getY() + origin.getY();
								g2d.drawLine((int) prevX, (int) prevY, (int) nextX, (int) nextY);
								g2d.fillOval((int) (prevX - RADIUS), (int) (prevY - RADIUS), (int) RADIUS * 2,
										(int) RADIUS * 2);

								prevX = 3 * getWidth() / 4 + prevVect.getX() - origin.getX();
								prevY = getHeight() / 4 - prevVect.getZ() + origin.getZ();
								nextX = 3 * getWidth() / 4 + nextVect.getX() - origin.getX();
								nextY = getHeight() / 4 - nextVect.getZ() + origin.getZ();
								g2d.drawLine((int) prevX, (int) prevY, (int) nextX, (int) nextY);
								g2d.fillOval((int) (prevX - RADIUS), (int) (prevY - RADIUS), (int) RADIUS * 2,
										(int) RADIUS * 2);

								prevX = getWidth() / 4 - prevVect.getZ() + origin.getZ();
								prevY = 3 * getHeight() / 4 - prevVect.getY() + origin.getY();
								nextX = getWidth() / 4 - nextVect.getZ() + origin.getZ();
								nextY = 3 * getHeight() / 4 - nextVect.getY() + origin.getY();
								g2d.drawLine((int) prevX, (int) prevY, (int) nextX, (int) nextY);
								g2d.fillOval((int) (prevX - RADIUS), (int) (prevY - RADIUS), (int) RADIUS * 2,
										(int) RADIUS * 2);
								g2d.drawLine((int) prevX, (int) prevY, (int) nextX, (int) nextY);
								g2d.fillOval((int) (prevX - RADIUS), (int) (prevY - RADIUS), (int) RADIUS * 2,
										(int) RADIUS * 2);
							}
						}
					}

					g2d.setColor(Color.LIGHT_GRAY);
					int scale = (400 - (int)h.palmPosition().getZ()) / 30;
					float scaleWindowX = (getWidth()/2) / 400f;
					float scaleWindowY = (getHeight()/2) / 400f;
					g2d.fillOval((3*(getWidth() / 4)) + ((int)(h.palmPosition().getX()*scaleWindowX)), getHeight() - ((int)(h.palmPosition().getY()*scaleWindowY)), scale, scale);
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
