package com.example.cemozan.bankingsystemproject.Services;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class DatabaseUserDetailsService implements UserDetailsService{
	@Override
	public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Reader reader;
		 //read configuration and create session factory
		try {
			reader = Resources.getResourceAsReader("myBatis_conf.xml");
			SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
			// open session, session is connection between database and us(like JDBC connection)
			SqlSession session = sqlSessionFactory.openSession();
			// call query from mapper.xml files with defined id
			com.example.cemozan.bankingsystemproject.Models.User user = session.selectOne("findByUsername",username);

			if(user != null) {
				
				String userName = user.getUsername();
				String password = user.getPassword();
				String authorities = user.getAuthorities();
				
				String[] auths = authorities.split(",");
				
				List<GrantedAuthority> grantedAuhorities = new ArrayList<GrantedAuthority>();
				for(String authority : auths) {
					grantedAuhorities.add(new SimpleGrantedAuthority(authority));
				}
				
				return User
						.builder()
						.username(userName)
						.password(password)
						.accountExpired(false)
						.accountLocked(false)
						.credentialsExpired(false)
						.authorities(grantedAuhorities)
						.build();
			}else {
				throw new UsernameNotFoundException(username + " Not Found");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
}
