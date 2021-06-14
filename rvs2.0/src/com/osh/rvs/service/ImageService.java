package com.osh.rvs.service;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import com.osh.rvs.common.PathConsts;
import com.sun.image.codec.jpeg.ImageFormatException;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.FileUtils;

public class ImageService {

	public String cutImage(HttpServletRequest req, List<MsgInfo> msgs) {
		String sStartX = req.getParameter("startX");
		String sStartY = req.getParameter("startY");
		String sHeight = req.getParameter("height");
		String sWidth = req.getParameter("width");
		String sShowWidth = req.getParameter("showWidth");
		String sTrueWidth = req.getParameter("trueWidth");
		double dStartX = 0, dStartY = 0, dCutHeight = 0, dCutWidth = 0, dShowWidth = 0, dTrueWidth = 0;
		try {
			dStartX = Double.parseDouble(sStartX);
			dStartY = Double.parseDouble(sStartY);
			dCutHeight = Double.parseDouble(sHeight);
			dCutWidth = Double.parseDouble(sWidth);
			dShowWidth = Double.parseDouble(sShowWidth);
			dTrueWidth = Double.parseDouble(sTrueWidth);
		} catch (NumberFormatException pe) {
			MsgInfo info = new MsgInfo();
			info.setErrmsg(pe.getMessage());
			msgs.add(info);
			return "";
		}

		if (dCutHeight == 0.0 || dCutWidth == 0.0) {
			MsgInfo info = new MsgInfo();
			info.setErrmsg("无图或无选择区域");
			msgs.add(info);
			return "";
		}

		double fator = dShowWidth / dTrueWidth;

		int iRealityStartX = (int) (dStartX / fator);
		int iRealityStartY = (int) (dStartY / fator);
		int iCutWidth = (int) (dCutWidth / fator);
		int iCutHeight = (int) (dCutHeight / fator);

		String sFileName = req.getParameter("fileName");

		String ipath = getImagePath(sFileName, null, 0);

		String dpath = ipath + "_fix.jpg";
		// 截取
		cutImageTo(dpath, dpath, iRealityStartX, iRealityStartY, iCutWidth,
				iCutHeight);

		return sFileName;
	}

	public String resetImage(HttpServletRequest req, List<MsgInfo> msgs, Map<String, Object> jsonResponse) {
		String sFileName = req.getParameter("fileName");

		String ipath = getImagePath(sFileName, null, 0);

		String spath = ipath + ".jpg";
		String dpath = ipath + "_fix.jpg";
		// 截取
		FileUtils.copyFile(spath, dpath);

		jsonResponse.put("retPath", sFileName);
		getOriginalImageSize(new File(dpath), jsonResponse, 800);

		return sFileName;
	}

	private String getImagePath(String sFileName, String sType, int iCutHeight) {
		String path = PathConsts.BASE_PATH + PathConsts.PHOTOS + "/upload/"
				+ sFileName.substring(0, 4) + "/" + sFileName;

		return path;
	}

	public void getOriginalImageSize(File confFile,
			Map<String, Object> jsonResponse, int infoSize) {
		try {
			BufferedImage source = ImageIO.read(confFile);

			int srcWidth = source.getWidth();
			int srcHeight = source.getHeight();

			float factorS = (srcWidth + 0.0f) / srcHeight;

			if (srcWidth >= srcHeight && srcWidth > infoSize) {
				srcWidth = infoSize;
				srcHeight = (int) (srcWidth / factorS);
			} else if (srcHeight > infoSize) {
				srcHeight = infoSize;
				srcWidth = (int) (srcHeight * factorS);
			}

			jsonResponse.put("oWidth", srcWidth);
			jsonResponse.put("oHeight", srcHeight);
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
	}

	/**
	 * 剪切图片位置
	 */
	public static void cutImageTo(String sourceFile, String targetDir,
			int startX, int startY, int width, int height) {
		try {
			cutImageTo(new File(sourceFile), targetDir, startX, startY, width,
					height);
		} catch (Exception e) {
			e.printStackTrace(); // TODO

		}
	}

	public static void cutImageTo(File sourceFile, String targetDir,
			int startX, int startY, int width, int height) throws Exception {
		cutImageTo(ImageIO.read(sourceFile), targetDir, startX, startY, width,
				height);
	}

	private static void cutImageTo(BufferedImage source, String targetDir,
			int startX, int startY, int width, int height) throws Exception {
		int sWidth = source.getWidth();
		// 取得图片宽度 */
		int sHeight = source.getHeight();
		if (sWidth >= width && sHeight >= height) {

			String fileName = null;
			File file = new File(targetDir);
			if (!file.exists()) {
				if (targetDir.endsWith("/") || targetDir.endsWith("\\")) {
					file.mkdirs();
				} else {
					file.createNewFile();
				}
			}
			BufferedImage image = source.getSubimage(startX, startY, width,
					height);

//			// test方块
//			drawRect(image, "文化HV", 
//					"r", null, "g", "w", 
//					420, 450, 200, 200);
//
			fileName = targetDir;
			saveAsJpeg(fileName, image, 0.92f);
		}
	}

	public void rotateImage(HttpServletRequest req, List<MsgInfo> msgs) throws Exception {
		String sFileName = req.getParameter("fileName");
		String clockwise = req.getParameter("clockwise");

		String sourceFile = getImagePath(sFileName, null, 0) + "_fix.jpg";

		if ("+1".equals(clockwise)) {
			rotateImageTo(new File(sourceFile), sourceFile, (float) Math.toRadians(90));
		} else if ("-1".equals(clockwise)) {
			rotateImageTo(new File(sourceFile), sourceFile, (float) Math.toRadians(270));
		}
	}

	public static void rotateImageTo(File sourceFile, String targetDir,
			float degree) throws Exception {
		rotateImageTo(ImageIO.read(sourceFile), targetDir, degree);
	}

	private static void rotateImageTo(BufferedImage source, String targetDir,
			float degree) throws Exception {
		int sWidth = source.getWidth();
		// 取得图片宽度 */
		int sHeight = source.getHeight();
		if (degree != 0.0f) {

			String fileName = null;
			File file = new File(targetDir);
			if (!file.exists()) {
				if (targetDir.endsWith("/") || targetDir.endsWith("\\")) {
					file.mkdirs();
				} else {
					file.createNewFile();
				}
			}

		    // 获取原始图片的透明度
			int transparency = source.getColorModel().getTransparency();
			int type = source.getType();

			// int[] in = {1,4,5,8,9};
			BufferedImage image = new BufferedImage(sHeight, sWidth, type); // TYPE_INT_BGR
			Graphics2D g2d = image.createGraphics();
			// TODO 目前只是给旋转90的
			if (degree > 1.5 && degree < 1.6) {
				g2d.translate(sHeight, 0);
				g2d.rotate(degree);
				g2d.drawImage(source, 0, 0, null);
			} else if (degree > 4.7 && degree < 4.8) {
				g2d.translate(0, sWidth);
				g2d.rotate(degree);
				g2d.drawImage(source, 0, 0, null);
			}
			fileName = targetDir;
			saveAsJpeg(fileName, image, 0.92f);
		}
	}

	/**
	 * 图片大小调整
	 * 
	 * @param sourceFile
	 * @param targetPath
	 * @param width
	 * @param height
	 */
	public static void fixWidthImageTo(File sourceFile, String targetPath,
			int width, int height) throws Exception {
		BufferedImage source = ImageIO.read(sourceFile);
		BufferedImage scaleImage = new BufferedImage(width, height,
				source.getType());
		Graphics g = scaleImage.getGraphics();
		g.drawImage(source, 0, 0, width, height, null);
		g.dispose();
		saveAsJpeg(targetPath, scaleImage, 1f);
	}

	public static Font FONT_MSYH_SMALL = null;
	public static Font FONT_MSYH_NORMAL = null;
	public static Font FONT_MSYH_LARGE = null;
	public static Font FONT_MSYH_HUGE = null;
	public static final int FONT_PT = 24;
	public static final int BOX_RADIUS = 24;
	static {
		File ttfFile = new File(PathConsts.BASE_PATH + "\\msyh.ttf");
		FileInputStream fis;
		try {
			fis = new FileInputStream(ttfFile);
			Font dynamicFont = Font.createFont(Font.TRUETYPE_FONT, fis);
			fis.close();
			FONT_MSYH_SMALL = dynamicFont.deriveFont(Font.BOLD, FONT_PT / 2);
			FONT_MSYH_NORMAL = dynamicFont.deriveFont(Font.BOLD, FONT_PT);
			FONT_MSYH_LARGE = dynamicFont.deriveFont(Font.BOLD, FONT_PT * 3);
			FONT_MSYH_HUGE = dynamicFont.deriveFont(Font.BOLD, FONT_PT * 4);
		} catch (FontFormatException | IOException e) {
			FONT_MSYH_SMALL = new Font("宋体", Font.BOLD, FONT_PT / 2);
			FONT_MSYH_NORMAL = new Font("宋体", Font.BOLD, FONT_PT);
			FONT_MSYH_LARGE = new Font("宋体", Font.BOLD, FONT_PT * 3);
			FONT_MSYH_HUGE = new Font("宋体", Font.PLAIN, FONT_PT * 4);
		}
	}

	/**
	 * 图片上写字
	 * 
	 * @param sourceFile
	 * @param targetPath
	 * @param width
	 * @param height
	 */
	public static void drawString(BufferedImage bImg, String text, Color color,
			int left, int top, double ratio) {

		int w = bImg.getWidth();
		int h = bImg.getHeight();

		Graphics g = bImg.getGraphics();
		drawString(g, text, color, w, h, left, top, ratio);
	}
	public static void drawString(Graphics g, String text, Color color,
			int w, int h, int left, int top, double ratio) {

		g.setColor(color);
		if (ratio < 0.75) {
			g.setFont(FONT_MSYH_SMALL);
		} else if (ratio >= 2) {
			g.setFont(FONT_MSYH_LARGE);
		} else if (ratio >= 4) {
			g.setFont(FONT_MSYH_HUGE);
		} else {
			g.setFont(FONT_MSYH_NORMAL);
		}

		if (left >= h) {
			left = h - FONT_PT + 2;
		}

		if (top >= w) {
			top = w;
		}

		g.drawString(text, left, top);
	}

	/**
	 * 图上画方块
	 * 
	 * @param bImg
	 * @param text
	 * @param borderColor
	 * @param borderRadius
	 * @param contentColor
	 * @param textColor
	 * @param left
	 * @param top
	 * @param width
	 * @param height
	 */
	public static void drawRect(BufferedImage bImg, String text, 
			String borderColor, String borderRadius, String contentColor, String textColor, 
			int left, int top, int width, int height, Double ratio) {

		Graphics g = bImg.getGraphics();
		int w = bImg.getWidth();
		int h = bImg.getHeight();

		if (ratio == null) ratio = w / 800.0;

		// 根据比例调整位置
		left *= ratio;
		top *= ratio;
		width *= ratio;
		height *= ratio;

		if (contentColor != null) {
			Color cContent = null;
			switch(contentColor) {
			case "g" : cContent = new Color(127, 127, 127, 85); break;
			case "h" : cContent = new Color(255, 255, 255, 127); break;
			default : cContent = Color.WHITE;
			}
			g.setColor(cContent);
			if (borderRadius != null) {
				g.fillRoundRect(left, top, width, height, BOX_RADIUS, BOX_RADIUS);
			} else {
				g.fillRect(left, top, width, height);
			}
		}

		if (!CommonStringUtil.isEmpty(text)) {
			Color cText = null;
			switch(textColor) {
			case "r" : cText = Color.RED; break;
			case "g" : cText = Color.GREEN; break;
			case "b" : cText = Color.BLUE; break;
			case "w" : cText = Color.WHITE; break;
			default : cText = Color.BLACK;
			}
			drawString(g, text, cText, w, h, left + (int)(FONT_PT * 0.5), top + (int)(FONT_PT * 1.5), ratio);
		}

		if (borderColor != null) {
			Color cBorder = null;
			switch(borderColor) {
			case "r" : cBorder = Color.RED; break;
			case "g" : cBorder = Color.GREEN; break;
			case "b" : cBorder = Color.BLUE; break;
			case "w" : cBorder = Color.WHITE; break;
			default : cBorder = Color.BLACK;
			}
			g.setColor(cBorder);
			if (g instanceof Graphics2D) {
				Graphics2D g2d = (Graphics2D) g;
				g2d.setStroke(new BasicStroke(2.5f));
			}
			if (borderRadius != null) {
				g.drawRoundRect(left, top, width, height, BOX_RADIUS, BOX_RADIUS);
			} else {
				g.drawRect(left, top, width, height);
			}
		}
	}

	/**
	 * 设定质量后保存为jpeg
	 * 
	 * @param fileName
	 *            输出文件路径
	 * @param image
	 *            图象
	 * @param quality
	 *            质量
	 * @throws ImageFormatException
	 * @throws IOException
	 */
	public static void saveAsJpeg(String fileName, BufferedImage image,
			float quality) throws ImageFormatException, IOException {
		FileOutputStream newimage = new FileOutputStream(fileName);
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(newimage);
		JPEGEncodeParam jep = JPEGCodec.getDefaultJPEGEncodeParam(image);
		/* 设定压缩质量 */
		jep.setQuality(quality, true);
		encoder.encode(image, jep);
		// encoder.encode(tag);
		// JPEG格式保存
		newimage.close();
	}
}
