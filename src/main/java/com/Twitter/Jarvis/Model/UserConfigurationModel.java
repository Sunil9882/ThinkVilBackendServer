package com.Twitter.Jarvis.Model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class UserConfigurationModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String fullName;
    private String email;
    private String password;
    private String mobile;
    private String image;

    @Override
    public String toString() {
        return "UserConfigurationModel{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", mobile='" + mobile + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}
