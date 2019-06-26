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
import net.imagej.ops.OpService;
import net.imagej.ops.Ops.Create.IntegerType;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.labeling.ConnectedComponents.StructuringElement;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.roi.Regions;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.python.jline.internal.Curses;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.table.DefaultGenericTable;
import org.scijava.table.DoubleColumn;
import org.scijava.table.Table;
import org.scijava.ui.UIService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This example illustrates how to create an ImageJ {@link Command} plugin.
 * <p>
 * The code here is a simple Gaussian blur using ImageJ Ops.
 * </p>
 * <p>
 * You should replace the parameter fields with your own inputs and outputs, and
 * replace the {@link run} method implementation with your own logic.
 * </p>
 */
@Plugin(type = Command.class, menuPath = "Plugins>Gauss Filtering")
public class CellCountingWorkflow<T extends RealType<T>> implements Command {
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

		RandomAccessibleInterval<T> blurred = ij.op().filter().gauss(image, 3.0);

		ij.op().filter().gauss(image, 3.0);

		IterableInterval thresholded = ij.op().threshold().otsu(Views.iterable(
			blurred));

		 invertImage(thresholded);

		Img<BitType> inverted = ij.op().create().img(thresholded);
		ij.op().image().invert(inverted, thresholded);

		ij.ui().show(thresholded);
		ij.ui().show(inverted);

		ImgLabeling test = ij.op().labeling().cca(ij.convert().convert(thresholded,
			RandomAccessibleInterval.class), StructuringElement.FOUR_CONNECTED);

		ij.ui().show(test.getIndexImg());

		LabelRegions<IntegerType> regions = new LabelRegions(test);

		DoubleColumn areaColumn = new DoubleColumn();
		DoubleColumn intensityColumn = new DoubleColumn();

		for (LabelRegion region : regions) {
			System.out.println(region.size());

			IterableInterval<T> sample = Regions.sample(region, image);

			RealType intensity = ij.op().stats().mean(sample);

			double area = region.size();

			areaColumn.add(area);
			intensityColumn.add(intensity.getRealDouble());
		}

		Table table = new DefaultGenericTable();
		table.add(areaColumn);
		table.add(intensityColumn);

		ij.ui().show(table);
	}

	private void invertImage(IterableInterval<T> input) {
		Cursor<T> cursor = input.cursor();

		while (cursor.hasNext()) {
			BitType pixel = (BitType) cursor.next();
			pixel.set(!pixel.get());
		}
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
		Dataset dataset = (Dataset) ij.io().open("../images/blobs.tif");

		// show the image
		ij.ui().show(dataset);

		// invoke the plugin
		ij.command().run(CellCountingWorkflow.class, true);
	}

}
