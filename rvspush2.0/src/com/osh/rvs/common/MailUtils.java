package com.osh.rvs.common;

import java.io.UnsupportedEncodingException;
import java.util.Collection;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;
import org.apache.log4j.Logger;

import framework.huiqing.common.util.CommonStringUtil;

public class MailUtils {

	public static Logger logger = Logger.getLogger("MailUtils");

	public static final String OLYMPUS_SMTP = "10.220.2.30"; // 10.220.2.22
	public static final String RVS_ADDRESS = "rvs@olympus.com.cn";
	public static final String RVS_NAME = "RVS Mail Report";

	public static void sendMail(Collection<InternetAddress> toIas, Collection<InternetAddress> ccIas, String subject, String message) {

		// 使用SimpleEmail对于中文内容，可能会产生乱码
		SimpleEmail email = new SimpleEmail();

		// SMTP服务器名
		String hostName =  PathConsts.MAIL_CONFIG.getProperty("smtp.ipaddr");
		logger.info("MAIL_CONFIG ready? " + hostName);
		if (CommonStringUtil.isEmpty(hostName)) {
			hostName = OLYMPUS_SMTP;
		}
		email.setHostName(hostName);

		email.setTLS(false);

		// 登陆邮件服务器的用户名和密码
		// email.setAuthentication("fkmugen", "rorento.");
		
		email.setCharset("UTF-8");

		int retry = 0;
		boolean success = false;

		if (toIas == null) return;

		for (InternetAddress toIa : toIas) {
			logger.info("to: " + toIa.getAddress());
		}

		do {
			// 接收人
			try {
				email.setTo(toIas);

				// 发送人
				String senderAccount = PathConsts.MAIL_CONFIG.getProperty("rvs.sender.account");
				String senderName = PathConsts.MAIL_CONFIG.getProperty("rvs.sender.name");
				if (CommonStringUtil.isEmpty(senderAccount)) {
					senderAccount = RVS_ADDRESS;
				}
				if (CommonStringUtil.isEmpty(senderName)) {
					senderName = RVS_NAME;
				}
				email.setFrom(senderAccount, senderName);

				// 标题
				email.setSubject(subject);

				// 抄送
				if (ccIas!=null && ccIas.size() > 0) {
					for (InternetAddress ccIa : ccIas) {
						logger.info("cc: " + ccIa.getAddress());
					}
					email.setCc(ccIas);
				}

				// 邮件内容
				email.setMsg(message);
				// 发送
				email.send();
	
				logger.info("Send email successful!");
				success = true;

			} catch (Exception me) {
				retry++;
				if (retry == 5)
					logger.error("Send email failed!!!!!", me);
				else
					logger.error("Send email failed!!!!!");
			}
		
		} while (retry < 5 && !success);
	}

	public static void sendHtmlMail(Collection<InternetAddress> toIas, Collection<InternetAddress> ccIas, String subject, String htmlMessage, String TextMessage) {
		// 使用SimpleEmail对于中文内容，可能会产生乱码
		HtmlEmail email = new HtmlEmail();

		// SMTP服务器名
		String hostName =  PathConsts.MAIL_CONFIG.getProperty("smtp.ipaddr");
		if (CommonStringUtil.isEmpty(hostName)) {
			hostName = OLYMPUS_SMTP;
		}
		email.setHostName(hostName);

		email.setTLS(false);

		// 登陆邮件服务器的用户名和密码
		// email.setAuthentication("fkmugen", "rorento.");
		
		email.setCharset("UTF-8");

		if (toIas == null) return;

		for (InternetAddress toIa : toIas) {
			logger.info("to: " + toIa.getAddress());
		}

		int retry = 0;
		boolean success = false;

		do {
			// 接收人
			try {
				email.setTo(toIas);
	
				// 发送人
				String senderAccount = PathConsts.MAIL_CONFIG.getProperty("rvs.sender.account");
				String senderName = PathConsts.MAIL_CONFIG.getProperty("rvs.sender.name");
				if (CommonStringUtil.isEmpty(senderAccount)) {
					senderAccount = RVS_ADDRESS;
				}
				if (CommonStringUtil.isEmpty(senderName)) {
					senderName = RVS_NAME;
				}
				email.setFrom(senderAccount, senderName);

				// 标题
				email.setSubject(subject);

				// 抄送
				if (ccIas!=null && ccIas.size() > 0) {
					for (InternetAddress ccIa : ccIas) {
						logger.info("cc: " + ccIa.getAddress());
					}
					email.setCc(ccIas);
				}
	
				// 邮件内容
				email.setHtmlMsg(htmlMessage);
				email.setTextMsg(TextMessage);

				// 发送
				email.send();
	
				logger.info("Send email successful!");
				success = true;

			} catch (Exception me) {
				retry++;
				if (retry == 5)
					logger.error("Send email failed!!!!!", me);
				else
					logger.error("Send email failed!!!!!");
			}
		
		} while (retry < 5 && !success);
	}

	public static void sendMultipartMail(Collection<InternetAddress> toIas, Collection<InternetAddress> ccIas, String subject, String TextMessage,
			String... parts) {
		HtmlEmail email = new HtmlEmail();

		// SMTP服务器名
		String hostName = PathConsts.MAIL_CONFIG.getProperty("smtp.ipaddr");
		if (CommonStringUtil.isEmpty(hostName)) {
			hostName = OLYMPUS_SMTP;
		}
		email.setHostName(hostName);

		email.setTLS(false);

		// 登陆邮件服务器的用户名和密码

		email.setCharset("UTF-8");

		if (toIas == null) return;

		for (InternetAddress toIa : toIas) {
			logger.info("to: " + toIa.getAddress());
		}

		int retry = 0;
		boolean success = false;

		do {
			// 接收人
			try {
				email.setTo(toIas);

				// 发送人
				String senderAccount = PathConsts.MAIL_CONFIG
						.getProperty("rvs.sender.account");
				String senderName = PathConsts.MAIL_CONFIG
						.getProperty("rvs.sender.name");
				if (CommonStringUtil.isEmpty(senderAccount)) {
					senderAccount = RVS_ADDRESS;
				}
				if (CommonStringUtil.isEmpty(senderName)) {
					senderName = RVS_NAME;
				}
				email.setFrom(senderAccount, senderName);

				// 标题
				email.setSubject(subject);

				// 抄送
				if (ccIas!=null && ccIas.size() > 0) {
					for (InternetAddress ccIa : ccIas) {
						logger.info("cc: " + ccIa.getAddress());
					}
					email.setCc(ccIas);
				}

				// 本文
				email.setTextMsg(TextMessage);

				if (parts != null && parts.length > 0) {
					for (String part : parts) {
						EmailAttachment attachment = new EmailAttachment();  
						try {
							attachment.setPath(part);
							String[] pp = part.split("\\\\");
							String name = MimeUtility.encodeText(pp[pp.length - 1]);
							attachment.setName(name);
							attachment.setDisposition(EmailAttachment.ATTACHMENT);
							attachment.setDescription(name);
							email.attach(attachment);
						} catch (UnsupportedEncodingException e) {
							logger.error(e.getMessage());
						}
		             }
				}

				// 发送
				email.send();

				logger.info("Send email successful!");
				success = true;

			} catch (Exception me) {
				retry++;
				if (retry == 5)
					logger.error("Send email failed!!!!!", me);
				else
					logger.error("Send email failed!!!!!");
			}

		} while (retry < 5 && !success);
	}

}
