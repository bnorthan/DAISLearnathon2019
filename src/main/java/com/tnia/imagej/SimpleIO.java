
package com.tnia.imagej;

import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import sc.fiji.simplifiedio.SimplifiedIO;

public class SimpleIO {

	public static <T extends RealType<T> & NativeType<T>> void main(
		final String[] args) 
	{
		
		ImageJ ij=new ImageJ();
		ij.launch(args);

//		ImgPlus<?> readImage = SimplifiedIO.openImage( "../images/CHUM_CR_R12802_SDTIRF_coreg_2018_05_04_mai_40X_fovA.czi" );
		ImgPlus<?> readImage = SimplifiedIO.openImage( "../images/deconvolvedbars.tif");
//		ImgPlus<?> readImage = SimplifiedIO.openImage( "../images/bridge.tif" );
	
//		ImgPlus< DoubleType > readImageDouble = SimplifiedIO.convert( readImage, new DoubleType() );
		System.out.println(readImage.numDimensions());
		
		ij.ui().show(readImage);
		
	}
}
