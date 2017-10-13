package com.sheffield.leapmotion.display;

import com.leapmotion.leap.Bone;
import com.leapmotion.leap.CircleGesture;
import com.leapmotion.leap.Finger;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Matrix;
import com.leapmotion.leap.Pointable;
import com.leapmotion.leap.Vector;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.controller.SeededController;
import com.sheffield.leapmotion.controller.mocks.SeededGesture;

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

		public void drawXYBasis(Graphics2D g, Hand h, Vector offset){
			Matrix basis = h.basis();

			Vector v = offset;

			final float LINE_SCALE = 100f;

			g.setColor(Color.BLUE);

			//App.out.println(basis.getXBasis());

			int height = getHeight();

			int x = (int)(v.getX() + (basis.getXBasis().getX()*LINE_SCALE));
			int y = height - (int)((v.getY() + basis.getXBasis().getY()*LINE_SCALE));

			g.drawLine(x, y, (int)v.getX(), height - (int)v.getY());
			g.drawString("x", x, y);

			g.setColor(Color.GREEN);

			x = (int)(v.getX() + (basis.getZBasis().getX()*LINE_SCALE));
			y = height - (int)((v.getY() + basis.getZBasis().getY()*LINE_SCALE));

			g.drawLine((int)v.getX(), height - (int)v.getY(), x, y);

			g.drawString("z", x, y);

			g.setColor(Color.RED);

			x = (int)(v.getX() + (basis.getYBasis().getX()*LINE_SCALE));
			y = height - (int)((v.getY() + basis.getYBasis().getY()*LINE_SCALE));

			g.drawLine((int)v.getX(), height - (int)v.getY(), x, y);
			g.drawString("y", x, y);
		}

		public void drawXZBasis(Graphics2D g, Hand h, Vector offset){
			Matrix basis = h.basis();

			Vector v = offset;//.plus(new Vector(h.palmNormal().getX(), h.palmPosition().getZ(), 0f));


			final float LINE_SCALE = 50f;

			g.setColor(Color.BLUE);

			//App.out.println(basis.getXBasis());

			int height = getHeight();

			int x = (int)(v.getX() + (basis.getXBasis().getX()*LINE_SCALE));
			int y = height - (int)((v.getY() + basis.getXBasis().getZ()*LINE_SCALE));

			g.drawLine(x, y, (int)v.getX(), height - (int)v.getY());
			g.drawString("x", x, y);

			g.setColor(Color.GREEN);

			x = (int)(v.getX() + (basis.getZBasis().getX()*LINE_SCALE));
			y = height - (int)((v.getY() + basis.getZBasis().getZ()*LINE_SCALE));

			g.drawLine((int)v.getX(), height - (int)v.getY(), x, y);

			g.drawString("z", x, y);

			g.setColor(Color.RED);

			x = (int)(v.getX() + (basis.getYBasis().getX()*LINE_SCALE));
			y = height - (int)((v.getY() + basis.getYBasis().getZ()*LINE_SCALE));

			g.drawLine((int)v.getX(), height - (int)v.getY(), x, y);
			g.drawString("y", x, y);
		}

		public void drawYZBasis(Graphics2D g, Hand h, Vector offset){
			Matrix basis = h.basis();

			Vector v = offset;//.plus(new Vector(h.palmNormal().getZ(), h.palmPosition().getY(), 0f));

			final float LINE_SCALE = 50f;

			g.setColor(Color.BLUE);

			//App.out.println(basis.getXBasis());

			int height = getHeight();

			int x = (int)(v.getX() + (-basis.getXBasis().getZ()*LINE_SCALE));
			int y = height - (int)((v.getY() + basis.getXBasis().getY()*LINE_SCALE));

			g.drawLine(x, y, (int)v.getX(), height - (int)v.getY());
			g.drawString("x", x, y);

			g.setColor(Color.GREEN);

			x = (int)(v.getX() + (-basis.getZBasis().getZ()*LINE_SCALE));
			y = height - (int)((v.getY() + basis.getZBasis().getY()*LINE_SCALE));

			g.drawLine((int)v.getX(), height - (int)v.getY(), x, y);

			g.drawString("z", x, y);

			g.setColor(Color.RED);

			x = (int)(v.getX() + (-basis.getYBasis().getZ()*LINE_SCALE));
			y = height - (int)((v.getY() + basis.getYBasis().getY()*LINE_SCALE));

			g.drawLine((int)v.getX(), height - (int)v.getY(), x, y);
			g.drawString("y", x, y);
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
			Vector offset = new Vector(100f, -400f, 0f);
			if (currentFrame != null && currentFrame.isValid()) {

				g2d.setColor(Color.BLACK);
				g2d.drawString("Status: " + SeededController.getSeededController().status(), 26, 26);


				for (Hand h : currentFrame.hands()) {
					g2d.drawString("Stablised Tip: " + h.fingers().frontmost().stabilizedTipPosition().toString() +
							h.fingers().frontmost().tipVelocity().toString(), 26, 56);

					drawXYBasis(g2d, h, new Vector( getWidth() / 4 , 3 * getHeight() / 4 , 0f).plus(offset));

					drawXZBasis(g2d, h, new Vector( 3 * getWidth() / 4 , 3 * getHeight() / 4 , 0f).plus(offset));

					drawYZBasis(g2d, h, new Vector( getWidth() / 4 ,  getHeight() / 4 , 0f).plus(offset));

//					if (h instanceof SeededHand){
//						SeededHand sh = (SeededHand) h;
//
//						Quaternion q = QuaternionHelper.toQuaternion(new Vector[]{
//								sh.basis().getXBasis(), sh.basis().getYBasis(), sh.basis().getZBasis()
//						});
//
//						Vector[] vs = q.toMatrix(true);
//
//						sh.setBasis(vs[0], vs[1], vs[2]);
//
//						drawXYBasis(g2d, h, new Vector( getWidth() / 4 , 3 * getHeight() / 4 , 0f).plus(offset));
//
//						drawXZBasis(g2d, h, new Vector( 3 * getWidth() / 4 , 3 * getHeight() / 4 , 0f).plus(offset));
//
//						drawYZBasis(g2d, h, new Vector( getWidth() / 4 ,  getHeight() / 4 , 0f).plus(offset));
//					}

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

                        if (f.equals(frontMost)){
                            g2d.setColor(Color.ORANGE);
                        } else if (f.equals(leftMost)) {
                            g2d.setColor(Color.BLUE);
                        } else if (f.equals(rightMost)){
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

								origin.setY(0f);

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


					int scale = (400 - (int)h.palmPosition().getZ()) / 30;
					float scaleWindowX = (getWidth()/2) / 400f;
					float scaleWindowY = (getHeight()/2) / 400f;


					Finger frontMostTip = h.fingers().frontmost();

					g2d.setColor(Color.LIGHT_GRAY);
					g2d.fillOval((3*(getWidth() / 4)) + ((int)(h.palmPosition().getX()*scaleWindowX)), getHeight() - ((int)(h.palmPosition().getY()*scaleWindowY)), scale, scale);
					g2d.drawString("(" + (Math.round(10*h.palmPosition().getX())/10f) + ", " + (Math.round(10*h.palmPosition().getY())/10f) + ")",
							(3*(getWidth() / 4)) + ((int)(h.palmPosition().getX()*scaleWindowX)) + scale + 5, getHeight() - ((int)(h.palmPosition().getY()*scaleWindowY)) + scale);

					g2d.drawString(h.fingers().frontmost().stabilizedTipPosition().toString(),
							(3*(getWidth() / 4)) + ((int)(h.palmPosition().getX()*scaleWindowX)) + scale + 5, getHeight() - ((int)(h.palmPosition().getY()*scaleWindowY)) + (2 * scale));


					g2d.setColor(Color.CYAN);
					float tipScale = (400 - (int)frontMostTip.stabilizedTipPosition().getZ()) / 30;
					g2d.fillRect((3*(getWidth() / 4)) + ((int)(frontMostTip.stabilizedTipPosition().getX()*scaleWindowX)), getHeight() - ((int)(frontMostTip.stabilizedTipPosition().getY()*scaleWindowY)), (int)tipScale, (int)tipScale);

					g2d.setColor(Color.DARK_GRAY);
					tipScale = (400 - (int)frontMostTip.tipPosition().getZ()) / 30;
					g2d.drawRect((3*(getWidth() / 4)) + ((int)(frontMostTip.tipPosition().getX()*scaleWindowX)), getHeight() - ((int)(frontMostTip.tipPosition().getY()*scaleWindowY)), (int)tipScale, (int)tipScale);


					g2d.setColor(Color.GREEN);
					int counter = 3;
					for (Gesture gesture : currentFrame.gestures()){
						g2d.drawString(gesture.type().toString(),
								(3*(getWidth() / 4)) + ((int)(h.palmPosition().getX()*scaleWindowX)) + scale + 5, getHeight() - ((int)(h.palmPosition().getY()*scaleWindowY)) + (counter * scale));
						counter++;

						if (gesture.type().equals(Gesture.Type.TYPE_CIRCLE)){
							CircleGesture cg = SeededGesture.getCircleGesture(gesture);

							g2d.drawString(cg.center() + " r=" + cg.radius(),
									(3*(getWidth() / 4)) + ((int)(h.palmPosition().getX()*scaleWindowX)) + scale + 5, getHeight() - ((int)(h.palmPosition().getY()*scaleWindowY)) + (counter * scale));
							int x = (3*(getWidth() / 4)) + ((int)((cg.center().getX()-cg.radius())*scaleWindowX)) + scale + 5;
							int y = getHeight() - ((int)((cg.center().getY()-cg.radius())*scaleWindowY)) + (2 * scale);
							int rad = (int)cg.radius();
							int dia = 2 * rad;
							g2d.drawLine(x - rad, y,
									x + rad, y);

							g2d.drawLine(x, y - rad,
									x, y + rad);

							g2d.drawOval(x - rad, y - rad,
									dia, dia);
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
