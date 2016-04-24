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

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;
/**
 * Chapter 12: OCR and the Self Organizing Map
 * 
 * Sample: GUI element that displays sampled data.
 * 
 * @author Jeff Heaton
 * @version 2.1
 */
public class Sample extends JPanel {

	/**
	 * Serial id for this class.
	 */
	private static final long serialVersionUID = 2250441617163548592L;
	/**
	 * The image data.
	 */
	SampleData data;

	/**
	 * The constructor.
	 * 
	 * @param width
	 *            The width of the downsampled image
	 * @param height
	 *            The height of the downsampled image
	 */
	Sample(final int width, final int height) {
		this.data = new SampleData(' ', width, height);
	}

	/**
	 * The image data object.
	 * 
	 * @return The image data object.
	 */
	SampleData getData() {
		return this.data;
	}

	/**
	 * @param g
	 *            Display the downsampled image.
	 */
	@Override
	public void paint(final Graphics g) {
		if (this.data == null) {
			return;
		}

		int x, y;
		final int vcell = getHeight() / this.data.getHeight();
		final int hcell = getWidth() / this.data.getWidth();

		g.setColor(Color.white);
		g.fillRect(0, 0, getWidth(), getHeight());

		g.setColor(Color.black);
		for (y = 0; y < this.data.getHeight(); y++) {
			g.drawLine(0, y * vcell, getWidth(), y * vcell);
		}
		for (x = 0; x < this.data.getWidth(); x++) {
			g.drawLine(x * hcell, 0, x * hcell, getHeight());
		}

		for (y = 0; y < this.data.getHeight(); y++) {
			for (x = 0; x < this.data.getWidth(); x++) {
				if (this.data.getData(x, y)) {
					g.fillRect(x * hcell, y * vcell, hcell, vcell);
				}
			}
		}

		g.setColor(Color.black);
		g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

	}

	/**
	 * Assign a new image data object.
	 * 
	 * @param data
	 *            The image data object.
	 */

	void setData(final SampleData data) {
		this.data = data;
	}

}