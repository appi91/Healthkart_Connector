package com.hk.HKConnector.Configurations;

import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.authentication.builders.*;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.crypto.factory.*;
import org.springframework.security.crypto.password.*;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${hkc.uname}")
    private String portalUname;

    @Value("${hkc.pwd}")
    private String portalPassword;

    //TODO : move to Google Login
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder authenticationMgr) throws Exception {
        PasswordEncoder encoder =
                PasswordEncoderFactories.createDelegatingPasswordEncoder();
        authenticationMgr.inMemoryAuthentication()
                .withUser(portalUname).password(encoder.encode(portalPassword)).authorities("ADMIN_USER");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        //TODO : Change the success mapping
        http.authorizeRequests()
                    .antMatchers("/bulk/process/*").access("hasRole('ADMIN_USER')")
                    .antMatchers("/").access("hasRole('ADMIN_USER')")
                    .antMatchers("/process/**").permitAll()
                    .antMatchers("/actuator/health").permitAll()
                    .anyRequest().fullyAuthenticated()
                    .and()
                .formLogin()
                    .loginPage("/loginPage")
                    .successHandler(myAuthenticationSuccessHandler())
//                    .defaultSuccessUrl("/bulk/process/page/allChannels")
                    .failureUrl("/loginPage?error").permitAll()
                    .usernameParameter("username").passwordParameter("password")
                .and()
                    .logout()
//                    .permitAll()
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout")).logoutSuccessUrl("/loginPage");
//                .logoutSuccessUrl("/loginPage?logout");

        http.csrf().disable();
    }

    @Bean
    public AuthenticationSuccessHandler myAuthenticationSuccessHandler(){
        return new MySimpleUrlAuthenticationSuccessHandler();
    }


}
