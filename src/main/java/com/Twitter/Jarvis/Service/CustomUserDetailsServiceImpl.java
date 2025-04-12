package com.Twitter.Jarvis.Service;

import com.Twitter.Jarvis.Model.UserConfigurationModel;
import com.Twitter.Jarvis.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserConfigurationModel userConfigurationModel = userRepository.findByEmail(username);

        List<GrantedAuthority> authorities = new ArrayList<>();
        return new User(userConfigurationModel.getEmail(),userConfigurationModel.getPassword(), authorities);
    }
}
