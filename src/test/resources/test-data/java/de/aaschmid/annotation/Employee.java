package de.aachmid.annotation;

import javax.persistence.*;


@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = { "name" })},
        indexes= {
        @Index(name = "idx_name", columnNames = { "name" }),
        @Index(name = "idx_dayOfBirth", columnNames = { "dayOfBirth" }),
}
)
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_STORE")
    long id;

    @Column(length = 80, nullable = false)
    String name;

    @Column(name = "DAY_OF_BIRTH", nullable = false)
    Date dayOfBirth;

    // ...
}
