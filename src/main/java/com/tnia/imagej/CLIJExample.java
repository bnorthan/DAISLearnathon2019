/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

package com.tnia.imagej;

import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * CLIJ example
 */
@Plugin(type = Command.class, menuPath = "Plugins>Gauss Filtering")
public class CLIJExample<T extends RealType<T>> implements Command {
	//
	// Feel free to add more parameters here...
	//

	@Parameter
	private Dataset data;

	@Parameter
	private ImageJ ij;

	@Override
	public void run() {
		final Img<T> image = (Img<T>) data.getImgPlus();

		CLIJ clij = CLIJ.getInstance();

		ClearCLBuffer gpuInput = clij.push(image);
		ClearCLBuffer gpuBlurred = clij.create(gpuInput);
		ClearCLBuffer gpuThresholded = clij.create(gpuBlurred);

		clij.op().blur(gpuInput, gpuBlurred, 3.0f, 3.0f);

		clij.show(gpuBlurred, "GPU blurred");

		clij.op().automaticThreshold(gpuBlurred, gpuThresholded, "otsu");

		clij.show(gpuThresholded, "GPU thresholded");

		clij.pullRAI(gpuBlurred);

		gpuInput.close();
		gpuBlurred.close();
		gpuThresholded.close();

	}

	/**
	 * This main function serves for development purposes. It allows you to run
	 * the plugin immediately out of your integrated development environment
	 * (IDE).
	 *
	 * @param args whatever, it's ignored
	 * @throws Exception
	 */
	public static void main(final String... args) throws Exception {
		// create the ImageJ application context with all available services
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		// load the dataset
		Dataset dataset = (Dataset) ij.io().open("../images/bridge.tif");

		// show the image
		ij.ui().show(dataset);

		// invoke the plugin
		ij.command().run(CLIJExample.class, true);
	}

}
