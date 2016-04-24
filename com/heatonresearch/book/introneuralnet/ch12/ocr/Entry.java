/**
 * Introduction to Neural Networks with Java, 2nd Edition
 * Copyright 2008 by Heaton Research, Inc. 
 * http://www.heatonresearch.com/books/java-neural-2/
 * 
 * ISBN13: 978-1-60439-008-7  	 
 * ISBN:   1-60439-008-5
 *   
 * This class is released under the:
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/copyleft/lesser.html
 */
package com.heatonresearch.book.introneuralnet.ch12.ocr;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.image.PixelGrabber;

import javax.swing.JPanel;

/**
 * Chapter 12: OCR and the Self Organizing Map
 * 
 * Entry: GUI element to allow the user to enter a character by 
 * drawing it.
 * 
 * @author Jeff Heaton
 * @version 2.1
 */
public class Entry extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 656936515012546346L;

	/**
	 * The image that the user is drawing into.
	 */
	protected Image entryImage;

	/**
	 * A graphics handle to the image that the user is drawing into.
	 */
	protected Graphics entryGraphics;

	/**
	 * The last x that the user was drawing at.
	 */
	protected int lastX = -1;

	/**
	 * The last y that the user was drawing at.
	 */
	protected int lastY = -1;

	/**
	 * The down sample component used with this component.
	 */
	protected Sample sample;

	/**
	 * Specifies the left boundary of the cropping rectangle.
	 */
	protected int downSampleLeft;

	/**
	 * Specifies the right boundary of the cropping rectangle.
	 */
	protected int downSampleRight;

	/**
	 * Specifies the top boundary of the cropping rectangle.
	 */
	protected int downSampleTop;

	/**
	 * Specifies the bottom boundary of the cropping rectangle.
	 */
	protected int downSampleBottom;

	/**
	 * The downsample ratio for x.
	 */
	protected double ratioX;

	/**
	 * The downsample ratio for y
	 */
	protected double ratioY;

	/**
	 * The pixel map of what the user has drawn. Used to downsample it.
	 */
	protected int pixelMap[];

	/**
	 * The constructor.
	 */
	Entry() {
		enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK
				| AWTEvent.MOUSE_EVENT_MASK | AWTEvent.COMPONENT_EVENT_MASK);
	}

	/**
	 * Called to clear the image.
	 */
	public void clear() {
		this.entryGraphics.setColor(Color.white);
		this.entryGraphics.fillRect(0, 0, getWidth(), getHeight());
		this.downSampleBottom = this.downSampleTop = this.downSampleLeft = this.downSampleRight = 0;
		repaint();
	}

	/**
	 * Called to downsample the image and store it in the down sample component.
	 */
	public void downSample() {
		final int w = this.entryImage.getWidth(this);
		final int h = this.entryImage.getHeight(this);

		final PixelGrabber grabber = new PixelGrabber(this.entryImage, 0, 0, w,
				h, true);
		try {

			grabber.grabPixels();
			this.pixelMap = (int[]) grabber.getPixels();
			findBounds(w, h);

			// now downsample
			final SampleData data = this.sample.getData();

			this.ratioX = (double) (this.downSampleRight - this.downSampleLeft)
					/ (double) data.getWidth();
			this.ratioY = (double) (this.downSampleBottom - this.downSampleTop)
					/ (double) data.getHeight();

			for (int y = 0; y < data.getHeight(); y++) {
				for (int x = 0; x < data.getWidth(); x++) {
					if (downSampleRegion(x, y)) {
						data.setData(x, y, true);
					} else {
						data.setData(x, y, false);
					}
				}
			}

			this.sample.repaint();
			repaint();
		} catch (final InterruptedException e) {
		}
	}

	/**
	 * Called to downsample a quadrant of the image.
	 * 
	 * @param x
	 *            The x coordinate of the resulting downsample.
	 * @param y
	 *            The y coordinate of the resulting downsample.
	 * @return Returns true if there were ANY pixels in the specified quadrant.
	 */
	protected boolean downSampleRegion(final int x, final int y) {
		final int w = this.entryImage.getWidth(this);
		final int startX = (int) (this.downSampleLeft + (x * this.ratioX));
		final int startY = (int) (this.downSampleTop + (y * this.ratioY));
		final int endX = (int) (startX + this.ratioX);
		final int endY = (int) (startY + this.ratioY);

		for (int yy = startY; yy <= endY; yy++) {
			for (int xx = startX; xx <= endX; xx++) {
				final int loc = xx + (yy * w);

				if (this.pixelMap[loc] != -1) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * This method is called to automatically crop the image so that whitespace
	 * is removed.
	 * 
	 * @param w
	 *            The width of the image.
	 * @param h
	 *            The height of the image
	 */
	protected void findBounds(final int w, final int h) {
		// top line
		for (int y = 0; y < h; y++) {
			if (!hLineClear(y)) {
				this.downSampleTop = y;
				break;
			}

		}
		// bottom line
		for (int y = h - 1; y >= 0; y--) {
			if (!hLineClear(y)) {
				this.downSampleBottom = y;
				break;
			}
		}
		// left line
		for (int x = 0; x < w; x++) {
			if (!vLineClear(x)) {
				this.downSampleLeft = x;
				break;
			}
		}

		// right line
		for (int x = w - 1; x >= 0; x--) {
			if (!vLineClear(x)) {
				this.downSampleRight = x;
				break;
			}
		}
	}

	/**
	 * Get the down sample component to be used with this component.
	 * 
	 * @return The down sample component.
	 */
	public Sample getSample() {
		return this.sample;
	}

	/**
	 * This method is called internally to see if there are any pixels in the
	 * given scan line. This method is used to perform autocropping.
	 * 
	 * @param y
	 *            The horizontal line to scan.
	 * @return True if there were any pixels in this horizontal line.
	 */
	protected boolean hLineClear(final int y) {
		final int w = this.entryImage.getWidth(this);
		for (int i = 0; i < w; i++) {
			if (this.pixelMap[(y * w) + i] != -1) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Setup the internal image that the user draws onto.
	 */
	protected void initImage() {
		this.entryImage = createImage(getWidth(), getHeight());
		this.entryGraphics = this.entryImage.getGraphics();
		this.entryGraphics.setColor(Color.white);
		this.entryGraphics.fillRect(0, 0, getWidth(), getHeight());
	}

	/**
	 * Paint the drawn image and cropping box (if active).
	 * 
	 * @param g
	 *            The graphics context
	 */
	@Override
	public void paint(final Graphics g) {
		if (this.entryImage == null) {
			initImage();
		}
		g.drawImage(this.entryImage, 0, 0, this);
		g.setColor(Color.black);
		g.drawRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.red);
		g.drawRect(this.downSampleLeft, this.downSampleTop,
				this.downSampleRight - this.downSampleLeft,
				this.downSampleBottom - this.downSampleTop);

	}

	/**
	 * Process messages.
	 * 
	 * @param e
	 *            The event.
	 */
	@Override
	protected void processMouseEvent(final MouseEvent e) {
		if (e.getID() != MouseEvent.MOUSE_PRESSED) {
			return;
		}
		this.lastX = e.getX();
		this.lastY = e.getY();
	}

	/**
	 * Process messages.
	 * 
	 * @param e
	 *            The event.
	 */
	@Override
	protected void processMouseMotionEvent(final MouseEvent e) {
		if (e.getID() != MouseEvent.MOUSE_DRAGGED) {
			return;
		}

		this.entryGraphics.setColor(Color.black);
		this.entryGraphics.drawLine(this.lastX, this.lastY, e.getX(), e.getY());
		getGraphics().drawImage(this.entryImage, 0, 0, this);
		this.lastX = e.getX();
		this.lastY = e.getY();
	}

	/**
	 * Set the sample control to use. The sample control displays a downsampled
	 * version of the character.
	 * 
	 * @param s
	 */
	public void setSample(final Sample s) {
		this.sample = s;
	}

	/**
	 * This method is called to determine ....
	 * 
	 * @param x
	 *            The vertical line to scan.
	 * @return True if there are any pixels in the specified vertical line.
	 */
	protected boolean vLineClear(final int x) {
		final int w = this.entryImage.getWidth(this);
		final int h = this.entryImage.getHeight(this);
		for (int i = 0; i < h; i++) {
			if (this.pixelMap[(i * w) + x] != -1) {
				return false;
			}
		}
		return true;
	}
}