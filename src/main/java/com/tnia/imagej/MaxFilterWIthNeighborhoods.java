package com.tnia.imagej;

import net.imagej.ImageJ;
import net.imagej.ops.Ops;
import net.imagej.ops.special.computer.Computers;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imglib2.RandomAccessible;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.img.Img;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.Views;
import sc.fiji.simplifiedio.SimplifiedIO;

/**
 * Use Neighborhoods to create a MaxFilter
 */
public class MaxFilterWIthNeighborhoods {
	public static void main(String[] args) {
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		final Img<UnsignedByteType> img = SimplifiedIO
				.openImage("../../images/blobs.tif");
		ij.ui().show("img", img);

		final Img<UnsignedByteType> a = img.factory().create(img);
		final Img<UnsignedByteType> b = img.factory().create(img);
		final Img<UnsignedByteType> c = img.factory().create(img);

		final RectangleShape shape = new RectangleShape(8, false);

		final RandomAccessible<Neighborhood<UnsignedByteType>> neighborhoods = shape
				.neighborhoodsRandomAccessible(Views.extendBorder(img));

		// a) iterate over region
		long start = System.nanoTime();
		LoopBuilder.setImages(Views.interval(neighborhoods, a), a).multiThreaded().forEachPixel((neighborhood, o) -> {

			o.set(neighborhood.firstElement());
			neighborhood.forEach(t -> {
				final int v = t.get();
				if (v > o.get())
					o.set(v);
			});

		});
		long end = System.nanoTime();
		System.out.println("a) time ms " + (end - start) / 10e6);

		start = System.nanoTime();
		// b) use ops through ij (slow because of ops matching)
		LoopBuilder.setImages(Views.interval(neighborhoods, b), b).multiThreaded().forEachPixel((neighborhood, o) -> {

			ij.op().stats().max(o, neighborhood);
		});
		end = System.nanoTime();
		System.out.println("b) time ms " + (end - start) / 10e6);

		// c) match op then use
		start = System.nanoTime();
		UnaryComputerOp maxOp = Computers.unary(ij.op(), Ops.Stats.Max.class, c.firstElement(), img);
		LoopBuilder.setImages(Views.interval(neighborhoods, c), c).multiThreaded().forEachPixel((neighborhood, o) -> {

			maxOp.compute(neighborhood, o);
		});
		end = System.nanoTime();
		System.out.println("c) time ms " + (end - start) / 10e6);

		ij.ui().show("output", a);
		ij.ui().show("output", b);
		ij.ui().show("output", c);
	}
}

