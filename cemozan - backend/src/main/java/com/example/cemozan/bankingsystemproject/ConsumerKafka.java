package com.example.cemozan.bankingsystemproject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;


import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;



public class ConsumerKafka {
	
	private static final Logger logger = LogManager.getLogger(ConsumerKafka.class);
	
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		
		
			
		String bootstrapServers = "localhost:9092";
		String groupId = "logs";

		Properties properties = new Properties();
		properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
		properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		
	    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
	    String[] topic = {"logs"};
	    consumer.subscribe(Arrays.asList(topic));
	    String consumerWritingTxt = "";
	    while (true) {
			ConsumerRecords<String, String> records = consumer.poll(Duration.ofHours(1));
		    for (ConsumerRecord<String, String> record : records) {
		    	System.out.println("Topic : " + record.topic());
		        
		        System.out.println("Message : " + record.value()); //message içeriği value() ile okunmaktadır;
		 		
		        logger.info(record.value());
		        

		 		consumerWritingTxt = record.value().toString();
		 		System.out.println("ConsumerWritingTxt: "+ consumerWritingTxt);
		 		
		 		Connection conn = null;  
		 		try {
			 		Class.forName("com.mysql.cj.jdbc.Driver");
					String url = "jdbc:mysql://localhost:3306/bootcampdb?useServerPrepStmts=true";
					conn = DriverManager.getConnection(url, "cemozan", "123456");
					conn.setAutoCommit(false);
					
					String queryOne = "INSERT INTO logs (detail) VALUES (?)";
					PreparedStatement ps = conn.prepareStatement(queryOne);
					ps.setString(1, consumerWritingTxt);
					ps.executeUpdate();

					
					conn.commit();
		 		} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SQLException e) {
					try {
						conn.rollback();
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					e.printStackTrace();
				}  
		 		
				
				
		 		
			}
		}
			
	}

}
