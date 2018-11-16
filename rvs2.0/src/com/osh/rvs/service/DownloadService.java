package com.osh.rvs.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.osh.rvs.bean.data.MaterialEntity;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.common.RvsUtils;

import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.DateUtil;

public class DownloadService {
	public static final String CONTENT_TYPE_EXCEL = "application/vnd.ms-excel";
	public static final String CONTENT_TYPE_PDF = "application/pdf";
	public static final String CONTENT_TYPE_GIF = "image/gif";
	public static final String CONTENT_TYPE_ZIP = "application/zip";

	private static final int ACCEPTANCE_WORK_REPORT_START_LINE = 5;
	//private static final int ACCEPTANCE_WORK_REPORT_FILL_LINES = 27;

	private Logger log = Logger.getLogger(getClass());

	/**
	 * 文件流输出
	 * @param res 输出目标相应
	 * @param contentType 输出上下文类型
	 * @param fileName 输出文件名
	 * @param filePath 数据源文件
	 * @throws Exception
	 */
	public void writeFile(HttpServletResponse res, String contentType, String fileName, String filePath) throws Exception {
		res.setHeader("Content-Disposition","attachment;filename=\""+fileName + "\"");
		res.setContentType(contentType);
		File file = new File(filePath);
		InputStream is = new BufferedInputStream(new FileInputStream(file));
		byte[] buffer = new byte[is.available()];
		is.read(buffer);
		is.close();
		
		OutputStream os = new BufferedOutputStream(res.getOutputStream());
		os.write(buffer);
		os.flush();
		os.close();
	}

	/**
	 * 打印小票单张小票
	 * @param mBean
	 * @param conn
	 * @param operator 
	 * @return
	 * @throws Exception
	 */	
	public String printTicket(MaterialEntity mBean, SqlSession conn, int operator) throws Exception {

		Rectangle rect = new Rectangle(240, 184); //192
		Document document = new Document(rect, 6, 6, 0, 0);

		Date today = new Date();
		String folder = PathConsts.BASE_PATH + PathConsts.LOAD_TEMP + "\\" + DateUtil.toString(today, "yyyyMM");
		String filename = UUID.randomUUID().toString() + ".pdf";

		try {
			PdfWriter pdfWriter = PdfWriter.getInstance(document,
					new FileOutputStream(folder + "\\" + filename));

			document.open();
			BaseFont bfChinese = BaseFont.createFont(PathConsts.BASE_PATH + "\\msyh.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
			BaseFont bfStencil = BaseFont.createFont(PathConsts.BASE_PATH + "\\BOOKOSB.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

			Font titleFont = new Font(bfChinese, 16, Font.BOLD);
			Font boldFont = new Font(bfStencil, 30, Font.BOLD);
			Font boldFontWhite = new Font(bfStencil, 30, Font.BOLD);
			boldFontWhite.setColor(BaseColor.WHITE);

			Font specialFont = new Font(bfChinese, 11, Font.BOLD);
			Font detailFont = new Font(bfChinese, 7, Font.BOLD);

			addPage(pdfWriter, document, mBean, titleFont, specialFont, detailFont, boldFont, boldFontWhite, conn, operator);

		} catch (DocumentException de) {
			log.error(de.getMessage(), de);
			return null;
		} catch (IOException ioe) {
			log.error(ioe.getMessage(), ioe);
			return null;
		} finally {
			document.close();
			document = null;
		}

		return filename;
	}

	/**
	 * 打印小票多张小票
	 * @param mBean
	 * @param conn
	 * @return
	 * @throws Exception
	 */	
	public String printTickets(List<MaterialEntity> mBeans, SqlSession conn) throws Exception {

		Rectangle rect = new Rectangle(240, 184); //192
		Document document = new Document(rect, 6, 6, 0, 0);

		Date today = new Date();
		String folder = PathConsts.BASE_PATH + PathConsts.LOAD_TEMP + "\\" + DateUtil.toString(today, "yyyyMM");
		String filename = UUID.randomUUID().toString() + ".pdf";

		try {
			PdfWriter pdfWriter = PdfWriter.getInstance(document,
					new FileOutputStream(folder + "\\" + filename));
			document.open();
			BaseFont bfChinese = BaseFont.createFont(PathConsts.BASE_PATH + "\\msyh.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
			BaseFont bfStencil = BaseFont.createFont(PathConsts.BASE_PATH + "\\BOOKOSB.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

			Font titleFont = new Font(bfChinese, 16, Font.BOLD);
			Font boldFont = new Font(bfStencil, 30, Font.BOLD);
			Font boldFontWhite = new Font(bfStencil, 30, Font.BOLD);
			boldFontWhite.setColor(BaseColor.WHITE);
			Font specialFont = new Font(bfChinese, 11, Font.BOLD);
			Font detailFont = new Font(bfChinese, 7, Font.BOLD);

			for (int i = 0; i < mBeans.size() - 1; i++) {
				MaterialEntity mBean = mBeans.get(i);
				addPage(pdfWriter, document, mBean, titleFont, specialFont, detailFont, boldFont, boldFontWhite, conn, RvsConsts.TICKET_RECEPTOR);
				document.newPage();
			}
			MaterialEntity mBean = mBeans.get(mBeans.size() - 1);
			addPage(pdfWriter, document, mBean, titleFont, specialFont, detailFont, boldFont, boldFontWhite, conn, RvsConsts.TICKET_RECEPTOR);

		} catch (DocumentException de) {
			log.error(de.getMessage(), de);
			return null;
		} catch (IOException ioe) {
			log.error(ioe.getMessage(), ioe);
			return null;
		} finally {
			document.close();
			document = null;
		}

		return filename;
	}

	/**
	 * 打印小票单页
	 * @param boldFontWhite 
	 * @param boldFont 
	 * @param modelFont 
	 * @param operator 
	 * @param 
	 * @return
	 * @throws Exception
	 */	
	private void addPage(PdfWriter pdfWriter, Document document, MaterialEntity mBean, Font titleFont, Font specialFont,
			Font detailFont, Font boldFont, Font boldFontWhite, SqlSession conn, int operator) throws DocumentException {
		int printTimes = mBean.getQuotation_first();
		Integer service_repair_flg = mBean.getService_repair_flg();
		if (service_repair_flg == null) service_repair_flg = 0; 
		Integer directflg = mBean.getDirect_flg();
		if (directflg == null) directflg = -1;
		// 没有同意日期打印两张
		if (RvsConsts.TICKET_RECEPTOR == operator && mBean.getAgreed_date() == null) {
			printTimes = 1;
		} else
		// 补打一张
		if (RvsConsts.TICKET_ADDENDA == operator) {
			printTimes =1;
		}

		// 计算型号字宽设计
		String model_name = mBean.getModel_name();
		model_name = model_name.replaceAll("（", "(").replaceAll("　", " ").replaceAll("）", ")").toUpperCase();

		Chunk c = new Chunk(model_name, boldFont);
		float chunkWidth = (c.getWidthPoint());
		if (chunkWidth < 80) {
			c.setHorizontalScaling(80 / chunkWidth);
		} else if (chunkWidth > 158) {
			c.setHorizontalScaling(158 / chunkWidth);
		}

		// 图画对象
		PdfContentByte cb= pdfWriter.getDirectContentUnder();

		for(int i=0;i<printTimes;i++) {
			PdfPTable mainTable = new PdfPTable(1);
			mainTable.setHorizontalAlignment(Element.ALIGN_CENTER);
			mainTable.setTotalWidth(236);
			mainTable.setLockedWidth(true);
			mainTable.getDefaultCell().setBorder(PdfPCell.NO_BORDER);

			float[] topTable_widths = { 14, 14, 17, 19, 17, 19 };
			PdfPTable topTable = new PdfPTable(topTable_widths);
			topTable.setHorizontalAlignment(Element.ALIGN_CENTER);

			topTable.setTotalWidth(236);
			topTable.setLockedWidth(true);

			PdfPCell cell = new PdfPCell(new Paragraph("修 理 号", titleFont));
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cell.setPaddingBottom(8f);
			cell.setColspan(2);
			topTable.addCell(cell);

			String sorc_no = CommonStringUtil.nullToAlter(mBean.getSorc_no(), " ");
//			if (i == 0 && RvsConsts.TICKET_RECEPTOR == operator) {
//				sorc_no = "▼▼ " + sorc_no + " ▼▼";
//			} else
			cell = new PdfPCell(new Paragraph(sorc_no, boldFont));
			cell.setFixedHeight(40);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cell.setPadding(0.0f);
			cell.setColspan(4);
			cell.setPaddingBottom(10f);
			topTable.addCell(cell);

			cell = new PdfPCell(new Paragraph("型 号", titleFont));
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cell.setPaddingBottom(8f);
			cell.setColspan(2);
			topTable.addCell(cell);

			cell = new PdfPCell();
			cell.setPhrase(new Paragraph(c));

			cell.setFixedHeight(40);
			cell.setHorizontalAlignment(PdfPTable.ALIGN_CENTER);
			cell.setVerticalAlignment(Element.ALIGN_TOP);
			cell.setPadding(0.0f);
			cell.setPaddingTop(1f);
			cell.setPaddingBottom(1f);
			cell.setColspan(4);

			topTable.addCell(cell);

			cell = new PdfPCell(new Paragraph("机 身 号", titleFont));
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cell.setPaddingBottom(8f);
			cell.setColspan(2);
			topTable.addCell(cell);

			cell = new PdfPCell(new Paragraph(new Paragraph(mBean.getSerial_no(), boldFontWhite)));
			cell.setFixedHeight(40);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cell.setPadding(0.0f);
			cell.setPaddingBottom(10f);
			cell.setColspan(4);
			cell.setBackgroundColor(BaseColor.BLACK);
			topTable.addCell(cell);

			cell = new PdfPCell(new Paragraph("等 级", specialFont));
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setVerticalAlignment(Element.ALIGN_TOP);
			topTable.addCell(cell);

			cell = new PdfPCell(new Paragraph(CodeListUtils.getValue("material_level", "" + mBean.getLevel()), specialFont));
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			topTable.addCell(cell);

			cell = new PdfPCell(new Paragraph("同意日", specialFont));
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setVerticalAlignment(Element.ALIGN_TOP);
			topTable.addCell(cell);

			cell = new PdfPCell(new Paragraph(DateUtil.toString(mBean.getAgreed_date(), DateUtil.ISO_DATE_PATTERN), detailFont));
			cell.setFixedHeight(18);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			topTable.addCell(cell);

			cell = new PdfPCell(new Paragraph("完成日", specialFont));
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setVerticalAlignment(Element.ALIGN_TOP);
			topTable.addCell(cell);

			String sSchedulePlan = "";
			if (mBean.getAgreed_date() != null) {
				// 取得总组工程设定的计划产出日
				if (mBean.getScheduled_date() == null) {

					Date dSchedulePlan = RvsUtils.getTimeLimit(mBean.getAgreed_date(), 
							mBean.getLevel(), null, conn, false)[0];
					sSchedulePlan = DateUtil.toString(dSchedulePlan, DateUtil.ISO_DATE_PATTERN);
				} else {
					sSchedulePlan = DateUtil.toString(mBean.getScheduled_date(), DateUtil.ISO_DATE_PATTERN);
				}
			}
			cell = new PdfPCell(new Paragraph(sSchedulePlan, detailFont));
			cell.setFixedHeight(16);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			topTable.addCell(cell);

			float[] bottomTable_widths = { 100 };
			PdfPTable bottomTable = new PdfPTable(bottomTable_widths);
			bottomTable.setHorizontalAlignment(Element.ALIGN_CENTER);

			bottomTable.setTotalWidth(228);
			bottomTable.setLockedWidth(true);

			PdfContentByte cd = pdfWriter.getDirectContent();
			Barcode128 code128 = new Barcode128();
			code128.setCode(mBean.getMaterial_id());
			Image image128 = code128.createImageWithBarcode(cd, null, null);
			PdfPCell barcodeCell = new PdfPCell(image128);
			barcodeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			barcodeCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			barcodeCell.setPaddingTop(1.8f);
			barcodeCell.setBorder(PdfPCell.NO_BORDER);
			bottomTable.addCell(barcodeCell);

			mainTable.addCell(topTable);
			mainTable.addCell(bottomTable);

			document.add(mainTable);

			if (i == printTimes - 1 && RvsConsts.TICKET_RECEPTOR == operator) { // 
				cb.setLineWidth(1f);
				cb.setLineDash(4, 2, 0);
				cb.moveTo(0, 184);
				cb.lineTo(240,184);
				cb.stroke();
			}
		}
	}
}
