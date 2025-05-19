package com.briscola4legenDs.briscola.User;

import lombok.*;

// TODO: implementarlo per tutti i trasferimenti di user
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UserDTO {
    private long id;
    private String username;
    private String imageUrl;
}
