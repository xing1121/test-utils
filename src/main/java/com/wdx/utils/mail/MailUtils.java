package com.wdx.utils.mail;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailUtils {

	private static Logger logger = LoggerFactory.getLogger(MailUtils.class);

	/**
	 * 发送邮件
	 */
	public static boolean sendEmail(String hostName, Integer port, String userName, String password, String subject,
			String content, List<String> sendToList, String sendFrom) {

		if (StringUtils.isBlank(hostName)) {
			logger.info("登录主机不能为空！");
			return false;
		}

		if (port == null) {
			logger.info("登录主机的端口不能为空！");
			return false;
		}

		if (StringUtils.isBlank(userName)) {
			logger.info("登录邮箱账号不能为空！");
			return false;
		}

		if (StringUtils.isBlank(password)) {
			logger.info("登录邮箱密码不能为空！");
			return false;
		}

		if (StringUtils.isBlank(subject)) {
			subject = "这是一封来自" + userName + "的邮件";
		}

		if (StringUtils.isBlank(content)) {
			content = "this is an email from" + userName + ",but there is no content!Sorry!";
		}

		if (StringUtils.isBlank(sendFrom)) {
			sendFrom = userName;
		}

		if (sendToList == null || sendToList.size() == 0) {
			logger.info("收件人列表不能为空！");
			return false;
		}

		// 创建一封邮件
		HtmlEmail htmlEmail = new HtmlEmail();

		// 设置链接登录主机
		htmlEmail.setHostName(hostName); // 127.0.0.1

		// 设置端口
		htmlEmail.setSmtpPort(port); // 25

		// 设置登录邮箱账户密码
		htmlEmail.setAuthentication(userName, password); // admin@qq.com 123456

		// 设置主题
		htmlEmail.setSubject(subject); // 邮件主题

		try {
			// 设置内容
			// htmlEmail.setHtmlMsg("<h1>点击链接重置密码</h1><br/><a href='http://www.baidu.com?msg="+"哈哈"+"'></a>");
			// htmlEmail.setContent("<h1>点击跳转百度</h1><br/><a href='www.baidu.com?msg="+ "哈哈" +"'>百度</a>","text/html;charset=UTF-8");
			htmlEmail.setContent(content, "text/html;charset=UTF-8");

			// 设置收件人邮箱
			for (String sendTo : sendToList) {
				if (checkEmail(sendTo)) {
					htmlEmail.addTo(sendTo);
				} else {
					logger.info(sendTo + "不符合邮件格式");
				}
			}

			// 设置发件人邮箱
			htmlEmail.setFrom(sendFrom);

			// 发送
			htmlEmail.send();

			logger.info(htmlEmail.getSubject() + "邮件发送到" + htmlEmail.getToAddresses() + "成功！");
		} catch (Exception e) {

			logger.error("邮件发送失败：" + e.getMessage());
			return false;
		}

		return true;
	}

	/**
	 * 检查是否符合邮件格式
	 */
	public static boolean checkEmail(String email){
		String regex = "^([\\w-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([\\w-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
		return Pattern.matches(regex, email);
	}
	
}
