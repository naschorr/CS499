package com.sqs.training.controller;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.validator.routines.EmailValidator;
import java.sql.Types;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sqs.training.domain.EmailSub;

@Controller
public class EmailController {	
	/* Members */
	private static final String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
	private static final String HOST = "localhost";
	private static final int PORT = 3306;
	private static final String DATABASE= "sys";
	private static final String USERNAME = "sqs";
	private static final String PASSWORD = "sqs";
	
	/* Methods */
	private static DriverManagerDataSource getDataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(DRIVER_CLASS_NAME);
		dataSource.setUrl("jdbc:mysql://" + HOST + ":" + Integer.toString(PORT) + "/" + DATABASE);
		dataSource.setUsername(USERNAME);
		dataSource.setPassword(PASSWORD);
		return dataSource;
	}
	
	private static boolean isValidEmail(String email){
		EmailValidator validator = EmailValidator.getInstance();
		return validator.isValid(email);
	}
	
	private static String executeQuery(String sql, Object[] parameters, int[] types){
		JdbcTemplate template = new JdbcTemplate(getDataSource());
		String paramStr = Arrays.toString(parameters);
		
		try{
			int rows = template.update(sql, parameters, types);
			return String.format("Parameters: %1$s. %2$d row inserted.", paramStr, rows);
		}
		catch (DataAccessException e){
			// Needs expanding
			if(e instanceof DuplicateKeyException){
				return String.format("Error: Data already exists. Parameters: %1$s.", paramStr);
			}else if(e instanceof DataIntegrityViolationException){
				// Maybe check for a MysqlDataTruncation exception instead?
				return String.format("Error: Data doesn't fit into the table. Parameters: %1$s.", paramStr);
			}else{
				return String.format("Error: Unhandled exception (%1$s). Parameters: %2$s.", e, paramStr);
			}
		}
	}
	
	/* Site Mapping */
	@RequestMapping("/subscribe")
	public String displaySubscriptionPage(Map<String, Object> model) {
		EmailSub emailForm = new EmailSub();
		model.put("emailForm", emailForm);
		return "subscribe";
	}
	
	@RequestMapping("/subscribeEmail")
	public String registerUser(@ModelAttribute("emailForm") EmailSub emailForm) {
		String email = emailForm.getEmail();
		String textOutput;
		// Basic email validation is already done in the HTML form. This just prevents less garbage from getting inserted.
		if(isValidEmail(email)){
			String registerSql = "INSERT INTO email_sub (email) VALUES (?)";	// primary key ID gets handled automatically
			Object[] params = new Object[] {email};
			int[] types = new int[] {Types.VARCHAR};
			textOutput = executeQuery(registerSql, params, types);
		}else{
			textOutput = String.format("Error: Invalid address for email: %1$s.", email);
		}
		System.out.println(textOutput);
		
		return "subscribe";
	}
	
	/*
	@RequestMapping("/unsubscribe")
	public String unregisterUser(@ModelAttribute("emailForm") EmailSub emailForm) {
		return "unsubscribe";
	}
	*/
}
