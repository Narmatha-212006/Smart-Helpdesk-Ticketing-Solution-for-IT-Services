package com.helpdesk.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Setting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "theme_color")
    private String themeColor;

    private Boolean notifications;

    @Column(name = "auto_assign")
    private Boolean autoAssign;
}
