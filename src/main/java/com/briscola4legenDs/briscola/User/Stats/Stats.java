package com.briscola4legenDs.briscola.User.Stats;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table
@Getter
@Setter
public class Stats {
    @Id
    private long id;

    private int win;
    private int loss;
    private int matches;

    private int maxPoints;
    private int totalPoints;

    // TODO: aggiungere: punteggio massimo e punteggio medio
}
