package Security;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.cemozan.bankingsystemproject.Services.DatabaseUserDetailsService;

import JWT.JwtRequestFilter;


@SuppressWarnings("deprecation")
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter{
	@Autowired
	private JwtRequestFilter jwtRequestFilter;	  
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(new DatabaseUserDetailsService());
	}
	
	@Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
	
	@Override
	@Bean
	public AuthenticationManager authenticationManagerBean() throws Exception {
	    return super.authenticationManagerBean();
	}
	 
	@Override
	protected void configure(HttpSecurity httpSecurity) throws Exception {
			
			httpSecurity
			.cors()
			.and()
			.csrf()
			.disable()
			.authorizeHttpRequests()
			.antMatchers("/auth").permitAll()
			.antMatchers("/register").permitAll()
			.antMatchers(HttpMethod.POST, "/accounts/**").hasAuthority("CREATE_ACCOUNT")
			.antMatchers(HttpMethod.POST, "/createBank").hasAuthority("CREATE_BANK")
			.antMatchers(HttpMethod.PATCH, "/accounts/**").hasAuthority("CREATE_ACCOUNT")
			.antMatchers(HttpMethod.GET, "/accounts/**").hasAuthority("CREATE_ACCOUNT")
			.antMatchers(HttpMethod.GET, "/allaccounts/**").hasAuthority("CREATE_ACCOUNT")
			.antMatchers(HttpMethod.PUT, "/accounts/**").hasAuthority("CREATE_ACCOUNT")
			.antMatchers(HttpMethod.DELETE, "/accounts/**").hasAuthority("REMOVE_ACCOUNT")
			.antMatchers(HttpMethod.PATCH, "/users/**").hasAuthority("ACTIVATE_DEACTIVATE_USER")
			.antMatchers(HttpMethod.GET,"/accounts/logs/**").permitAll()
			.anyRequest()
			.authenticated();

			
			
			httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
	}
	
	
	
}
