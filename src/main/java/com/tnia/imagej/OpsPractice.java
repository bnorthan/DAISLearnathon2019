
package com.tnia.imagej;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.display.DataView;
import net.imagej.display.DatasetView;
import net.imagej.ops.OpService;
import net.imagej.ops.convert.RealTypeConverter;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, menuPath = "Plugins>Learnathon>Practice")
public class OpsPractice<T extends RealType<T> & NativeType<T>> implements
	Command
{

	@Parameter(type = ItemIO.INPUT)
	private Img<T> image;

	@Parameter(type = ItemIO.INPUT)
	private double sigma = 2;

	@Parameter(type = ItemIO.INPUT)
	private double factor = 1.6;

	@Parameter(type = ItemIO.INPUT)
	private int levels = 5;

	@Parameter(type = ItemIO.OUTPUT)
	private RandomAccessibleInterval<T> output;

	@Parameter
	OpService ops;

	@Parameter
	LogService log;

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Img<FloatType> converted = ops.convert().float32(image);

		List<RandomAccessibleInterval<T>> dogs = new ArrayList<>();

		double s1 = sigma;
		double s2 = sigma * factor;

		final RealTypeConverter<FloatType, T> scale_op =
			(RealTypeConverter<FloatType, T>) ops.op("convert.normalizeScale",
				converted.firstElement(), image.firstElement());

		for (int i = 0; i < levels; i++) {

			RandomAccessibleInterval<FloatType> dog = ops.filter().dog(converted, s1, s2);

			final Img<T> out = (Img<T>) ops.create().img(image);
			
			ops.convert().imageType(out, Views.iterable(dog), scale_op);
			
			dogs.add(out);

			s1 = s2;
			s2 = s1 * factor;
		}
		output = Views.stack(dogs);
	}

	public static <T extends RealType<T> & NativeType<T>> void main(
		final String[] args) throws IOException
	{
		ImageJ ij = new ImageJ();
		ij.launch(args);
		

		Dataset dataset = (Dataset) ij.io().open("blobs.tif");
		
		DatasetView dv=(DatasetView)ij.imageDisplay().createDataView(dataset);
		dv.setChannelRange(0, 0, 10);
		ij.ui().show(dv);
		
		IterableInterval<BitType> test=ij.op().threshold().otsu((Img<T>)dataset.getImgPlus().getImg());
		
		ij.ui().show(test);
		
	//	ij.ui().show(dataset);
	}

}
