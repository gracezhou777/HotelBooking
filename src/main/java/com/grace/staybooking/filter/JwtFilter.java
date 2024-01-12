package com.grace.staybooking.filter;

import com.grace.staybooking.model.Authority;
import com.grace.staybooking.repository.AuthorityRepository;
import com.grace.staybooking.util.JwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Component
public class JwtFilter extends OncePerRequestFilter {
  //看看前端是否传进来authorization的东西
  private static final String HEADER = "Authorization";
  private static final String PREFIX = "Bearer ";

  private final AuthorityRepository authorityRepository;
  private final JwtUtil jwtUtil;

  public JwtFilter(AuthorityRepository authorityRepository, JwtUtil jwtUtil) {
    this.authorityRepository = authorityRepository;
    this.jwtUtil = jwtUtil;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
    final String authorizationHeader = httpServletRequest.getHeader(HEADER);

    String jwt = null;
    //看token是否存在并以Bearer开头
    if (authorizationHeader != null && authorizationHeader.startsWith(PREFIX)) {
      jwt = authorizationHeader.substring(PREFIX.length());
    }

    //validate token，如果合法并且是第一次验证（重复验证没有必要）
    if (jwt != null && jwtUtil.validateToken(jwt) && SecurityContextHolder.getContext().getAuthentication() == null) {

      //找到对应authority
      String username = jwtUtil.extractUsername(jwt);
      Authority authority = authorityRepository.findById(username).orElse(null);
      if (authority != null) {
        //constructor里要求是collection，虽然我们只有一个role
        List<GrantedAuthority> grantedAuthorities = Arrays.asList(new GrantedAuthority[]{new SimpleGrantedAuthority(authority.getAuthority())});
        //创建token
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
            username, null, grantedAuthorities);
        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
        //存储用户token, thread里
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
      }
    }
    filterChain.doFilter(httpServletRequest, httpServletResponse);
  }
}
